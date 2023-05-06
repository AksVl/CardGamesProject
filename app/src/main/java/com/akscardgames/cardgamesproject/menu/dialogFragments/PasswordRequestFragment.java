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
import com.example.cardgamesproject.databinding.FragmentPasswordRequestBinding;

public class PasswordRequestFragment extends DialogFragment {
    private FragmentPasswordRequestBinding binding;
    public String password;
    private String input;
    private final String gameType;

    public PasswordRequestFragment(String gameType) {
        this.gameType = gameType;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPasswordRequestBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.enter.setOnClickListener(v ->
        {
            input = binding.password.getText().toString();
            if(!input.equals("")){
                if(input.equals(password)){
                    switch (gameType){
                        case "Fool":
                            FoolSearchFragment.connectPrivateRoom();
                        case "TwentyOne":
                            TwentyOneSearchFragment.connectPrivateRoom();
                        case "Liar":
                            LiarSearchFragment.connectPrivateRoom();
                    }
                    dismiss();
                }else{
                    Toast.makeText(getContext(), "password is wrong", Toast.LENGTH_SHORT).show();
                    binding.password.setText("");
                }
            }
            else{
                Toast.makeText(getContext(), "password can't be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }
}