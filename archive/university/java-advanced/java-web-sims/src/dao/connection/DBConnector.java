package dao.connection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnector {

  private DBConnector() {
  }

  /**
   * 从配置文件 properties/jdbc.properties 中读取 driver、url、username 和 password，连接数据库
   *
   * @return 数据库连接
   * @throws IOException            读取配置文件失败
   * @throws ClassNotFoundException 加载数据库驱动失败
   * @throws SQLException           连接数据库失败
   */
  public static Connection connect() throws IOException, ClassNotFoundException, SQLException {
    InputStream is = DBConnector.class.getClassLoader().getResourceAsStream("jdbc.properties");
    Properties properties = new Properties();

    // 加载配置文件，读取配置
    properties.load(is);
    String driver = properties.getProperty("driver");
    String url = properties.getProperty("url");
    String username = properties.getProperty("username");
    String password = properties.getProperty("password");

    // 注册数据库驱动
    Class.forName(driver);

    // 返回数据库连接
    return DriverManager.getConnection(url, username, password);
  }
}
