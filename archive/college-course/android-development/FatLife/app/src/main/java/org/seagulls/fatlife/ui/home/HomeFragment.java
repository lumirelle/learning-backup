package org.seagulls.fatlife.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.seagulls.fatlife.databinding.FragmentHomeBinding;
import org.seagulls.fatlife.ui.washing.WashingActivity;

public class HomeFragment extends Fragment {

  private FragmentHomeBinding binding;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    initViewActions();
    return binding.getRoot();
  }

  private void initViewActions() {
    binding.llWashing.setOnClickListener(view -> startActivity(
        new Intent(requireContext(), WashingActivity.class)));
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

}
