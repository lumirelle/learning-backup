public class Resource {

  /**
   * 缓冲区容量
   */
  public static final int MAX = 10;

  /**
   * 互斥信号量
   */
  public static final Semaphore MUTEX = new Semaphore(1);

  /**
   * 空缓冲区信号量
   */
  public static final Semaphore EMPTY = new Semaphore(MAX);

  /**
   * 满缓冲区信号量
   */
  public static final Semaphore FULL = new Semaphore(0);

  /**
   * 表示缓冲区的数组
   */
  private static final int[] BUFFER = new int[MAX];

  /**
   * 表示缓冲区的头指针
   */
  private static int i = 0;

  /**
   * 表示缓冲区的尾指针
   */
  private static int j = 0;

  /**
   * 表示缓冲区中的产品个数
   */
  private static int count = 0;

  /**
   * 模拟向缓冲区中放入一个产品
   */
  public static void put() {
    BUFFER[i] = 1;
    i = (i + 1) % MAX;
    count++;
  }

  /**
   * 模拟从缓冲区中取出一个产品
   */
  public static void get() {
    BUFFER[j] = 0;
    j = (j + 1) % MAX;
    count--;
  }

  /**
   * 获取缓冲区中产品的个数
   *
   * @return 缓冲区中产品的个数
   */
  public static int getCount() {
    return count;
  }

  /**
   * 打印缓冲区中的内容
   */
  public static void print() {
    for (int k = 0; k < MAX; k++) {
      System.out.print(BUFFER[k] + " ");
    }
    System.out.println();
  }

}
