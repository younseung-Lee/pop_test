// 비밀번호 표시/숨김 토글
function togglePassword() {
    const pwInput = document.getElementById('pw');
    const toggleBtn = document.querySelector('.password-toggle-btn');

    if (pwInput.type === 'password') {
        pwInput.type = 'text';
        toggleBtn.textContent = '비밀번호 가리기🙈';
    } else {
        pwInput.type = 'password';
        toggleBtn.textContent = '비밀번호 보기👁️';
    }
}

// 폼 제출 시 유효성 검사
document.getElementById('loginForm').addEventListener('submit', function(e) {
    const id = document.getElementById('id').value.trim();
    const pw = document.getElementById('pw').value.trim();

    if (id === '' || pw === '') {
        e.preventDefault();
        alert('아이디와 비밀번호를 모두 입력해주세요.');
        return false;
    }
});

// 페이지 로드 시 아이디 입력창에 포커스
window.onload = function() {
    const idInput = document.getElementById('id');
    // 아이디 필드가 비어있으면 아이디에 포커스, 아니면 비밀번호에 포커스
    if (idInput.value === '') {
        idInput.focus();
    } else {
        document.getElementById('pw').focus();
    }
};