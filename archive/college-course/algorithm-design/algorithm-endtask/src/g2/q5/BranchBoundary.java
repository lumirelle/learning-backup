package g2.q5;

import java.util.Arrays;
import java.util.PriorityQueue;

public class BranchBoundary {

  private final int[][] costs;
  private final int n;
  private final PriorityQueue<Node> queue = new PriorityQueue<>();
  private int[] assignment;
  private int minCost = Integer.MAX_VALUE;

  public BranchBoundary(int[][] costs) {
    this.costs = costs;
    this.n = costs.length;

    // 初始化根节点
    int[] assignment = new int[n];
    Arrays.fill(assignment, -1);
    Node root = new Node(assignment);
    root.lb = getLowerBound(root);
    queue.add(root);
  }

  public void solve() {
    while (!queue.isEmpty()) {
      Node node = queue.poll();
      // 剪枝
      if (node.lb >= minCost) {
        continue;
      }
      // 已经分配到最后一个人，即找到一个可行解
      if (node.no == n - 1) {
        minCost = node.lb;
        this.assignment = node.assignment;
        continue;
      }
      // 扩展
      for (int i = 0; i < n; i++) {
        if (node.assignment[i] == -1) {
          Node child = new Node(Arrays.copyOf(node.assignment, n));
          child.no = node.no + 1;
          child.assignment[i] = child.no;
          child.lb = getLowerBound(child);
          queue.add(child);
        }
      }
    }
  }

  private int getLowerBound(Node node) {
    int lb = 0;
    // 计算已经分配的任务的成本
    for (int i = 0; i < n; i++) {
      if (node.assignment[i] != -1) {
        lb += costs[node.assignment[i]][i];
      }
    }
    // 计算未分配的任务的成本
    for (int i = node.no + 1; i < n; i++) {
      int min = Integer.MAX_VALUE;
      for (int j = 0; j < n; j++) {
        if (node.assignment[j] == -1 && costs[i][j] < min) {
          min = costs[i][j];
        }
      }
      lb += min;
    }
    return lb;
  }

  public int[] getAssignment() {
    return assignment;
  }

  private static class Node implements Comparable<Node> {

    // 当前已经分配到的人员编号
    int no = -1;

    int lb;

    int[] assignment;

    public Node(int[] assignment) {
      this.assignment = assignment;
    }

    @Override
    public int compareTo(Node o) {
      return lb - o.lb;
    }

  }

}
