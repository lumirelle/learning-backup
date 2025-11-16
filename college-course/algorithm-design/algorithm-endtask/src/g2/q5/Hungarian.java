package g2.q5;

import java.util.Arrays;

public class Hungarian {

  private final int[][] costs;

  // 用于记录环路0的位置，避免转换矩阵时修改环路0的值
  private final boolean[][] isCircle;

  private final int n;

  private final int[] assignment;

  public Hungarian(int[][] costs) {
    this.n = costs.length;

    this.costs = new int[costs.length][];
    for (int i = 0; i < n; i++) {
      this.costs[i] = Arrays.copyOf(costs[i], costs[i].length);
    }
    this.isCircle = new boolean[n][n];
    this.assignment = new int[n];
    Arrays.fill(assignment, -1);
  }

  public void solve() {
    rowReduction();
    columnReduction();
    assignTask();
  }

  private void rowReduction() {
    for (int i = 0; i < n; i++) {
      int min = Integer.MAX_VALUE;
      for (int j = 0; j < n; j++) {
        if (costs[i][j] < min) {
          min = costs[i][j];
        }
      }
      for (int j = 0; j < n; j++) {
        costs[i][j] -= min;
      }
    }
  }

  private void columnReduction() {
    for (int j = 0; j < n; j++) {
      int min = Integer.MAX_VALUE;
      for (int i = 0; i < n; i++) {
        if (costs[i][j] < min) {
          min = costs[i][j];
        }
      }
      for (int i = 0; i < n; i++) {
        costs[i][j] -= min;
      }
    }
  }

  private void assignTask() {
    int count = 0;
    // 表示哪一行已经被覆盖
    int[] rowCover = new int[n];
    // 表示哪一列已经被覆盖
    int[] columnCover = new int[n];

    // true表示按行查找，false表示按列查找
    boolean findByRow = true;

    while (true) {
      int[] zeroLocation;

      if (findByRow) {
        zeroLocation = findZeroByRow(rowCover, columnCover);
        while (zeroLocation != null) {
          int i = zeroLocation[0];
          int j = zeroLocation[1];
          assignment[j] = i;
          count++;
          columnCover[j] = 1;
          zeroLocation = findZeroByRow(rowCover, columnCover);
        }
      } else {
        zeroLocation = findZeroByColumn(rowCover, columnCover);
        while (zeroLocation != null) {
          int i = zeroLocation[0];
          int j = zeroLocation[1];
          assignment[j] = i;
          count++;
          rowCover[i] = 1;
          zeroLocation = findZeroByColumn(rowCover, columnCover);
        }
      }

      // 已经得解
      if (count == n) {
        break;
      }

      zeroLocation = findZero(rowCover, columnCover);
      // 没有0了，需要转换矩阵
      if (zeroLocation == null) {
        // 矩阵转换，重新分配任务
        int min = findMin(rowCover, columnCover);
        convertMatrix(min, rowCover, columnCover);

        Arrays.fill(assignment, -1);
        Arrays.fill(rowCover, 0);
        Arrays.fill(columnCover, 0);
        count = 0;
      }
      // 有0，分情况
      else {
        int[] tmp = findByRow ? findZeroByColumn(rowCover, columnCover)
            : findZeroByRow(rowCover, columnCover);
        // 有唯一的0，直接分配
        if (tmp != null) {
          findByRow = !findByRow;
        }
        // 没有唯一的0，环路0，多解选择其一
        else {
          circleZero(rowCover, columnCover, zeroLocation);
        }
      }
    }
  }

  private int[] findZeroByRow(int[] rowCover, int[] columnCover) {
    for (int i = 0; i < n; i++) {
      if (rowCover[i] == 1) {
        continue;
      }
      for (int j = 0; j < n; j++) {
        if (columnCover[j] == 1) {
          continue;
        }
        if (costs[i][j] == 0) {
          // 保证是行内唯一的0
          int k;
          for (k = 0; k < n; k++) {
            if (k != j && costs[i][k] == 0 && columnCover[k] == 0) {
              break;
            }
          }
          if (k == n) {
            return new int[]{i, j};
          }
        }
      }
    }
    return null;
  }

  private int[] findZeroByColumn(int[] rowCover, int[] columnCover) {
    for (int j = 0; j < n; j++) {
      if (columnCover[j] == 1) {
        continue;
      }
      for (int i = 0; i < n; i++) {
        if (rowCover[i] == 1) {
          continue;
        }
        if (costs[i][j] == 0) {
          // 保证是列内唯一的0
          int k;
          for (k = 0; k < n; k++) {
            if (k != i && costs[k][j] == 0 && rowCover[k] == 0) {
              break;
            }
          }
          if (k == n) {
            return new int[]{i, j};
          }
        }
      }
    }
    return null;
  }

  private int[] findZero(int[] rowCover, int[] columnCover) {
    for (int i = 0; i < n; i++) {
      if (rowCover[i] == 1) {
        continue;
      }
      for (int j = 0; j < n; j++) {
        if (columnCover[j] == 1) {
          continue;
        }
        if (costs[i][j] == 0 && assignment[j] == -1) {
          return new int[]{i, j};
        }
      }
    }
    return null;
  }

  private int findMin(int[] rowCover, int[] columnCover) {
    int min = Integer.MAX_VALUE;
    for (int i = 0; i < n; i++) {
      if (rowCover[i] == 1) {
        continue;
      }
      for (int j = 0; j < n; j++) {
        if (columnCover[j] == 1) {
          continue;
        }
        if (costs[i][j] < min) {
          min = costs[i][j];
        }
      }
    }
    return min;
  }

  private void convertMatrix(int min, int[] rowCover, int[] columnCover) {
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (rowCover[i] == 1 && columnCover[j] == 1 && !isCircle[i][j]) {
          costs[i][j] += min;
        } else if (rowCover[i] == 0 && columnCover[j] == 0) {
          costs[i][j] -= min;
        }
      }
    }
  }

  private void circleZero(int[] rowCover, int[] columnCover, int[] zeroLocation) {
    int i = zeroLocation[0];
    int j = zeroLocation[1];
    isCircle[i][j] = true;
    for (int k = i + 1; k < n; k++) {
      if (rowCover[k] == 1) {
        continue;
      }
      if (costs[k][j] == 0 && assignment[j] != k) {
        for (int l = 0; l < n; l++) {
          if (columnCover[l] == 1) {
            continue;
          }
          if (costs[k][l] == 0 && assignment[l] == k) {
            isCircle[k][l] = true;
            isCircle[k][j] = true;
            isCircle[i][l] = true;
            circleZero(rowCover, columnCover, new int[]{k, l});
            break;
          }
        }
      }
    }
    assignment[j] = i;
    rowCover[i] = 1;
    columnCover[j] = 1;
  }

  public int[] getAssignment() {
    return assignment;
  }

}
