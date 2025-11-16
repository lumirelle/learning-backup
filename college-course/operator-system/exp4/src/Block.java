public class Block {

  private String processName;

  private int size;

  private int startAddress;

  private boolean isFree;

  public Block(String processName, int size, int startAddress, boolean isFree) {
    this.processName = processName;
    this.size = size;
    this.startAddress = startAddress;
    this.isFree = isFree;
  }

  public String getProcessName() {
    return processName;
  }

  public void setProcessName(String processName) {
    this.processName = processName;
  }

  public int getSize() {
    return size;
  }

  public int getStartAddress() {
    return startAddress;
  }

  public int nextStartAddress() {
    return startAddress + size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public void setStartAddress(int startAddress) {
    this.startAddress = startAddress;
  }

  public void setFree(boolean isFree) {
    this.isFree = isFree;
  }

  public boolean isFree() {
    return isFree;
  }

}
