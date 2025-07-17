import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Memory {

  private final int fragmentLimit;

  private final String algorithmType;

  /**
   * 内存块列表，按起始地址存放
   */
  private final List<Block> blocks = new ArrayList<>();

  public Memory(int size, int fragmentLimit, String algorithmType) {
    if (!algorithmType.equals("FF") && !algorithmType.equals("BF")) {
      throw new IllegalArgumentException("Invalid algorithm type");
    }

    // 初始化内存块
    Block block = new Block(null, size, 0, true);
    blocks.add(block);

    this.fragmentLimit = fragmentLimit;
    this.algorithmType = algorithmType;
  }

  // 申请内存
  public void allocate(String processName, int allocateSize) {
    if (allocateSize <= 0) {
      System.out.println("Size must be greater than 0");
      return;
    }

    List<Block> usedBlocks = getUsedBlocks();
    for (Block usedBlock : usedBlocks) {
      if (processName.equals(usedBlock.getProcessName())) {
        System.out.println("Process name already exists");
        return;
      }
    }

    PriorityQueue<Block> freeBlocks = getFreeBlocks();
    for (Block freeBlock : freeBlocks) {
      // 如果空闲内存块足够大，则使用该内存块
      if (allocateSize <= freeBlock.getSize()) {
        // 如果产生的新空闲内存块大小大于碎片限制，则分割
        if ((freeBlock.getSize() - allocateSize) > fragmentLimit) {
          Block newblock = new Block(null, freeBlock.getSize() - allocateSize,
              freeBlock.getStartAddress() + allocateSize, true);
          blocks.add(newblock);
          freeBlock.setSize(allocateSize);
        }
        freeBlock.setProcessName(processName);
        freeBlock.setFree(false);
        return;
      }
    }
    System.out.println("Memory is full");
  }

  // 回收内存
  public void recycle(String processName) {
    List<Block> usedBlocks = getUsedBlocks();
    for (Block usedBlock : usedBlocks) {
      // 如果找到该内存块，则释放
      if (processName.equals(usedBlock.getProcessName())) {
        // 查找前后是否有空闲内存块
        Block previousFreeBlock = findAdjacentFreeBlock(usedBlock, true);
        Block nextFreeBlock = findAdjacentFreeBlock(usedBlock, false);
        // 如果前面有空闲块，则合并
        if (previousFreeBlock != null) {
          usedBlock.setStartAddress(previousFreeBlock.getStartAddress());
          usedBlock.setSize(previousFreeBlock.getSize() + usedBlock.getSize());
          blocks.remove(previousFreeBlock);
        }
        // 如果后面有空闲块，则合并
        if (nextFreeBlock != null) {
          usedBlock.setSize(nextFreeBlock.getSize() + usedBlock.getSize());
          blocks.remove(nextFreeBlock);
        }
        usedBlock.setProcessName(null);
        usedBlock.setFree(true);
        return;
      }
    }
    System.out.println("Process name not found");
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder("Current Usage:");
    for (Block block : blocks) {
      str.append("\n\t")
          .append(block.getProcessName() != null ? "name=" + block.getProcessName() : "nameless")
          .append(" address=")
          .append(block.getStartAddress())
          .append("~")
          .append(block.nextStartAddress() - 1)
          .append(" size=")
          .append(block.getSize())
          .append(block.isFree() ? " free" : " used");
    }
    return str.toString();
  }

  private PriorityQueue<Block> getFreeBlocks() {
    PriorityQueue<Block> freeBlocks;
    if (algorithmType.equals("FF")) {
      freeBlocks = new PriorityQueue<>(Comparator.comparing(Block::getStartAddress));
    } else {
      freeBlocks = new PriorityQueue<>(Comparator.comparing(Block::getSize));
    }
    freeBlocks.addAll(blocks.stream()
        .filter(Block::isFree)
        .toList());
    return freeBlocks;
  }

  private List<Block> getUsedBlocks() {
    return blocks.stream()
        .filter(block -> !block.isFree())
        .toList();
  }

  private Block findAdjacentFreeBlock(Block block, boolean isPrevious) {
    PriorityQueue<Block> freeBlocks = getFreeBlocks();
    return freeBlocks.stream()
        .filter(freeBlock -> {
          if (isPrevious) {
            return freeBlock.nextStartAddress() == block.getStartAddress();
          } else {
            return freeBlock.getStartAddress() == block.nextStartAddress();
          }
        })
        .findFirst()
        .orElse(null);
  }

}
