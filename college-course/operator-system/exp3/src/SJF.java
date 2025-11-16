import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class SJF {

  private final Process[] processes;

  private int totalTurnaroundTime = 0;

  private double totalWeightedTurnaroundTime = 0;

  public SJF(Process[] processes) {
    this.processes = processes;
  }

  public void run() {
    System.out.println("SJF Scheduling Algorithm");
    executeProcesses();
    printProcesses();
  }

  /**
   * 模拟接收进程的过程
   */
  private void executeProcesses() {
    // 按照到达时间排序，模拟进程到达
    Arrays.sort(processes, Comparator.comparingInt(Process::getArrivalTime));

    int currentTime = 0;
    PriorityQueue<Process> processesWaitQueue = new PriorityQueue<>(
        Comparator.comparingInt(Process::getServeTime));
    // 遍历，模拟接收进程
    for (Process process : processes) {
      // 如果当前进程是已到达的则直接加入进程等待队列，等待执行
      if (process.getArrivalTime() <= currentTime) {
        processesWaitQueue.add(process);
      }
      // 如果当前进程是未到达的，则执行优先队列中的进程，直到当前进程到达或者进程等待队列为空
      else {
        while (currentTime < process.getArrivalTime() && !processesWaitQueue.isEmpty()) {
          // 计算进程信息，模拟执行一个进程
          currentTime = executeProcess(currentTime, processesWaitQueue);
        }
        // 取当前时间和当前进程的到达时间的最大值，保证当前时间这个进程可以到达
        currentTime = Math.max(currentTime, process.getArrivalTime());
        processesWaitQueue.add(process);
      }
    }
    // 所有进程都加入优先队列后，执行优先队列中全部进程
    while (!processesWaitQueue.isEmpty()) {
      currentTime = executeProcess(currentTime, processesWaitQueue);
    }
    System.out.println();
  }

  /**
   * 模拟执行一个进程
   *
   * @param currentTime        当前时间
   * @param processesWaitQueue 进程等待队列
   * @return 执行完一个进程后的时间
   */
  private int executeProcess(int currentTime, PriorityQueue<Process> processesWaitQueue) {
    if (processesWaitQueue.isEmpty()) {
      return currentTime;
    }

    Process process = processesWaitQueue.poll();
    System.out.print(process.getName() + " -> ");
    // 计算进程的完成时间
    process.setFinishTime(currentTime + process.getServeTime());
    // 计算进程的周转时间，加到总周转时间上
    int turnaroundTime = process.getFinishTime() - process.getArrivalTime();
    process.setTurnaroundTime(turnaroundTime);
    totalTurnaroundTime += turnaroundTime;
    // 计算进程的带权周转时间，加到总带权周转时间上
    double weightedTurnaroundTime = (double) turnaroundTime / process.getServeTime();
    process.setWeightedTurnaroundTime(weightedTurnaroundTime);
    totalWeightedTurnaroundTime += weightedTurnaroundTime;
    return currentTime + process.getServeTime();
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
