public class Consumer extends Thread {

  private static int count = 0;

  private int i = 0;

  public Consumer() {
    super("消费线程-" + count);
    i = count++;
  }

  @Override
  public void run() {
    while (true) {
      // 临界区开始
      // 申请满缓冲区资源、互斥锁
      Resource.FULL.acquire();
      Resource.MUTEX.acquire();
      // 使用 get 方法取走资源（模拟消费产品）
      Resource.get();
      System.out.println("消费线程 " + i + " 消费了一个产品，当前产品数量：" + Resource.getCount());
      // 释放空缓冲区资源、互斥锁
      Resource.MUTEX.release();
      Resource.EMPTY.release();
      // 临界区结束

      // 模拟消费时间
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
