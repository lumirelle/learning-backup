package org.seagulls.cainiao.main.mine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import org.seagulls.cainiao.databinding.FragmentMineBinding;

public class MineFragment extends Fragment {

  private FragmentMineBinding binding;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    MineViewModel mineViewModel =
        new ViewModelProvider(this).get(MineViewModel.class);

    binding = FragmentMineBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    final TextView textView = binding.textMine;
    mineViewModel.getText()
        .observe(getViewLifecycleOwner(), textView::setText);
    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

}
