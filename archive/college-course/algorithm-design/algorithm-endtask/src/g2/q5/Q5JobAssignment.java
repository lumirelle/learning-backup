package g2.q5;

import java.util.Random;
import java.util.Scanner;

public class Q5JobAssignment {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("请输入矩阵的大小：");
    int length = scanner.nextInt();

    int[][] costs = new int[length][length];
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < length; j++) {
        costs[i][j] = new Random().nextInt() % 10 + 10;
      }
    }

    System.out.println("任务分布如下：");
    for (int[] aCosts : costs) {
      for (int cost : aCosts) {
        System.out.printf("%2d ", cost);
      }
      System.out.println();
    }

    BranchBoundary branchBoundary = new BranchBoundary(costs);
    branchBoundary.solve();
    int[] assignment = branchBoundary.getAssignment();
    int sum = 0;
    System.out.println("分支限界法任务分配：");
    for (int i = 0; i < assignment.length; i++) {
      System.out.println("第" + (i + 1) + "任务分配给第" + (assignment[i] + 1) + "个人");
      sum += costs[assignment[i]][i];
    }
    System.out.println("总成本为：" + sum);

    BruteForce bruteForce = new BruteForce(costs);
    bruteForce.solve();
    assignment = bruteForce.getAssignment();
    sum = 0;
    System.out.println("蛮力法任务分配：");
    for (int i = 0; i < assignment.length; i++) {
      System.out.println("第" + (i + 1) + "任务分配给第" + (assignment[i] + 1) + "个人");
      sum += costs[assignment[i]][i];
    }
    System.out.println("总成本为：" + sum);

    Hungarian hungarian = new Hungarian(costs);
    hungarian.solve();
    assignment = hungarian.getAssignment();
    sum = 0;
    System.out.println("匈牙利法任务分配：");
    for (int i = 0; i < assignment.length; i++) {
      System.out.println("第" + (i + 1) + "任务分配给第" + (assignment[i] + 1) + "个人");
      sum += costs[assignment[i]][i];
    }
    System.out.println("总成本为：" + sum);
  }

}
