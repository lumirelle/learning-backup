package org.seagulls.fatlife.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.seagulls.fatlife.database.OrderHelper;
import org.seagulls.fatlife.databinding.FragmentOrderBinding;

public class OrderFragment extends Fragment {

  private FragmentOrderBinding binding;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentOrderBinding.inflate(inflater, container, false);
    initViewData();
    return binding.getRoot();
  }

  private void initViewData() {
    OrderHelper helper = new OrderHelper(requireContext());
    binding.rvOrder.setAdapter(new OrderAdapter(helper.selectOrders()));
    helper.close();
    binding.rvOrder.setLayoutManager(new LinearLayoutManager(requireContext()));
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

}
