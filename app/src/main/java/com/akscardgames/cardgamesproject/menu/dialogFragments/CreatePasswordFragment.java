package com.akscardgames.cardgamesproject.menu.dialogFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.akscardgames.cardgamesproject.menu.roomSearchFragments.TwentyOneSearchFragment;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.FragmentCreatePasswordBinding;

public class CreatePasswordFragment extends DialogFragment {
    private FragmentCreatePasswordBinding binding;
    private String input;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreatePasswordBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.create.setOnClickListener(v -> {
            input = binding.password.getText().toString();
            if(!input.equals("")){
                TwentyOneSearchFragment.createPrivateRoom(input);
                dismiss();
            }else{
                Toast.makeText(getContext(), "password can't be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }
}