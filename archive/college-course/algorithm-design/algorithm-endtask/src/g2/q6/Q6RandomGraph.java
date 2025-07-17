package g2.q6;

import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class Q6RandomGraph {

  public static void main(String[] args) {
    // 概率不变，顶点数增加
    Map<Integer, Double> map1 = new HashMap<>();
    for (int i = 0; i < 20; i++) {
      double p = 0;
      // 50 次取平均
      for (int j = 0; j < 50; j++) {
        Graph g = new Graph();
        g.generateVertices(i);
        g.generateEdges(0.5);
        if (g.hasLoop()) {
          p++;
        }
      }
      map1.put(i, p / 50);
    }

    // 顶点数不变，概率增加
    Map<Double, Double> map2 = new HashMap<>();
    for (double i = 0; i <= 1; i += 0.1) {
      double p = 0;
      // 50 次取平均
      for (int j = 0; j < 50; j++) {
        Graph g = new Graph();
        g.generateVertices(10);
        g.generateEdges(i);
        if (g.hasLoop()) {
          p++;
        }
      }
      map2.put(i, p / 50);
    }

    // 利用 Swing 和 jfreechart 显示折线图
    SwingUtilities.invokeLater(() -> {
      LineChartEx ex1 = new LineChartEx(map1, "Vertices vs. Has Loop (p=0.5)");
      ex1.setVisible(true);

      LineChartEx ex2 = new LineChartEx(map2, "P vs. Has Loop (n=10)");
      ex2.setVisible(true);
    });
  }

}
