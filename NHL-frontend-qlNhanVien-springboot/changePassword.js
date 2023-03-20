function changePassword() {
    var token = document.getElementById("token").value;
    var password = document.getElementById("password").value;

    var change = {
        token: token,
        password: password,
    }

    $.ajax({
        type: "PUT",
        url: "http://localhost:8080/api/qlNhanVien/accounts/changePassword",
        data: JSON.stringify(change), // body, truyền dữ liệu và body
        contentType: "application/json", // type of body, kiểu dữ liệu khi truyền vào body (json, xml, text)
        // dataType: 'json', // datatype return
        success: function (data) {
            console.log(data);
            // success
            if (data == undefined || data == null) {
                alert("Load dữ liệu bị lỗi");
                return;
            }
            window.location.replace("http://127.0.0.1:8081/login.html")
        }
    });
}