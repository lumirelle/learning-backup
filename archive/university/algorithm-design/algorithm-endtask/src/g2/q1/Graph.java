package g2.q1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Graph {

  int V;
  List<Edge> edges;

  public Graph(int V) {
    this.V = V;
    edges = new ArrayList<>();
  }

  public void addEdge(int src, int dest, double weight) {
    edges.add(new Edge(src, dest, weight));
  }

  public List<Edge> kruskalMST() {
    // 按照权重从小到大排序
    Collections.sort(edges);

    int[] parentOfVertices = new int[V];
    for (int i = 0; i < V; i++) {
      parentOfVertices[i] = i;
    }

    List<Edge> mst = new ArrayList<>();

    int count = 0;
    for (Edge edge : edges) {
      if (count == V - 1) {
        break;
      }

      int srcParent = find(parentOfVertices, edge.src);
      int destParent = find(parentOfVertices, edge.dest);

      if (srcParent != destParent) {
        mst.add(edge);
        union(parentOfVertices, srcParent, destParent);
        count++;
      }
    }

    return mst;
  }

  private int find(int[] parentOfVertices, int vertex) {
    if (parentOfVertices[vertex] != vertex) {
      parentOfVertices[vertex] = find(parentOfVertices, parentOfVertices[vertex]);
    }
    return parentOfVertices[vertex];
  }

  private void union(int[] parentOfVertices, int x, int y) {
    int xParent = find(parentOfVertices, x);
    int yParent = find(parentOfVertices, y);
    parentOfVertices[xParent] = yParent;
  }

}
