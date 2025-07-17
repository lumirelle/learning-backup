package org.seagulls.cainiao.main.mine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MineViewModel extends ViewModel {

  private final MutableLiveData<String> mText;

  public MineViewModel() {
    mText = new MutableLiveData<>();
    mText.setValue("This is mine fragment");
  }

  public LiveData<String> getText() {
    return mText;
  }

}
