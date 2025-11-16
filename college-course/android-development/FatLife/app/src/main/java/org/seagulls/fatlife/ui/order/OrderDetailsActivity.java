package org.seagulls.fatlife.ui.order;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.seagulls.fatlife.database.OrderHelper;
import org.seagulls.fatlife.databinding.ActivityOrderDetailsBinding;
import org.seagulls.fatlife.entity.Order;
import org.seagulls.fatlife.entity.WashingMachine;
import org.seagulls.fatlife.type.WashingType;
import org.seagulls.fatlife.util.DatetimeUtil;
import org.seagulls.fatlife.util.TestDataUtil;

public class OrderDetailsActivity extends AppCompatActivity {

  private static final String LOG_TAG = "FL.ODActivity";

  private ActivityOrderDetailsBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    initViewData();
    initViewAction();
  }

  private void initViewData() {
    Intent intent = getIntent();

    // 如果没有传入订单，需要生成订单信息
    Order order = (Order) intent.getSerializableExtra("order");
    if (order == null) {
      WashingMachine washingMachine = (WashingMachine) intent.getSerializableExtra(
          "washingMachine");
      assert washingMachine != null;
      WashingType washingType = (WashingType) intent.getSerializableExtra(
          "washingType");
      assert washingType != null;
      binding.tvWmName1.setText(washingMachine.getName());
      binding.tvWmName2.setText(washingMachine.getName());
      binding.tvWmShop1.setText(washingMachine.getShop());
      binding.tvWmShop2.setText(washingMachine.getShop());
      binding.tvWtAmount.setText(washingType.getAmount());
      binding.tvTotalAmount.setText(washingType.getAmount());
      binding.tvRealAmount.setText(washingType.getAmount());
      binding.tvWtName.setText(washingType.getStr());

      String no = TestDataUtil.randomOrderNo();
      String timeNow = DatetimeUtil.nowFormatted();
      binding.tvOrderNo.setText(no);
      binding.tvMakeTime.setText(timeNow);
      binding.tvFinishTime.setText(timeNow);
      binding.tvPayTime.setText(timeNow);

      OrderHelper helper = new OrderHelper(this);
      helper.insert(new Order(no, timeNow, timeNow, timeNow, washingMachine,
          washingType));
      helper.close();
    }
    // 否则直接展示
    else {
      WashingMachine washingMachine = order.getWashingMachine();
      WashingType washingType = order.getWashingType();
      binding.tvWmName1.setText(washingMachine.getName());
      binding.tvWmName2.setText(washingMachine.getName());
      binding.tvWmShop1.setText(washingMachine.getShop());
      binding.tvWmShop2.setText(washingMachine.getShop());
      binding.tvWtAmount.setText(washingType.getAmount());
      binding.tvTotalAmount.setText(washingType.getAmount());
      binding.tvRealAmount.setText(washingType.getAmount());
      binding.tvWtName.setText(washingType.getStr());
      binding.tvOrderNo.setText(order.getNo());
      binding.tvMakeTime.setText(order.getMakeTime());
      binding.tvFinishTime.setText(order.getFinishTime());
      binding.tvPayTime.setText(order.getPayTime());
    }
  }

  private void initViewAction() {
    binding.tvBack.setOnClickListener(v -> finish());
  }

}
