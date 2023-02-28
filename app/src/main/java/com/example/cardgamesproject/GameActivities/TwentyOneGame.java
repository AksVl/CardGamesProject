package com.example.cardgamesproject.GameActivities;


import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.ActivityFoolGameBinding;
import com.example.cardgamesproject.databinding.ActivityTwentyOneGameBinding;
import com.example.cardgamesproject.databinding.PlayerItemBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class TwentyOneGame extends AppCompatActivity {

    ActivityTwentyOneGameBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference PlayerRef;
    String RoomName;
    String playerName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTwentyOneGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent inputIntent = getIntent();
        RoomName = inputIntent.getStringExtra("RoomName");
        playerName = inputIntent.getStringExtra("playerName");
        PlayerRef = database.getReference("TwentyOneRooms/"+RoomName+"/"+playerName);
        //adding players's windows to room layout
        database.getReference("TwentyOneRooms/"+RoomName).addListenerForSingleValueEvent(new ValueEventListener() {
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
        /*Button im_ready = new Button(this);
        im_ready.setText("I'm ready");
        binding.buttonBar.addView(im_ready);
        im_ready.setOnClickListener(view -> SetStatusToReady());*/
    }

    /*private void SetStatusToReady() {
        PlayerRef.child("status").setValue("ready");
        binding.buttonBar.getChildAt(0).setEnabled(false);
    }*/

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
        database.getReference("TwentyOneRooms/"+RoomName).addValueEventListener(new ValueEventListener() {
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