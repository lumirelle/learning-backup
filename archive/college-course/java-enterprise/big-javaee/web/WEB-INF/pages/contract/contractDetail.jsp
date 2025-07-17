<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>合同详情</title>
    <script>
        function printpage() {
            document.getElementById("p").style.display = "none";
            window.print();
            document.getElementById("p").style.display = "inline-block";
        }
    </script>
</head>
<body>
<h2 style="text-align: center">销购合同</h2>
<div style="float:left;font-size:10px;">创建日期：${contract.createTime}</div>
</body>
合同编码：
<b style="color: blue">${contract.barCode}</b><br>
类型：
<c:if test="${contract.type==0}">省内</c:if>
<c:if test="${contract.type==1}">省外</c:if><br>
<hr>
<div class="info">
    零售商信息：<br>
    姓名：${contract.retailer.name}<br>
    联系电话：${contract.retailer.telephone}<br>
    送货地址：${contract.retailer.address}<br>
</div>
<br>
<div class="info">
    货物信息：<br>
    <c:if test="${contract.commoditiesList!=null}">
        <table style="width: 510px;text-align: center;" border="1">
            <tr>
                <td>名称</td>
                <td>价格</td>
                <td>产地</td>
                <td>附属品</td>
                <td>数量</td>
            </tr>
            <c:forEach items="${contract.commoditiesList}" var="item">
                <tr>
                    <td>${item.name}</td>
                    <td>${item.price}元/斤</td>
                    <td>${item.locality}</td>
                    <td>
                        <c:if test="${item.accessoryList!=null}">
                            <c:if test="${item.accessoryList[0]==null}">无</c:if>
                            <c:forEach items="${item.accessoryList}" var="accessoryItem">
                                ${ accessoryItem.name}:${ accessoryItem.price}元/斤<br>
                            </c:forEach>
                        </c:if>
                    </td>
                    <td>${item.number}斤</td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
</div>

<br>
<button id="p" onclick="printpage()" style="background-color: #173e65;color: #ffffff;width: 70px">
    打印
</button>

</html>
