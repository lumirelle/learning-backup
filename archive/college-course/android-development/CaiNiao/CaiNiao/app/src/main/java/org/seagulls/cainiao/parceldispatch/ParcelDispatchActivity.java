package org.seagulls.cainiao.parceldispatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import org.seagulls.cainiao.R;
import org.seagulls.cainiao.common.bean.Parcel;
import org.seagulls.cainiao.common.bean.ParcelStakeholder;
import org.seagulls.cainiao.common.bean.type.LogisticsStatus;
import org.seagulls.cainiao.common.server.ServerRequester;
import org.seagulls.cainiao.common.util.LogTagGenerator;
import org.seagulls.cainiao.databinding.ActivityParcelDispatchBinding;

public class ParcelDispatchActivity extends AppCompatActivity {

  private static final String LOG_TAG = LogTagGenerator.generate(
      ParcelDispatchActivity.class);

  private final ServerRequester serverRequester = new ServerRequester(this);

  private ActivityParcelDispatchBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "启动包裹寄件界面");
    // ActivityParcelDispatchBinding 相当于 activity_parcel_dispatch.xml 文件
    binding = ActivityParcelDispatchBinding.inflate(getLayoutInflater());
    // binding.getRoot() 获得的就是 activity_parcel_dispatch.xml 里的最外层布局
    setContentView(binding.getRoot());
    initViewsWithData();
    setListener();
  }

  private void initViewsWithData() {
    Log.d(LOG_TAG, "加载数据并初始化界面");

    SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
    String userName = sp.getString("userName", "");
    Log.d(LOG_TAG, "数据：用户名称 = " + userName + "，来源 = SharedPreferences");

    // 从服务器加载该用户的干系人信息
    serverRequester.queryStakeholder(userName,
        stakeholders -> runOnUiThread(() -> {
          Log.d(LOG_TAG,
              "数据：干系人列表 = " + stakeholders + "，来源 = 服务器");
          initViews(userName, stakeholders);
        }));
  }

  private void initViews(String userName,
      List<ParcelStakeholder> stakeholders) {
    // TODO：展示多个干系人信息，即单用户多地址支持
    binding.sendName.setText(userName);
    if (stakeholders.isEmpty()) {
      Log.d(LOG_TAG, "用户没有干系人信息");
    } else {
      Log.d(LOG_TAG, "用户已有干系人信息，加载");
      binding.sendAddress.setText(stakeholders.get(0).getAddress());
      binding.sendTel.setText(stakeholders.get(0).getTelephone());
    }
  }

  private void setListener() {
    Log.d(LOG_TAG, "设置监听器");
    binding.btnSubmit.setOnClickListener(view -> {
      // 下单
      if (view.getId() == R.id.btn_submit) {
        Log.d(LOG_TAG, "获取用户输入的数据");
        String sendName = binding.sendName.getText().toString().trim();
        String sendAddress = binding.sendAddress.getText().toString().trim();
        String sendTel = binding.sendTel.getText().toString().trim();
        String recipientName = binding.recipientName.getText()
            .toString()
            .trim();
        String recipientAddress = binding.recipientAddress.getText()
            .toString()
            .trim();
        String recipientTel = binding.recipientTel.getText().toString().trim();
        String name = binding.name.getText().toString().trim();
        String way = binding.sendWays.getCheckedRadioButtonId() == R.id.pick_up
            ? "上门取件" : "服务点自寄";

        // 检查用户输入的数据
        Log.d(LOG_TAG, "检查用户输入的数据");
        if (TextUtils.isEmpty(sendName)) {
          Log.d(LOG_TAG, "检查未通过，寄件姓名为空");
          Toast.makeText(getApplicationContext(), "请输入寄件姓名",
              Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(sendAddress)) {
          Log.d(LOG_TAG, "检查未通过，寄件地址为空");
          Toast.makeText(getApplicationContext(), "请输入寄件地址",
              Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(sendTel)) {
          Log.d(LOG_TAG, "检查未通过，寄件电话为空");
          Toast.makeText(getApplicationContext(), "请输入寄件电话",
              Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(recipientName)) {
          Log.d(LOG_TAG, "检查未通过，收件姓名为空");
          Toast.makeText(getApplicationContext(), "请输入收件姓名",
              Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(recipientAddress)) {
          Log.d(LOG_TAG, "检查未通过，收件地址为空");
          Toast.makeText(getApplicationContext(), "请输入收件地址",
              Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(recipientTel)) {
          Log.d(LOG_TAG, "检查未通过，收件电话为空");
          Toast.makeText(getApplicationContext(), "请输入收件电话",
              Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(name)) {
          Log.d(LOG_TAG, "检查未通过，物品名称为空");
          Toast.makeText(getApplicationContext(), "请输入物品名称",
              Toast.LENGTH_SHORT).show();
        }
        // 检查通过
        else {
          Log.d(LOG_TAG, "检查通过");
          Log.d(LOG_TAG, "用户输入的数据为：" + "寄件姓名：" + sendName + "，寄件地址"
              + sendAddress + "，寄件电话：" + sendTel + "，收件姓名："
              + recipientName + "，收件地址：" + recipientAddress + "，收件电话："
              + recipientTel + "，寄件方式：" + way + "，物品名称：" + name);

          ServerRequester serverRequester = new ServerRequester(this);

          // 新建寄件人信息，添加进服务器
          ParcelStakeholder sender = new ParcelStakeholder();
          sender.setName(sendName);
          sender.setAddress(sendAddress);
          sender.setTelephone(sendTel);
          serverRequester.addStakeholderIfNotExist(sender, senderId -> {
            // 成功则更新寄件人 ID
            sender.setId(senderId);

            // 新建收件人信息，添加进服务器
            ParcelStakeholder recipient = new ParcelStakeholder();
            recipient.setName(recipientName);
            recipient.setAddress(recipientAddress);
            recipient.setTelephone(recipientTel);
            serverRequester.addStakeholderIfNotExist(recipient, recipientId -> {
              // 成功则更新收件人 ID
              recipient.setId(recipientId);

              // 新建包裹信息，添加进服务器，商品图片、物流公司、物流 ID 应由服务器指定
              Parcel parcel = new Parcel();
              parcel.setTitle(name);
              parcel.setLogisticsStatus(LogisticsStatus.UNSHIPPED);
              parcel.setSender(sender);
              parcel.setReceiver(recipient);
              serverRequester.addParcel(parcel, () -> runOnUiThread(() -> {
                Log.d(LOG_TAG, "添加成功");
                Toast.makeText(getApplicationContext(), "下单成功",
                    Toast.LENGTH_SHORT).show();
                finish();
              }));
            });
          });
        }
      }
    });
  }

}
