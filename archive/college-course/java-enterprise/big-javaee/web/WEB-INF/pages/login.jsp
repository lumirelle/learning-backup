<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<html>
<head>
    <title>Login</title>
    <link href="${pageContext.request.contextPath}/css/regcss.css" type="text/css"
          rel="stylesheet"/>
    <script>
      function validate() {
        let username = document.getElementById("username").value;
        let password = document.getElementById("password").value;
        if (username == null || username === "") {
          alert("用户名不能为空");
          document.getElementById("username").focus();
          return false;
        } else if (password == null || password === "") {
          alert("密码不能为空");
          document.getElementById("password").focus();
          return false;
        }
        return true;
      }
    </script>
</head>
<body>
<div id="content">
    <div id="form">
        <h1>用户登录</h1><br/>
        <form action="login.action" method="post" id="myform"
              onsubmit="return validate()">
            <label for="username">用户名：</label>
            <input type="text" name="username" id="username"
                   style="width: 190px;height: 26px;margin-left: 39px"/><br/>
            <label for="password">密码：</label>
            <input type="password" name="password" id="password"
                   style="width: 190px;height: 26px;margin-top: 8px;margin-left: 54px"/><br/>
            <input type="submit" value="登录" style="width: 50px;height: 30px;margin-top: 8px;"/>
            <a href="registerPage.action">注册</a>
        </form>
        <c:if test="${errorMsg!=null}">
            <div style="color: red">${errorMsg}</div>
        </c:if>
        <c:if test="${noticeMsg!=null}">
            <div style="color: green">${noticeMsg}</div>
        </c:if>
    </div>
</div>
</body>
</html>
