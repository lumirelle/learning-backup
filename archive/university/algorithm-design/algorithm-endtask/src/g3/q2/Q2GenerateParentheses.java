package g3.q2;

import java.util.ArrayList;
import java.util.List;

public class Q2GenerateParentheses {

  public static void main(String[] args) {
    int n = 4;
    System.out.println(generateParentheses(n));
  }

  public static List<String> generateParentheses(int n) {
    List<String> result = new ArrayList<>();
    StringBuilder tmp = new StringBuilder();
    backtrack(result, tmp, 0, 0, n);
    return result;
  }

  private static void backtrack(List<String> result, StringBuilder tmp, int open, int close,
      int max) {
    // 当前字符串长度达到2n，即括号组合完成
    if (tmp.length() == 2 * max) {
      result.add(tmp.toString());
      return;
    }

    // 左括号数量小于n，可以添加左括号
    if (open < max) {
      tmp.append('(');
      backtrack(result, tmp, open + 1, close, max);
      tmp.deleteCharAt(tmp.length() - 1);
    }

    // 右括号数量小于左括号数量，可以添加右括号
    if (close < open) {
      tmp.append(')');
      backtrack(result, tmp, open, close + 1, max);
      tmp.deleteCharAt(tmp.length() - 1);
    }
  }

}
