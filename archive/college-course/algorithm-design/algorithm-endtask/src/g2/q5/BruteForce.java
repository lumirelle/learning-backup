package g2.q5;

import java.util.Arrays;

public class BruteForce {
    private final int[][] costs;

    private int[] assignment;

    private int minCost;

    private final int n;

    public BruteForce(int[][] costs) {
        this.costs = costs;
        this.n = costs.length;

        assignment = new int[n];
        Arrays.fill(assignment, -1);
        minCost = Integer.MAX_VALUE;
    }

    public void solve() {
        int[] permutation = new int[n];
        for (int i = 0; i < n; i++) {
            permutation[i] = i;
        }
        permute(permutation, 0);
    }

    // 排列组合
    private void permute(int[] permutation, int start) {
        if (start == n - 1) {
            calculateCost(permutation);
        } else {
            for (int i = start; i < n; i++) {
                swap(permutation, start, i);
                permute(permutation, start + 1);
                swap(permutation, start, i);
            }
        }
    }

    // 计算成本
    private void calculateCost(int[] permutation) {
        int cost = 0;
        for (int i = 0; i < n; i++) {
            cost += costs[permutation[i]][i];
        }
        if (cost < minCost) {
            minCost = cost;
            assignment = permutation.clone();
        }
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public int[] getAssignment() {
        return assignment;
    }

}

