package g1;

public class Q7WildcardMatch {

  public static void main(String[] args) {
    String s = "aa";
    String p = "a*";
    System.out.println(isMatch(s, p));
  }

  public static boolean isMatch(String s, String p) {
    int n = s.length();
    int m = p.length();

    // 创建一个二维数组用于存储匹配结果
    boolean[][] dp = new boolean[n + 1][m + 1];
    // 边界条件
    dp[0][0] = true;
    for (int j = 1; j <= m; j++) {
      if (p.charAt(j - 1) == '*') {
        dp[0][j] = true;
      }
    }

    // 动态规划计算匹配结果
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= m; j++) {
        if (p.charAt(j - 1) == '?' || s.charAt(i - 1) == p.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1];
        } else if (p.charAt(j - 1) == '*') {
          dp[i][j] = dp[i][j - 1] || dp[i - 1][j];
        }
      }
    }

    return dp[n][m];
  }

}
