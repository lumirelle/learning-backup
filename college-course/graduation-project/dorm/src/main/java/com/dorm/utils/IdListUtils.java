package com.dorm.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class IdListUtils {
    public static List<Integer> convertToIntegerList(String idListStr) {
        if (idListStr == null || idListStr.isEmpty()) {
            return new ArrayList<>();
        }
        // “1,2,3” -> [1,2,3]
        String[] ids = idListStr.split(",");
        List<Integer> idList = new ArrayList<>();
        for (String id : ids) {
            try {
                idList.add(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
            }
        }
        return idList;
    }
}
