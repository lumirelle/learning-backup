<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>附属品信息管理</title>
    <style>
      * {
        margin: 0;
        padding: 0;
      }

      .addAccessoryMask {
        width: 100%;
        height: 100%;
        position: absolute;
        background: rgba(0, 0, 0, .3);
        display: none;
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
    </style>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script type="text/javascript">
      function showAddAccessory(flag) {
        if (flag === "true") {
          $(".addAccessoryMask").css("display", "block");
        } else {
          $(".addAccessoryMask").css("display", "none");
        }
      }

      function checkAll(obj) {
        const isCheck = obj.checked;
        const checkList = document.getElementsByName("arrays");
        for (let i = 0; i < checkList.length; i++) {
          checkList[i].checked = isCheck;
        }
      }

      function deleteAccessory() {
        const myArray = [];
        let len = 0;
        const fruitId = document.getElementById("aFruitId").value;
        const arrays = document.getElementsByName("arrays");
        for (let i = 0; i < arrays.length; i++) {
          if (arrays[i].checked) {
            myArray[len++] = arrays[i].value;
          }
        }
        console.log(myArray)
        $.ajax({
          type: 'post',
          url: '${pageContext.request.contextPath}/accessory/deleteList.action',
          data: {"arrays": myArray, "fruitId": fruitId},
          traditional: true,
          success: function () {
            alert("删除成功！")
            window.location.href = '${pageContext.request.contextPath}/accessory/list.action?fruitId='
                + fruitId;
          }
        })
      }

      function checkAddAccessory() {
        if ($("#addAccessoryName").val() === null || $("#addAccessoryName").val() === "") {
          alert("名称不能为空！");
          return false;
        }
        if ($("#addAccessoryPrice").val() === null || $("#addAccessoryPrice").val() === "") {
          alert("价格不能为空！");
          return false;
        }
        return true;
      }
    </script>
</head>
<body>

<div class="addAccessoryMask">
    <div class="c">
        <div style="background-color: #173e65;height: 20px;color: #ffffff;font-size: 12px;padding-left: 7px">
            添加附属品信息
            <span style="float: right;padding-right: 10px;"
                  onclick="showAddAccessory('false')">x</span>
        </div>
        <form id="addAccessoryForm" action="add.action" method="post"
              onsubmit="return checkAddAccessory()">
            名称:
            <input type="text" id="addAccessoryName" name="name" style="width: 120px"/><br/>
            价格:
            <input type="text" id="addAccessoryPrice" name="price" style="width: 120px"/><br/>
            <input type="hidden" id="aFruitId" name="fruitId" value="${fruitId}"/>
            <input type="submit" value="添加"
                   style="background-color: #173e65;color: #ffffff;width: 70px">
        </form>
    </div>
</div>

<button onclick="showAddAccessory('true')"
        style="background-color: #173e65;color: #ffffff;width: 70px">添加
</button>
<button onclick="deleteAccessory()"
        style="background-color: #173e65;color: #ffffff;width: 70px">删除
</button>

<c:if test="${list!=null}">
    <table style="margin-top: 10px;width: 800px;text-align: center;" border=1>
        <tr>
            <td><input type="checkbox" name="checkAll" onclick="checkAll(this)"></td>
            <td>名称</td>
            <td>价格</td>
            <td>创建日期</td>
        </tr>
        <c:forEach items="${list}" var="item" varStatus="status">
            <tr>
                <td><input type="checkbox" name="arrays" value="${item.accessoryId}"></td>
                <td>${item.name}</td>
                <td>${item.price}</td>
                <td>${item.createTime}</td>
            </tr>
        </c:forEach>
    </table>
</c:if>
<c:if test="${list==null}">
    <b>结果为空！</b>
</c:if>

</body>
</html>
