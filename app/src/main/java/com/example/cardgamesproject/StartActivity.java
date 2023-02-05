package com.example.cardgamesproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.cardgamesproject.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class StartActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference playerRef;
    String playerName = "";
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance(
                "https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
        SharedPreferences prefs = getSharedPreferences("PREFS",0);
        playerName = prefs.getString("name","");
        binding.name.setText(playerName);
        if(!playerName.equals("")){
            playerRef = database.getReference("players/" + playerName);
            AddEventListener();
            playerRef.setValue("");
        }

        binding.btnLogin.setOnClickListener(view -> {
            playerName = binding.name.getText().toString();
            if(!playerName.equals("")){
                prefs.edit().putString("name",playerName).apply();
                binding.btnLogin.setEnabled(false);
                binding.btnLogin.setText("Please Wait...");
                playerRef = database.getReference("players/" + playerName);
                AddEventListener();
                playerRef.setValue("");
            }
        });
    }

    private void AddEventListener() {
        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if(!playerName.equals("")){
                     SharedPreferences prefs = getSharedPreferences("PREFS",0);
                     prefs.edit().putString("name",playerName).apply();
                     startActivity(new Intent(StartActivity.this,GameChooseActivity.class));
                     finish();
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.btnLogin.setText("Log in");
                binding.btnLogin.setEnabled(true);
                Toast.makeText(StartActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}