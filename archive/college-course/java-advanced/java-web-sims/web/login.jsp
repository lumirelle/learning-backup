<%@ page contentType="text/html;charset=UTF-8" %>
<%-- 第一次打开登陆页面，没有 inputUsername 和 inputPassword，将它们初始化为空字符串 --%>
<%
    if (request.getAttribute("inputUsername") == null) {
        request.setAttribute("inputUsername", "");
    }
    if (request.getAttribute("inputPassword") == null) {
        request.setAttribute("inputPassword", "");
    }
%>
<%-- 声明变量 --%>
<jsp:useBean id="inputUsername" scope="request" type="java.lang.String"/>
<jsp:useBean id="inputPassword" scope="request" type="java.lang.String"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>登录学生管理系统</title>
    <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/css/login.css"/>
    <link
            crossorigin=""
            rel="shortcut icon"
            type="image/svg"
            href="${pageContext.request.contextPath}/images/book.svg"/>
</head>
<body>
<main>
    <%-- 验证失败时，显示该元素 --%>
    <% if (request.getAttribute("invalidate") == "yes") { %>
    <div
            style="
          color: red;
          font-size: 0.9rem;
          font-weight: bold;
          text-align: center;
        ">
        <p>登录验证失败，请重试！</p>
    </div>
    <% } %>
    <%-- 表单 servlet/login post --%>
    <form
            action="${pageContext.request.contextPath}/servlet/login"
            method="post">
        <fieldset class="login-box">
            <legend>登录学生管理系统</legend>
            <%-- 用户名栏 --%>
            <div class="login-box-row">
                <label for="username">用户名</label>
                <input
                        type="text"
                        name="username"
                        id="username"
                        value="${inputUsername}"/>
            </div>
            <%-- 密码栏 --%>
            <div class="login-box-row">
                <label for="password">密码</label>
                <input
                        type="password"
                        name="password"
                        id="password"
                        value="${inputPassword}"/>
            </div>
            <%-- 按钮栏 --%>
            <div class="login-box-row">
                <input class="button" type="submit" value="登录"/>
                <input class="button" type="reset" value="重置"/>
            </div>
        </fieldset>
    </form>
</main>
</body>
</html>
