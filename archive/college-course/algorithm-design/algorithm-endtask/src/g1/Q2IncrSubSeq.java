package g1;

public class Q2IncrSubSeq {

  public static void main(String[] args) {
    int[] nums = {10, 9, 2, 5, 3, 7, 101, 18};
    System.out.println(getMaxLength(nums));
  }

  public static int getMaxLength(int[] nums) {
    int n = nums.length;
    if (n == 0) {
      return 0;
    }
    int[] dp = new int[n];
    dp[0] = 1;
    int maxans = 1;
    for (int i = 1; i < n; i++) {
      dp[i] = 1;
      for (int j = 0; j < i; j++) {
        if (nums[i] > nums[j]) {
          dp[i] = Math.max(dp[i], dp[j] + 1);
        }
      }
      maxans = Math.max(maxans, dp[i]);
    }
    return maxans;
  }

}
