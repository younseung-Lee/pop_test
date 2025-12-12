// /js/popEditor.js

const PopEditor = (() => {
    let editCanvas;
    let previewCanvas;

    // ===== 템플릿 조회 상태 =====
    let templateSource = 'COMMON';      // COMMON(공통), MY(우리 마트)
    let templatePage = 1;              // 현재 페이지 (1부터 시작)
    const TEMPLATE_PAGE_SIZE = 12;     // 한 페이지당 템플릿 개수
    let lastLayoutFilter = '';
    let lastCategoryFilter = '';

    // 레이아웃별 고정 캔버스 사이즈
    const LAYOUT_SIZE_MAP = {
        VERTICAL:   { width: 800,  height: 1200 },
        HORIZONTAL: { width: 1200, height: 800  },
        SHOWCARD:   { width: 600,  height: 600  }
    };

    function getCanvasSizeForLayout(layoutType) {
        const lt = layoutType || 'VERTICAL';
        return LAYOUT_SIZE_MAP[lt] || { width: 800, height: 600 };
    }

    // ===== 캔버스 초기화 =====
    function initCanvases() {
        editCanvas = new fabric.Canvas('editCanvas', {
            preserveObjectStacking: true,
            selection: true
        });

        previewCanvas = new fabric.Canvas('previewCanvas', {
            interactive: false,
            selection: false
        });

        editCanvas.backgroundColor = '#ffffff';
        editCanvas.renderAll();
        updateEmptyMessage();
        syncPreview();

        const syncEvents = [
            'object:added',
            'object:modified',
            'object:removed',
            'object:moving',
            'object:scaling',
            'object:rotating'
        ];
        syncEvents.forEach(ev => {
            editCanvas.on(ev, () => {
                updateEmptyMessage();
                syncPreview();
            });
        });
        bindCanvasPanZoom();
    }

    // ... (중략: 캔버스 관련 함수들은 그대로 유지) ...

    // ===== 템플릿 로드 =====
    async function loadTemplate(el) {
        if (!editCanvas || !el) return;

        const bgUrl = el.getAttribute('data-bg');
        const layoutType = el.getAttribute('data-layout') || 'VERTICAL';
        const { width: w, height: h } = getCanvasSizeForLayout(layoutType);

        editCanvas.clear();
        editCanvas.setDimensions({ width: w, height: h });

        editCanvas.setViewportTransform([1, 0, 0, 1, 0, 0]);

        if (!bgUrl) {
            editCanvas.backgroundColor = '#ffffff';
            editCanvas.renderAll();
            updateEmptyMessage();
            syncPreview();
            return;
        }

        try {
            const img = await fabric.FabricImage.fromURL(bgUrl, {
                crossOrigin: 'anonymous'
            });

            const scaleX = w / img.width;
            const scaleY = h / img.height;
            const scale = Math.min(scaleX, scaleY);

            img.set({
                scaleX: scale,
                scaleY: scale,
                top: 0,
                left: 0
            });

            editCanvas.backgroundImage = img;
            editCanvas.renderAll();
            updateEmptyMessage();
            syncPreview();
        } catch (error) {
            console.error('템플릿 로드 실패:', error);
            alert('템플릿을 불러오는데 실패했습니다.');
        }
    }

    // ===== 템플릿 필터(레이아웃+카테고리+타입+페이징) =====
    function filterTemplateByLayout() {
        const layoutSel   = document.getElementById('templateLayout');
        const categorySel = document.getElementById('templateCategory');
        const sourceSel   = document.getElementById('templateSource');

        const layoutType = layoutSel ? layoutSel.value : '';
        const category   = categorySel ? categorySel.value : '';
        const source     = sourceSel ? sourceSel.value : 'COMMON';

        // 템플릿 타입 상태 저장
        templateSource = source;

        // 필터 변경 시 페이지를 1로 리셋
        if (layoutType !== lastLayoutFilter || category !== lastCategoryFilter) {
            templatePage = 1;
            lastLayoutFilter = layoutType;
            lastCategoryFilter = category;
        }

        // 엔드포인트 결정
        let url = (templateSource === 'MY')
            ? '/api/templates/my'
            : '/api/templates/common';

        const params = [];

        if (layoutType) params.push('layoutType=' + encodeURIComponent(layoutType));
        // 카테고리는 백엔드의 tpl_ctgy_big 에 매핑 → ctgyBig 파라미터 사용
        if (category) params.push('ctgyBig=' + encodeURIComponent(category));

        // 페이징 파라미터
        params.push('page=' + encodeURIComponent(templatePage));
        params.push('size=' + encodeURIComponent(TEMPLATE_PAGE_SIZE));

        if (params.length > 0) {
            url += '?' + params.join('&');
        }

        fetch(url)
            .then(res => res.json())
            .then(data => {
                const grid = document.querySelector('.template-grid');
                if (!grid) return;

                grid.innerHTML = '';

                const templates = data.templates || [];
                const totalCount = (typeof data.totalCount === 'number')
                    ? data.totalCount
                    : templates.length;

                // 총 개수 표시 갱신
                const totalEl = document.getElementById('templateTotalCount');
                if (totalEl) {
                    totalEl.textContent = totalCount;
                }

                // 현재 선택된 레이아웃/카테고리 라벨 갱신
                updateFilterLabels(layoutType, category, source);

                // 페이징 UI 갱신
                updatePaginationUI(totalCount);

                if (templates.length === 0) {
                    const emptyItem = document.createElement('div');
                    emptyItem.className = 'template-item';
                    emptyItem.innerHTML = `
                        <div class="template-thumb">템플릿 없음</div>
                        <div class="template-name">조건에 해당하는 템플릿이 없습니다.</div>
                        <div class="template-size"></div>
                    `;
                    grid.appendChild(emptyItem);
                    return;
                }

                templates.forEach(tpl => {
                    const item = document.createElement('div');
                    item.className = 'template-item';
                    item.setAttribute('data-template-id', tpl.tplId);
                    item.setAttribute('data-bg', tpl.bgImgUrl);
                    item.setAttribute('data-layout', tpl.layoutType);

                    const thumb = document.createElement('div');
                    thumb.className = 'template-thumb';
                    const img = document.createElement('img');
                    img.src = tpl.bgImgUrl;
                    img.alt = '템플릿';
                    img.style.width = '100%';
                    img.style.height = '100%';
                    img.style.objectFit = 'cover';
                    img.style.borderRadius = '4px';
                    thumb.appendChild(img);

                    const name = document.createElement('div');
                    name.className = 'template-name';
                    name.textContent = tpl.tplNm || '템플릿';

                    const size = document.createElement('div');
                    size.className = 'template-size';
                    // 필요하면 레이아웃/카테고리 등 표시 가능
                    size.textContent = '';

                    item.appendChild(thumb);
                    item.appendChild(name);
                    item.appendChild(size);

                    grid.appendChild(item);
                });
            })
            .catch(err => {
                console.error('템플릿 조회 실패', err);
                alert('템플릿 조회 중 오류가 발생했습니다.');
            });
    }

    function updateFilterLabels(layoutType, category, source) {
        const layoutLabelEl   = document.getElementById('currentLayoutLabel');
        const categoryLabelEl = document.getElementById('currentCategoryLabel');
        const sourceLabelEl   = document.getElementById('currentSourceLabel');

        const layoutLabelMap = {
            '': '전체 레이아웃',
            'VERTICAL': '세로형',
            'HORIZONTAL': '가로형',
            'SHOWCARD': '쇼카드'
        };

        const categoryLabelMap = {
            '': '전체 카테고리',
            '봄': '봄',
            '여름': '여름',
            '가을': '가을',
            '겨울': '겨울',
            '명절': '명절'
        };

        const sourceLabelMap = {
            'COMMON': '공통 템플릿',
            'MY': '우리 마트 템플릿'
        };

        if (layoutLabelEl) {
            layoutLabelEl.textContent = layoutLabelMap[layoutType || ''] || '전체 레이아웃';
        }
        if (categoryLabelEl) {
            categoryLabelEl.textContent = categoryLabelMap[category || ''] || '전체 카테고리';
        }
        if (sourceLabelEl) {
            sourceLabelEl.textContent = sourceLabelMap[source || 'COMMON'] || '공통 템플릿';
        }
    }

    // ===== 페이징 UI 갱신 =====
    function updatePaginationUI(totalCount) {
        const pageInfoEl = document.getElementById('tplPageInfo');
        const prevBtn    = document.getElementById('tplPrevPage');
        const nextBtn    = document.getElementById('tplNextPage');

        const totalPages = Math.max(1, Math.ceil(totalCount / TEMPLATE_PAGE_SIZE));

        if (pageInfoEl) {
            pageInfoEl.textContent = `${templatePage} / ${totalPages} 페이지`;
        }
        if (prevBtn) {
            prevBtn.disabled = (templatePage <= 1);
        }
        if (nextBtn) {
            nextBtn.disabled = (templatePage >= totalPages);
        }
    }

    // ===== 저장 더미 =====
    function saveWork() {
        if (!editCanvas) return;
        const json = editCanvas.toJSON();
        console.log('저장용 JSON:', json);
        alert('저장 로직은 백엔드 구현 후 연결할 예정입니다.\n(콘솔에서 JSON 확인 가능)');
    }

    // ===== DOM 이벤트 바인딩 =====
    document.addEventListener('DOMContentLoaded', () => {
        initCanvases();
        bindImageUpload();
        bindColorPickerSync();

        // 초기 템플릿 조회
        filterTemplateByLayout();

        // 템플릿 카드 클릭: 이벤트 위임
        const templateList = document.getElementById('templateList');
        if (templateList) {
            templateList.addEventListener('click', (e) => {
                const item = e.target.closest('.template-item');
                if (item) {
                    loadTemplate(item);
                }
            });
        }

        // 사이드바 탭 전환 (UI만)
        document.querySelectorAll('.sidebar-tab').forEach(tab => {
            tab.addEventListener('click', function () {
                document.querySelectorAll('.sidebar-tab').forEach(t => t.classList.remove('active'));
                this.classList.add('active');
            });
        });

        // 페이징 버튼
        const prevBtn = document.getElementById('tplPrevPage');
        const nextBtn = document.getElementById('tplNextPage');

        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                if (templatePage > 1) {
                    templatePage -= 1;
                    filterTemplateByLayout();
                }
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                templatePage += 1;
                filterTemplateByLayout();
            });
        }
    });

    // ... (색상 피커/폰트 동기화 함수들은 그대로 유지) ...

    return {
        newWork,
        addText,
        addShape,
        addImage,
        deleteSelected,
        applyColors,
        removeColors,
        applyTextStyle,
        bringForward,
        sendBackward,
        loadTemplate,
        filterTemplateByLayout,
        saveWork
    };
})();

window.PopEditor = PopEditor;
