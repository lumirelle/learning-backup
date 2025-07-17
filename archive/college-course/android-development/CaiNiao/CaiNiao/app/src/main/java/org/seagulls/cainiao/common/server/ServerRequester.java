package org.seagulls.cainiao.common.server;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.alibaba.fastjson2.JSON;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.seagulls.cainiao.common.bean.Parcel;
import org.seagulls.cainiao.common.bean.ParcelStakeholder;
import org.seagulls.cainiao.common.config.ServerConfig;
import org.seagulls.cainiao.common.server.callback.Callback;
import org.seagulls.cainiao.common.server.callback.DataCallback;
import org.seagulls.cainiao.common.util.LogTagGenerator;
import org.seagulls.cainiao.common.util.TypeBuilder;

/**
 * 访问服务器时使用的工具类
 */
public class ServerRequester {

  private static final String LOG_TAG = LogTagGenerator.generate(
      ServerRequester.class);

  private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder().connectTimeout(
      5, TimeUnit.SECONDS).build();

  private static final MediaType MEDIA_TYPE_JSON = MediaType.parse(
      "application/json;charset=utf-8");

  /**
   * 访问服务器的上下文环境
   */
  private final Activity context;

  public ServerRequester(Activity context) {
    this.context = context;
  }

  /**
   * Get：从服务器中查询用户的所有包裹，用户是所有同名的干系人 ParcelStakeholder
   *
   * @param userName  用户名称
   * @param onSuccess 操作成功时的回调方法，参数为包裹列表对象
   */
  public void queryParcelsByUserName(String userName,
      DataCallback<List<Parcel>> onSuccess) {
    Log.d(LOG_TAG, "从服务器中查询用户名称为 " + userName + " 的所有包裹");
    // 新开一个线程异步访问服务器，发送 Get 请求
    new Thread(() -> {
      Request request = new Builder().url(
              ServerConfig.URL + "parcel/queryByUserName?userName=" + userName)
          .build();
      OK_HTTP_CLIENT.newCall(request)
          .enqueue(new OkHttpDataCallback<>(context, onSuccess,
              TypeBuilder.build(List.class, Parcel.class)));
    }).start();
  }

  /**
   * Get：从服务器中查询包裹
   *
   * @param id        包裹 ID
   * @param onSuccess 操作成功时的回调方法，参数为包裹对象
   */
  public void queryParcelById(int id, DataCallback<Parcel> onSuccess) {
    Log.d(LOG_TAG, "从服务器中查询 ID 为 " + id + " 的包裹");
    new Thread(() -> {
      Request request = new Builder().url(
          ServerConfig.URL + "parcel/queryById?id=" + id).build();
      OK_HTTP_CLIENT.newCall(request)
          .enqueue(new OkHttpDataCallback<>(context, onSuccess, Parcel.class));
    }).start();
  }

  /**
   * Get：从服务器中查询身份码
   *
   * @param userName  用户名称
   * @param onSuccess 操作成功时的回调方法，参数为身份码字符串
   */
  public void queryIdentityCode(String userName,
      DataCallback<String> onSuccess) {
    Log.d(LOG_TAG, "从服务器中查询身份码");
    new Thread(() -> {
      Request request = new Request.Builder().url(
          ServerConfig.URL + "identityCode/queryByUserName?userName="
              + userName).build();
      OK_HTTP_CLIENT.newCall(request)
          .enqueue(new OkHttpDataCallback<>(context, onSuccess, String.class));
    }).start();
  }

  /**
   * Get：从服务器中查询干系人
   *
   * @param userName  用户名称
   * @param onSuccess 操作成功时的回调方法，参数为干系人对象
   */
  public void queryStakeholder(String userName,
      DataCallback<List<ParcelStakeholder>> onSuccess) {
    Log.d(LOG_TAG, "从服务器中查询用户名称为 " + userName + " 的信息");
    new Thread(() -> {
      Request request = new Builder().url(
              ServerConfig.URL + "stakeholder/queryByUserName?userName=" + userName)
          .build();
      OK_HTTP_CLIENT.newCall(request)
          .enqueue(new OkHttpDataCallback<>(context, onSuccess,
              TypeBuilder.build(List.class, ParcelStakeholder.class)));
    }).start();
  }

  /**
   * Post & Get：如果该干系人不存在，则向服务器中添加干系人信息
   *
   * @param stakeholder 干系人信息
   * @param onSuccess   操作成功时的回调方法，参数为干系人的 ID。注意：干系人不存在时，参数为新添加的干系人的
   *                    ID；已经存在时，参数为已经存在的干系人 ID。这样可以避免重复创建拥有同样信息（姓名、电话和地址）的干系人
   */
  public void addStakeholderIfNotExist(ParcelStakeholder stakeholder,
      DataCallback<Integer> onSuccess) {
    Log.d(LOG_TAG, "向服务器中添加干系人：" + stakeholder);
    new Thread(() -> {
      RequestBody requestBody = RequestBody.create(
          JSON.toJSONString(stakeholder), MEDIA_TYPE_JSON);
      Request request = new Request.Builder().url(
          ServerConfig.URL + "stakeholder/add").post(requestBody).build();
      OK_HTTP_CLIENT.newCall(request)
          .enqueue(new OkHttpDataCallback<>(context, onSuccess, Integer.class));
    }).start();
  }

  /**
   * 向服务器中添加包裹
   *
   * @param parcel    需要添加的包裹数据
   * @param onSuccess 操作成功时的回调方法
   */
  public void addParcel(Parcel parcel, Callback onSuccess) {
    Log.d(LOG_TAG, "向服务器中添加包裹：" + parcel);
    new Thread(() -> {
      RequestBody requestBody = RequestBody.create(JSON.toJSONString(parcel),
          MEDIA_TYPE_JSON);
      Request request = new Request.Builder().url(
          ServerConfig.URL + "parcel/add").post(requestBody).build();
      OK_HTTP_CLIENT.newCall(request)
          .enqueue(new OkHttpCallback(context, onSuccess));
    }).start();
  }

  static void defaultOnSuccess(String json) {
    Log.d(LOG_TAG, "查询数据成功：" + json);
  }

  static void defaultOnFailure(Activity context, Exception e) {
    Log.e(LOG_TAG, e.getMessage(), e);
    context.runOnUiThread(
        () -> Toast.makeText(context, "出现网络连接问题", Toast.LENGTH_LONG)
            .show());
  }

  /**
   * 使用 OkHttp 对服务器发送请求后，不需要接收数据时的 OkHttp Callback 实现，实现 OnResponse、OnFailure。该
   * Callback 不对外开放
   */
  private static class OkHttpCallback implements okhttp3.Callback {

    private final Activity context;

    private final Callback onSuccess;

    /**
     * 使用 OkHttp 对服务器发送请求后，不需要接收数据时的 OkHttp Callback 实现，实现
     * OnResponse、OnFailure。该 Callback 不对外开放
     *
     * @param context   上下文环境
     * @param onSuccess onResponse 时提供给外部的回调方法
     */
    public OkHttpCallback(Activity context, Callback onSuccess) {
      this.context = context;
      this.onSuccess = onSuccess;
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
      defaultOnFailure(context, e);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
      onSuccess.execute();
    }

  }

  /**
   * 使用 OkHttp 对服务器发送请求后，需要接收数据时的 OkHttp Callback 实现，实现 OnResponse、OnFailure。该
   * Callback 不对外开放
   *
   * @param <T> 请求成功时返回的数据类型
   */
  private static class OkHttpDataCallback<T> implements okhttp3.Callback {

    private final Activity context;

    private final DataCallback<T> onSuccess;

    private final Type type;

    /**
     * 使用 OkHttp 对服务器发送请求后，需要接收数据时的 OkHttp Callback 实现，实现 OnResponse、OnFailure。该
     * Callback 不对外开放
     *
     * @param context   上下文环境
     * @param onSuccess onResponse 时提供给外部的回调方法
     * @param type      onResponse 时返回给外部的数据类型
     */
    public OkHttpDataCallback(Activity context, DataCallback<T> onSuccess,
        Type type) {
      this.context = context;
      this.onSuccess = onSuccess;
      this.type = type;
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
      defaultOnFailure(context, e);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response)
        throws IOException {
      if (response.body() != null) {
        String json = response.body().string();
        defaultOnSuccess(json);
        onSuccess.execute(JSON.parseObject(json, type));
      }
    }

  }

}
