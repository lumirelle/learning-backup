import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    int option;
    while (true) {
      System.out.println("Option:\n\t1.First Fit\n\t2.Best Fit\n\t3.Exit");
      option = scanner.nextInt();

      if (option == 3) {
        System.out.println("Exit");
        break;
      }

      System.out.print("Please enter the total memory size:");
      int memorySize = scanner.nextInt();
      System.out.print("Please enter the fragment limit:");
      int fragmentLimit = scanner.nextInt();
      scanner.nextLine();
      if (option == 1) {
        new MemoryManager(memorySize, fragmentLimit, "FF").run();
      } else if (option == 2) {
        new MemoryManager(memorySize, fragmentLimit, "BF").run();
      } else {
        System.out.println("Invalid option");
      }
    }
  }

}
