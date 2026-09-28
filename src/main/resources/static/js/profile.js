$(document).ready(function () {
    // Hien thi thong tin nguoi dung dang nhap thanh cong: GET /users/me kem "Authorization: Bearer <token>"
    $.ajax({
        type: 'GET',
        url: '/users/me',
        dataType: 'json',
        contentType: "application/json; charset=utf-8",
        beforeSend: function (xhr) {
            var token = localStorage.getItem('token');
            if (token) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + token);
            }
        },
        success: function (data) {
            $('#profile').text(data.fullName);          // .text() de tranh XSS (khong dung .html())
            $('#images').attr('src', data.images);
        },
        error: function () {
            alert("Sorry, you are not logged in.");
            window.location.href = "/login";
        }
    });

    // Ham dang xuat: xoa token
    $('#logout').click(function () {
        localStorage.clear();
        window.location.href = "/login";
    });
});
