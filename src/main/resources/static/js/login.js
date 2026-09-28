$(document).ready(function () {
    // Ham Login: POST /auth/login -> luu JWT -> chuyen sang trang profile
    $('#loginForm').on('submit', function (e) {
        e.preventDefault();
        var basicInfo = JSON.stringify({
            email: $('#email').val(),
            password: $('#password').val()
        });
        $.ajax({
            type: "POST",
            url: "/auth/login",
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: basicInfo,
            success: function (data) {
                localStorage.setItem('token', data.token);
                window.location.href = "/user/profile";
            },
            error: function () {
                alert("Login Failed");
            }
        });
    });
});
