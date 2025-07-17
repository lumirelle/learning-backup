package g3.q3;

public class Q3SearchWord {

  public static void main(String[] args) {
    char[][] board = {
        {'A', 'B', 'C', 'E'},
        {'S', 'F', 'C', 'S'},
        {'A', 'D', 'E', 'E'}
    };
    String word = "ABCCEF";

    System.out.println(exist(board, word));
  }

  public static boolean exist(char[][] board, String word) {
    // 将 board、word 中所有字符转换为大写
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board.length; j++) {
        board[i][j] = Character.toUpperCase(board[i][j]);
      }
    }
    word = word.toUpperCase();

    int m = board.length;
    int n = board[0].length;
    boolean[][] visited = new boolean[m][n];

    // 从 board 中开始搜索 word 的第一个字符
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        // 如果找到了 word 的第一个字符，则从该字符开始 dfs 搜索
        if (board[i][j] == word.charAt(0)) {
          if (dfsSearch(board, word, visited, i, j, 0)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private static boolean dfsSearch(char[][] board, String word, boolean[][] visited, int i, int j,
      int index) {
    // 所有字符都已经搜索完毕，表明存在该单词
    if (index == word.length()) {
      return true;
    }

    // 到达边界、该字符已经访问过或者该字符与 word[index] 不同，则回到上一层
    if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || visited[i][j]
        || board[i][j] != word.charAt(index)) {
      return false;
    }

    // 该字符就是 word[index]，则继续往四周搜索下一个字符
    visited[i][j] = true;
    if (dfsSearch(board, word, visited, i + 1, j, index + 1) ||
        dfsSearch(board, word, visited, i - 1, j, index + 1) ||
        dfsSearch(board, word, visited, i, j + 1, index + 1) ||
        dfsSearch(board, word, visited, i, j - 1, index + 1)) {
      return true;
    }

    // 如果四周都搜索不到 word[index + 1]，则回到上一层
    visited[i][j] = false;
    return false;
  }

}
