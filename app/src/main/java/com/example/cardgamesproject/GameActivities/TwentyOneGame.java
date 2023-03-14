package com.example.cardgamesproject.GameActivities;


import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cardgamesproject.AppMethods;
import com.example.cardgamesproject.GameActivities.DialogFragments.DialogSetBankSize;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.ActivityTwentyOneGameBinding;
import com.example.cardgamesproject.databinding.FragmentDialogBetChooseBinding;
import com.example.cardgamesproject.databinding.FragmentDialogSetBankBinding;
import com.example.cardgamesproject.databinding.PlayerItemBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class TwentyOneGame extends AppCompatActivity {
    private ActivityTwentyOneGameBinding binding;
    private static FragmentManager fm;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference PlayerRef;
    private static DatabaseReference RoomRef;
    private static String RoomName;
    private static String playerName;
    private static String adminName;
    private static String bankerName;
    private ValueEventListener listener;
    private ValueEventListener InGameListener;
    private static ValueEventListener ReadingForRoles;
    private static ValueEventListener HandingOut;
    private static int Bank_size;
    static ArrayList<Card> deck = new ArrayList<>();
    private final static int[] size = new int[1];
    private static int my_pos;
    private final static ArrayList<String>[] InRoomPlayers = new ArrayList[]{new ArrayList<>()};
    private final static int[] readyCount = {0};
    private static DialogSetBankSize dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTwentyOneGameBinding.inflate(getLayoutInflater());
        dialog = new DialogSetBankSize();
        setContentView(binding.getRoot());
        fm = getSupportFragmentManager();
        deck.addAll(Arrays.asList(AppMethods.TwentyOne_raw_deck));
        Intent inputIntent = getIntent();
        RoomName = inputIntent.getStringExtra("RoomName");
        playerName = inputIntent.getStringExtra("playerName");
        int IntentSize = inputIntent.getIntExtra("size", 0);
        PlayerRef = database.getReference("TwentyOneRooms/" + RoomName + "/" + playerName);
        RoomRef = database.getReference("TwentyOneRooms/" + RoomName);
        binding.ready.setEnabled(false);
        RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(playerName).child("status").toString().equals("InGame")) {
                    onGameStart(TwentyOneGame.this, binding);
                    RoomRef.removeEventListener(listener);
                    RoomRef.addValueEventListener(InGameListener);
                }
                InRoomPlayers[0].clear();
                for (DataSnapshot d : snapshot.getChildren()) {
                    InRoomPlayers[0].add(d.getKey());
                }
                int pos;
                if (snapshot.getChildrenCount() - 1 == size[0] &&
                        !snapshot.child(playerName).child("status").getValue().toString().equals("ready")) {
                    binding.ready.setEnabled(true);
                } else {
                    binding.ready.setEnabled(false);
                    if (snapshot.child(playerName).child("status").getValue().toString().equals("ready") && snapshot.getChildrenCount() - 1 != size[0]) {
                        PlayerRef.child("status").setValue("joined");
                        readyCount[0] = 0;
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
                        if (!player.equals(playerName) && !player.equals("_size") && snapshot.child(player).child("position").exists()
                                && snapshot.child(player).child("role").exists()) {
                            //banker's shown card
                            if (snapshot.child(player).child("role").getValue().toString().equals("banker")
                                    && snapshot.child(player).child("hand").child("shown").exists()) {
                                String gotShownCard = snapshot.child(player).child("hand").child("shown").getValue().toString();
                                Card shownCard = AppMethods.CardLink(gotShownCard);
                                binding.shownCard.setImageResource(shownCard.img_res);
                            }

                            //game status update
                            if (snapshot.child("_bank").exists()) {
                                if(!playerName.equals(bankerName))
                                    binding.gameStatus.setText("bank : " + snapshot.child("_bank").getValue().toString() + "\n");
                                else{
                                    binding.ShowBet.setText(snapshot.child("_bank").getValue().toString());
                                }
                                //binding.gameStatus.append... other players' bets
                            }

                            //player's status update
                            pos = parseInt(snapshot.child(player).child("position").getValue().toString());
                            TextView status = binding.playersContainer
                                    .getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.status);
                            TextView CardCount = binding.playersContainer
                                    .getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.CardCount);
                            String gotCount = String.valueOf(snapshot.child(player).child("hand").getChildrenCount());
                            String gotStatus = snapshot.child(player).child("status").getValue().toString();
                            CardCount.setText(gotCount);
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

    public static void onGameStart(Context context, ActivityTwentyOneGameBinding binding) {
        UiCreate(context, binding);
        final int[] Bank_choosing = new int[1];
        final int[] Bank_chosen = new int[1];
        Handler handler = new Handler();
        HandingOut = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Collections.shuffle(deck);
                for (String player : InRoomPlayers[0]) {
                    if (!player.equals("_size")) {
                        if (!player.equals(playerName)) {
                            Card chosen = deck.get(0);
                            deck.remove(chosen);
                            int handCount = (int) snapshot.child(player).child("hand").getChildrenCount();
                            RoomRef.child(player).child("hand").child(String.valueOf(handCount))
                                    .setValue(chosen.toString());
                        } else {
                            Card chosen = deck.get(0);
                            deck.remove(chosen);
                            RoomRef.child(player).child("hand").child("shown")
                                    .setValue(chosen.toString());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ReadingForRoles = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (String player : InRoomPlayers[0]) {
                    if (!snapshot.child(player).equals("_size")) {
                        if (snapshot.child(player).child("position").exists()) {
                            if (parseInt(snapshot.child(player).child("position").getValue().toString()) == Bank_chosen[0]
                                    && !player.equals(playerName)) {
                                int pos = parseInt(snapshot.child(player).child("position").getValue().toString());
                                binding.message.setText(player + " is a banker");
                                bankerName = player;
                                if (playerName.equals(adminName)) {
                                    RoomRef.child(player).child("role").setValue("banker");
                                    RoomRef.child(player).child("status").setValue("sets a bank");
                                }
                                ImageView crown = binding.playersContainer.getChildAt(
                                        AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.crown);
                                crown.setImageResource(R.drawable.crown);
                            } else if (parseInt(snapshot.child(player).child("position").getValue().toString()) == Bank_chosen[0]
                                    && player.equals(playerName)) {
                                binding.betText.setText("bank:");
                                binding.message.setText("You are a banker");
                                bankerName = player;
                                if (playerName.equals(adminName)) {
                                    RoomRef.child(player).child("role").setValue("banker");
                                    RoomRef.child(player).child("status").setValue("sets a bank");
                                }
                                ImageView my_crown = new ImageView(context);
                                ViewGroup.LayoutParams params = new LinearLayout.
                                        LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                                binding.buttonBar.addView(my_crown, 4, params);
                                ImageView crown = (ImageView) binding.buttonBar.getChildAt(4);
                                crown.setImageResource(R.drawable.crown);
                            } else {
                                if (playerName.equals(adminName)) {
                                    RoomRef.child(player).child("role").setValue("player");
                                }
                            }
                        }
                    }
                }
                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                fadeOut.setDuration(2400);
                fadeOut.setFillAfter(true);
                binding.message.startAnimation(fadeOut);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (playerName.equals(bankerName)) {
                            dialog.show(fm.beginTransaction().addToBackStack("dialog"), "dialog");
                        }
                    }
                }, 800);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(playerName).child("position").exists() &&
                        parseInt(snapshot.child(playerName).child("position").getValue().toString()) == 1) {
                    adminName = playerName;
                    Bank_choosing[0] = new Random().ints(1, size[0] + 1).findAny().getAsInt();
                    RoomRef.child("_bank_choose").setValue(Bank_choosing[0]);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //creator of room sets a banker by random
        RoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("_bank_choose").exists()) {
                    Bank_chosen[0] = parseInt(snapshot.child("_bank_choose").getValue().toString());
                    RoomRef.addListenerForSingleValueEvent(ReadingForRoles);
                    RoomRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AppMethods.Disconnect(RoomRef, playerName, null);
            }
        });
        //triggers reading if bank_choose exists in database
    }

    public static void SetBankSize(String size) {
        if (!size.equals("")) {
            Bank_size = parseInt(size);
            RoomRef.child("_bank").setValue(Bank_size);
            RoomRef.child("_bank_choose").removeValue();
            fm.beginTransaction().remove(dialog).commit();
            RoomRef.child(playerName).child("status").setValue("waiting");
            RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child("_bank_start_size").exists()) {
                        Bank_size = parseInt(snapshot.child("_bank").getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Toast.makeText(dialog.getContext(), "input a size", Toast.LENGTH_SHORT).show();
        }
    }

    private static void UiCreate(Context context, ActivityTwentyOneGameBinding binding) {
        ViewGroup.LayoutParams params = new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        Button OneMore = new Button(context);
        Button Pass = new Button(context);
        OneMore.setText("More");
        Pass.setText("Pass");
        TextView text = new TextView(context);
        text.setText("TOTAL :");
        text.setTextSize(18);
        text.setTextColor(Color.WHITE);
        text.setGravity(Gravity.CENTER);
        TextView total = new TextView(context);
        total.setText("0");
        total.setTextColor(Color.WHITE);
        total.setTextSize(24);
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
        readyCount[0]++;
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