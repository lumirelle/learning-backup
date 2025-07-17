<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>销购合同管理</title>
    <style>
      * {
        margin: 0;
        padding: 0;
      }

      #menuContent a {
        text-decoration: none;
        color: #ffffff;
      }

      .c {
        border-style: solid;
        width: 200px;
        height: 130px;
        border-radius: 5px;
        display: block;
        background: #ffffff;
        margin: 10% auto;
      }

      .mask {
        width: 100%;
        height: 100%;
        position: absolute;
        background: rgba(0, 0, 0, .3);
        display: none;
      }
    </style>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script type="text/javascript">
      function changeType() {
        document.getElementById("type").value = document.getElementById("indexType").value;
      }

      function init() {
        const countNumber = document.getElementById("countNumber").value;
        const sumPage = document.getElementById("sumPageNumber").value;
        const currentPage = document.getElementById("currentPage").value;
        document.getElementById("pageInfo").innerHTML = "一共<font color='bule'>" + countNumber
            + "</font>条数据，" +
            "共<font color='blue'>" + sumPage + "</font>页," +
            "当前第<font color='blue'>" + currentPage + "</font>页";

        const typeAll = document.getElementById("indexType").children[0];
        const typeOut = document.getElementById("indexType").children[1];
        const typeIn = document.getElementById("indexType").children[2];
        if ("${contract.type}" === "1") {
          // 表示查询的是省外
          typeOut.selected = true;
        } else if ("${contract.type}" === "0") {
          // 表示查询的是省内
          typeIn.selected = true;
        } else {
          // 表示查询的是全部状态
          typeAll.selected = true;
        }
      }

      function toPrePage() {
        const currentPageObject = document.getElementById("currentPage");
        const currentPage = parseInt(currentPageObject.value);
        if (currentPage === 1) {
          alert("数据已到顶！");
        } else {
          currentPageObject.value = currentPage - 1;
          const pageSize = parseInt(document.getElementById("pageSize").value);
          const startPageObject = document.getElementById("startPage");
          startPageObject.value = parseInt(startPageObject.value) - pageSize;
          document.getElementById("listForm").submit();
        }
      }

      function toNextPage() {
        const currentPageObject = document.getElementById("currentPage");
        const currentPage = parseInt(currentPageObject.value);
        const sumPage = parseInt(document.getElementById("sumPageNumber").value);
        if (currentPage >= sumPage) {
          alert("数据已到底！");
        } else {
          currentPageObject.value = currentPage + 1;
          const pageSize = parseInt(document.getElementById("pageSize").value);
          const startPageObject = document.getElementById("startPage");
          startPageObject.value = parseInt(startPageObject.value) + pageSize;
          document.getElementById("listForm").submit();
        }
      }

      function toLocationPage() {
        let pageNumber = document.getElementById("pageNumber").value;
        const currentPageObject = document.getElementById("currentPage");
        const currentPage = currentPageObject.value;
        if (pageNumber == null || pageNumber === "") {
          alert("请输入要跳转的页数！")
        } else {
          pageNumber = parseInt(pageNumber);
          const sumPage = parseInt(document.getElementById("sumPageNumber").value);
          if (pageNumber < 1) {
            alert("数据已到顶!")
          } else if (pageNumber > sumPage) {
            alert("数据已到底!")
          } else {
            currentPageObject.value = pageNumber;
            const pageSize = parseInt(document.getElementById("pageSize").value);
            const startPageObject = document.getElementById("startPage");
            if (pageNumber > currentPage) {
              startPageObject.value = parseInt(startPageObject.value) + pageSize;
            } else if (pageNumber < currentPage) {
              startPageObject.value = parseInt(startPageObject.value) - pageSize;
            }
            document.getElementById("listForm").submit();
          }
        }
      }

      function deleteContract(id, barCode) {
        if (!confirm("确定要删除编号为" + barCode + "的合同吗？")) {
          return;
        }
        $("#dContractId").val(id);
        $("#dStartPage").val($("#startPage").val());
        $("#dCurrentPage").val($("#currentPage").val());
        $("#dPageSize").val($("#pageSize").val());
        $("#deleteForm").submit();
      }

      function addContract() {
        const url = "${pageContext.request.contextPath}/contract/toAddPage.action";
        window.open(url, "创建合同", "height=700,width=700,scrollbars=yes")
      }

      function editContract(id) {
        const url = "${pageContext.request.contextPath}/contract/toEditPage.action?contractId="
            + id;
        window.open(url, "修改合同", "height=700,width=700,scrollbars=yes")
      }

      function getContractDetail(id) {
        const url = "${pageContext.request.contextPath}/contract/getContractDetail.action?contractId="
            + id;
        window.open(url, "合同详情", "height=700,width=700,scrollbars=yes")
      }
    </script>
</head>
<body onload="init()">
<%@ include file="../menu.jsp" %>

<button onclick="addContract()" style="background-color: #173e65;color: #ffffff;width: 70px">
    添加
</button>

<form id="listForm" action="list.action" method="post">
    合同号:
    <input type="text" name="barCode" style="width: 120px" value="${contract.barCode}"/>
    零售商:
    <input type="text" name="retailerName" style="width: 120px" value="${contract.retailerName}"/>
    类型:
    <select id="indexType" onchange="changeType()">
        <option value="-1" selected="selected">全部</option>
        <option value="1">省外</option>
        <option value="0">省内</option>
    </select>
    <input type="hidden" id="type" name="type" value="-1"><br><br>
    创建日期:
    <input type="datetime-local" name="startTime" value="${startTime}">
    -
    <input type="datetime-local" name="endTime" value="${endTime}">
    <input type="submit" value="搜索" style="background-color: #173e65;color: #ffffff;width: 70px"/><br/>
    <c:if test="${errorMsg}">
        <span style="color: red; ">${errorMsg}</span><br/>
    </c:if>
    <input type="hidden" name="startPage" id="startPage" value="${startPage}"/>
    <input type="hidden" name="currentPage" id="currentPage" value="${currentPage}"/>
    <input type="hidden" name="pageSize" id="pageSize" value="${pageSize}"/>
    <input type="hidden" name="sumPageNumber" id="sumPageNumber" value="${sumPageNumber}"/>
    <input type="hidden" name="countNumber" id="countNumber" value="${countNumber}">
</form>

<hr style="margin-top: 10px;"/>

<c:if test="${list!=null}">
    <table style="margin-top: 10px;width: 800px;text-align: center;" border=1>
        <tr>
            <td>序号</td>
            <td>合同编号</td>
            <td>零售商</td>
            <td>类型</td>
            <td>创建日期</td>
            <td>操作</td>
        </tr>
        <c:forEach items="${list}" var="item" varStatus="status">
            <tr>
                <td>${status.index+1}</td>
                <td><a href="#"
                       onclick="getContractDetail('${item.contractId}')">${item.barCode}</a></td>
                <td>${item.retailerName}</td>
                <td>
                    <c:if test="${item.type==1}">
                        <span style="color: green; ">省外</span>
                    </c:if>
                    <c:if test="${item.type==0}">
                        <span style="color: blue; ">省内</span>
                    </c:if>
                </td>
                <td>${item.createTime}</td>
                <td><a onclick="editContract('${item.contractId}')">编辑</a>|
                    <a onclick="deleteContract('${item.contractId}','${item.barCode}')">删除</a>
                    <form id="deleteForm" action="delete.action" method="post">
                        <input type="hidden" name="contractId" id="dContractId"/>
                        <input type="hidden" name="startPage" id="dStartPage"/>
                        <input type="hidden" name="currentPage" id="dCurrentPage"/>
                        <input type="hidden" name="pageSize" id="dPageSize"/>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>
<c:if test="${list==null}">
    <b>搜索结果为空！</b>
</c:if>

<div style="margin-top: 10px">
    <a onclick="toPrePage()">上一页</a>
    <a onclick="toNextPage()">下一页</a>
    <input type="text" id="pageNumber" style="width: 50px">
    <button onclick="toLocationPage()">go</button>
    <div id="pageInfo"></div>
</div>

</body>
</html>
