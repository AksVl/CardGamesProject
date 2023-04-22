package com.akscardgames.cardgamesproject.gameActivities.dialogFragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.akscardgames.cardgamesproject.gameActivities.TwentyOneGame;
import com.example.cardgamesproject.databinding.FragmentDialogSetBankBinding;

public class DialogSetBankSize extends DialogBetChooseFragment{
    private FragmentDialogSetBankBinding binding;
    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        super.show(manager, tag);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentDialogSetBankBinding.inflate(getLayoutInflater());
        binding.set.setOnClickListener(view -> TwentyOneGame.SetBankSize(binding.size.getText().toString()));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        TwentyOneGame.RecallOfDialogSetBankSize(getContext());
    }
}
