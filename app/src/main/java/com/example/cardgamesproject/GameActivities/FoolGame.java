package com.example.cardgamesproject.GameActivities;

import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ThemedSpinnerAdapter;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cardgamesproject.AppMethods;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.StartActivity;
import com.example.cardgamesproject.databinding.ActivityFoolGameBinding;
import com.example.cardgamesproject.databinding.PlayerItemBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FoolGame extends AppCompatActivity {
    ActivityFoolGameBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference PlayerRef;
    DatabaseReference RoomRef;
    ValueEventListener listener;
    String RoomName;
    String playerName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFoolGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        Intent inputIntent = getIntent();
        RoomName = inputIntent.getStringExtra("RoomName");
        playerName = inputIntent.getStringExtra("playerName");
        PlayerRef = database.getReference("FoolRooms/" + RoomName + "/" + playerName);
        RoomRef = database.getReference("FoolRooms/"+RoomName);
        final int[] size = new int[1];
        ArrayList<String> InRoomPlayers = new ArrayList<>();
        RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ViewGroup.LayoutParams params = new LinearLayout.
                        LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1.0f);
                size[0] = Integer.parseInt(snapshot.child("_size").getValue().toString());
                for (int i = 0; size[0] - 1 > i; i++) {
                    PlayerItemBinding playerItem = PlayerItemBinding.inflate(getLayoutInflater());
                    binding.playersContainer.addView(playerItem.getRoot(),params);
                    binding.playersContainer.invalidate();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //nothing
            }
        });
        listener = RoomRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                InRoomPlayers.clear();
                for (DataSnapshot d : snapshot.getChildren()) {
                    InRoomPlayers.add(d.getKey());
                }
                int my_pos;
                int pos;
                my_pos = parseInt(snapshot.child(playerName).child("position").getValue().toString());
                for (String player : InRoomPlayers) {
                    if (!player.equals(playerName) && !player.equals("_size")) {
                        if(snapshot.child(player).child("position").exists()) {
                            pos = parseInt(snapshot.child(player).child("position").getValue().toString());
                            TextView name = binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.name);
                            TextView status = binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.status);
                            String gotStatus = snapshot.child(player).child("status").getValue().toString();
                            name.setText(player);
                            status.setText(gotStatus);
                            if(gotStatus.equals("ready")){
                                status.setTextColor(Color.GREEN);
                            }
                            else{
                                status.setTextColor(R.color.buttonGrey);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.ready.setOnClickListener(view -> SetStatusToReady());
    }
    private void SetStatusToReady() {
        PlayerRef.child("status").setValue("ready");
        binding.buttonBar.getChildAt(0).setEnabled(false);
    }
    @Override
    protected void onPause() {
        super.onPause();
        AppMethods.Disconnect(RoomRef,playerName,listener);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppMethods.Disconnect(RoomRef,playerName,listener);
        finish();
    }
}