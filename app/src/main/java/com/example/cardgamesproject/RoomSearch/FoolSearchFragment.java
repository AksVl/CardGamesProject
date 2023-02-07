package com.example.cardgamesproject.RoomSearch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.FragmentFoolSearchBinding;

public class FoolSearchFragment extends Fragment {
    private FragmentFoolSearchBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFoolSearchBinding.inflate(getLayoutInflater());
        //return inflater.inflate(R.layout.fragment_fool_search, container, false);
        return binding.getRoot();
    }
}