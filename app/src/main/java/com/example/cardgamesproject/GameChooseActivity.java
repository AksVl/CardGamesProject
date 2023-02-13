package com.example.cardgamesproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.cardgamesproject.RoomSearch.FoolSearchFragment;
import com.example.cardgamesproject.RoomSearch.LiarSearchFragment;
import com.example.cardgamesproject.RoomSearch.TwentyOneSearchFragment;
import com.example.cardgamesproject.databinding.ActivityGameChooseBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GameChooseActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference GameRef;
    DatabaseReference RoomRef;
    DatabaseReference searchRef;
    String playerName = "";
    String roomName = "";
    List AvailableRooms;
    private ActivityGameChooseBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameChooseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.Fool.setOnClickListener(view -> FoolSearch());
        binding.Liar.setOnClickListener(view -> LiarSearch());
        binding.TwentyOne.setOnClickListener(view -> TwentyOneSearch());
    }
    private void TwentyOneSearch() {
        //button managing
        binding.TwentyOne.setEnabled(false);
        binding.Fool.setEnabled(true);
        binding.Liar.setEnabled(true);
        //

        //showing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        TwentyOneSearchFragment fragment = new TwentyOneSearchFragment();
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.FragmentsContainer,fragment,String.valueOf(false)).commit();
        }
    private void LiarSearch() {
        //button managing
        binding.Liar.setEnabled(false);
        binding.Fool.setEnabled(true);
        binding.TwentyOne.setEnabled(true);
        //

        //showing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        LiarSearchFragment fragment = new LiarSearchFragment();
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.FragmentsContainer,fragment,String.valueOf(false)).commit();
    }
    private void FoolSearch() {
        //button managing
        binding.Fool.setEnabled(false);
        binding.TwentyOne.setEnabled(true);
        binding.Liar.setEnabled(true);
        //

        //showing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FoolSearchFragment fragment = new FoolSearchFragment();
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.FragmentsContainer,fragment,String.valueOf(false)).commit();

    }
}