package com.example.classroomschool.util.pinyin;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;


public class Chines2PingUtil {

  public static String getFirstSpell(String chinese) {
    StringBuilder pybf = new StringBuilder();
    char[] arr = chinese.toCharArray();
    HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
    defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

    for (char c : arr) {
      if (c > 128) {
        try {
          String[] temp = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat);
          if (temp != null) {
            pybf.append(temp[0].charAt(0));
          }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
          e.printStackTrace();
        }
      } else {
        pybf.append(c);
      }
    }
    return pybf.toString().replaceAll("\\W", "").trim().toUpperCase();
  }

  /**
   * 获取汉字串拼音，英文字符不变
   *
   * @param chinese
   * @return
   */
  public static String getFullSpell(String chinese) {
    StringBuilder pybf = new StringBuilder();
    char[] arr = chinese.toCharArray();
    HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
    defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    for (char c : arr) {
      if (c > 128) {
        try {
          pybf.append(PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)[0]);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
          e.printStackTrace();
        }
      } else {
        pybf.append(c);
      }
    }
    return pybf.toString();
  }
}
