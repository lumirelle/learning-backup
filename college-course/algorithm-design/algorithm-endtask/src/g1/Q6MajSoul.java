package g1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Q6MajSoul {

  public static void main(String[] args) {
    int[] brandsYouHave = {1, 1, 1, 1, 2, 2, 3, 3, 5, 6, 7, 8, 9};

    // 输入你有的牌，保存到计数数组里
    int[] brandsCount = new int[10];
    for (int i = 0; i < 13; i++) {
      int brand = brandsYouHave[i];
      brandsCount[brand]++;
    }

    // 计算和牌所缺少的牌
    List<Integer> result = getMissingBrands(brandsCount);

    // 输出结果
    if (result.isEmpty()) {
      System.out.println(0);
    } else {
      for (Integer brand : result) {
        System.out.print(brand + " ");
      }
      System.out.println();
    }
  }

  public static List<Integer> getMissingBrands(int[] brandsCount) {
    // 计算场上剩下的牌
    List<Integer> remainingBrands = getRemainingBrands(brandsCount);

    // 挑一张剩下的牌加到手牌里，看看能不能胡
    // 如果能胡，就把这张牌加入结果集
    // 进行下一轮尝试前，要把这张牌从手牌里拿走
    List<Integer> result = new ArrayList<>();
    for (Integer remainingBrand : remainingBrands) {
      brandsCount[remainingBrand]++;
      if (isHu(brandsCount)) {
        result.add(remainingBrand);
      }
      brandsCount[remainingBrand]--;
    }

    return result;
  }

  private static List<Integer> getRemainingBrands(int[] brandsCount) {
    List<Integer> remainingBrands = new ArrayList<>();
    for (int brand = 1; brand <= 9; brand++) {
      if (brandsCount[brand] < 4) {
        remainingBrands.add(brand);
      }
    }
    return remainingBrands;
  }

  /**
   * 判断手牌是否能胡牌
   * <p>
   * 如果能将牌挑干净，则可以胡牌，反之无法胡牌
   *
   * @param brandsCount 手牌计数
   * @return 是否胡牌
   */
  private static boolean isHu(int[] brandsCount) {
    // 在判断的过程中，会修改手牌计数，所以要复制一份
    return pickOutHead(Arrays.copyOf(brandsCount, brandsCount.length));
  }

  /**
   * 挑走雀头，并尝试将剩下的牌挑干净
   *
   * @param brandsCount 手牌计数
   * @return 是否能将牌挑干净
   */
  private static boolean pickOutHead(int[] brandsCount) {
    // 选出所有的候选雀头
    List<Integer> headBrands = new ArrayList<>();
    for (int brand = 1; brand <= 9; brand++) {
      // 如果该牌有两张以上，就可以作为雀头
      if (brandsCount[brand] >= 2) {
        headBrands.add(brand);
      }
    }

    // 一个一个尝试
    for (int headBrand : headBrands) {
      brandsCount[headBrand] -= 2;
      if (pickOutOthers(brandsCount, 12)) {
        return true;
      }
      brandsCount[headBrand] += 2;
    }
    return false;
  }

  /**
   * 挑走剩下的牌，以刻子和顺子的形式
   *
   * @param brandsCount 手牌计数
   * @param count       剩下的牌的张数
   * @return 是否能将牌挑干净
   */
  private static boolean pickOutOthers(int[] brandsCount, int count) {
    for (int brand = 1; brand <= 9; brand++) {
      if (brandsCount[brand] == 0) {
        continue;
      }

      // 先判断构不构成刻子，如果构成刻子，就挑走
      if (brandsCount[brand] >= 3) {
        brandsCount[brand] -= 3;
        count -= 3;
        if (pickOutOthers(brandsCount, count)) {
          return true;
        }
        brandsCount[brand] += 3;
        count += 3;
      }

      // 再判断（还）构不构成顺子，如果（还）构成顺子，就挑走
      if (brand <= 7 && brandsCount[brand] > 0 && brandsCount[brand + 1] > 0
          && brandsCount[brand + 2] > 0) {
        brandsCount[brand]--;
        brandsCount[brand + 1]--;
        brandsCount[brand + 2]--;
        count -= 3;
        if (pickOutOthers(brandsCount, count)) {
          return true;
        }
        brandsCount[brand]++;
        brandsCount[brand + 1]++;
        brandsCount[brand + 2]++;
        count += 3;
      }
    }

    return count == 0;
  }

}
