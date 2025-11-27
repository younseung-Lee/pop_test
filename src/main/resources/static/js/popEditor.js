// /js/popEditor.js

const PopEditor = (() => {
    let editCanvas;
    let previewCanvas;

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
        const w = parseInt(el.getAttribute('data-w') || '800', 10);
        const h = parseInt(el.getAttribute('data-h') || '600', 10);

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

    // ===== 템플릿 검색 더미 =====
    function searchTemplate() {
        const keywordEl = document.getElementById('templateKeyword');
        const catEl = document.getElementById('templateCategory');

        const kw = keywordEl ? keywordEl.value.trim() : '';
        const cat = catEl ? catEl.value : '';

        alert(`템플릿 검색 (백엔드 연결 예정)\n카테고리: ${cat}\n검색어: ${kw}`);
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

        // 템플릿 카드 클릭
        document.querySelectorAll('.template-item').forEach((el) => {
            el.addEventListener('click', () => loadTemplate(el));
        });

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
        searchTemplate,
        saveWork
    };
})();

// inline onclick에서 접근할 수 있도록 window에 노출
window.PopEditor = PopEditor;
