public class Process {

  private String name;

  private int arrivalTime;

  private int serveTime;

  private int finishTime;

  private int turnaroundTime;

  private double weightedTurnaroundTime;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(int arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public int getServeTime() {
    return serveTime;
  }

  public void setServeTime(int serveTime) {
    this.serveTime = serveTime;
  }

  public int getFinishTime() {
    return finishTime;
  }

  public void setFinishTime(int finishTime) {
    this.finishTime = finishTime;
  }

  public int getTurnaroundTime() {
    return turnaroundTime;
  }

  public void setTurnaroundTime(int turnaroundTime) {
    this.turnaroundTime = turnaroundTime;
  }

  public double getWeightedTurnaroundTime() {
    return weightedTurnaroundTime;
  }

  public void setWeightedTurnaroundTime(double weightedTurnaroundTime) {
    this.weightedTurnaroundTime = weightedTurnaroundTime;
  }

}
