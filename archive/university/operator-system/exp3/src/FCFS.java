import java.util.Arrays;
import java.util.Comparator;

public class FCFS {

  private final Process[] processes;

  private int totalTurnaroundTime = 0;

  private double totalWeightedTurnaroundTime = 0;

  public FCFS(Process[] processes) {
    this.processes = processes;
  }

  public void run() {
    System.out.println("FCFS Scheduling Algorithm");
    executeProcesses();
    printProcesses();
  }

  private void executeProcesses() {
    // 按照到达时间排序，模拟进程到达
    Arrays.sort(processes, Comparator.comparingInt(Process::getArrivalTime));

    // 遍历，模拟接收进程
    for (int i = 0; i < processes.length; i++) {
      // 计算进程信息，模拟执行一个进程
      System.out.print(processes[i].getName() + " -> ");
      // 计算进程的完成时间
      if (i == 0) {
        processes[i].setFinishTime(processes[i].getArrivalTime() + processes[i].getServeTime());
      } else {
        processes[i].setFinishTime(processes[i - 1].getFinishTime() + processes[i].getServeTime());
      }
      // 计算进程的周转时间，加到总周转时间上
      int turnaroundTime = processes[i].getFinishTime() - processes[i].getArrivalTime();
      processes[i].setTurnaroundTime(turnaroundTime);
      totalTurnaroundTime += turnaroundTime;
      // 计算进程的带权周转时间，加到总带权周转时间上
      double weightedTurnaroundTime = (double) turnaroundTime / processes[i].getServeTime();
      processes[i].setWeightedTurnaroundTime(weightedTurnaroundTime);
      totalWeightedTurnaroundTime += weightedTurnaroundTime;
    }
    System.out.println();
  }

  private void printProcesses() {
    // 输出每个进程的执行信息
    System.out.println(
        "Process   Arrival Time   Serve Time   Finish Time   Turnaround Time   Weighted Turnaround Time");
    for (Process process : processes) {
      System.out.printf(
          "   %-8s    %-8d      %-8d     %-8d        %-8d              %-12.2f%n",
          process.getName(),
          process.getArrivalTime(), process.getServeTime(), process.getFinishTime(),
          process.getTurnaroundTime(), process.getWeightedTurnaroundTime());
    }
    System.out.printf("Average Turnaround Time: %.2f%n",
        (double) totalTurnaroundTime / processes.length);
    System.out.printf(
        "Average Weighted Turnaround Time: %.2f%n", totalWeightedTurnaroundTime / processes.length);
  }

}
