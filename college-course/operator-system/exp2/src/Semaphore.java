public class Semaphore {

  /**
   * 信号量的值
   */
  private int value;

  public Semaphore(int value) {
    this.value = value;
  }

  /**
   * P 操作
   */
  public synchronized void acquire() {
    value--;
    // 如果信号量的值小于 0，表明资源不足，阻塞等待
    while (value < 0) {
      try {
        this.wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * V 操作
   */
  public synchronized void release() {
    value++;
    // 如果信号量的值小于等于 0，表明此前有线程处于等待状态，唤醒一个等待队列中的线程
    if (value <= 0) {
      this.notify();
    }
  }

}
