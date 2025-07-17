package org.seagulls.fatlife.ui.washing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.seagulls.fatlife.database.WashingMachineHelper;
import org.seagulls.fatlife.databinding.ActivityWashingBinding;
import org.seagulls.fatlife.entity.WashingMachine;
import org.seagulls.fatlife.type.WashingType;
import org.seagulls.fatlife.ui.order.OrderDetailsActivity;

public class WashingActivity extends AppCompatActivity {

  private static final String LOG_TAG = "FL.WashingActivity";

  private ActivityWashingBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityWashingBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    WashingMachineHelper helper = new WashingMachineHelper(this);
    WashingMachine defaultWashingMachine = helper.selectDefaultWaitingWashingMachine();
    helper.close();
    initViewData(defaultWashingMachine);
    initViewAction(defaultWashingMachine);
  }

  private void initViewData(WashingMachine washingMachine) {
    binding.tvWashingMachine.setText(washingMachine.getName());
    binding.tvWashingMachineLocation.setText(washingMachine.getShop());
  }

  @SuppressLint("SetTextI18n")
  private void initViewAction(WashingMachine washingMachine) {
    binding.tvBack.setOnClickListener(v -> finish());
    binding.tvEdit.setOnClickListener(
        v -> Toast.makeText(this, "暂不支持选择洗衣机！", Toast.LENGTH_SHORT)
            .show());
    binding.llSelfCleaning.setOnClickListener(v -> {
      binding.tvAmount.setText("￥0.00");
      binding.tvType.setText("桶自洁");
      Log.d(LOG_TAG, "选择了桶自洁");
    });
    binding.llDehydrationOnly.setOnClickListener(v -> {
      binding.tvAmount.setText("￥0.09");
      binding.tvType.setText("单脱水");
      Log.d(LOG_TAG, "选择了单脱水");
    });
    binding.llStandardWashing.setOnClickListener(v -> {
      binding.tvAmount.setText("￥2.85");
      binding.tvType.setText("标准洗");
      Log.d(LOG_TAG, "选择了标准洗");
    });
    binding.llExtendWashing.setOnClickListener(v -> {
      binding.tvAmount.setText("￥3.85");
      binding.tvType.setText("大物洗");
      Log.d(LOG_TAG, "选择了大物洗");
    });
    binding.tvDone.setOnClickListener(v -> {
      Intent intent = new Intent(this, OrderDetailsActivity.class);
      Bundle bundle = new Bundle();
      bundle.putSerializable("washingMachine", washingMachine);
      bundle.putSerializable("washingType",
          WashingType.valueOfCost(binding.tvAmount.getText().toString()));
      Log.d(LOG_TAG, "washingMachine:" + washingMachine + ", washingType:"
          + WashingType.valueOfCost(binding.tvAmount.getText().toString()));
      intent.putExtras(bundle);
      startActivity(intent);
      finish();
    });
  }

}
