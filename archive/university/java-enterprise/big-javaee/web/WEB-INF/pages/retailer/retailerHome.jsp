<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>零售商管理</title>
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

      .addMask {
        width: 100%;
        height: 100%;
        position: absolute;
        background: rgba(0, 0, 0, .3);
        display: none;
      }
    </style>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script type="text/javascript">
      function changeStatus() {
        document.getElementById("status").value = document.getElementById("indexStatus").value;
      }

      function init() {
        const countNumber = document.getElementById("countNumber").value;
        const sumPage = document.getElementById("sumPageNumber").value;
        const currentPage = document.getElementById("currentPage").value;
        document.getElementById("pageInfo").innerHTML = "一共<font color='bule'>" + countNumber
            + "</font>条数据，" +
            "共<font color='blue'>" + sumPage + "</font>页," +
            "当前第<font color='blue'>" + currentPage + "</font>页";

        const statusAl = document.getElementById("indexStatus").children[0];
        const statusEn = document.getElementById("indexStatus").children[1];
        const statusDs = document.getElementById("indexStatus").children[2];
        if ("${retailer.status}" === "1") {
          // 表示查询的是启用状态
          statusEn.selected = true;
        } else if ("${retailer.status}" === "0") {
          // 表示查询的是停用状态
          statusDs.selected = true;
        } else {
          // 表示查询的是全部状态
          statusAl.selected = true;
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

      function editRetailer(id) {
        const message = "{'id':'" + id + "'}";
        $.ajax({
          type: 'post',
          url: '${pageContext.request.contextPath}/retailer/editRetailer.action',
          contentType: 'application/json;charset=utf-8',
          data: message,
          success: function (data) {
            $("#editName").val(data["name"]);
            $("#editTelephone").val(data["telephone"]);
            $("#editAddress").val(data["address"]);
            $("#retailerId").val(data["retailerId"]);
            $("#editStatus").val(data["status"]);
            $("#eStatus").val(data["status"]);
            $(".mask").css("display", "block");
            $("#eStartPage").val($("#startPage").val());
            $("#eCurrentPage").val($("#currentPage").val());
            $("#ePageSize").val($("#pageSize").val());
          }
        });
      }

      function cancelEdit() {
        $(".mask").css("display", "none");
      }

      function changeEditStatus() {
        document.getElementById("editStatus").value = document.getElementById("eStatus").value;
      }

      function deleteRetailer(id, name) {
        if (!confirm("确定要删除零售商：" + name + "吗？")) {
          return;
        }
        $("#dRetailerId").val(id);
        $("#dStartPage").val($("#startPage").val());
        $("#dCurrentPage").val($("#currentPage").val());
        $("#dPageSize").val($("#pageSize").val());
        $("#deleteForm").submit();
      }

      function showAddMask(flag) {
        if (flag === "true") {
          $(".addMask").css("display", "block");
        } else {
          $(".addMask").css("display", "none");
        }
      }

      function checkAddRetailer() {
        if ($("#addName").val() == null || $("#addName").val() === "") {
          alert("用户名不能为空！");
          return false;
        }
        if ($("#addTelephone").val() === null || $("#addTelephone").val() === "") {
          alert("手机号不能为空！");
          return false;
        }
        const myreg = /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\d{8}$)/;
        if (!myreg.test($("#addTelephone").val())) {
          alert("请输入有效的手机号码！");
          return false;
        }
        if ($("#addAddress").val() === null || $("#addAddress").val() === "") {
          alert("地址不能为空！");
          return false;
        }
        return true;
      }
    </script>
</head>
<body onload="init()">
<%@ include file="../menu.jsp" %>

<div class="mask">
    <div class="c">
        <div style="background-color: #173e65;height: 20px;color: #ffffff;font-size: 12px;padding-left: 7px">
            修改信息
            <span style="float: right;padding-right: 10px;" onclick="cancelEdit()">x</span>
        </div>
        <form id="editForm" action="edit.action" method="post">
            姓名:
            <input type="text" id="editName" name="name" style="width: 120px"/><br/>
            手机:
            <input type="text" id="editTelephone" name="telephone"
                   style="width: 120px"/><br/>
            地址:
            <input type="text" id="editAddress" name="address" style="width: 120px"/><br/>
            状态:
            <select id="eStatus" onchange="changeEditStatus()">
                <option value="1">启用</option>
                <option value="0">停用</option>
            </select><br/>
            <input type="hidden" name="retailerId" id="retailerId"/>
            <input type="hidden" name="status" id="editStatus"/>
            <input type="hidden" name="startPage" id="eStartPage"/>
            <input type="hidden" name="currentPage" id="eCurrentPage"/>
            <input type="hidden" name="pageSize" id="ePageSize"/>
            <input type="submit" value="提交"
                   style="background-color: #173e65;color: #ffffff;width: 70px">
        </form>
    </div>
</div>

<div class="addMask">
    <div class="c">
        <div style="background-color: #173e65;height: 20px;color: #ffffff;font-size: 12px;padding-left: 7px">
            添加信息
            <span style="float: right;padding-right: 10px;" onclick="showAddMask('false')">x</span>
        </div>
        <form id="addForm" action="add.action" method="post" onsubmit="return checkAddRetailer()">
            姓名:<input type="text" id="addName" name="name" style="width: 120px"/><br/>
            手机:<input type="text" id="addTelephone" name="telephone" style="width: 120px"/><br/>
            地址:<input type="text" id="addAddress" name="address" style="width: 120px"/><br/>
            <input type="hidden" name="status" value="1"/>
            <input type="submit" value="添加"
                   style="background-color: #173e65;color: #ffffff;width: 70px">
        </form>
    </div>
</div>

<button onclick="showAddMask('true')" style="background-color: #173e65;color: #ffffff;width: 70px">
    添加
</button>

<form id="listForm" action="list.action" method="post">
    姓名:
    <input type="text" name="name" style="width: 120px" value="${retailer.name}"/>
    手机:
    <input type="text" name="telephone" style="width: 120px" value="${retailer.telephone}"/>
    地址:
    <input type="text" name="address" style="width: 120px" value="${retailer.address}"/><br/><br/>
    状态:
    <select id="indexStatus" onchange="changeStatus()">
        <option value="-1">全部</option>
        <option value="1">启用</option>
        <option value="0">停用</option>
    </select>
    <input type="hidden" id="status" name="status" value="-1">
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
            <td>姓名</td>
            <td>手机号</td>
            <td>地址</td>
            <td>状态</td>
            <td>创建日期</td>
            <td>操作</td>
        </tr>
        <c:forEach items="${list}" var="item" varStatus="status">
            <tr>
                <td>${status.index+1}</td>
                <td>${item.name}</td>
                <td>${item.telephone}</td>
                <td>${item.address}</td>
                <td>
                    <c:if test="${item.status==1}">
                        <span style="color: blue; ">启用</span>
                    </c:if>
                    <c:if test="${item.status==0}">
                        <span style="color: red; ">停用</span>
                    </c:if>
                </td>
                <td>${item.createTime}</td>
                <td><a onclick="editRetailer('${item.retailerId}')">编辑</a>|
                    <a onclick="deleteRetailer('${item.retailerId}','${item.name}')">删除</a>
                    <form id="deleteForm" action="delete.action" method="post">
                        <input type="hidden" name="retailerId" id="dRetailerId"/>
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
