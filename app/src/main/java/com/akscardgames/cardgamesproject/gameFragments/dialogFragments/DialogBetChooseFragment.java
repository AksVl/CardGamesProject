package com.akscardgames.cardgamesproject.gameFragments.dialogFragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.akscardgames.cardgamesproject.gameFragments.TwentyOneGame;
import com.example.cardgamesproject.databinding.FragmentDialogBetChooseBinding;

public class DialogBetChooseFragment extends DialogFragment {
    private FragmentDialogBetChooseBinding binding;
    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        super.show(manager, tag);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentDialogBetChooseBinding.inflate(getLayoutInflater());
        binding.bet.setOnClickListener(view -> TwentyOneGame.SetBetSize(binding.size.getText().toString()));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        TwentyOneGame.RecallOfDialogBetChooseFragment(getContext());
    }
}