package org.seagulls.fatlife;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import org.seagulls.fatlife.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    binding.navView.setItemIconTintList(null);
    binding.navView.setItemTextColor(new ColorStateList(
        new int[][]{
            new int[]{android.R.attr.state_selected},
            new int[]{-android.R.attr.state_selected}
        },
        new int[]{
            getResources().getColor(R.color.black, null),
            getResources().getColor(R.color.gray, null)
        }))
    ;
  }

  @Override
  protected void onStart() {
    super.onStart();
    NavController navController = Navigation.findNavController(this,
        R.id.nav_host_fragment_activity_main);
    NavigationUI.setupWithNavController(binding.navView, navController);
  }

}
