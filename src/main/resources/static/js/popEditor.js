// /js/popEditor.js

const PopEditor = (() => {
    let editCanvas;
    let previewCanvas;

    // ✅ 미리보기 확대 모달용
    let previewZoomCanvas;
    let isPreviewZoomOpen = false;

    // ===== 템플릿 조회 상태 =====
    let templateSource = 'COMMON';      // COMMON(공통), MY(우리 마트)
    let templatePage = 1;              // 현재 페이지 (1부터 시작)
    const TEMPLATE_PAGE_SIZE = 12;     // 한 페이지당 템플릿 개수
    let lastLayoutFilter = '';
    let lastCategoryFilter = '';
    let selectedCommonTemplate = null;

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

                // ✅ 확대 모달이 열려있으면 같이 갱신
                if (isPreviewZoomOpen) {
                    syncPreviewZoom();
                }
            });
        });

        bindCanvasPanZoom();

        // ✅ 미리보기 클릭 확대 바인딩
        bindPreviewZoom();
    }

    // ===== 확대/축소 & 팬 =====
    function bindCanvasPanZoom() {
        if (!editCanvas) return;

        // 마우스 휠 줌
        editCanvas.on('mouse:wheel', function (opt) {
            const delta = opt.e.deltaY;
            let zoom = editCanvas.getZoom();

            // 휠 방향에 따라 줌 값 변경
            zoom *= Math.pow(0.999, delta);

            // 최소/최대 줌 제한 (1% ~ 2000%)
            if (zoom > 20) zoom = 20;
            if (zoom < 0.01) zoom = 0.01;

            // 커서 위치를 기준으로 줌
            editCanvas.zoomToPoint(
                { x: opt.e.offsetX, y: opt.e.offsetY },
                zoom
            );

            opt.e.preventDefault();
            opt.e.stopPropagation();
        });

        // Alt + Drag 로 패닝
        editCanvas.on('mouse:down', function (opt) {
            const evt = opt.e;
            if (evt.altKey === true) {
                this.isDragging = true;
                this.selection = false;
                this.lastPosX = evt.clientX;
                this.lastPosY = evt.clientY;
            }
        });

        editCanvas.on('mouse:move', function (opt) {
            if (this.isDragging) {
                const e = opt.e;
                const vpt = this.viewportTransform;
                vpt[4] += e.clientX - this.lastPosX;
                vpt[5] += e.clientY - this.lastPosY;
                this.requestRenderAll();
                this.lastPosX = e.clientX;
                this.lastPosY = e.clientY;
            }
        });

        editCanvas.on('mouse:up', function () {
            this.setViewportTransform(this.viewportTransform);
            this.isDragging = false;
            this.selection = true;
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
    async function syncPreview() {
        if (!editCanvas || !previewCanvas) return;

        const json = editCanvas.toJSON();
        previewCanvas.clear();

        try {
            await previewCanvas.loadFromJSON(json);

            const scaleX = previewCanvas.getWidth() / editCanvas.getWidth();
            const scaleY = previewCanvas.getHeight() / editCanvas.getHeight();
            const zoom = Math.min(scaleX, scaleY);

            previewCanvas.setViewportTransform([zoom, 0, 0, zoom, 0, 0]);
            previewCanvas.renderAll();
        } catch (error) {
            console.error('미리보기 동기화 실패:', error);
        }
    }

    // ✅ 확대 모달 캔버스 갱신 (열려있을 때만)
    async function syncPreviewZoom() {
        if (!isPreviewZoomOpen) return;
        if (!editCanvas || !previewZoomCanvas) return;

        const zoomCanvasEl = document.getElementById('previewZoomCanvas');
        const modal = document.getElementById('previewZoomModal');
        const content = modal ? modal.querySelector('.preview-zoom-content') : null;

        if (!zoomCanvasEl || !content) return;

        const w = editCanvas.getWidth();
        const h = editCanvas.getHeight();

        previewZoomCanvas.setDimensions({ width: w, height: h });

        const json = editCanvas.toJSON();
        previewZoomCanvas.clear();

        try {
            await previewZoomCanvas.loadFromJSON(json);
            previewZoomCanvas.renderAll();
        } catch (e) {
            console.error('확대 미리보기 동기화 실패:', e);
        }

        // 모달 컨테이너에 맞춰 축소/확대
        requestAnimationFrame(() => {
            const maxW = content.clientWidth - 24;
            const maxH = content.clientHeight - 24;
            const scale = Math.min(maxW / w, maxH / h, 1);

            zoomCanvasEl.style.transformOrigin = 'top left';
            zoomCanvasEl.style.transform = `scale(${scale})`;
        });
    }

    // ✅ 미리보기 클릭 확대/ESC/배경 클릭 닫기
    function bindPreviewZoom() {
        const previewContainer = document.getElementById('previewContainer');
        const modal = document.getElementById('previewZoomModal');
        if (!previewContainer || !modal) return;

        const overlay = modal.querySelector('.preview-zoom-overlay');
        const content = modal.querySelector('.preview-zoom-content');

        // 미리보기 클릭 -> 확대 열기
        previewContainer.addEventListener('click', () => {
            openPreviewZoom();
        });

        // 배경 클릭 -> 닫기
        if (overlay) {
            overlay.addEventListener('click', () => closePreviewZoom());
        }

        // 모달 내부 클릭은 닫힘 방지
        if (content) {
            content.addEventListener('click', (e) => e.stopPropagation());
        }

        // ESC -> 닫기
        window.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && isPreviewZoomOpen) {
                closePreviewZoom();
            }
        });
    }

    async function openPreviewZoom() {
        const modal = document.getElementById('previewZoomModal');
        const content = modal ? modal.querySelector('.preview-zoom-content') : null;
        const zoomCanvasEl = document.getElementById('previewZoomCanvas');

        if (!modal || !content || !zoomCanvasEl) return;
        if (!editCanvas) return;

        isPreviewZoomOpen = true;
        modal.classList.add('open');
        modal.setAttribute('aria-hidden', 'false');

        // 1회만 Fabric 캔버스 생성
        if (!previewZoomCanvas) {
            previewZoomCanvas = new fabric.Canvas('previewZoomCanvas', {
                selection: false,
                interactive: false
            });
        }

        await syncPreviewZoom();
    }

    function closePreviewZoom() {
        const modal = document.getElementById('previewZoomModal');
        const zoomCanvasEl = document.getElementById('previewZoomCanvas');
        if (!modal) return;

        isPreviewZoomOpen = false;
        modal.classList.remove('open');
        modal.setAttribute('aria-hidden', 'true');

        if (zoomCanvasEl) {
            zoomCanvasEl.style.transform = 'scale(1)';
        }
    }

    // ===== 새 문서 =====
    function newWork() {
        if (!editCanvas) return;
        if (!confirm('현재 작업 내용을 지우고 새 문서를 시작할까요?')) return;

        editCanvas.clear();
        editCanvas.setDimensions({ width: 800, height: 600 });
        editCanvas.backgroundColor = '#ffffff';
        editCanvas.renderAll();

        editCanvas.setViewportTransform([1, 0, 0, 1, 0, 0]);

        editCanvas.renderAll();

        updateEmptyMessage();
        syncPreview();

        // ✅ 확대 모달 열려있으면 같이 갱신
        if (isPreviewZoomOpen) syncPreviewZoom();
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
            width: 200,
            fontSize,
            fontFamily,
            fill: '#000000'
        });

        editCanvas.add(textbox);
        editCanvas.setActiveObject(textbox);
        editCanvas.renderAll();
        updateEmptyMessage();
        syncPreview();

        if (isPreviewZoomOpen) syncPreviewZoom();
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
                fill: 'transparent',  // 투명 (색없음)
                stroke: '#000000',
                strokeWidth: 2
            });
        } else {
            shape = new fabric.Rect({
                left: 150,
                top: 150,
                width: 150,
                height: 150,
                fill: 'transparent',
                stroke: '#000000',
                strokeWidth: 2
            });
        }

        editCanvas.add(shape);
        editCanvas.setActiveObject(shape);
        editCanvas.renderAll();
        updateEmptyMessage();
        syncPreview();

        if (isPreviewZoomOpen) syncPreviewZoom();
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

        imageInput.addEventListener('change', async (e) => {
            const file = e.target.files[0];
            if (!file) return;

            const reader = new FileReader();
            reader.onload = async (event) => {
                try {
                    const img = await fabric.FabricImage.fromURL(event.target.result, {
                        crossOrigin: 'anonymous'
                    });

                    img.scaleToWidth(200);
                    img.set({ left: 100, top: 100 });
                    editCanvas.add(img);
                    editCanvas.setActiveObject(img);
                    editCanvas.renderAll();
                    updateEmptyMessage();
                    syncPreview();

                    if (isPreviewZoomOpen) syncPreviewZoom();
                } catch (error) {
                    console.error('이미지 로드 실패:', error);
                    alert('이미지를 불러오는데 실패했습니다.');
                }
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

            if (isPreviewZoomOpen) syncPreviewZoom();
        }
    }

    // ===== 색상 적용 =====
    function applyColors() {
        if (!editCanvas) return;
        const obj = editCanvas.getActiveObject();
        if (!obj) {
            alert('색상을 적용할 객체를 먼저 선택해주세요.');
            return;
        }

        const fillColorPicker = document.getElementById('fillColorPicker');
        const strokeColorPicker = document.getElementById('strokeColorPicker');

        const fillColor = fillColorPicker ? fillColorPicker.value : '#ffffff';
        const strokeColor = strokeColorPicker ? strokeColorPicker.value : '#000000';

        if (obj.type === 'textbox' || obj.type === 'text' || obj.type === 'i-text') {
            obj.set('fill', fillColor);
        } else {
            obj.set({
                fill: fillColor,
                stroke: strokeColor,
                strokeWidth: obj.strokeWidth || 2
            });
        }

        editCanvas.renderAll();
        syncPreview();

        if (isPreviewZoomOpen) syncPreviewZoom();
    }

    // ===== 색상 제거 (투명하게) =====
    function removeColors() {
        if (!editCanvas) return;
        const obj = editCanvas.getActiveObject();
        if (!obj) {
            alert('색상을 제거할 객체를 먼저 선택해주세요.');
            return;
        }

        if (obj.type === 'textbox' || obj.type === 'text' || obj.type === 'i-text') {
            alert('텍스트의 색상은 제거할 수 없습니다.');
            return;
        }

        obj.set({
            fill: 'transparent',
            stroke: obj.stroke || '#000000',
            strokeWidth: obj.strokeWidth || 2
        });

        editCanvas.renderAll();
        syncPreview();

        if (isPreviewZoomOpen) syncPreviewZoom();
    }

    // ===== 텍스트 스타일 적용 (폰트, 크기) =====
    function applyTextStyle() {
        if (!editCanvas) return;
        const obj = editCanvas.getActiveObject();
        if (!obj) {
            alert('폰트를 적용할 텍스트를 먼저 선택해주세요.');
            return;
        }

        if (obj.type !== 'textbox' && obj.type !== 'text' && obj.type !== 'i-text') {
            alert('텍스트 객체만 폰트를 변경할 수 있습니다.');
            return;
        }

        const fontSizeSelect = document.getElementById('fontSizeSelect');
        const fontFamilySelect = document.getElementById('fontFamilySelect');

        const fontSize = fontSizeSelect ? parseInt(fontSizeSelect.value, 10) : 36;
        const fontFamily = fontFamilySelect ? fontFamilySelect.value : 'Malgun Gothic';

        obj.set({
            fontSize: fontSize,
            fontFamily: fontFamily
        });

        editCanvas.renderAll();
        syncPreview();

        if (isPreviewZoomOpen) syncPreviewZoom();
    }

    // ===== 앞으로, 뒤로 =====
    function bringForward() {
        if (!editCanvas) return;
        const obj = editCanvas.getActiveObject();
        if (!obj) return;
        editCanvas.bringObjectForward(obj);
        editCanvas.renderAll();
        syncPreview();

        if (isPreviewZoomOpen) syncPreviewZoom();
    }

    function sendBackward() {
        if (!editCanvas) return;
        const obj = editCanvas.getActiveObject();
        if (!obj) return;
        editCanvas.sendObjectBackwards(obj);
        editCanvas.renderAll();
        syncPreview();

        if (isPreviewZoomOpen) syncPreviewZoom();
    }

    // ===== 템플릿 로드 =====
    async function loadTemplate(el) {
        if (!editCanvas || !el) return;

        selectedCommonTemplate = {
            tplId: el.getAttribute('data-template-id'),
            tplSeq: el.getAttribute('data-tpl-seq'),
            bgImgUrl: el.getAttribute('data-bg'),
            layoutType: el.getAttribute('data-layout') || 'VERTICAL',
            ctgyBig: el.getAttribute('data-ctgy-big') || '',
            ctgyMid: el.getAttribute('data-ctgy-mid') || '',
            ctgySml: el.getAttribute('data-ctgy-sml') || '',
            ctgySub: el.getAttribute('data-ctgy-sub') || '',
            tplJson: el.getAttribute('data-tpl-json') || ''
        };

        const bgUrl = selectedCommonTemplate.bgImgUrl;
        const layoutType = selectedCommonTemplate.layoutType;
        const tplJson = selectedCommonTemplate.tplJson;
        const { width: w, height: h } = getCanvasSizeForLayout(layoutType);

        editCanvas.clear();
        editCanvas.setDimensions({ width: w, height: h });
        editCanvas.setViewportTransform([1, 0, 0, 1, 0, 0]);

        // tplJson이 있으면 전체 캔버스 데이터 로드 (텍스트, 도형 포함)
        if (tplJson) {
            try {
                const jsonData = JSON.parse(tplJson);
                await editCanvas.loadFromJSON(jsonData);
                editCanvas.renderAll();
                updateEmptyMessage();
                syncPreview();
                if (isPreviewZoomOpen) syncPreviewZoom();
                console.log('템플릿 로드 완료 (편집 데이터 포함)');
                return;
            } catch (error) {
                console.warn('tplJson 파싱 실패, 배경 이미지만 로드:', error);
                // tplJson 파싱 실패 시 배경 이미지만 로드
            }
        }

        // tplJson이 없거나 파싱 실패 시 배경 이미지만 로드
        if (!bgUrl) {
            editCanvas.backgroundColor = '#ffffff';
            editCanvas.renderAll();
            updateEmptyMessage();
            syncPreview();

            if (isPreviewZoomOpen) syncPreviewZoom();
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

            if (isPreviewZoomOpen) syncPreviewZoom();
            console.log('템플릿 로드 완료 (배경 이미지만)');
        } catch (error) {
            console.error('템플릿 로드 실패:', error);
            alert('템플릿을 불러오는데 실패했습니다.');
        }
    }

    // ===== 템플릿 필터(레이아웃+카테고리+타입+페이징) =====
    async function filterTemplateByLayout() {
        const layoutSel   = document.getElementById('templateLayout');
        const categorySel = document.getElementById('templateCategory');
        const sourceSel   = document.getElementById('templateSource');

        const layoutType = layoutSel ? layoutSel.value : '';
        const category   = categorySel ? categorySel.value : '';
        const source     = sourceSel ? sourceSel.value : 'COMMON';

        // 템플릿 타입이 변경된 경우 카테고리 재로드
        if (templateSource !== source) {
            templateSource = source;
            await loadCategories(source);
            // 카테고리가 초기화되었으므로 현재 값을 다시 가져옴
            const newCategory = categorySel ? categorySel.value : '';
            lastCategoryFilter = newCategory;
        } else {
            templateSource = source;
        }

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
        if (category)  params.push('ctgyBig=' + encodeURIComponent(category));

        // 페이징 파라미터
        params.push('page=' + encodeURIComponent(templatePage));
        params.push('size=' + encodeURIComponent(TEMPLATE_PAGE_SIZE));

        if (params.length > 0) url += '?' + params.join('&');

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
                if (totalEl) totalEl.textContent = totalCount;

                updateFilterLabels(layoutType, category, source);
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
                    item.setAttribute('data-tpl-seq', tpl.tplSeq);
                    item.setAttribute('data-bg', tpl.bgImgUrl);
                    item.setAttribute('data-layout', tpl.layoutType);
                    item.setAttribute('data-tpl-json', tpl.tplJson || '');

                    item.setAttribute('data-ctgy-big', tpl.tplCtgyBig || '');
                    item.setAttribute('data-ctgy-mid', tpl.tplCtgyMid || '');
                    item.setAttribute('data-ctgy-sml', tpl.tplCtgySml || '');
                    item.setAttribute('data-ctgy-sub', tpl.tplCtgySub || '');

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

                    // a4 관리자에게만 삭제 버튼 표시
                    const isAdmin = document.querySelector('[data-user-id="a4"]');
                    if (isAdmin) {
                        const deleteBtn = document.createElement('button');
                        deleteBtn.className = 'delete-template-btn';
                        deleteBtn.type = 'button';
                        deleteBtn.textContent = '❌';
                        deleteBtn.title = '템플릿 삭제';
                        deleteBtn.setAttribute('data-tpl-seq', tpl.tplSeq);
                        deleteBtn.setAttribute('data-tpl-nm', tpl.tplNm);
                        deleteBtn.onclick = function(e) {
                            e.stopPropagation();
                            if (typeof deleteTemplate === 'function') {
                                deleteTemplate(e, tpl.tplSeq, tpl.tplNm);
                            }
                        };
                        thumb.appendChild(deleteBtn);
                    }

                    const name = document.createElement('div');
                    name.className = 'template-name';
                    name.textContent = tpl.tplNm || '템플릿';

                    const size = document.createElement('div');
                    size.className = 'template-size';
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

        if (pageInfoEl) pageInfoEl.textContent = `${templatePage} / ${totalPages} 페이지`;
        if (prevBtn) prevBtn.disabled = (templatePage <= 1);
        if (nextBtn) nextBtn.disabled = (templatePage >= totalPages);
    }

    // ===== 카테고리 동적 로드 =====
    async function loadCategories(source) {
        const categorySel = document.getElementById('templateCategory');
        if (!categorySel) return;

        // 엔드포인트 결정
        const url = (source === 'MY')
            ? '/api/templates/my/categories'
            : '/api/templates/categories';

        try {
            const res = await fetch(url);
            const data = await res.json();

            if (!res.ok || !data.success) {
                console.error('카테고리 조회 실패:', data);
                return;
            }

            const categories = data.categories || [];

            // 선택박스 초기화
            categorySel.innerHTML = '<option value="">전체 카테고리</option>';

            // 카테고리 옵션 추가
            categories.forEach(category => {
                const option = document.createElement('option');
                option.value = category;
                option.textContent = category;
                categorySel.appendChild(option);
            });

            // 현재 필터 유지 (가능한 경우)
            if (lastCategoryFilter && categories.includes(lastCategoryFilter)) {
                categorySel.value = lastCategoryFilter;
            } else {
                categorySel.value = '';
                lastCategoryFilter = '';
            }

        } catch (error) {
            console.error('카테고리 조회 중 오류:', error);
        }
    }

    // 우리매장 저장 - 모달창으로 입력받기
    async function saveWork() {
        if (!editCanvas) return;

        // 모달 열기
        showSaveModal();
    }

    // 저장 모달 표시
    function showSaveModal() {
        const modal = document.getElementById('saveModal');
        if (!modal) {
            console.error('저장 모달을 찾을 수 없습니다.');
            return;
        }

        // 입력 필드 초기화
        document.getElementById('saveTplNm').value = '';
        document.getElementById('saveCtgyBig').value = '';
        document.getElementById('saveLayoutType').value = selectedCommonTemplate?.layoutType || 'VERTICAL';

        modal.style.display = 'flex';
    }

    // 저장 모달 닫기
    function closeSaveModal() {
        const modal = document.getElementById('saveModal');
        if (modal) {
            modal.style.display = 'none';
        }
    }

    // 저장 실행
    async function confirmSave() {
        const tplNm = document.getElementById('saveTplNm').value.trim();
        const ctgyBig = document.getElementById('saveCtgyBig').value.trim();
        const layoutType = document.getElementById('saveLayoutType').value;

        // 유효성 검사
        if (!tplNm) {
            alert('템플릿명을 입력해주세요.');
            document.getElementById('saveTplNm').focus();
            return;
        }

        if (!ctgyBig) {
            alert('카테고리(대)를 입력해주세요.');
            document.getElementById('saveCtgyBig').focus();
            return;
        }

        if (!layoutType) {
            alert('레이아웃을 선택해주세요.');
            document.getElementById('saveLayoutType').focus();
            return;
        }

        const payload = {
            tplNm: tplNm,
            tplCtgyBig: ctgyBig,
            layoutType: layoutType,
            bgImgUrl: selectedCommonTemplate?.bgImgUrl || '',
            tplCtgyMid: selectedCommonTemplate?.ctgyMid || '',
            tplCtgySml: selectedCommonTemplate?.ctgySml || '',
            tplCtgySub: selectedCommonTemplate?.ctgySub || '',
            tplJson: JSON.stringify(editCanvas.toJSON()),
            useYn: 'Y'
        };

        try {
            const res = await fetch('/api/templates/my', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json().catch(() => ({}));

            if (!res.ok || !data.success) {
                alert('저장 실패: ' + (data.message || '서버 오류'));
                return;
            }

            alert('우리 매장 템플릿 저장 완료!');
            closeSaveModal();

            // 우리 마트 템플릿으로 필터 전환
            const sourceSel = document.getElementById('templateSource');
            if (sourceSel) {
                sourceSel.value = 'MY';
                filterTemplateByLayout();
            }
        } catch (e) {
            console.error(e);
            alert('저장 중 오류가 발생했습니다.');
        }
    }

    // ===== DOM 이벤트 바인딩 =====
    document.addEventListener('DOMContentLoaded', () => {
        initCanvases();
        bindImageUpload();

        //  메인 진입 시: 무조건 "공통 템플릿 + 전체 필터 + 1페이지"
        const sourceSel = document.getElementById('templateSource');
        const layoutSel = document.getElementById('templateLayout');
        const categorySel = document.getElementById('templateCategory');

        if (sourceSel) sourceSel.value = 'COMMON';
        if (layoutSel) layoutSel.value = '';
        if (categorySel) categorySel.value = '';

        templateSource = 'COMMON';
        templatePage = 1;
        lastLayoutFilter = '';
        lastCategoryFilter = '';

        //  초기 카테고리 및 템플릿 조회(공통)
        (async () => {
            await loadCategories('COMMON');
            filterTemplateByLayout();
        })();

        // 템플릿 카드 클릭: 이벤트 위임
        const templateList = document.getElementById('templateList');
        if (templateList) {
            templateList.addEventListener('click', (e) => {
                const item = e.target.closest('.template-item');
                if (item) loadTemplate(item);
            });
        }

        // 사이드바 탭 전환 (UI만)
        document.querySelectorAll('.sidebar-tab').forEach(tab => {
            tab.addEventListener('click', function () {
                document.querySelectorAll('.sidebar-tab').forEach(t => t.classList.remove('active'));
                this.classList.add('active');
            });
        });

        // 템플릿 소스 변경 시 필터링
        if (sourceSel) {
            sourceSel.addEventListener('change', () => {
                templatePage = 1;
                filterTemplateByLayout();
            });
        }

        // 레이아웃 변경 시 필터링
        if (layoutSel) {
            layoutSel.addEventListener('change', () => {
                filterTemplateByLayout();
            });
        }

        // 카테고리 변경 시 필터링
        if (categorySel) {
            categorySel.addEventListener('change', () => {
                filterTemplateByLayout();
            });
        }

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

    // ===== URL로 이미지 추가 (상품 이미지 검색용) =====
    function addImageFromUrl(imageUrl) {
        if (!editCanvas) {
            console.error('캔버스가 초기화되지 않았습니다.');
            return;
        }

        if (!imageUrl) {
            console.error('이미지 URL이 없습니다.');
            return;
        }

        console.log('URL로 이미지 추가:', imageUrl);

        // Fabric.js Image 로드
        fabric.Image.fromURL(imageUrl, (img) => {
            if (!img) {
                alert('이미지를 불러올 수 없습니다.');
                return;
            }

            // 이미지 크기 조정 (캔버스의 1/3 크기로)
            const scale = Math.min(
                editCanvas.width / 3 / img.width,
                editCanvas.height / 3 / img.height
            );
            
            img.scale(scale);
            
            // 캔버스 중앙에 배치
            img.set({
                left: editCanvas.width / 2,
                top: editCanvas.height / 2,
                originX: 'center',
                originY: 'center'
            });

            // 캔버스에 추가
            editCanvas.add(img);
            editCanvas.setActiveObject(img);
            editCanvas.renderAll();

            console.log('이미지가 캔버스에 추가되었습니다.');
        }, {
            crossOrigin: 'anonymous'
        });
    }

    return {
        newWork,
        addText,
        addShape,
        addImage,
        addImageFromUrl,
        deleteSelected,
        applyColors,
        removeColors,
        applyTextStyle,
        bringForward,
        sendBackward,
        loadTemplate,
        filterTemplateByLayout,
        saveWork,
        showSaveModal,
        closeSaveModal,
        confirmSave
    };
})();

window.PopEditor = PopEditor;
