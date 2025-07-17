<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>注册</title>
    <link href="${pageContext.request.contextPath}/css/regcss.css" type="text/css" rel="stylesheet">
    <script>
      function validate() {
        if (document.getElementById("username").value === "") {
          alert("用户名不能为空");
          document.getElementById("username").focus();
          return false;
        } else if (document.getElementById("password").value === "") {
          alert("密码不能为空");
          document.getElementById("password").focus();
          return false;
        } else if (document.getElementById("name").value === "") {
          alert("姓名不能为空");
          document.getElementById("name").focus();
          return false;
        } else if (document.getElementById("telephone").value === ""
            || !(/^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\d{8}$)/.test(
                document.getElementById("telephone").value))) {
          alert("手机号格式有误");
          document.getElementById("telephone").focus();
          return false;
        }
        return true;
      }
    </script>
    <style>
      #myform input[type="text"], input[type="password"] {
        width: 190px;
        height: 26px;
      }

      #myform input[type="submit"] {
        width: 50px;
        height: 30px;
      }
    </style>
</head>
<body>
<div id="content">
    <div id="form">
        <h1>注册</h1>
        <form action="register.action" method="post" id="myform" onsubmit="return validate()">
            用户名：<input type="text" name="username" id="username" placeholder="请输入用户名"
                          style="margin-left: 39px"><br>
            密码：<input type="password" name="password" id="password" placeholder="请输入密码"
                        style="margin-top: 8px;margin-left: 54px;"><br>
            姓名：<input type="text" name="name" id="name" placeholder="请输入姓名"
                        style="margin-top: 8px;margin-left: 54px;"><br>
            手机号：<input type="text" name="telephone" id="telephone"
                          placeholder="请输入手机号" style="margin-top: 8px;margin-left: 39px;"><br>
            <input type="submit" value="注册" style="margin-top: 8px">
            <a href="toLogin.action">返回登录</a>
        </form>
        <c:if test="${errorMsg!=null}">
            <span style="color: red; ">${errorMsg}</span>
        </c:if>
    </div>
</div>
</body>
</html>
