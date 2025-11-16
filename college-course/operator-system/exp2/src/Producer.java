public class Producer extends Thread {

  // 用于记录创建的生产线程的数量
  private static int count = 0;

  // 用于记录当前生产线程的编号
  private int i = 0;

  public Producer() {
    super("生产线程-" + count);
    i = count++;
  }

  @Override
  public void run() {
    while (true) {
      // 模拟生产时间
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      // 临界区开始
      // 申请空缓冲区资源、互斥锁
      Resource.EMPTY.acquire();
      Resource.MUTEX.acquire();
      // 使用 put 方法向缓冲区添加资源（模拟生产产品）
      Resource.put();
      System.out.println("生产线程 " + i + " 生产了一个产品，当前产品数量：" + Resource.getCount());
      // 释放满缓冲区资源、互斥锁
      Resource.MUTEX.release();
      Resource.FULL.release();
      // 临界区结束
    }
  }

}
