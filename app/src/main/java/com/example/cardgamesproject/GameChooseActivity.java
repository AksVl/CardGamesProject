package com.example.cardgamesproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;

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
        binding.Fool.setOnClickListener(view -> FoolSearch());
        binding.Liar.setOnClickListener(view -> LiarSearch());
        binding.TwentyOne.setOnClickListener(view -> TwentyOneSearch());
    }

    private void TwentyOneSearch() {
        DatabaseReference myRef = database.getReference("TwentyOne");
        FragmentManager fragmentManager = getSupportFragmentManager();
        TwentyOneSearchFragment fragment = (TwentyOneSearchFragment) fragmentManager.findFragmentById(R.id.FragmentsContainer);


    }

    private void LiarSearch() {

        DatabaseReference myRef = database.getReference("Liar");
    }

    private void FoolSearch() {
        DatabaseReference myRef = database.getReference("Fool");
    }
}