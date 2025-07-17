package g1;


public class Q3CommonSubSeq {

  public static void main(String[] args) {
    String text1 = "abcde";
    String text2 = "ace";
    System.out.println(getMaxLength(text1, text2));
  }

  public static int getMaxLength(String text1, String text2) {
    int n = text1.length();
    int m = text2.length();
    int[][] dp = new int[n + 1][m + 1];
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= m; j++) {
        if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1] + 1;
        } else {
          dp[i][j] = Math.max(dp[i][j - 1], dp[i - 1][j]);
        }
      }
    }
    return dp[n][m];
  }

}
