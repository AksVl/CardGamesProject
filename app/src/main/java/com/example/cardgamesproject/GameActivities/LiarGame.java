package com.example.cardgamesproject.GameActivities;

import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.ActivityFoolGameBinding;
import com.example.cardgamesproject.databinding.ActivityLiarGameBinding;
import com.example.cardgamesproject.databinding.PlayerItemBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LiarGame extends AppCompatActivity {

    ActivityLiarGameBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference PlayerRef;
    DatabaseReference RoomRef;
    ValueEventListener listener;
    String RoomName;
    String playerName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiarGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        Intent inputIntent = getIntent();
        RoomName = inputIntent.getStringExtra("RoomName");
        playerName = inputIntent.getStringExtra("playerName");
        PlayerRef = database.getReference("LiarRooms/"+RoomName+"/"+playerName);
        RoomRef = database.getReference("LiarRooms/"+RoomName);
        ArrayList<String> InRoomPlayers = new ArrayList<>();
        RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ViewGroup.LayoutParams params = new LinearLayout.
                        LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1.0f);
                int size = Integer.parseInt(snapshot.child("_size").getValue().toString());
                for (int i = 0; size - 1 > i; i++) {
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
                            TextView name = binding.playersContainer.getChildAt(getUiPosition(my_pos, pos)).findViewById(R.id.name);
                            TextView status = binding.playersContainer.getChildAt(getUiPosition(my_pos, pos)).findViewById(R.id.status);
                            String gotStatus = snapshot.child(player).child("status").getValue().toString();
                            name.setText(player);
                            status.setText(gotStatus);
                            if(gotStatus.equals("ready")){
                                status.setTextColor(Color.GREEN);
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
    private int getUiPosition(int my_position, int position) {
        int uiPosition;
        if(position>my_position){
            uiPosition = position-my_position-1;
        }
        else{
            uiPosition = my_position- position - 1;
        }
        return uiPosition;
    }
    @Override
    protected void onPause() {
        super.onPause();
        Disconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Disconnect();
    }

    private void Disconnect() {
        RoomRef.removeEventListener(listener);
        RoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.child(playerName).getRef().removeValue();
                if(snapshot.getChildrenCount()==1){
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        finish();
    }
}