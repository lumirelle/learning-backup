package listener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 关闭数据库连接的监听器
 */
public class SimpleContextListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    try {
      while (DriverManager.getDrivers().hasMoreElements()) {
        DriverManager.deregisterDriver(DriverManager.getDrivers().nextElement());
      }
      AbandonedConnectionCleanupThread.checkedShutdown();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
