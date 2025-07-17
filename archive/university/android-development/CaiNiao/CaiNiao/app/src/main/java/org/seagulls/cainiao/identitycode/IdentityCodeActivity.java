package org.seagulls.cainiao.identitycode;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import org.seagulls.cainiao.R;
import org.seagulls.cainiao.common.server.ServerRequester;
import org.seagulls.cainiao.common.util.LogTagGenerator;
import org.seagulls.cainiao.databinding.ActivityIdentifyCodeBinding;

public class IdentityCodeActivity extends AppCompatActivity {

  private static final String LOG_TAG = LogTagGenerator.generate(
      IdentityCodeActivity.class);

  private ActivityIdentifyCodeBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "启动身份码界面");
    // ActivityIdentifyCodeBinding 相当于 activity_identify_code.xml 文件
    binding = ActivityIdentifyCodeBinding.inflate(getLayoutInflater());
    // binding.getRoot() 获得的就是 activity_identify_code.xml 里的最外层布局
    setContentView(binding.getRoot());
    initViewsWithData();
  }

  private void initViewsWithData() {
    Log.d(LOG_TAG, "加载数据并初始化界面");

    SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
    String userName = sp.getString("userName", "");
    Log.d(LOG_TAG, "数据：用户名称 = " + userName + "，来源 = SharedPreferences");

    ServerRequester serverRequester = new ServerRequester(this);
    serverRequester.queryIdentityCode(userName, identityCodeData -> {
      Log.d(LOG_TAG,
          "数据：身份码数据 = " + identityCodeData + "，来源 = 服务器");

      try {
        Log.d(LOG_TAG, "生成并显示身份码");
        Bitmap identityCodeBitmap = generateBarcode(identityCodeData);
        runOnUiThread(() -> {
          // 显示身份码容器
          binding.rlIdentityCodeContainer.setVisibility(View.VISIBLE);
          // 显示身份码数据
          binding.tvIdentityCode1.setText(
              String.valueOf(identityCodeData.charAt(0)));
          binding.tvIdentityCode2.setText(
              String.valueOf(identityCodeData.charAt(1)));
          binding.tvIdentityCode3.setText(
              String.valueOf(identityCodeData.charAt(2)));
          binding.tvIdentityCode4.setText(
              String.valueOf(identityCodeData.charAt(3)));
          binding.tvIdentityCode5.setText(
              String.valueOf(identityCodeData.charAt(4)));
          binding.tvIdentityCode6.setText(
              String.valueOf(identityCodeData.charAt(5)));
          binding.tvIdentityCode7.setText(
              String.valueOf(identityCodeData.charAt(6)));
          binding.tvIdentityCode8.setText(
              String.valueOf(identityCodeData.charAt(7)));
          binding.tvIdentityCode9.setText(
              String.valueOf(identityCodeData.charAt(8)));
          // 显示身份码图片
          binding.identityCodeImage.setImageBitmap(identityCodeBitmap);
        });
      } catch (WriterException e) {
        Log.e(LOG_TAG, e.getLocalizedMessage(), e);
      }
    });
  }

  private Bitmap generateBarcode(String barcodeData) throws WriterException {
    BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;
    int barcodeWidth = 800;
    int barcodeHeight = 300;

    BitMatrix barcodeMatrix = new BarcodeEncoder().encode(barcodeData,
        barcodeFormat, barcodeWidth, barcodeHeight);

    int width = barcodeMatrix.getWidth();
    int height = barcodeMatrix.getHeight();
    int[] pixels = new int[width * height];

    for (int y = 0; y < height; y++) {
      int offset = y * width;
      for (int x = 0; x < width; x++) {
        pixels[offset + x] = barcodeMatrix.get(x, y) ? getResources().getColor(
            R.color.black, null) : getResources().getColor(R.color.white, null);
      }
    }

    Bitmap barcodeBitmap = Bitmap.createBitmap(width, height,
        Bitmap.Config.ARGB_8888);
    barcodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

    return barcodeBitmap;
  }

}
