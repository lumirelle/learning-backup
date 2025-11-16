package org.seagulls.cainiaoserver.util;


import static org.seagulls.cainiaoserver.util.UtilsHolder.RANDOM;

import java.util.Date;
import org.seagulls.cainiaoserver.enums.LogisticsCompany;

@Deprecated
public class TestDataGenerator {

  private static final String[] imageUrls = new String[]{
      "https://pic.zsucai.com/files/2020/1121/ffpic0224ddfere05047.jpg",
      "https://img.zcool.cn/community/01900b5e512031a8012165180f30cb.jpg@3000w_1l_0o_100sh.jpg",
      "https://gd-hbimg.huaban.com/3d41251a0f3f5480cf7e9693b409b90b014090f51c8b42-Ux2L75_fw658",
      "https://img.zcool.cn/community/01decc5a56f106a80120121f890e07.jpg@1280w_1l_2o_100sh.jpg",
      "https://img.zcool.cn/community/01587d5c3f109da801213f26fb4164.jpg@3000w_1l_2o_100sh.jpg",
      "https://t00img.yangkeduo.com/goods/images/2019-02-27/2b7690ae-6742-484c-a260-d473119585d1.jpeg",
      "http://img.91jm.com/2016/05/154F15DA086.jpg"};

  private TestDataGenerator() {
  }

  /**
   * 生成一个随机的图片 URL
   *
   * @return 图片 URL
   */
  public static String generateImageUrl() {
    return imageUrls[RANDOM.nextInt(imageUrls.length)];
  }

  /**
   * 生成两位大写字母的物流公司前缀 + 10 位随机数字的物流 ID
   *
   * @param company 物流公司
   * @return 物流 ID
   */
  public static String generateLogisticsId(LogisticsCompany company) {
    // 前缀加生成的 10 位数字
    return company.getPrefix() + new Date().getTime() % 10000000L
        + RANDOM.nextInt(1000);
  }

  /**
   * 生成一个随机的物流公司
   *
   * @return 物流公司
   */
  public static LogisticsCompany generateLogisticsCompany() {
    return LogisticsCompany.values()[RANDOM.nextInt(
        LogisticsCompany.values().length)];
  }

}
