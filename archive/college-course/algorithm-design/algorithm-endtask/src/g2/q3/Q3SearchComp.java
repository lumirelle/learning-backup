package g2.q3;

import java.util.Random;

public class Q3SearchComp {

  // 二分查找算法
  public static int binarySearch(int[] arr, int target) {
    int left = 0;
    int right = arr.length - 1;

    while (left <= right) {
      int mid = (left + right) / 2;

      if (arr[mid] == target) {
        return mid;
      } else if (arr[mid] < target) {
        left = mid + 1;
      } else {
        right = mid - 1;
      }
    }

    return -1;
  }

  // 三分查找算法
  public static int ternarySearch(int[] arr, int target) {
    int left = 0;
    int right = arr.length - 1;

    while (left <= right) {
      int partitionSize = (right - left) / 3;
      int mid1 = left + partitionSize;
      int mid2 = right - partitionSize;

      if (arr[mid1] == target) {
        return mid1;
      }
      if (arr[mid2] == target) {
        return mid2;
      }

      if (target < arr[mid1]) {
        right = mid1 - 1;
      } else if (target > arr[mid2]) {
        left = mid2 + 1;
      } else {
        left = mid1 + 1;
        right = mid2 - 1;
      }
    }

    return -1;
  }

  public static void main(String[] args) {
    Random random = new Random();

    long averageTimeBin = 0;
    long averageTimeTer = 0;

    for (int i = 0; i < 10; i++) {
      int size = random.nextInt(10) + 10;

      int[] arr = new int[size];
      arr[0] = random.nextInt(100);
      for (int j = 1; j < size; j++) {
        arr[j] = random.nextInt(100) + arr[j - 1];
      }

      int target;
      if (random.nextInt(10) > 2) {
        target = arr[random.nextInt(size)];
      } else {
        target = random.nextInt(100) + 100;
      }

      long time = System.nanoTime();
      int binaryResult = binarySearch(arr, target);
      time = System.nanoTime() - time;
      System.out.println("二分查找花费时间 (ns)：" + time);
      averageTimeBin += time;

      time = System.nanoTime();
      int ternaryResult = ternarySearch(arr, target);
      time = System.nanoTime() - time;
      System.out.println("三分查找花费时间 (ns)：" + time);
      averageTimeTer += time;

      System.out.println("二分查找结果: " + binaryResult);
      System.out.println("三分查找结果: " + ternaryResult);
    }

    System.out.println();
    System.out.println("二分查找平均时间 (ns)：" + averageTimeBin / 20);
    System.out.println("三分查找平均时间 (ns)：" + averageTimeTer / 20);
  }

}
