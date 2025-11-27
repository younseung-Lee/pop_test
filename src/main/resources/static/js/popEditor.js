// popEditor.js

// 전역 네임스페이스 하나로 묶어두기
const PopEditor = (() => {

    let editCanvas;
    let previewCanvas;

    function initCanvases() {
        editCanvas = new fabric.Canvas('editCanvas', {
            preserveObjectStacking: true,
        });

        previewCanvas = new fabric.Canvas('previewCanvas', {
            interactive: false,
            selection: false
        });

        // 초기 배경(가이드용 약한 그리드 느낌 주고 싶으면 여기서 가능)
        // 지금은 그냥 흰색
        syncPreview();

        // 편집 변경 이벤트 → 미리보기 반영
        const syncEvents = ['object:added', 'object:modified', 'object:removed'];
        syncEvents.forEach(ev => {
            editCanvas.on(ev, () => syncPreview());
        });
    }

    function syncPreview() {
        if (!editCanvas || !previewCanvas) return;

        const json = editCanvas.toJSON();

        // 미리보기 캔버스는 크기가 다르므로 비율 맞춰서 그려야 함
        // 여기선 단순히 scale로 전체 그룹을 줄이는 방식이 아니라,
        // 캔버스 크기 비율로 zoom만 조정.
        previewCanvas.clear();

        // loadFromJSON은 비동기
        previewCanvas.loadFromJSON(json, () => {
            // 편집 캔버스 기준 비율
            const scaleX = previewCanvas.getWidth() / editCanvas.getWidth();
            const scaleY = previewCanvas.getHeight() / editCanvas.getHeight();
            const zoom = Math.min(scaleX, scaleY);

            previewCanvas.setViewportTransform([zoom, 0, 0, zoom, 0, 0]);
            previewCanvas.renderAll();
        });
    }

    // 텍스트 추가
    function addText() {
        if (!editCanvas) return;
        const fontSize = parseInt(document.getElementById('fontSizeSelect').value || '40', 10);
        const fontFamily = document.getElementById('fontFamilySelect').value || '맑은 고딕';

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
    }

    // 선택 삭제
    function deleteSelected() {
        const obj = editCanvas.getActiveObject();
        if (obj) {
            editCanvas.remove(obj);
            editCanvas.renderAll();
        }
    }

    // 앞으로, 뒤로
    function bringForward() {
        const obj = editCanvas.getActiveObject();
        if (!obj) return;
        editCanvas.bringForward(obj);
        editCanvas.renderAll();
    }

    function sendBackward() {
        const obj = editCanvas.getActiveObject();
        if (!obj) return;
        editCanvas.sendBackwards(obj);
        editCanvas.renderAll();
    }

    // 새 문서 (초기화)
    function newWork() {
        if (!editCanvas) return;
        if (!confirm('현재 작업 내용을 지우고 새 문서를 시작할까요?')) return;

        editCanvas.clear();
        editCanvas.setBackgroundColor('#ffffff', editCanvas.renderAll.bind(editCanvas));
        syncPreview();
    }

    // 템플릿 로드 (템플릿 카드 클릭 시 호출)
    function loadTemplate(el) {
        if (!editCanvas) return;

        const bgUrl = el.getAttribute('data-bg');
        const w = parseInt(el.getAttribute('data-w') || '800', 10);
        const h = parseInt(el.getAttribute('data-h') || '600', 10);

        editCanvas.setWidth(w);
        editCanvas.setHeight(h);

        if (!bgUrl) {
            editCanvas.clear();
            editCanvas.setBackgroundColor('#ffffff', editCanvas.renderAll.bind(editCanvas));
            syncPreview();
            return;
        }

        fabric.Image.fromURL(bgUrl, img => {
            editCanvas.clear();

            const scaleX = w / img.width;
            const scaleY = h / img.height;

            editCanvas.setBackgroundImage(img, editCanvas.renderAll.bind(editCanvas), {
                scaleX,
                scaleY
            });

            syncPreview();
        }, { crossOrigin: 'anonymous' });
    }

    // 템플릿 검색 (지금은 콘솔만, 나중에 fetch로 교체)
    function searchTemplate() {
        const category = document.getElementById('templateCategory').value;
        const keyword = document.getElementById('templateKeyword').value.trim();
        console.log('템플릿 검색', { category, keyword });

        // TODO: 나중에 백엔드 API 연동
        // fetch(`/api/pop/template/search?category=${...}&keyword=${...}`)
        //   .then(res => res.json()).then(renderTemplateList)
        alert('템플릿 검색은 백엔드 구현 후 연결할 예정입니다.');
    }

    // 작업 저장 (백엔드 연동용 기본 골격)
    function saveWork() {
        if (!editCanvas) return;

        const payload = {
            martCd: MART_CD,
            userId: USER_ID,
            canvasJson: JSON.stringify(editCanvas.toJSON()),
            // templateId, title 등은 필요할 때 채우기
        };

        console.log('save payload = ', payload);

        // TODO: 실제 저장 API 연결시 사용
        /*
        fetch('/api/pop/work/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json;charset=UTF-8' },
            body: JSON.stringify(payload),
        })
            .then(res => res.json())
            .then(data => {
                if (data.code === '00') {
                    alert('저장되었습니다. workId=' + data.workId);
                } else {
                    alert('저장 실패: ' + (data.msg || '알 수 없는 오류'));
                }
            })
            .catch(err => {
                console.error(err);
                alert('서버 오류가 발생했습니다.');
            });
        */

        // 일단은 동작 확인용
        alert('저장 로직은 백엔드 구현 후 연결할 예정입니다.\n(콘솔 payload 참고)');
    }

    // DOM 로드 후 초기화
    document.addEventListener('DOMContentLoaded', () => {
        initCanvases();

        // 템플릿 카드에 클릭 핸들러 붙이기 (Thymeleaf로 렌더링된 요소들)
        document.querySelectorAll('.template-item').forEach(el => {
            el.addEventListener('click', () => loadTemplate(el));
        });
    });

    // 외부에 노출할 메서드들
    return {
        newWork,
        addText,
        deleteSelected,
        bringForward,
        sendBackward,
        loadTemplate,
        searchTemplate,
        saveWork
    };
})();
