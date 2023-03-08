package com.example.cardgamesproject.GameActivities;


import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cardgamesproject.AppMethods;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.ActivityFoolGameBinding;
import com.example.cardgamesproject.databinding.ActivityTwentyOneGameBinding;
import com.example.cardgamesproject.databinding.PlayerItemBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBNode;

import java.util.ArrayList;
import java.util.Objects;

public class TwentyOneGame extends AppCompatActivity {

    ActivityTwentyOneGameBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference PlayerRef;
    DatabaseReference RoomRef;
    String RoomName;
    String playerName;
    ValueEventListener listener;
    ValueEventListener InGameListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTwentyOneGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent inputIntent = getIntent();
        RoomName = inputIntent.getStringExtra("RoomName");
        playerName = inputIntent.getStringExtra("playerName");
        int IntentSize = inputIntent.getIntExtra("size", 0);
        PlayerRef = database.getReference("TwentyOneRooms/" + RoomName + "/" + playerName);
        RoomRef = database.getReference("TwentyOneRooms/" + RoomName);
        binding.ready.setEnabled(false);
        final int[] readyCount = {0};
        final int[] size = new int[1];
        final ArrayList<String>[] InRoomPlayers = new ArrayList[]{new ArrayList<>()};
        RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.playersContainer.removeAllViews();
                ViewGroup.LayoutParams params = new LinearLayout.
                        LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                size[0] = parseInt(snapshot.child("_size").getValue().toString());
                for (int i = 0; size[0] - 1 > i; i++) {
                    PlayerItemBinding playerItem = PlayerItemBinding.inflate(getLayoutInflater());
                    binding.playersContainer.addView(playerItem.getRoot(), params);
                    binding.playersContainer.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //nothing
            }
        });
        final boolean[] ReadyPermissionFlag = {false};
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(playerName).child("status").toString().equals("InGame")) {
                    Start(TwentyOneGame.this, binding);
                    RoomRef.removeEventListener(listener);
                    RoomRef.addValueEventListener(InGameListener);
                }
                InRoomPlayers[0].clear();
                for (DataSnapshot d : snapshot.getChildren()) {
                    InRoomPlayers[0].add(d.getKey());
                }
                int my_pos;
                int pos;
                if (snapshot.getChildrenCount() - 1 == size[0] &&
                        !snapshot.child(playerName).child("status").getValue().toString().equals("ready")) {
                    binding.ready.setEnabled(true);
                } else {
                    binding.ready.setEnabled(false);
                    if(snapshot.child(playerName).child("status").getValue().toString().equals("ready") && snapshot.getChildrenCount() - 1 != size[0]){
                        PlayerRef.child("status").setValue("joined");
                    }
                }
                if (snapshot.child(playerName).child("position").exists()) {
                    my_pos = parseInt(snapshot.child(playerName).child("position").getValue().toString());
                    for (String player : InRoomPlayers[0]) {
                        if (!player.equals(playerName) && !player.equals("_size") && snapshot.child(player).child("position").exists()) {
                            pos = parseInt(snapshot.child(player).child("position").getValue().toString());
                            TextView name = binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.name);
                            TextView status = binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.status);
                            String gotStatus = snapshot.child(player).child("status").getValue().toString();
                            name.setText(player);
                            status.setText(gotStatus);
                            status.setTextColor(Color.WHITE);
                            if (gotStatus.equals("ready")) {
                                readyCount[0]++;
                                status.setTextColor(Color.GREEN);
                            }
                            if (gotStatus.equals("empty")) {
                                name.setText("none");
                                binding.ready.setEnabled(false);
                            }
                        }
                    }
                }
                AppMethods.readyCheck(listener, InGameListener, InRoomPlayers[0], RoomRef, readyCount[0], IntentSize, binding, TwentyOneGame.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        InGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int my_pos;
                int pos;
                if (snapshot.child(playerName).child("position").exists()) {
                    my_pos = parseInt(snapshot.child(playerName).child("position").getValue().toString());
                    for (String player : InRoomPlayers[0]) {
                        if (!player.equals(playerName) && !player.equals("_size") && snapshot.child(player).child("position").exists()) {
                            pos = parseInt(snapshot.child(player).child("position").getValue().toString());
                            TextView name = binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.name);
                            TextView status = binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.status);
                            String gotStatus = snapshot.child(player).child("status").getValue().toString();
                            name.setText(player);
                            status.setText(gotStatus);
                            status.setTextColor(Color.WHITE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        RoomRef.addValueEventListener(listener);
        binding.ready.setOnClickListener(view -> SetStatusToReady());
    }

    public static void Start(Context context, ActivityTwentyOneGameBinding binding) {
        ViewGroup.LayoutParams params = new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        Button OneMore = new Button(context);
        Button Pass = new Button(context);
        OneMore.setText("OneMore");
        Pass.setText("Pass");
        TextView text = new TextView(context);
        text.setText("TOTAL :");
        text.setTextSize(24);
        text.setTextColor(Color.WHITE);
        text.setGravity(Gravity.CENTER);
        TextView total = new TextView(context);
        total.setText("0");
        total.setTextColor(Color.WHITE);
        total.setTextSize(36);
        total.setGravity(Gravity.CENTER);
        binding.buttonBar.addView(text, 0, params);
        binding.buttonBar.addView(total, 1, params);
        binding.buttonBar.addView(OneMore, 2, params);
        binding.buttonBar.addView(Pass, 3, params);
        binding.buttonBar.getChildAt(2).setEnabled(false);
        binding.buttonBar.getChildAt(3).setEnabled(false);

    }

    private void SetStatusToReady() {
        PlayerRef.child("status").setValue("ready");
        binding.ready.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppMethods.Disconnect(RoomRef, playerName, listener);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppMethods.Disconnect(RoomRef, playerName, listener);
        finish();
    }
}