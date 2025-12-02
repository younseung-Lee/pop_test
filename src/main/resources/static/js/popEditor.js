// /js/popEditor.js

const PopEditor = (() => {
    let editCanvas;
    let previewCanvas;

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

        editCanvas.setBackgroundColor('#ffffff', editCanvas.renderAll.bind(editCanvas));
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
    }

    // 빈 캔버스 안내 메시지 표시/숨김
    function updateEmptyMessage() {
        const msg = document.querySelector('.empty-canvas-message');
        if (!msg || !editCanvas) return;

        const hasObjects = editCanvas.getObjects().length > 0;
        const hasBg = !!editCanvas.backgroundImage;
        msg.style.display = (hasObjects || hasBg) ? 'none' : 'block';
    }

    // ===== 미리보기 동기화 =====
    function syncPreview() {
        if (!editCanvas || !previewCanvas) return;

        const json = editCanvas.toJSON();
        previewCanvas.clear();

        previewCanvas.loadFromJSON(json, () => {
            const scaleX = previewCanvas.getWidth() / editCanvas.getWidth();
            const scaleY = previewCanvas.getHeight() / editCanvas.getHeight();
            const zoom = Math.min(scaleX, scaleY);

            previewCanvas.setViewportTransform([zoom, 0, 0, zoom, 0, 0]);
            previewCanvas.renderAll();
        });
    }

    // ===== 새 문서 =====
    function newWork() {
        if (!editCanvas) return;
        if (!confirm('현재 작업 내용을 지우고 새 문서를 시작할까요?')) return;

        editCanvas.clear();
        editCanvas.setWidth(800);
        editCanvas.setHeight(600);
        editCanvas.setBackgroundColor('#ffffff', editCanvas.renderAll.bind(editCanvas));

        updateEmptyMessage();
        syncPreview();
    }

    // ===== 텍스트 추가 =====
    function addText() {
        if (!editCanvas) return;

        const fontSizeSelect = document.getElementById('fontSizeSelect');
        const fontFamilySelect = document.getElementById('fontFamilySelect');

        const fontSize = fontSizeSelect ? parseInt(fontSizeSelect.value, 10) : 36;
        const fontFamily = fontFamilySelect ? fontFamilySelect.value : 'Malgun Gothic';

        const textbox = new fabric.Textbox('새 텍스트', {
            left: 100,
            top: 100,
            width: 400,
            fontSize,
            fontFamily,
            fill: '#000000'
        });

        editCanvas.add(textbox);
        editCanvas.setActiveObject(textbox);
        editCanvas.renderAll();
        updateEmptyMessage();
        syncPreview();
    }

    // ===== 도형 추가 =====
    function addShape(shapeType = 'rectangle') {
        if (!editCanvas) return;

        let shape;
        if (shapeType === 'circle') {
            shape = new fabric.Circle({
                radius: 75,
                left: 150,
                top: 150,
                fill: '#667eea',
                stroke: '#000000',
                strokeWidth: 2
            });
        } else {
            shape = new fabric.Rect({
                left: 150,
                top: 150,
                width: 150,
                height: 150,
                fill: '#667eea',
                stroke: '#000000',
                strokeWidth: 2
            });
        }

        editCanvas.add(shape);
        editCanvas.setActiveObject(shape);
        editCanvas.renderAll();
        updateEmptyMessage();
        syncPreview();
    }

    // ===== 이미지 추가(파일 선택 트리거) =====
    function addImage() {
        const input = document.getElementById('imageFileInput');
        if (input) {
            input.value = '';
            input.click();
        }
    }

    // 이미지 파일 업로드 처리
    function bindImageUpload() {
        const imageInput = document.getElementById('imageFileInput');
        if (!imageInput) return;

        imageInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (!file) return;

            const reader = new FileReader();
            reader.onload = (event) => {
                fabric.Image.fromURL(event.target.result, (img) => {
                    img.scaleToWidth(200);
                    img.set({ left: 100, top: 100 });
                    editCanvas.add(img);
                    editCanvas.setActiveObject(img);
                    editCanvas.renderAll();
                    updateEmptyMessage();
                    syncPreview();
                }, { crossOrigin: 'anonymous' });
            };
            reader.readAsDataURL(file);
        });
    }

    // ===== 선택 삭제 =====
    function deleteSelected() {
        if (!editCanvas) return;
        const obj = editCanvas.getActiveObject();
        if (obj) {
            editCanvas.remove(obj);
            editCanvas.renderAll();
            updateEmptyMessage();
            syncPreview();
        }
    }

    // ===== 앞으로, 뒤로 =====
    function bringForward() {
        if (!editCanvas) return;
        const obj = editCanvas.getActiveObject();
        if (!obj) return;
        editCanvas.bringForward(obj);
        editCanvas.renderAll();
        syncPreview();
    }

    function sendBackward() {
        if (!editCanvas) return;
        const obj = editCanvas.getActiveObject();
        if (!obj) return;
        editCanvas.sendBackwards(obj);
        editCanvas.renderAll();
        syncPreview();
    }

    // ===== 템플릿 로드 =====
    function loadTemplate(el) {
        if (!editCanvas || !el) return;

        const bgUrl = el.getAttribute('data-bg');
        const layoutType = el.getAttribute('data-layout') || 'VERTICAL';
        const { width: w, height: h } = getCanvasSizeForLayout(layoutType);

        editCanvas.clear();
        editCanvas.setWidth(w);
        editCanvas.setHeight(h);

        if (!bgUrl) {
            editCanvas.setBackgroundColor('#ffffff', editCanvas.renderAll.bind(editCanvas));
            updateEmptyMessage();
            syncPreview();
            return;
        }

        fabric.Image.fromURL(bgUrl, (img) => {
            const scaleX = w / img.width;
            const scaleY = h / img.height;
            const scale = Math.min(scaleX, scaleY);

            editCanvas.setBackgroundImage(img, editCanvas.renderAll.bind(editCanvas), {
                scaleX: scale,
                scaleY: scale,
                top: 0,
                left: 0
            });

            updateEmptyMessage();
            syncPreview();
        }, { crossOrigin: 'anonymous' });
    }

    // ===== 템플릿 필터(레이아웃+카테고리) =====
    function filterTemplateByLayout() {
        const layoutSel = document.getElementById('templateLayout');
        const categorySel = document.getElementById('templateCategory');

        const layoutType = layoutSel ? layoutSel.value : '';
        const category = categorySel ? categorySel.value : '';

        let url = '/api/templates';
        const params = [];
        if (layoutType) params.push('layoutType=' + encodeURIComponent(layoutType));
        if (category) params.push('category=' + encodeURIComponent(category));
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
                const totalCount = typeof data.totalCount === 'number'
                    ? data.totalCount
                    : templates.length;

                // 총 개수 표시 갱신
                const totalEl = document.getElementById('templateTotalCount');
                if (totalEl) {
                    totalEl.textContent = totalCount;
                }

                // 현재 선택된 레이아웃/카테고리 라벨 갱신
                updateFilterLabels(layoutType, category);

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
                    item.setAttribute('data-template-id', tpl.templateId);
                    item.setAttribute('data-bg', tpl.templateImage);
                    item.setAttribute('data-layout', tpl.layoutType);

                    const thumb = document.createElement('div');
                    thumb.className = 'template-thumb';
                    const img = document.createElement('img');
                    img.src = tpl.templateImage;
                    img.alt = '템플릿';
                    img.style.width = '100%';
                    img.style.height = '100%';
                    img.style.objectFit = 'cover';
                    img.style.borderRadius = '4px';
                    thumb.appendChild(img);

                    const name = document.createElement('div');
                    name.className = 'template-name';
                    name.textContent = tpl.templateName || '템플릿';

                    const size = document.createElement('div');
                    size.className = 'template-size';
                    size.textContent = ''; // 필요하면 레이아웃/카테고리 등 표시

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

    function updateFilterLabels(layoutType, category) {
        const layoutLabelEl = document.getElementById('currentLayoutLabel');
        const categoryLabelEl = document.getElementById('currentCategoryLabel');

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

        if (layoutLabelEl) {
            layoutLabelEl.textContent = layoutLabelMap[layoutType || ''] || '전체 레이아웃';
        }
        if (categoryLabelEl) {
            categoryLabelEl.textContent = categoryLabelMap[category || ''] || '전체 카테고리';
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
    });

    // 외부에서 접근 가능하게 반환
    return {
        newWork,
        addText,
        addShape,
        addImage,
        deleteSelected,
        bringForward,
        sendBackward,
        loadTemplate,
        filterTemplateByLayout,
        saveWork
    };
})();

// inline onclick에서 접근할 수 있도록 window에 노출
window.PopEditor = PopEditor;
