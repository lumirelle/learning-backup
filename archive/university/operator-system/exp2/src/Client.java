public class Client {

  public static void main(String[] args) {
    Producer producer = new Producer();
    Consumer consumer = new Consumer();

    // 启动生产者、消费者线程
    producer.start();
    consumer.start();
  }

}
