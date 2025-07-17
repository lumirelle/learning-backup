package org.seagulls.cainiao.main.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import org.seagulls.cainiao.databinding.FragmentStudentBinding;

public class StudentFragment extends Fragment {

  private FragmentStudentBinding binding;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    StudentModel studentModel =
        new ViewModelProvider(this).get(StudentModel.class);

    binding = FragmentStudentBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    final TextView textView = binding.textStudent;
    studentModel.getText()
        .observe(getViewLifecycleOwner(), textView::setText);
    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

}
