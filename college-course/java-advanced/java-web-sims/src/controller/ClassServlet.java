package controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import entity.ClassBean;
import entity.annotation.Column;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.impl.SimpleBeanService;
import util.BeanClassAnalyzer;
import util.Logger;

@WebServlet("/servlet/class")
public class ClassServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    doPost(request, response);
  }

  /**
   * 处理请求，返回 JSON 格式的结果
   *
   * @param request  请求
   * @param response 响应
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 设置编码
      request.setCharacterEncoding("utf-8");
      response.setCharacterEncoding("utf-8");
      response.setContentType("application/json;charset=utf-8");

      // 获取流
      BufferedReader reader = request.getReader();
      JSONObject jsonObject = JSON.parseObject(reader.readLine());
      PrintWriter writer = response.getWriter();

      // 获取简单类服务
      SimpleBeanService<ClassBean> simpleClassService = new SimpleBeanService<>(ClassBean.class);

      // 获取操作参数
      String action = request.getParameter("action");

      // 回应前端
      // 查找所有
      switch (action) {
        case "findAll" -> {
          writer.println(JSON.toJSONString(simpleClassService.findAllBeans()));
          Logger.info("ClassServlet 查找成功");
        }
        // 根据主码查找
        case "findByPrimaryKey" -> {
          Field primaryKeyField = BeanClassAnalyzer.getPrimaryKeyField(ClassBean.class);
          Column primaryKeyColumn = primaryKeyField.getAnnotation(Column.class);
          Object value = jsonObject.getObject(primaryKeyColumn.name(), primaryKeyField.getType());
          writer.println(JSON.toJSONString(simpleClassService.findBeanByPrimaryKey(value)));
          Logger.info("ClassServlet 查找成功");
        }
        // 添加
        case "add" -> {
          if (simpleClassService.addBean(constructClassBean(jsonObject)) != -1) {
            writer.println(JSON.toJSONString("{state:'ok'}"));
            Logger.info("添加成功");
          } else {
            Logger.info("添加失败");
          }
        }
        // 删除
        case "deleteByPrimaryKey" -> {
          Field primaryKeyField = BeanClassAnalyzer.getPrimaryKeyField(ClassBean.class);
          Column primaryKeyColumn = primaryKeyField.getAnnotation(Column.class);
          Object value = jsonObject.getObject(primaryKeyColumn.name(), primaryKeyField.getType());
          if (simpleClassService.deleteBeanByPrimaryKey(value) != -1) {
            writer.println(JSON.toJSONString("{state:'ok'}"));
            Logger.info("删除成功");
          } else {
            Logger.info("删除失败");
          }
        }
        // 更新
        case "update" -> {
          if (simpleClassService.updateBean(constructClassBean(jsonObject)) != -1) {
            writer.println(JSON.toJSONString("{state:'ok'}"));
            Logger.info("更新成功");
          } else {
            Logger.info("更新失败");
          }
        }
        default -> Logger.info("未知的操作");

      }
    } catch (IOException | NoSuchFieldException | InvocationTargetException |
             IllegalAccessException | IntrospectionException e) {
      e.printStackTrace();
    }
  }

  private ClassBean constructClassBean(JSONObject jsonObject)
      throws IllegalAccessException, InvocationTargetException, IntrospectionException {
    ClassBean classBean = new ClassBean();
    for (Field field : ClassBean.class.getDeclaredFields()) {
      Column column = field.getAnnotation(Column.class);
      Object value = jsonObject.getObject(column.name(), field.getType());
      PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),
          ClassBean.class);
      propertyDescriptor.getWriteMethod().invoke(classBean, value);
    }
    return classBean;
  }
}
