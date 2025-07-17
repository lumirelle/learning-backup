<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="username" scope="session" type="java.lang.String"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>学生管理系统</title>
    <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/css/index.css"/>
    <link
            crossorigin=""
            rel="shortcut icon"
            type="image/svg"
            href="${pageContext.request.contextPath}/images/book.svg"/>
    <script src="js/jquery-3.7.0.min.js"></script>
</head>
<body>
<main>
    <%-- 顶部导航栏 --%>
    <div class="header">
        <h1>你好，${username}！😃</h1>
    </div>
    <%-- 退出登录按钮 --%>
    <form
            action="${pageContext.request.contextPath}/servlet/logout"
            method="post">
        <input
                class="button block-center"
                type="submit"
                value="点我退出登录！"/>
    </form>
    <%-- 主内容容器 --%>
    <div class="container">
        <%-- 卡片 --%>
        <div class="card">
            <div class="card-header">
                <h2>学生管理</h2>
            </div>
            <div class="card-body">
                <input
                        class="operation-button"
                        type="button"
                        value="学生列表"
                        data-op-target="student"
                        data-op-action="findAll"/>
                <input
                        class="operation-button"
                        type="button"
                        value="查找学生"
                        data-op-target="student"
                        data-op-action="findByPrimaryKey"/>
                <input
                        class="operation-button"
                        type="button"
                        value="添加学生"
                        data-op-target="student"
                        data-op-action="add"/>
                <input
                        class="operation-button"
                        type="button"
                        value="删除学生"
                        data-op-target="student"
                        data-op-action="deleteByPrimaryKey"/>
                <input
                        class="operation-button"
                        type="button"
                        value="修改学生"
                        data-op-target="student"
                        data-op-action="update"/>
            </div>
        </div>
        <div class="card">
            <div class="card-header">
                <h2>班级管理</h2>
            </div>
            <div class="card-body">
                <input
                        class="operation-button"
                        type="button"
                        value="班级列表"
                        data-op-target="class"
                        data-op-action="findAll"/>
                <input
                        class="operation-button"
                        type="button"
                        value="查找班级"
                        data-op-target="class"
                        data-op-action="findByPrimaryKey"/>
                <input
                        class="operation-button"
                        type="button"
                        value="添加班级"
                        data-op-target="class"
                        data-op-action="add"/>
                <input
                        class="operation-button"
                        type="button"
                        value="删除班级"
                        data-op-target="class"
                        data-op-action="deleteByPrimaryKey"/>
                <input
                        class="operation-button"
                        type="button"
                        value="修改班级"
                        data-op-target="class"
                        data-op-action="update"/>
            </div>
        </div>
        <div class="card">
            <div class="card-header">
                <h2>班主任管理</h2>
            </div>
            <div class="card-body">
                <input
                        class="operation-button"
                        type="button"
                        value="班主任列表"
                        data-op-target="headteacher"
                        data-op-action="findAll"/>
                <input
                        class="operation-button"
                        type="button"
                        value="查找班主任"
                        data-op-target="headteacher"
                        data-op-action="findByPrimaryKey"/>
                <input
                        class="operation-button"
                        type="button"
                        value="添加班主任"
                        data-op-target="headteacher"
                        data-op-action="add"/>
                <input
                        class="operation-button"
                        type="button"
                        value="删除班主任"
                        data-op-target="headteacher"
                        data-op-action="deleteByPrimaryKey"/>
                <input
                        class="operation-button"
                        type="button"
                        value="修改班主任"
                        data-op-target="headteacher"
                        data-op-action="update"/>
            </div>
        </div>
        <div class="card">
            <div class="card-header">
                <h2>输入参数</h2>
            </div>
            <%-- 其第一层子 div 默认是隐藏的 --%>
            <div id="input-params-card-body" class="card-body">
                <div id="input-cls-params">
                    <h3>请输入班级信息</h3>
                    <label class="input-params-pk" for="class_no">班级号</label>
                    <input class="input-params-pk" type="text" name="class_no" id="class_no"/>
                    <label for="class_name">班级名称</label>
                    <input type="text" name="class_name" id="class_name"/>
                    <label for="student_count">学生数量</label>
                    <input type="text" name="student_count" id="student_count"/>
                </div>
                <div id="input-stu-params">
                    <h3>请输入学生信息</h3>
                    <label class="input-params-pk" for="student_no">学号</label>
                    <input class="input-params-pk" type="text" name="student_no" id="student_no"/>
                    <label for="student_name">姓名</label>
                    <input type="text" name="student_name" id="student_name"/>
                    <label for="stu_class_no">班级号</label>
                    <input type="text" name="class_no" id="stu_class_no"/>
                    <label for="student_sex">性别</label>
                    <input type="text" name="student_sex" id="student_sex"/>
                    <label for="student_birthday">生日</label>
                    <input type="text" name="student_birthday" id="student_birthday"/>
                    <label for="student_telephone">电话</label>
                    <input type="text" name="student_telephone" id="student_telephone"/>
                    <label for="student_note">备注</label>
                    <input type="text" name="student_note" id="student_note"/>
                </div>
                <div id="input-ht-params">
                    <h3>请输入班主任信息</h3>
                    <label class="input-params-pk" for="headteacher_no">班主任号</label>
                    <input class="input-params-pk" type="text" name="headteacher_no"
                           id="headteacher_no"/>
                    <label for="headteacher_name">姓名</label>
                    <input type="text" name="headteacher_name" id="headteacher_name"/>
                    <label for="ht_class_no">班级号</label>
                    <input type="text" name="class_no" id="ht_class_no"/>
                    <label for="headteacher_sex">性别</label>
                    <input type="text" name="headteacher_sex" id="headteacher_sex"/>
                    <label for="headteacher_birthday">生日</label>
                    <input type="text" name="headteacher_birthday" id="headteacher_birthday"/>
                    <label for="headteacher_telephone">电话</label>
                    <input type="text" name="headteacher_telephone" id="headteacher_telephone"/>
                    <label for="headteacher_note">备注</label>
                    <input type="text" name="headteacher_note" id="headteacher_note"/>
                </div>
                <div id="input-err-params">
                    <h3>请使用正确的 opTarget 信息</h3>
                </div>
                <div id="input-op-buttons">
                    <input id="input-submit-button" type="button" value="发送"/>
                </div>
            </div>
        </div>
        <div class="card">
            <div class="card-header">
                <h2>操作结果</h2>
            </div>
            <div id="op-result-card-body" class="card-body">
                <table id="op-cls-result">
                    <caption>班级信息</caption>
                    <tr>
                        <th>班级号</th>
                        <th>班级名称</th>
                        <th>学生数量</th>
                    </tr>
                </table>
                <table id="op-stu-result">
                    <caption>学生信息</caption>
                    <tr>
                        <th>学号</th>
                        <th>姓名</th>
                        <th>班级号</th>
                        <th>性别</th>
                        <th>生日</th>
                        <th>电话</th>
                        <th>备注</th>
                </table>
                <table id="op-ht-result">
                    <caption>班主任信息</caption>
                    <tr>
                        <th>班主任号</th>
                        <th>姓名</th>
                        <th>班级号</th>
                        <th>性别</th>
                        <th>生日</th>
                        <th>电话</th>
                        <th>备注</th>
                </table>
            </div>
        </div>
    </div>
</main>
</body>
<script>
  $(function () {
    let currentOpTarget
    let currentOpAction

    // 为操作按钮绑定点击事件
    $('.operation-button').click(function () {
      // 获取操作按钮的 op-target 数据
      currentOpTarget = $(this).data('op-target')
      currentOpAction = $(this).data('op-action')

      // 清空结果表格
      // 隐藏所有的输入参数框，如果是添加和更新，显示对应的输入参数框和提交按钮
      $('#op-result-card-body table').hide().find('tr[class="result-tr"]').remove()
      // 查找所有
      if (currentOpAction === 'findAll') {
        $('#input-stu-params').hide().children().hide()
        $('#input-cls-params').hide().children().hide()
        $('#input-ht-params').hide().children().hide()
        $('#input-err-params').hide()
        $('#input-op-buttons').show()
      }
      // 添加、更新，需要所有的输入参数框
      else if (currentOpAction === 'add' || currentOpAction === 'update') {
        if (currentOpTarget === 'student') {
          $('#input-stu-params').css('display', 'flex').children().show()
          $('#input-stu-params').siblings().hide()
        } else if (currentOpTarget === 'class') {
          $('#input-cls-params').css('display', 'flex').children().show()
          $('#input-cls-params').siblings().hide()
        } else if (currentOpTarget === 'headteacher') {
          $('#input-ht-params').css('display', 'flex').children().show()
          $('#input-ht-params').siblings().hide()
        } else {
          $('#input-err-params').show()
        }
        $('#input-op-buttons').show()
      }
      // 通过主码查找
      else if (currentOpAction === 'findByPrimaryKey' || currentOpAction === 'deleteByPrimaryKey') {
        if (currentOpTarget === 'student') {
          $('#input-stu-params').css('display', 'flex').children().hide()
          $('#input-stu-params').siblings().hide()
        } else if (currentOpTarget === 'class') {
          $('#input-cls-params').css('display', 'flex').children().hide()
          $('#input-cls-params').siblings().hide()
        } else if (currentOpTarget === 'headteacher') {
          $('#input-ht-params').show().children().hide()
          $('#input-ht-params').siblings().hide()
        } else {
          $('#input-err-params').show()
        }
        $('.input-params-pk').show()
        $('#input-op-buttons').show()
      }
      // 其他
      else {
        $('#input-stu-params').hide().children().hide()
        $('#input-cls-params').hide().children().hide()
        $('#input-ht-params').hide().children().hide()
        $('#input-err-params').hide()
        $('#input-op-buttons').hide()
      }

      // 为提交按钮绑定点击事件
      $('#input-submit-button').unbind('click')
      $('#input-submit-button').click(function () {
        // 清空结果表格
        $('#op-result-card-body table').find('tr[class="result-tr"]').remove()

        // 获取输入参数框的值，构造请求数据
        let data = {}
        if (currentOpTarget === 'class') {
          $('#input-cls-params')
          .find('input[type="text"]')
          .each(function () {
            data[$(this).attr('name')] = $(this).val()
          })
        } else if (currentOpTarget === 'student') {
          $('#input-stu-params')
          .find('input[type="text"], input[type="date"], input[type="radio"]:checked')
          .each(function () {
            let val = $(this).val()
            if (val === '男') {
              val = true
            } else if (val === '女') {
              val = false
            }
            data[$(this).attr('name')] = val
          })
        } else if (currentOpTarget === 'headteacher') {
          $('#input-ht-params')
          .find('input[type="text"], input[type="date"], input[type="radio"]:checked')
          .each(function () {
            let val = $(this).val()
            if (val === '男') {
              val = true
            } else if (val === '女') {
              val = false
            }
            data[$(this).attr('name')] = val
          })
        } else {
          return
        }

        console.log(data)
        // 发送请求
        $.ajax({
          url:
              '${pageContext.request.contextPath}/servlet/' + currentOpTarget + '?action='
              + currentOpAction,
          type: 'POST',
          contentType: 'application/json;charset=utf-8',
          data: JSON.stringify(data),
          dataType: 'json',
          success: function (data) {
            alert('操作成功')
            console.log(data)
            // 显示对应表格
            let currentOpResultTable
            // 班级
            if (currentOpTarget === 'class') {
              currentOpResultTable = $('#op-cls-result')
              currentOpResultTable.show().siblings().hide()
              if (currentOpAction === 'findAll') {
                for (let i = 0; i < data.length; i++) {
                  currentOpResultTable.append(
                      '<tr class="result-tr">\n<td>' + data[i].class_no
                      + '</td>\n<td>' + data[i].class_name
                      + '</td>\n<td>' + data[i].student_count
                      + '</td>\n</tr>')
                }
              } else if (currentOpAction === 'findByPrimaryKey') {
                currentOpResultTable.append(
                    '<tr class="result-tr">\n<td>' + data.class_no
                    + '</td>\n<td>' + data.class_name
                    + '</td>\n<td>' + data.student_count
                    + '</td>\n</tr>')
              }
            }
            // 学生
            else if (currentOpTarget === 'student') {
              currentOpResultTable = $('#op-stu-result')
              currentOpResultTable.show().siblings().hide()
              if (currentOpAction === 'findAll') {
                for (let i = 0; i < data.length; i++) {
                  currentOpResultTable.append(
                      '<tr class="result-tr">\n<td>' + data[i].student_no
                      + '</td>\n<td>' + data[i].student_name
                      + '</td>\n<td>' + data[i].class_no
                      + '</td>\n<td>' + (data[i].student_sex ? '男' : '女')
                      + '</td>\n<td>' + data[i].student_birthday
                      + '</td>\n<td>' + data[i].student_telephone
                      + '</td>\n<td>' + data[i].student_note
                      + '</td>\n</tr>'
                  )
                }
              } else if (currentOpAction === 'findByPrimaryKey') {
                currentOpResultTable.append(
                    '<tr class="result-tr">\n<td>' + data.student_no
                    + '</td>\n<td>' + data.student_name
                    + '</td>\n<td>' + data.class_no
                    + '</td>\n<td>' + (data.student_sex ? '男' : '女')
                    + '</td>\n<td>' + data.student_birthday
                    + '</td>\n<td>' + data.student_telephone
                    + '</td>\n<td>' + data.student_note
                    + '</td>\n</tr>'
                )
              }
            }
            // 班主任
            else if (currentOpTarget === 'headteacher') {
              currentOpResultTable = $('#op-ht-result')
              currentOpResultTable.show().siblings().hide()
              if (currentOpAction === 'findAll') {
                for (let i = 0; i < data.length; i++) {
                  currentOpResultTable.append(
                      '<tr class="result-tr">\n<td>' + data[i].headteacher_no
                      + '</td>\n<td>' + data[i].headteacher_name
                      + '</td>\n<td>' + data[i].class_no
                      + '</td>\n<td>' + (data[i].headteacher_sex ? '男' : '女')
                      + '</td>\n<td>' + data[i].headteacher_birthday
                      + '</td>\n<td>' + data[i].headteacher_telephone
                      + '</td>\n<td>' + data[i].headteacher_note
                      + '</td>\n</tr>'
                  )
                }
              } else if (currentOpAction === 'findByPrimaryKey') {
                currentOpResultTable.append(
                    '<tr class="result-tr">\n<td>' + data.headteacher_no
                    + '</td>\n<td>' + data.headteacher_name
                    + '</td>\n<td>' + data.class_no
                    + '</td>\n<td>' + (data.headteacher_sex ? '男' : '女')
                    + '</td>\n<td>' + data.headteacher_birthday
                    + '</td>\n<td>' + data.headteacher_telephone
                    + '</td>\n<td>' + data.headteacher_note
                    + '</td>\n</tr>'
                )
              }
            }
          },
          error: function (data) {
            console.log(data)
          }
        })
      })
    })
  })
</script>
</html>
