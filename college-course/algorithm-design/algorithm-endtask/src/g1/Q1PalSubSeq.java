package g1;

public class Q1PalSubSeq {

  public static void main(String[] args) {
    // 5, bcbcb
    String str = "abcbkaefcbc";
    System.out.println(getMaxLength(str));
  }

  public static int getMaxLength(String str) {
    int n = str.length();

    // dp[i][j] 表示 s[i..j] 的最长回文子序列长度
    int[][] dp = new int[n][n];
    // 边界条件：单个字符的回文长度为1
    for (int i = 0; i < n; i++) {
      dp[i][i] = 1;
    }

    // 想求 dp[i][j] 需要知道左下 dp[i+1][j-1]，下 dp[i+1][j]，左 dp[i][j-1]
    // 所以需要从下往上，从左往右遍历
    for (int i = n - 1; i >= 0; i--) {
      for (int j = i + 1; j < n; j++) {
        // 状态转移方程
        if (str.charAt(i) == str.charAt(j)) {
          dp[i][j] = dp[i + 1][j - 1] + 2;
        } else {
          dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
        }
      }
    }

    return dp[0][n - 1];
  }

}
