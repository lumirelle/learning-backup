package g2.q1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class Q1Kruskal extends Application {

  private static final int WIDTH = 1000;
  private static final int HEIGHT = 600;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    Canvas canvas = new Canvas(WIDTH, HEIGHT);
    drawGraph(canvas.getGraphicsContext2D(), createGraph());

    Pane root = new Pane(canvas);
    Scene scene = new Scene(root, WIDTH, HEIGHT);

    primaryStage.setTitle("Kruskal's Minimum Spanning Tree");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private Graph createGraph() {
    Graph graph = new Graph(9);
    graph.addEdge(4, 7, 7);
    graph.addEdge(2, 8, 8);
    graph.addEdge(0, 1, 10);
    graph.addEdge(0, 5, 11);
    graph.addEdge(1, 8, 12);
    graph.addEdge(3, 7, 16);
    graph.addEdge(1, 6, 16);
    graph.addEdge(5, 6, 17);
    graph.addEdge(1, 2, 18);
    graph.addEdge(6, 7, 19);
    graph.addEdge(3, 4, 20);
    graph.addEdge(3, 8, 21);
    graph.addEdge(2, 3, 22);
    graph.addEdge(3, 6, 24);
    graph.addEdge(4, 5, 26);
    return graph;
  }

  private void drawGraph(GraphicsContext gc, Graph graph) {
    // 绘制最小生成树的边
    gc.setFill(Color.BLUE);
    gc.setStroke(Color.BLUE);
    for (Edge edge : graph.kruskalMST()) {
      int x1 = generateX(edge.src);
      int y1 = generateY(edge.src);
      int x2 = generateX(edge.dest);
      int y2 = generateY(edge.dest);
      gc.strokeLine(x1, y1, x2, y2);
    }

    // 黑色绘制所有节点
    gc.setFill(Color.BLACK);
    gc.setStroke(Color.BLACK);
    for (int i = 0; i < graph.V; i++) {
      int x = generateX(i);
      int y = generateY(i);
      // 绘制节点索引
      gc.fillText("v" + i, x - 10, y - 10);
      gc.fillOval(x - 4, y - 4, 8, 8);
    }
  }

  private int generateX(int vertex) {
    // 自定义节点横坐标位置
    int[] customX = {300, 150, 100, 250, 500, 450, 300, 380, 180};
    if (vertex < customX.length) {
      return customX[vertex];
    } else {
      return 0; // 如果节点索引超出了自定义坐标数组的长度，则返回0或其他默认值
    }
  }

  private int generateY(int vertex) {
    // 自定义节点纵坐标位置
    int[] customY = {100, 200, 350, 450, 400, 200, 250, 360, 320};
    if (vertex < customY.length) {
      return customY[vertex];
    } else {
      return 0; // 如果节点索引超出了自定义坐标数组的长度，则返回0或其他默认值
    }
  }

}

