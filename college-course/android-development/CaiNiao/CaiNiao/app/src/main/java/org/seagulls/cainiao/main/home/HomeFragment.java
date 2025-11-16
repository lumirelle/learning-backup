package org.seagulls.cainiao.main.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.seagulls.cainiao.common.bean.Parcel;
import org.seagulls.cainiao.common.listener.impl.HomeFragmentOCL;
import org.seagulls.cainiao.common.listener.impl.HomeFragmentOQTL;
import org.seagulls.cainiao.common.server.ServerRequester;
import org.seagulls.cainiao.common.util.LogTagGenerator;
import org.seagulls.cainiao.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

  private static final String LOG_TAG = LogTagGenerator.generate(
      HomeFragment.class);

  private FragmentHomeBinding binding;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(LOG_TAG, "创建首页 Fragment");
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    initViewsWithData();
    setListeners();
    return binding.getRoot();
  }

  @Override
  public void onResume() {
    super.onResume();
    // 回到 Home Fragment 时刷新数据，保证用户寄件之后即使刷新数据
    initViewsWithData();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Log.d(LOG_TAG, "销毁首页 Fragment");
    binding = null;
  }

  private void initViewsWithData() {
    Log.d(LOG_TAG, "加载数据并初始化界面");

    SharedPreferences sp = requireActivity().getSharedPreferences("user",
        Context.MODE_PRIVATE);
    String userName = sp.getString("userName", "");
    Log.d(LOG_TAG, "数据：用户名称 = " + userName + "，来源 = SharedPreferences");

    // 从服务器加载该用户的全部包裹数据
    ServerRequester serverRequester = new ServerRequester(requireActivity());
    serverRequester.queryParcelsByUserName(userName,
        parcels -> requireActivity().runOnUiThread(() -> {
          Log.d(LOG_TAG, "数据：包裹列表 = " + parcels + "，来源 = 服务器");
          initViews(parcels);
        }));
  }

  private void initViews(List<Parcel> parcels) {
    Log.d(LOG_TAG, "开始初始化界面");
    RecyclerView rvParcels = binding.rvParcels;
    rvParcels.setLayoutManager(new LinearLayoutManager(getContext()));
    ParcelAdapter parcelAdapter = new ParcelAdapter(parcels);
    rvParcels.setAdapter(parcelAdapter);
  }

  private void setListeners() {
    Log.d(LOG_TAG, "设置监听器");

    OnClickListener listenerGoTo = new HomeFragmentOCL(getContext());
    binding.tvLocation.setOnClickListener(listenerGoTo);
    binding.tvParcelPickup.setOnClickListener(listenerGoTo);
    binding.tvParcelDispatch.setOnClickListener(listenerGoTo);
    binding.tvIdentityCode.setOnClickListener(listenerGoTo);

    SearchView svParcel = binding.svParcel;
    svParcel.setOnQueryTextListener(
        new HomeFragmentOQTL(getContext(), svParcel));
  }

}
