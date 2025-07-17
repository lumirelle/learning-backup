package org.seagulls.cainiao.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.seagulls.cainiao.R;
import org.seagulls.cainiao.common.util.LogTagGenerator;
import org.seagulls.cainiao.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  private static final String LOG_TAG = LogTagGenerator.generate(
      MainActivity.class);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // 弹出弹窗要求输入用户名称
    Log.d(LOG_TAG, "弹窗要求用户输入用户名称");
    showLoginDialog();
  }

  @SuppressLint("SetTextI18n")
  private void showLoginDialog() {
    TextView tvTitle = new TextView(this);
    tvTitle.setText("请输入用户名称");
    tvTitle.setTextSize(18);
    tvTitle.setTextColor(getResources().getColor(R.color.black, null));
    tvTitle.setPadding(20, 20, 20, 20);

    EditText etUserId = new EditText(this);
    // 读取 SharedPreferences 中的用户名称
    SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
    etUserId.setText(sp.getString("userName", ""));
    // 监听编辑器文本变化
    etUserId.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
          int after) {
        // Do nothing.
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before,
          int count) {
        // Do nothing.
      }

      @Override
      public void afterTextChanged(Editable s) {
        // 不允许空格开头
        if (s.toString().startsWith(" ")) {
          etUserId.setText(s.toString().trim());
        }
      }
    });

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCustomTitle(tvTitle);
    builder.setView(etUserId);
    builder.setPositiveButton("确定", (dialog, which) -> {
      String userName = etUserId.getText().toString();
      Log.d(LOG_TAG, "本次用户输入的名称 为：" + userName);
      // 使用 SharedPreferences 记录用户的名称
      sp.edit().putString("userName", userName).apply();
      // 用户输入完毕，显示 UI
      Log.d(LOG_TAG, "用户输入完毕，显示 UI");
      ActivityMainBinding binding = ActivityMainBinding.inflate(
          getLayoutInflater());
      setContentView(binding.getRoot());
      BottomNavigationView navView = binding.navView;
      NavController navController = Navigation.findNavController(this,
          R.id.nav_host_fragment_activity_main);
      NavigationUI.setupWithNavController(navView, navController);
    });
    // 用户取消输入，结束应用
    builder.setOnCancelListener(dialog -> {
      Log.d(LOG_TAG, "用户取消输入");
      finish();
    });
    builder.show();
  }

}
