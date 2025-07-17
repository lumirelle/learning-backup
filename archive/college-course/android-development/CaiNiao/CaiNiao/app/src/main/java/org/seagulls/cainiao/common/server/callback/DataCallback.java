package org.seagulls.cainiao.common.server.callback;

/**
 * 对外开放的带有服务器返回的数据的回调方法类
 *
 * @param <T> 服务器返回的数据的类型定义
 */
public interface DataCallback<T> {

  void execute(T data);

}
