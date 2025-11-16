import java.util.Scanner;

public class MemoryManager {

  private final Memory memory;

  private final String algorithmType;

  public MemoryManager(int memorySize, int fragmentLimit, String algorithmType) {
    this.algorithmType = algorithmType;
    this.memory = new Memory(memorySize, fragmentLimit, algorithmType);
  }

  public void run() {
    System.out.println("Running " + algorithmType + " Algorithm...");

    Scanner scanner = new Scanner(System.in);

    String command;
    while (true) {
      System.out.println(
          "Command:\n\tc[reate] <process name> <process size>\n\td[estroy] <process name>\n\tu[sage]\n\te[xit]");
      command = scanner.nextLine();
      String[] commandParts = command.split(" ");

      if (command.matches("create \\w+ \\d+") || command.matches("c \\w+ \\d+")) {
        memory.allocate(commandParts[1], Integer.parseInt(commandParts[2]));
      } else if (command.matches("destroy \\w+") || command.matches("d \\w+")) {
        memory.recycle(commandParts[1]);
      } else if (command.equals("usage") || command.equals("u")) {
        System.out.println(memory);
      } else if (command.equals("exit") || command.equals("e")) {
        System.out.println("Exit");
        return;
      } else {
        System.out.println("Invalid command");
      }
    }
  }

}

