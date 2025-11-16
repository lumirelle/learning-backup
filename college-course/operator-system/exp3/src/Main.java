import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    System.out.print("Enter the number of processes: ");
    int n = scanner.nextInt();
    scanner.nextLine();

    Process[] processes = new Process[n];
    for (int i = 0; i < n; i++) {
      processes[i] = new Process();
      System.out.print("Enter the name, arrival time and serve time of process " + (i + 1)
          + " like 'A 0 3': ");
      String[] temp = scanner.nextLine().split(" ");
      processes[i].setName(temp[0]);
      processes[i].setArrivalTime(Integer.parseInt(temp[1]));
      processes[i].setServeTime(Integer.parseInt(temp[2]));
    }

    System.out.println("Enter the option of algorithm: ");
    System.out.println("1. FCFS");
    System.out.println("2. SJF");
    System.out.println("3. BOTH");
    int option = scanner.nextInt();

    switch (option) {
      case 1:
        new FCFS(processes).run();
        break;
      case 2:
        new SJF(processes).run();
        break;
      case 3:
        new FCFS(processes).run();
        new SJF(processes).run();
        break;
      default:
        System.out.println("Invalid option!");
    }
  }

}
