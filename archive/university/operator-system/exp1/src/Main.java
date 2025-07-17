import java.time.LocalTime;

public class Main {

  private static int remainingTickets = 10;

  private record Window(Integer sleepMillis) implements Runnable {

    public void run() {
      int soldTicket = 0;

      while (true) {
        synchronized (Main.class) {
          if (remainingTickets == 0) {
            System.out.println(
                Thread.currentThread().getName() + " " + LocalTime.now() + " 发现无票，停止售票");
            break;
          }
          remainingTickets--;
          soldTicket++;
          System.out.println(
              Thread.currentThread().getName() + " " + LocalTime.now() + " 售卖出第 " + soldTicket
                  + " 张票");
        }

        try {
          Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
          e.printStackTrace();
          Thread.currentThread().interrupt();
        }
      }
    }

  }

  public static void main(String[] args) {
    Thread windowA = new Thread(new Window(1000), "窗口A");
    Thread windowB = new Thread(new Window(2300), "窗口B");
    windowA.start();
    windowB.start();
  }

}
