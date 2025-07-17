package com.waoap.addressbook.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class PinYinComparator implements Comparator<String> {
    /**
     * 中文环境下的字符串比较器
     */
    private static final Collator collator = Collator.getInstance(Locale.CHINA);

    /**
     * 判断词语是否以中文（严格意义上说是非 ASCII 字符）开头
     *
     * @param word 词语
     * @return 是否以中文（严格意义上说是非 ASCII 字符）开头
     */
    public static boolean isBeginWithChinese(String word) {
        return word.length() != 0 && word.charAt(0) >= 128;
    }

    /**
     * 获取词语的全拼，英文转为大写，中文转为拼音，格式为无声调大写字母
     *
     * @param word 词语
     * @return 词语的全拼
     */
    public static String getFullSpell(String word) {
        StringBuilder builder = new StringBuilder();
        char[] chars = word.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char c : chars) {
            if (c > 128) {
                try {
                    builder.append(PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)[0]);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                builder.append(Character.toUpperCase(c));
            }
        }
        return builder.toString();
    }

    /**
     * 比较字符串
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return o1 > o2 -> 1<br>o1 == o2 -> 0<br>o1 < o2 -> -1
     */
    @Override
    public int compare(String o1, String o2) {
        // 比较导航条目与联系人信息时，默认将联系人放在后面
        if (o1.matches("[#A-Z]")) {
            if (o1.equals(o2.substring(0, 1)) && o2.length() > 1) return -1;
        }
        if (o2.matches("[#A-Z]")) {
            if (o2.equals(o1.substring(0, 1)) && o1.length() > 1) return 1;
        }

        // 比较英文和中文开头的联系人时，默认将中文放在后面
        if (o1.matches("[#a-zA-Z].+") && isBeginWithChinese(o2)) {
            return -1;
        }
        if (o2.matches("[#a-zA-Z].+") && isBeginWithChinese(o1)) {
            return 1;
        }

        CollationKey key1 = collator.getCollationKey(getFullSpell(o1));
        CollationKey key2 = collator.getCollationKey(getFullSpell(o2));
        return key1.compareTo(key2);
    }
}
