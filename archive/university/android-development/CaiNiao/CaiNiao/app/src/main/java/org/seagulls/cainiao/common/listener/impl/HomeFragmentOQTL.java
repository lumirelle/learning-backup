package org.seagulls.cainiao.common.listener.impl;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import org.seagulls.cainiao.common.util.LogTagGenerator;

/**
 * Home Fragment 中搜索框的查询事件监听器
 */
public class HomeFragmentOQTL implements OnQueryTextListener {

  private static final String LOG_TAG = LogTagGenerator.generate(
      HomeFragmentOQTL.class);

  private final Context context;

  /**
   * 被监听的 SearchView
   */
  private final SearchView searchView;

  public HomeFragmentOQTL(Context context, SearchView searchView) {
    this.context = context;
    this.searchView = searchView;
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    Log.d(LOG_TAG, "用户发起包裹查询：" + query);
    Toast.makeText(context, "您查询了：" + query, Toast.LENGTH_SHORT).show();
    searchView.clearFocus();
    searchView.setQuery("", false);
    return true;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    return false;
  }

}
