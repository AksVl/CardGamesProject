package com.example.cardgamesproject.RoomSearch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.FragmentTwentyOneSearchBinding;

public class TwentyOneSearchFragment extends Fragment {
    FragmentTwentyOneSearchBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTwentyOneSearchBinding.inflate(getLayoutInflater());
        //return inflater.inflate(R.layout.fragment_twenty_one_search, container, false);
        return binding.getRoot();
    }
}