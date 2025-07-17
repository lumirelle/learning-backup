package org.seagulls.cainiao.main.student;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StudentModel extends ViewModel {

  private final MutableLiveData<String> mText;

  public StudentModel() {
    mText = new MutableLiveData<>();
    mText.setValue("This is student only fragment");
  }

  public LiveData<String> getText() {
    return mText;
  }

}
