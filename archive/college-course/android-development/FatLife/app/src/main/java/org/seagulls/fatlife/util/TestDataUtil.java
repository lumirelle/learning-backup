package org.seagulls.fatlife.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.seagulls.fatlife.entity.WashingMachine;

public abstract class TestDataUtil {

  private TestDataUtil() {
  }

  public static List<WashingMachine> getWashingMachines() {
    List<WashingMachine> washingMachines = new ArrayList<>();
    washingMachines.add(new WashingMachine("1号洗衣机", "理工两江3栋"));
    washingMachines.add(new WashingMachine("2号洗衣机", "理工两江3栋"));
    washingMachines.add(new WashingMachine("3号洗衣机", "理工两江3栋"));
    washingMachines.add(new WashingMachine("4号洗衣机", "理工两江3栋"));
    washingMachines.add(new WashingMachine("5号洗衣机", "理工两江3栋"));
    washingMachines.add(new WashingMachine("6号洗衣机", "理工两江3栋"));
    washingMachines.add(new WashingMachine("7号洗衣机", "理工两江3栋"));
    washingMachines.add(new WashingMachine("8号洗衣机", "理工两江3栋"));
    return washingMachines;
  }

  private static final Random RANDOM = new Random();

  public static String randomOrderNo() {
    // 14 位
    return String.valueOf(System.currentTimeMillis() + RANDOM.nextInt(1000));
  }

}
