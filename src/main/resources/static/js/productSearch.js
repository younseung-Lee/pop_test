/**
 * 상품 이미지 검색 모듈
 */
const ProductImageSearch = {
    // 현재 검색 결과
    currentResults: [],
    
    /**
     * 초기화
     */
    init() {
        console.log('ProductImageSearch 초기화');
        this.bindEvents();
    },

    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        // 검색 버튼 클릭
        const searchBtn = document.getElementById('productSearchBtn');
        if (searchBtn) {
            searchBtn.addEventListener('click', () => this.search());
        }

        // 엔터키로 검색
        const keywordInput = document.getElementById('productSearchKeyword');
        if (keywordInput) {
            keywordInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.search();
                }
            });
        }
    },

    /**
     * 상품 이미지 검색
     */
    async search() {
        const searchType = document.getElementById('productSearchType')?.value || 'NAME';
        const keyword = document.getElementById('productSearchKeyword')?.value?.trim() || '';

        console.log('상품 이미지 검색:', { searchType, keyword });

        // 키워드가 없으면 알림
        if (!keyword) {
            alert('검색어를 입력해주세요.');
            return;
        }

        try {
            // 로딩 표시
            this.showLoading();

            // API 호출
            const response = await fetch(`/api/product-images/search?searchType=${searchType}&keyword=${encodeURIComponent(keyword)}`);
            const data = await response.json();

            console.log('검색 결과:', data);

            if (!data.success) {
                throw new Error(data.message || '검색에 실패했습니다.');
            }

            // 결과 저장
            this.currentResults = data.productImages || [];

            // 결과 표시
            this.displayResults(data.productImages, data.totalCount);

        } catch (error) {
            console.error('검색 오류:', error);
            alert('검색 중 오류가 발생했습니다: ' + error.message);
            this.showEmptyResult();
        }
    },

    /**
     * 검색 결과 표시
     */
    displayResults(products, totalCount) {
        const container = document.getElementById('productImageGrid');
        const countElement = document.getElementById('productSearchCount');

        if (!container) return;

        // 개수 표시
        if (countElement) {
            countElement.textContent = totalCount || 0;
        }

        // 결과가 없으면
        if (!products || products.length === 0) {
            this.showEmptyResult();
            return;
        }

        // 그리드 생성
        let html = '';
        products.forEach(product => {
            const imageUrl = product.mstrPrdtMUrl || '';
            const productName = product.mstrPrdtNm || '상품명 없음';
            const productCode = product.mstrPrdtCd || '';
            const categoryName = product.mstrPrdtCtgyFrstNm || '';

            html += `
                <div class="product-image-item" onclick="ProductImageSearch.selectProduct(${product.seqMstrPrdt})">
                    <div class="product-image-thumb ${imageUrl ? '' : 'no-image'}">
                        ${imageUrl 
                            ? `<img src="${imageUrl}" alt="${productName}" onerror="this.parentElement.classList.add('no-image'); this.style.display='none'; this.parentElement.textContent='이미지 없음';">` 
                            : '이미지 없음'
                        }
                    </div>
                    <div class="product-image-name" title="${productName}">${productName}</div>
                    <div class="product-image-code" title="${productCode}">${productCode}</div>
                    ${categoryName ? `<div class="product-image-category" title="${categoryName}">${categoryName}</div>` : ''}
                </div>
            `;
        });

        container.innerHTML = html;
    },

    /**
     * 빈 결과 표시
     */
    showEmptyResult() {
        const container = document.getElementById('productImageGrid');
        const countElement = document.getElementById('productSearchCount');

        if (countElement) {
            countElement.textContent = '0';
        }

        if (container) {
            container.innerHTML = `
                <div class="empty-result" style="grid-column: 1 / -1;">
                    <h3>검색 결과가 없습니다</h3>
                    <p>다른 검색어로 다시 시도해보세요.</p>
                </div>
            `;
        }
    },

    /**
     * 로딩 표시
     */
    showLoading() {
        const container = document.getElementById('productImageGrid');
        if (container) {
            container.innerHTML = `
                <div class="loading-spinner" style="grid-column: 1 / -1;">
                    <p>🔍 검색 중...</p>
                </div>
            `;
        }
    },

    /**
     * 상품 선택 (캔버스에 이미지 추가)
     */
    selectProduct(seqMstrPrdt) {
        const product = this.currentResults.find(p => p.seqMstrPrdt === seqMstrPrdt);
        
        if (!product || !product.mstrPrdtMUrl) {
            alert('이미지 URL을 찾을 수 없습니다.');
            return;
        }

        console.log('상품 선택:', product);

        // PopEditor의 이미지 추가 기능 사용
        if (typeof PopEditor !== 'undefined' && PopEditor.addImageFromUrl) {
            PopEditor.addImageFromUrl(product.mstrPrdtMUrl);
        } else {
            // Fabric.js 캔버스에 직접 추가
            this.addImageToCanvas(product.mstrPrdtMUrl);
        }
    },

    /**
     * 캔버스에 이미지 추가 (Fabric.js 사용)
     */
    addImageToCanvas(imageUrl) {
        const canvas = window.editCanvas || window.canvas;
        
        if (!canvas) {
            alert('캔버스를 찾을 수 없습니다.');
            return;
        }

        // Fabric.js Image 로드
        fabric.Image.fromURL(imageUrl, (img) => {
            // 이미지 크기 조정 (캔버스의 1/3 크기로)
            const scale = Math.min(
                canvas.width / 3 / img.width,
                canvas.height / 3 / img.height
            );
            
            img.scale(scale);
            
            // 캔버스 중앙에 배치
            img.set({
                left: canvas.width / 2,
                top: canvas.height / 2,
                originX: 'center',
                originY: 'center'
            });

            // 캔버스에 추가
            canvas.add(img);
            canvas.setActiveObject(img);
            canvas.renderAll();

            console.log('이미지가 캔버스에 추가되었습니다.');
        }, {
            crossOrigin: 'anonymous'
        });
    }
};

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    ProductImageSearch.init();
});
