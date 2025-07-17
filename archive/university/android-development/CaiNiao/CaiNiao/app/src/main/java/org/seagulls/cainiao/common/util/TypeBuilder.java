package org.seagulls.cainiao.common.util;

import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import java.lang.reflect.Type;

/**
 * 用于构建二至多层泛型的类型
 */
public class TypeBuilder {

  private TypeBuilder() {
  }

  public static Type build(Type... types) {
    ParameterizedTypeImpl beforeType = null;
    if (types != null && types.length > 0) {
      for (int i = types.length - 1; i > 0; i--) {
        beforeType = new ParameterizedTypeImpl(
            new Type[]{beforeType == null ? types[i] : beforeType}, null,
            types[i - 1]);
      }
    }
    return beforeType;
  }

}
