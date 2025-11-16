package g3.q1;

import java.util.ArrayList;
import java.util.List;

public class Q1Phone {

  public static void main(String[] args) {
    String digits = "23";
    System.out.println(generateCombinations(digits));
  }

  public static List<String> generateCombinations(String digits) {
    String[] letters = new String[]{
        "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"
    };
    StringBuilder tmp = new StringBuilder();
    List<String> result = new ArrayList<>();
    combine(digits, 0, letters, tmp, result);
    return result;
  }

  private static void combine(String digits, int index, String[] letters, StringBuilder tmp,
      List<String> result) {
    if (index == digits.length()) {
      result.add(tmp.toString());
      return;
    }
    // '2' -> 0, '3' -> 1, '4' -> 2, ...
    int digit = digits.charAt(index) - '2';
    for (int i = 0; i < letters[digit].length(); i++) {
      tmp.append(letters[digit].charAt(i));
      combine(digits, index + 1, letters, tmp, result);
      tmp.deleteCharAt(tmp.length() - 1);
    }
  }

}
