package com.example.cardgamesproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.example.cardgamesproject.RoomSearch.FoolSearchFragment;
import com.example.cardgamesproject.RoomSearch.LiarSearchFragment;
import com.example.cardgamesproject.RoomSearch.TwentyOneSearchFragment;
import com.example.cardgamesproject.databinding.ActivityGameChooseBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameChooseActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference myRef;
    boolean running;
    private ActivityGameChooseBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameChooseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /*FragmentManager fragmentManager = getSupportFragmentManager();
        FoolSearchFragment fragment = new FoolSearchFragment();
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .add(R.id.FragmentsContainer,fragment,String.valueOf(false)).commit();
        binding.Fool.setEnabled(false);*/
        binding.Fool.setOnClickListener(view -> FoolSearch());
        binding.Liar.setOnClickListener(view -> LiarSearch());
        binding.TwentyOne.setOnClickListener(view -> TwentyOneSearch());
    }

    private void TwentyOneSearch() {
        binding.TwentyOne.setEnabled(false);
        binding.Fool.setEnabled(true);
        binding.Liar.setEnabled(true);
        DatabaseReference myRef = database.getReference("TwentyOne");
        FragmentManager fragmentManager = getSupportFragmentManager();
        TwentyOneSearchFragment fragment = new TwentyOneSearchFragment();
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.FragmentsContainer,fragment,String.valueOf(false)).commit();
        }

    private void LiarSearch() {
        binding.Liar.setEnabled(false);
        binding.Fool.setEnabled(true);
        binding.TwentyOne.setEnabled(true);
        DatabaseReference myRef = database.getReference("Liar");
        FragmentManager fragmentManager = getSupportFragmentManager();
        LiarSearchFragment fragment = new LiarSearchFragment();
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.FragmentsContainer,fragment,String.valueOf(false)).commit();
    }

    private void FoolSearch() {
        binding.Fool.setEnabled(false);
        binding.TwentyOne.setEnabled(true);
        binding.Liar.setEnabled(true);
        DatabaseReference myRef = database.getReference("Fool");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FoolSearchFragment fragment = new FoolSearchFragment();
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.FragmentsContainer,fragment,String.valueOf(false)).commit();
    }
}