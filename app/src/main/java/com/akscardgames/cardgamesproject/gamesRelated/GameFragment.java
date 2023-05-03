package com.akscardgames.cardgamesproject.gamesRelated;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akscardgames.cardgamesproject.general.AppMethods;
import com.akscardgames.cardgamesproject.general.adapters.GameViewPagerAdapter;
import com.akscardgames.cardgamesproject.menu.GameChooseActivity;
import com.example.cardgamesproject.databinding.FragmentGameBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


public class GameFragment extends Fragment {
    private FragmentGameBinding binding;
    public static ViewPager2 viewPager2;
    public static boolean isInGame = false;
    public static DatabaseReference RoomRef;
    public static String playerName;
    public static ValueEventListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGameBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager2 = binding.viewPager2;
        GameViewPagerAdapter adapter = new GameViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (isInGame) {
            RoomRef.child("_offline").setValue(playerName);
            AppMethods.Disconnect(RoomRef, playerName, listener, ChatFragment.listener);
            GameChooseActivity.fragmentManager.beginTransaction().remove(this).commit();
        } else {
            AppMethods.Disconnect(RoomRef, playerName, listener, ChatFragment.listener);
            GameChooseActivity.fragmentManager.beginTransaction().remove(this).commit();
        }
        ((GameChooseActivity)getActivity()).updateAvgProfit();
    }
}