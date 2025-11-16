package g2.q6;

import java.util.ArrayList;
import java.util.List;

public class Graph {

  private Vertex[] vertices;

  public void generateVertices(int n) {
    vertices = new Vertex[n];
    for (int i = 0; i < n; i++) {
      vertices[i] = new Vertex();
      vertices[i].setId(i);
    }
  }

  public void generateEdges(double p) {
    if (p < 0 || p > 1) {
      System.out.println("p must be between 0 and 1");
      return;
    }

    for (int i = 0; i < vertices.length; i++) {
      for (int j = 0; j < vertices.length; j++) {
        if (i == j) {
          continue;
        }
        if (Math.random() < p) {
          vertices[i].addNext(vertices[j]);
        }
      }
    }
  }

  // dfs 判断是否有环
  public boolean hasLoop() {
    List<Vertex> path = new ArrayList<>();
    int[] visited = new int[vertices.length];
    for (int i = 0; i < vertices.length; i++) {
      if (visited[i] == 0) {
        return dfsCheck(i, visited, path, -1);
      }
    }
    return false;
  }

  private boolean dfsCheck(int i, int[] visited, List<Vertex> path, int parent) {
    visited[i] = 1;
    path.add(vertices[i]);
    for (Vertex v : vertices[i].getNexts()) {
      if (visited[v.getId()] == 0) {
        if (dfsCheck(v.getId(), visited, path, i)) {
          return true;
        }
      } else if (visited[v.getId()] == 1 && v.getId() != parent) {
        path.add(v);
        return true;
      }
    }
    visited[i] = 2;
    path.remove(path.size() - 1);
    return false;
  }

}
