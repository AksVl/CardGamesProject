package com.akscardgames.cardgamesproject.menu.dialogFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.akscardgames.cardgamesproject.menu.roomSearchFragments.FoolSearchFragment;
import com.akscardgames.cardgamesproject.menu.roomSearchFragments.LiarSearchFragment;
import com.akscardgames.cardgamesproject.menu.roomSearchFragments.TwentyOneSearchFragment;
import com.example.cardgamesproject.databinding.FragmentCreatePasswordBinding;

public class CreatePasswordFragment extends DialogFragment {
    private FragmentCreatePasswordBinding binding;
    private String input;
    private final String gameType;

    public CreatePasswordFragment(String gameType) {
        this.gameType = gameType;
    }

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
                switch (gameType){
                    case "Fool":
                        FoolSearchFragment.createPrivateRoom(input);
                    case "TwentyOne":
                        TwentyOneSearchFragment.createPrivateRoom(input);
                    case "Liar":
                        LiarSearchFragment.createPrivateRoom(input);
                }
                dismiss();
            }else{
                Toast.makeText(getContext(), "password can't be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }
}