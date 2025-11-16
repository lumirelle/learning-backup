package com.dorm.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * Bean转换工具类，支持多个PO转换为单个VO
 */
@Slf4j
public class BeanConvertUtils {

    /**
     * 将源对象属性复制到目标对象
     *
     * @param source 源对象
     * @param target 目标对象
     * @param <T>    目标对象类型
     * @return 目标对象
     */
    public static <T> T copyProperties(T target, Object source) {
        if (source == null || target == null) {
            return null;
        }
        BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * 将源对象的属性复制到目标对象，默认忽略 additions 中的 id 属性，如果 source 对象有 no 属性，则忽略 additions 中的 no 属性。
     *
     * @param target    目标对象
     * @param source    源对象
     * @param additions 附加对象数组
     * @param <T>       目标对象类型
     * @return 目标对象
     */
    public static <T> T copyProperties(T target, Object source, Object... additions) {
        if (target == null || source == null) {
            return target;
        }
        BeanUtils.copyProperties(source, target);

        // 默认忽略 additions 中的 id 属性
        // 如果 source 对象有 no 属性，则忽略 additions 中的 no 属性
        String[] ignoreProperties;
        try {
            source.getClass().getDeclaredField("no");
            ignoreProperties = new String[]{"id", "no"};
        } catch (NoSuchFieldException e) {
            ignoreProperties = new String[]{"id"};
        }

        for (Object addition : additions) {
            if (addition == null) {
                continue;
            }
            BeanUtils.copyProperties(addition, target, ignoreProperties);
        }
        return target;
    }

    /**
     * 创建目标类实例并从源对象复制属性，目标类上不存在的属性将被忽略。
     *
     * @param targetClass  目标类
     * @param source       源对象
     * @param <T>          目标对象类型
     * @return 目标对象实例
     */
    public static <T> T convert(Class<T> targetClass, Object source) {
        if (targetClass == null || source == null) {
            return null;
        }

        try {
            // instance 实例 = object 对象
            // new instance = 新建对象
            T target = targetClass.getDeclaredConstructor().newInstance();
            return copyProperties(target, source);
        } catch (Exception e) {
            throw new RuntimeException(
                "转换 Bean %s <- %s 失败".formatted(targetClass.getName(), source.getClass()),
                e
            );
        }
    }

    /**
     * 创建目标类实例并从源对象复制属性，目标类上不存在的属性将被忽略。默认忽略 additions 中的 id 属性，如果 source 对象有 no 属性，则忽略 additions 中的 no 属性。
     *
     * @param targetClass  目标类
     * @param source       源对象
     * @param additions    附加对象数组
     * @param <T>          目标对象类型
     * @return 目标对象实例
     */
    public static <T> T convert(Class<T> targetClass, Object source, Object... additions) {
        if (targetClass == null || source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            return copyProperties(target, source, additions);
        } catch (Exception e) {
            throw new RuntimeException(
                "转换 Bean %s <- %s 失败".formatted(
                    targetClass.getName(),
                    Arrays.toString(Arrays.stream(additions).filter(Objects::nonNull).map(Object::getClass).toArray())
                ), e
            );
        }
    }
}
