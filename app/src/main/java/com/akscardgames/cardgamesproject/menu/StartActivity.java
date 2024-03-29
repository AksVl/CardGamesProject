package com.akscardgames.cardgamesproject.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        SharedPreferences prefs = getSharedPreferences("PREFS", 0);
        playerName = prefs.getString("name", "");
        binding.name.setText(playerName);
        if (!playerName.equals("")) {
            playerRef = database.getReference("playerList/" + playerName);
            AddEventListener();
        }

        binding.btnLogin.setOnClickListener(view -> {
            playerName = binding.name.getText().toString();
            if (!playerName.equals("")) {
                prefs.edit().putString("name", playerName).apply();
                binding.btnLogin.setEnabled(false);
                binding.btnLogin.setText(getString(R.string.please_wait));
                playerRef = database.getReference("playerList/" + playerName);
                AddEventListener();
            }
        });
    }

    private void AddEventListener() {
        playerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!playerName.equals("")) {
                    if (!snapshot.exists()) {
                        playerRef.setValue("");
                        SharedPreferences prefs = getSharedPreferences("PREFS", 0);
                        prefs.edit().putString("name", playerName).apply();
                        startActivity(new Intent(StartActivity.this, GameChooseActivity.class));
                    } else{
                        binding.btnLogin.setText(getString(R.string.log_in));
                        binding.btnLogin.setEnabled(true);
                        Toast.makeText(StartActivity.this, getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.btnLogin.setText(getString(R.string.log_in));
                binding.btnLogin.setEnabled(true);
                Toast.makeText(StartActivity.this, getString(R.string.smth_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        binding.btnLogin.setText(getString(R.string.log_in));
        binding.btnLogin.setEnabled(true);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        playerRef.removeValue();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //ignore this
    }
}