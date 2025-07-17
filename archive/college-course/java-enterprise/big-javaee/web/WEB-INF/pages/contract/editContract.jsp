<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>编辑销购合同</title>
    <style>
      * {
        margin: 0;
        padding: 0;
      }

      .btn {
        background-color: #173e65;
        color: #ffffff;
        width: 70px;
      }

      .btn-div {
        text-align: center;
      }

      .info {
        border: 1px solid #cccccc;
      }

      .c {
        border-style: solid;
        width: 250px;
        height: 400px;
        margin: 10px;
        text-align: center;
        background: #ffffff;
      }

      .c2 {
        border-style: solid;
        width: 500px;
        height: 400px;
        margin: 10px;
        text-align: center;
        background: #ffffff;
      }

      .retailerMask, .commoditiesMask {
        width: 100%;
        height: 100%;
        position: absolute;
        background: rgba(0, 0, 0, .3);
        display: none;
      }
    </style>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script>
      function init() {
        selectRetailer("${contract.retailer.retailerId}", "${contract.retailer.name}",
            "${contract.retailer.telephone}", "${contract.retailer.address}");
        let data = []
        let commoditiesList = ${commoditiesList};
        for (let i = 0; i < commoditiesList.length; i++) {
          data[i] = {
            "commodities": {
              "fruitId": commoditiesList[i].fruitId,
              "name": commoditiesList[i].name,
              "price": commoditiesList[i].price,
              "locality": commoditiesList[i].locality,
              "createTime": commoditiesList[i].createTime,
              "number": commoditiesList[i].number
            },
            "accessory": commoditiesList[i].accessoryList
          }
        }
        displayCommodities(data)

        const typeOut = document.getElementById("indexType").children[0];
        const typeIn = document.getElementById("indexType").children[1];
        if ("${contract.type}" === "1") {
          // 表示查询的是省外
          typeOut.selected = true;
        } else if ("${contract.type}" === "0") {
          // 表示查询的是省内
          typeIn.selected = true;
        }
      }

      function checkEditContract() {
        return true;
      }

      function changeType() {
        document.getElementById("type").value = document.getElementById("indexType").value;
      }

      function searchRetailer() {
        addRetailer($("#retailerName").val());
      }

      function selectRetailer(retailerId, name, telephone, address) {
        $("#retailerId").val(retailerId);
        $("#retailer_name").html("姓名：" + name);
        $("#retailer_telephone").html("联系电话：" + telephone);
        $("#retailer_address").html("送货地址：" + address);
        $(".retailerMask").css("display", "none");
        $("#retailer_info").css("display", "block");
      }

      function addRetailer(name) {
        $("#retailerList").html("");
        let message;
        if (name !== null) {
          message = "{\"name\":\"" + name + "\"}";
        } else {
          message = "{\"name\":\"\"}";
        }
        $.ajax({
          type: 'post',
          url: '${pageContext.request.contextPath}/contract/getAllRetailer.action',
          contentType: 'application/json;charset=utf-8',
          data: message,
          success: function (data) {
            for (let i = 0; i < data.length; i++) {
              const oldHtml = $("#retailerList").html();
              const info = "<p onclick=\"selectRetailer('" + data[i].retailerId + "','"
                  + data[i].name + "','" + data[i].telephone + "','" + data[i].address + "')\">"
                  + data[i].name + "</p>";
              $("#retailerList").html(oldHtml + info);
            }
            $(".retailerMask").css("display", "block");
          },
          error: function () {
            alert("通信异常!");
          }
        })
      }

      function searchCommodities() {
        addFruits($("#commoditiesName").val());
      }

      function addFruits(name) {
        let message;
        if (name !== null) {
          message = "{\"name\":\"" + name + "\"}";
        } else {
          message = "{\"name\":\"\"}";
        }
        $.ajax({
          type: 'post',
          url: '${pageContext.request.contextPath}/contract/getAllCommodities.action',
          contentType: 'application/json;charset=utf-8',
          data: message,
          success: function (data) {
            const tableHead = "<tr>" +
                "<td><input type='checkbox' onclick='checkAll(this)'></td>" +
                "<td>名称</td>" +
                "<td>价格</td>" +
                "<td>产地</td>" +
                "</tr>";
            $("#commoditiesList").html(tableHead);
            for (let i = 0; i < data.length; i++) {
              const oldHtml = $("#commoditiesList").html();
              const info = "<tr>" +
                  "<td><input type='checkbox' name='arrays' value='" + data[i].fruitId
                  + "'></td>" +
                  "<td>" + data[i].name + "</td>" +
                  "<td>" + data[i].price + "</td>" +
                  "<td>" + data[i].locality + "</td>" +
                  "</tr>";
              $("#commoditiesList").html(oldHtml + info);
            }
            $("#commoditiesList").html(
                "<table style='width:375px;text-align: center;' border='1'>" +
                $("#commoditiesList").html()
                + "</table>")
            $(".commoditiesMask").css("display", "block");
          },
          error: function () {
            alert("通信异常!");
          }
        })
      }

      function checkAll(obj) {
        const isCheck = obj.checked;
        const checkList = document.getElementsByName("arrays");
        for (let i = 0; i < checkList.length; i++) {
          checkList[i].checked = isCheck;
        }
      }

      function displayCommodities(data) {
        const tableHead = "<tr>" +
            "<td>名称</td>" +
            "<td>价格</td>" +
            "<td>产地</td>" +
            "<td>附属品</td>" +
            "<td>数量</td>" +
            "</tr>";
        $("#commodities_info").html(tableHead);
        for (let i = 0; i < data.length; i++) {
          const commodities = data[i].commodities;
          const accessory = data[i].accessory;
          let accessoryStr = "";
          for (let j = 0; j < accessory.length; j++) {
            accessoryStr += accessory[j].name + ":" + accessory[j].price + "元";
            if (j !== accessory.length - 1) {
              accessoryStr += "<br>";
            }
          }
          accessoryStr = accessory === "" ? "无" : accessoryStr;
          const oldHtml = $("#commodities_info").html();
          const info = "<tr>" +
              "<td>" + commodities.name + "</td>" +
              "<td>" + commodities.price + "元/斤</td>" +
              "<td>" + commodities.locality + "</td>" +
              "<td>" + accessoryStr + "</td>" +
              "<td><input type='number' name='priceArrays' style='width: 50px' value='"
              + `\${commodities.number}` + "'>斤</td>"
              +
              "</tr>" + "<input type='hidden' name='commoditiesIdArrays' value='"
              + commodities.fruitId + "'>";
          $("#commodities_info").html(oldHtml + info);
        }
        $("#commodities_info").html(
            "<table style='width:510px;text-align: center;' border='1'>" +
            $("#commodities_info").html()
            + "</table>")
        $(".commoditiesMask").css("display", "none");
        $("#commodities_info").css("display", "block");
      }

      function selectCommodities() {
        $("#commodities_info").html("");
        const myArray = [];
        let len = 0;
        const arrays = document.getElementsByName("arrays");
        for (let i = 0; i < arrays.length; i++) {
          if (arrays[i].checked) {
            myArray[len] = arrays[i].value;
            len++;
          }
        }
        $.ajax({
          type: 'post',
          url: '${pageContext.request.contextPath}/contract/getCommoditiesAndAccessory.action',
          data: {"arrays": myArray},
          traditional: true,
          success: function (data) {
            displayCommodities(data)
          },
          error: function () {
            alert("通信异常!");
          }
        })
      }

      function cancelEdit() {
        $(".retailerMask").css("display", "none");
        $(".commoditiesMask").css("display", "none");
      }
    </script>
</head>
<body onload="init()">

<div class="retailerMask">
    <div class="c">
        <div style="background-color: #173e65;height: 20px;color: #ffffff;font-size: 12px;padding-left: 7px">
            零售商信息
            <span style="float: right;padding-right: 10px;" onclick="cancelEdit()">x</span>
        </div>
        <input type="text" id="retailerName" width="width:20%" style="margin: 0 20px">
        <button class="btn" onclick="searchRetailer()" style="margin: 0 auto">查询</button>
        <div id="retailerList"
             style="border:5px solid #cccccc;overflow-y: scroll;margin: 10px;height: 300px">
        </div>
    </div>
</div>

<div class="commoditiesMask">
    <div class="c2">
        <div style="background-color: #173e65;height: 20px;color: #ffffff;font-size: 12px;padding-left: 7px">
            水果列表
            <span style="float: right;padding-right: 10px;" onclick="cancelEdit()">x</span>
        </div>
        <input type="text" id="commoditiesName" width="width:20%" style="margin: 0 20px">
        <button class="btn" onclick="searchCommodities()" style="margin: 0 auto">查询</button>
        <div id="commoditiesList"
             style="border:2px solid #cccccc;overflow-y: scroll;margin: 10px;height: 300px">
        </div>
        <button class="btn" onclick="selectCommodities(null)" style="margin: 0 auto">确定</button>
    </div>
</div>

<form id="editContractForm" action="edit.action" method="post"
      onsubmit="return checkEditContract()">
    合同编码:
    <input type="text" name="barCode" value="${contract.barCode}" readonly
           style="width: 120px"/><br/>
    类型:
    <select id="indexType" onchange="changeType()">
        <option value="1">省外</option>
        <option value="0">省内</option>
    </select>
    <input type="hidden" name="type" id="type" value="${contract.type}"><br>
    <div class="info">
        零售商信息：
        <button type="button" class="btn btn-div" onclick="addRetailer(null)" style="float: right">
            关联
        </button>
        <br>
        <div id="retailer_info" style="display: none">
            <p id="retailer_name"></p>
            <p id="retailer_telephone"></p>
            <p id="retailer_address"></p>
            <input name="retailerId" id="retailerId" type="hidden">
        </div>
    </div>
    <div class="info">
        货物信息：
        <button type="button" class="btn btn-div" onclick="addFruits(null)" style="float: right">添加
        </button>
        <br>
        <div id="commodities_info" style="display: none">
        </div>
    </div>
    <input type="hidden" name="contractId" value="${contract.contractId}">
    <input type="submit" value="提交" class="btn">
</form>
<c:if test="${resultMessage!=null}">
    <br><span style="color: red; ">${resultMessage}</span>
</c:if>
</body>
</html>
