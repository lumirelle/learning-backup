package com.dorm.utils;

import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@Component
public class NoGenerateUtils {

    public String generateUniqueNo(String prefix, int length) {
        if (length < prefix.length() + 14) {
            throw new IllegalArgumentException("长度必须大于前缀长度 + 时间戳长度");
        }

        // 格式化当前时间
        // 生成时间戳部分
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());

        // 生成随机数部分
        Random random = new Random();
        StringBuilder randomStr = new StringBuilder();
        for (int i = 0; i < length - timestamp.length() - prefix.length(); i++) {
            randomStr.append(random.nextInt(10));
        }

        // 组合前缀、时间戳和随机数
        return prefix + timestamp + randomStr;
    }

}
