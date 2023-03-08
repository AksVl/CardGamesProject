package com.example.cardgamesproject;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;

import com.example.cardgamesproject.GameActivities.TwentyOneGame;
import com.example.cardgamesproject.databinding.ActivityTwentyOneGameBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AppMethods {
    public static int getUiPosition(int my_position, int position, int size) {
        int uiPosition;
        if (position > my_position) {
            uiPosition = position - my_position - 1;
        } else {
            uiPosition = size - my_position;
        }
        return uiPosition;
    }

    public static void Disconnect(DatabaseReference RoomRef, String playerName, ValueEventListener listener) {
        RoomRef.removeEventListener(listener);
        RoomRef.child(playerName).child("status").setValue("empty");
        RoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.child(playerName).getRef().removeValue();
                if (snapshot.getChildrenCount() == 1) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static int getPosition(DataSnapshot snapshot, int size) {
        int AvailablePosition;
        ArrayList<Integer> PlayersPositions = new ArrayList<>();
        ArrayList<Integer> RoomPositions = new ArrayList<>();
        for (int j = 0; j < size; j++) {
            RoomPositions.add(j + 1);
        }
        ArrayList<String> InRoomPlayers = new ArrayList<>();
        for (DataSnapshot d : snapshot.getChildren()) {
            InRoomPlayers.add(d.getKey());
        }
        for (String player : InRoomPlayers) {
            if (!player.equals("_size")) {
                PlayersPositions.add(Integer.parseInt(snapshot.child(player).child("position").getValue().toString()));
            }
        }

        for (int j = 0; j < RoomPositions.size(); j++) {
            if (!PlayersPositions.contains(RoomPositions.get(j))) {
                return AvailablePosition = RoomPositions.get(j);
            }
        }
        return -1;
    }

    public static void readyCheck(ValueEventListener listener,
                                  ValueEventListener InGameListener,
                                  ArrayList<String> InRoomPlayers,
                                  DatabaseReference RoomRef, int readyCount, int IntentSize,
                                  ActivityTwentyOneGameBinding binding,Context context) {
        if (readyCount == IntentSize) {
            binding.buttonBar.removeAllViews();
            for (String player : InRoomPlayers) {
                if (!player.equals("_size")) {
                    RoomRef.child(player).child("status").setValue("InGame");
                }
            }
            RoomRef.removeEventListener(listener);
            RoomRef.addValueEventListener(InGameListener);
            TwentyOneGame.Start(context,binding);
        }
    }
}
