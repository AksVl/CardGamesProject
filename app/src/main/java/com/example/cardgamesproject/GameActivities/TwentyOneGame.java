package com.example.cardgamesproject.GameActivities;


import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cardgamesproject.AppMethods;
import com.example.cardgamesproject.GameActivities.DialogFragments.DialogBetChooseFragment;
import com.example.cardgamesproject.GameActivities.DialogFragments.DialogSetBankSize;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.ActivityTwentyOneGameBinding;
import com.example.cardgamesproject.databinding.CardLayoutBinding;
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
    private static DataSnapshot SnapshotForBackup;
    private static int bet;
    private int ChoosingPlayerPos;
    private int PlayerBet;
    private static int Bank;
    private static int available = 5000;
    private static boolean bet_flag = true;
    private boolean MainGameLoop = false;
    private boolean HandOutStart = true;
    private boolean OnceCheckFlag = true;
    private boolean OnceStart = true;
    private static boolean LoopEnding = false;
    boolean BankerEndingPermission;
    private static ArrayList<Card> deck = new ArrayList<>();
    private final static int[] size = new int[1];
    private static int my_pos;
    private final static ArrayList<String>[] InRoomPlayers = new ArrayList[]{new ArrayList<>()};
    private static DialogSetBankSize Banker_dialog;
    private static DialogBetChooseFragment dialog;
    private static final Handler handler = new Handler();
    private static boolean IsInGame = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BankerEndingPermission = true;
        super.onCreate(savedInstanceState);
        binding = ActivityTwentyOneGameBinding.inflate(getLayoutInflater());
        Banker_dialog = new DialogSetBankSize();
        dialog = new DialogBetChooseFragment();
        binding.available.setText(String.valueOf(available));
        setContentView(binding.getRoot());
        fm = getSupportFragmentManager();
        Intent inputIntent = getIntent();
        RoomName = inputIntent.getStringExtra("RoomName");
        playerName = inputIntent.getStringExtra("playerName");
        PlayerRef = database.getReference("TwentyOneRooms/" + RoomName + "/" + playerName);
        RoomRef = database.getReference("TwentyOneRooms/" + RoomName);
        binding.ready.setEnabled(false);
        binding.textBankersFirstCard.setVisibility(View.INVISIBLE);
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
                RoomRef.addValueEventListener(listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //nothing
            }
        });
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int readyCount = 0;
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
                    }
                }
                if (snapshot.child(playerName).child("position").exists()) {
                    my_pos = parseInt(snapshot.child(playerName).child("position").getValue().toString());
                    for (String player : InRoomPlayers[0]) {
                        if (!player.equals("_size") && snapshot.child(player).child("position").exists()) {
                            String gotStatus = snapshot.child(player).child("status").getValue().toString();
                            if (!player.equals(playerName)) {
                                pos = parseInt(snapshot.child(player).child("position").getValue().toString());
                                TextView name = binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.name);
                                TextView status = binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.status);
                                name.setText(player);
                                status.setText(gotStatus);
                                status.setTextColor(Color.WHITE);
                                if (gotStatus.equals("ready")) status.setTextColor(Color.GREEN);
                                if (gotStatus.equals("empty")) {
                                    name.setText("none");
                                    binding.ready.setEnabled(false);
                                }
                            }
                            if (gotStatus.equals("ready")) {
                                readyCount++;
                            }
                        }
                    }
                }
                AppMethods.readyCheck("TwentyOne", listener, InGameListener, InRoomPlayers[0], RoomRef, readyCount, size[0], binding, TwentyOneGame.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        InGameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.available.setText(String.valueOf(available));
                binding.ShowBet.setText(String.valueOf(bet));
                int my_pos;
                int pos;
                // region bank reading
                if(snapshot.child("_bank").exists()){
                    Bank = parseInt(snapshot.child("_bank").getValue().toString());
                }
                // endregion bank reading
                // region bet reading
                if(snapshot.child(playerName).child("currentBet").exists() && !LoopEnding){
                    bet = parseInt(snapshot.child(playerName).child("currentBet").getValue().toString());
                } else {
                    bet = 0;
                }
                // endregion bet reading
                // region Counters
                int FinishedCounter = 0;
                int LostCounter = 0;
                for (String player1 : InRoomPlayers[0]) {
                    if (snapshot.child(player1).child("status").exists()
                            && snapshot.child(player1).child("status").getValue().toString().equals("Lost")) {
                        LostCounter++;
                    }
                    if (snapshot.child(player1).child("status").exists()
                            && (snapshot.child(player1).child("status").getValue().toString().equals("passed")
                            || snapshot.child(player1).child("status").getValue().toString().equals("Lost"))) {
                        FinishedCounter++;
                    }
                    if (bankerName != null && snapshot.child(bankerName).child("status").exists()
                            && snapshot.child(bankerName).child("status").getValue().toString().equals("Lost all")) {
                        FinishedCounter = size[0];
                    }
                }
                // endregion Counters
                // region ChoosingPlayer reading
                if (snapshot.child("_ChoosingPlayer").exists()) {
                    ChoosingPlayerPos = parseInt(snapshot.child("_ChoosingPlayer").getValue().toString());
                } else {
                    ChoosingPlayerPos = -1;
                }
                // endregion ChoosingPlayer reading
                // region player's choosing case
                if (MainGameLoop) {
                    if (parseInt(snapshot.child(playerName).child("position").getValue().toString()) != ChoosingPlayerPos
                            && !LoopEnding) {
                        ((Button) binding.buttonBar.getChildAt(2)).setEnabled(false);
                        ((Button) binding.buttonBar.getChildAt(3)).setEnabled(false);
                    } else if (parseInt(snapshot.child(playerName).child("position").getValue().toString()) == ChoosingPlayerPos
                            && !LoopEnding) {
                        if (!snapshot.child(playerName).child("status").getValue().toString().equals("passed")
                                && !snapshot.child(playerName).child("status").getValue().toString().equals("Lost")
                                && !snapshot.child(playerName).child("status").getValue().toString().equals("Lost all")
                                && (snapshot.child(playerName).child("currentBet").exists() || playerName.equals(bankerName))) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (parseInt(snapshot.child(playerName).child("position").getValue().toString()) == ChoosingPlayerPos) {
                                        ((Button) binding.buttonBar.getChildAt(2)).setEnabled(true);
                                        ((Button) binding.buttonBar.getChildAt(3)).setEnabled(true);
                                    }
                                }
                            }, 1000);
                            RoomRef.child(playerName).child("status").setValue("choosing");
                            binding.buttonBar.getChildAt(2).setOnClickListener(view -> {
                                ((Button) binding.buttonBar.getChildAt(2)).setEnabled(false);
                                ((Button) binding.buttonBar.getChildAt(3)).setEnabled(false);
                                RoomRef.child(playerName).child("status").setValue("takes more");
                                binding.buttonBar.getChildAt(2).setOnClickListener(null);
                            });
                            binding.buttonBar.getChildAt(3).setOnClickListener(view -> {
                                ((Button) binding.buttonBar.getChildAt(2)).setEnabled(false);
                                ((Button) binding.buttonBar.getChildAt(3)).setEnabled(false);
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        RoomRef.child(playerName).child("status").setValue("passed");
                                    }
                                }, 50);
                                if (snapshot.child("_ChoosingPlayer").exists()) {
                                    RoomRef.child("_ChoosingPlayer").setValue(String.valueOf(
                                            AppMethods.nextPlayer(size[0], ChoosingPlayerPos)));
                                }
                                binding.buttonBar.getChildAt(3).setOnClickListener(null);
                            });
                        } else if (!LoopEnding) {
                            if (snapshot.child("_ChoosingPlayer").exists()) {
                                RoomRef.child("_ChoosingPlayer").setValue(String.valueOf(
                                        AppMethods.nextPlayer(size[0], ChoosingPlayerPos)));
                            }
                        }
                    }
                } else {
                    ((Button) binding.buttonBar.getChildAt(2)).setEnabled(false);
                    ((Button) binding.buttonBar.getChildAt(3)).setEnabled(false);
                }
                // endregion player's choosing case
                // region banker's shown card
                if (bankerName != null) {
                    binding.shownCard.setImageDrawable(null);
                    if (snapshot.child(bankerName).child("hand").child(String.valueOf(0)).exists()) {
                        String gotShownCard = snapshot.child(bankerName).child("hand").child(String.valueOf(0)).getValue().toString();
                        Card shownCard = AppMethods.CardLink(gotShownCard);
                        if (shownCard != null) {
                            binding.textBankersFirstCard.setVisibility(View.VISIBLE);
                            binding.shownCard.setImageResource(shownCard.img_res);
                        }
                    } else {
                        binding.textBankersFirstCard.setVisibility(View.INVISIBLE);
                        binding.shownCard.setImageDrawable(null);
                    }
                }
                // endregion banker's shown card
                // region MainGameLoop(everyone's role)
                if (MainGameLoop) {
                    boolean AllHaveCurrentBet = true;
                    for (String player1 : InRoomPlayers[0]) {
                        if (!player1.equals("_size") && !player1.equals("_bank") && !player1.equals(bankerName)) {
                            if (!snapshot.child(player1).child("currentBet").exists()) {
                                AllHaveCurrentBet = false;
                                break;
                            }
                        }
                    }
                    if (AllHaveCurrentBet && OnceStart) {
                        if (snapshot.child(bankerName).child("position").exists() && playerName.equals(adminName)) {
                            ChoosingPlayerPos = AppMethods.nextPlayer(size[0],
                                    parseInt(snapshot.child(bankerName).child("position").getValue().toString()));
                            RoomRef.child("_ChoosingPlayer").setValue(ChoosingPlayerPos);
                            OnceStart = false;
                        }
                    }
                }
                // endregion MainGameLoop(everyone's role)
                // region first handing out
                if (HandOutStart) {
                    if (snapshot.child("_bank").exists()) {
                        HandOutStart = false;
                        if (playerName.equals(bankerName)) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    deck.addAll(Arrays.asList(AppMethods.raw_deck));
                                    Collections.shuffle(deck);
                                    for (String player : InRoomPlayers[0]) {
                                        if (!player.equals("_size") && !player.equals("_bank")) {
                                            if (!player.equals(playerName)) {
                                                Card chosen = deck.get(0);
                                                deck.remove(chosen);
                                                RoomRef.child(player).child("hand").child(String.valueOf(0))
                                                        .setValue(chosen.toString());
                                            } else {
                                                Card chosen = deck.get(0);
                                                deck.remove(chosen);
                                                RoomRef.child(player).child("hand").child(String.valueOf(0))
                                                        .setValue(chosen.toString());
                                            }
                                        }
                                    }
                                }
                            }, 50);
                        }
                    }
                }
                // endregion first handing out
                if (snapshot.child(playerName).child("status").exists()) {
                    // region betting
                    if (snapshot.child(playerName).child("hand").getChildrenCount() == 1) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (bet_flag) {
                                    fm.beginTransaction().remove(dialog).commit();
                                    dialog.show(fm.beginTransaction().addToBackStack("dialog"), "dialog");
                                    RoomRef.child(playerName).child("status").setValue("betting");
                                    bet_flag = false;
                                }
                            }
                        }, 1000);
                    }
                    // endregion betting
                }
                String GameStatus = "";
                if (snapshot.child(playerName).child("position").exists()) {
                    my_pos = parseInt(snapshot.child(playerName).child("position").getValue().toString());
                    for (String player : InRoomPlayers[0]) {
                        if (!player.equals("_size") && snapshot.child(player).child("position").exists()
                                && snapshot.child(player).child("role").exists()) {
                            // region MainGameLoop(banker's role)
                            if (MainGameLoop) {
                                if (playerName.equals(bankerName) && snapshot.child(player).child("status").exists()) {
                                    if (snapshot.child(player).child("status").getValue().toString().equals("takes more") && OnceCheckFlag) {
                                        RoomRef.child(player).child("status").setValue("gets more");
                                        OnceCheckFlag = false;
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (snapshot.child(player).child("hand").exists()) {
                                                    int CardNum = (int) snapshot.child(player).child("hand").getChildrenCount();
                                                    Card Chosen = deck.get(0);
                                                    deck.remove(0);
                                                    RoomRef.child(player).child("hand").child(String.valueOf(CardNum)).setValue(Chosen.toString());
                                                    RoomRef.child(player).child("status").setValue("waiting");
                                                    OnceCheckFlag = true;
                                                }
                                            }
                                        }, 800);
                                    }
                                }
                            }
                            // endregion MainGameLoop(banker's role)
                            // region game status update
                            if (snapshot.child("_bank").exists()) {
                                if (!playerName.equals(bankerName)) {
                                    Bank = parseInt(snapshot.child("_bank").getValue().toString());
                                    if (!playerName.equals(bankerName)) {
                                        Bank = parseInt(snapshot.child("_bank").getValue().toString());
                                        GameStatus = "bank : " + Bank + "\n";
                                    }
                                    binding.gameStatus.setText(GameStatus);
                                } else {
                                    binding.ShowBet.setText(String.valueOf(Bank));
                                }
                            }
                            if (snapshot.child(player).child("currentBet").exists()) {
                                bet = parseInt(snapshot.child(player).child("currentBet").getValue().toString());
                                if (!player.equals(playerName)) {
                                    PlayerBet = parseInt(snapshot.child(player).child("currentBet").getValue().toString());
                                    if (LoopEnding) {
                                        if (!playerName.equals(bankerName)) {
                                            if (snapshot.child(player).child("status").exists()) {
                                                if (snapshot.child(player).child("status").getValue().toString().equals("Won")) {
                                                    GameStatus += player + " has won " + PlayerBet + "!\n";
                                                } else if (snapshot.child(player).child("status").getValue().toString().equals("Lost")) {
                                                    GameStatus += player + " has lost " + PlayerBet + "!\n";
                                                }
                                                binding.gameStatus.setText(GameStatus);
                                            }
                                        } else {
                                            if (snapshot.child(player).child("status").exists()) {
                                                if (snapshot.child(player).child("status").getValue().toString().equals("Won")) {
                                                    GameStatus += "You have lost " + PlayerBet + " as " + player + "'s bet" + "!\n";
                                                } else if (snapshot.child(player).child("status").getValue().toString().equals("Lost")) {
                                                    GameStatus += "You have won " + PlayerBet + " as " + player + "'s bet" + "!\n";
                                                }
                                                binding.gameStatus.setText(GameStatus);
                                            }
                                        }
                                    } else {
                                        GameStatus += player + "'s bet : " + PlayerBet + "\n";
                                        binding.gameStatus.setText(GameStatus);
                                    }
                                }
                            }
                            // endregion game status update
                            // region player's status update
                            if (!player.equals(playerName)) {
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
                                if (gotStatus.equals("TwentyOne") || gotStatus.equals("Won")) {
                                    status.setTextColor(Color.GREEN);
                                } else if (gotStatus.equals("Lost") || gotStatus.equals("Lost all")) {
                                    status.setTextColor(Color.RED);
                                }
                            }
                            // endregion player's status update
                        }
                    }
                }
                // region game status extention
                binding.ShowBet.setText(String.valueOf(bet));
                if (LoopEnding) {
                    if (snapshot.child(playerName).child("status").exists()) {
                        if (snapshot.child(playerName).child("status").getValue().toString().equals("Won")) {
                            GameStatus += "You have won " + bet + "!\n";
                        } else if (snapshot.child(playerName).child("status").getValue().toString().equals("Lost")) {
                            GameStatus += "You have lost " + bet + "!\n";
                        }
                        binding.gameStatus.setText(GameStatus);
                    }
                }
                // endregion game status extention
                // region user's hand
                int total = 0;
                binding.hand.removeAllViews();
                ArrayList<String> Hand = new ArrayList<>();
                if (snapshot.child(playerName).child("hand").exists()) {
                    for (int i = 0; i < snapshot.child(playerName).child("hand").getChildrenCount(); i++) {
                        if (snapshot.child(playerName).child("hand").child(String.valueOf(i)).exists()) {
                            Hand.add(snapshot.child(playerName).child("hand").child(String.valueOf(i)).getValue().toString());
                        }
                    }
                } else {
                    binding.hand.removeAllViews();
                }
                for (String got : Hand) {
                    Card card = AppMethods.CardLink(got);
                    total += GetValueOfCard(card);
                    if (card != null) {
                        CardLayoutBinding image = CardLayoutBinding.inflate(getLayoutInflater());
                        image.image.setImageResource(card.img_res);
                        binding.hand.addView(image.getRoot(),
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        binding.hand.invalidate();
                    }
                }
                ((TextView) (binding.buttonBar.getChildAt(1))).setText(String.valueOf(total));
                ((TextView) (binding.buttonBar.getChildAt(1))).setTextColor(Color.WHITE);
                if (total == 21) {
                    ((TextView) (binding.buttonBar.getChildAt(1))).setTextColor(Color.GREEN);
                } else if (total > 21) {
                    ((TextView) (binding.buttonBar.getChildAt(1))).setTextColor(Color.RED);
                    if (!playerName.equals(bankerName)) {
                        RoomRef.child(playerName).child("status").setValue("Lost");
                    } else {
                        RoomRef.child(playerName).child("status").setValue("Lost all");
                    }
                }
                // endregion user's hand
                if (snapshot.child(playerName).child("hand").exists()) {
                    MainGameLoop = true;
                    // region loop ending
                    if (FinishedCounter == size[0]
                            || LostCounter == size[0] - 1) {
                        MainGameLoop = false;
                        LoopEnding = true;
                        if (snapshot.child(bankerName).child("status").exists()
                                && !snapshot.child(bankerName).child("status").getValue().toString().equals("Lost all")) {
                            if (BankerEndingPermission && playerName.equals(bankerName)) {
                                RoomRef.child(bankerName).child("status").setValue("ending");
                            }
                        }
                    }
                    if (LoopEnding && !playerName.equals(bankerName)
                            && snapshot.child(bankerName).child("hand").exists()) {
                        ArrayList<String> BankerHand = new ArrayList<>();
                        for (int i = 0; i < snapshot.child(bankerName).child("hand").getChildrenCount(); i++) {
                            if (snapshot.child(bankerName).child("hand").child(String.valueOf(i)).exists()) {
                                BankerHand.add(snapshot.child(bankerName).child("hand").child(String.valueOf(i)).getValue().toString());
                            }
                        }
                        int bankerScore = 0;
                        for (String got : BankerHand) {
                            Card card = AppMethods.CardLink(got);
                            bankerScore += GetValueOfCard(card);
                        }
                        if (snapshot.child(bankerName).child("status").getValue().toString().equals("Lost all")) {
                            RoomRef.child(playerName).child("status").setValue("Won");
                        } else if (total <= 21 && bankerScore <= 21) {
                            if (total > bankerScore) {
                                RoomRef.child(playerName).child("status").setValue("Won");
                            } else {
                                RoomRef.child(playerName).child("status").setValue("Lost");
                            }
                        }
                    }
                    // endregion loop ending
                    // region back to start
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (LoopEnding) {
                                LoopEnding = false;
                                if (playerName.equals(bankerName)) {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            binding.gameStatus.setText("");
                                            RoomRef.setValue(SnapshotForBackup.getValue());
                                            HandOutStart = true;
                                        }
                                    }, 3000);
                                } else {
                                    bet_flag = true;
                                }
                                if(playerName.equals(adminName)){
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            OnceStart = true;
                                        }
                                    }, 3100);
                                }
                            }
                        }
                    }, 3000);
                    // endregion back to start
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        binding.ready.setOnClickListener(view ->
                SetStatusToReady());
    }

    private int GetValueOfCard(Card card) {
        switch (card.value) {
            case "6":
                return 6;
            case "7":
                return 7;
            case "8":
                return 8;
            case "9":
                return 9;
            case "10":
                return 10;
            case "j":
                return 2;
            case "d":
                return 3;
            case "k":
                return 4;
            case "a":
                return 11;
            default:
                return 0;
        }
    }

    public static void onGameStart(Context context, ActivityTwentyOneGameBinding binding) {
        IsInGame = true;
        UiCreate(context, binding);
        binding.buttonBar.getChildAt(2).setEnabled(false);
        binding.buttonBar.getChildAt(3).setEnabled(false);
        binding.buttonBar.getChildAt(2).setFocusable(false);
        binding.buttonBar.getChildAt(3).setFocusable(false);
        final int[] Bank_choosing = new int[1];
        final int[] Bank_chosen = new int[1];
        ReadingForRoles = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (String player : InRoomPlayers[0]) {
                    if (!snapshot.child(player).equals("_size")) {
                        if (snapshot.child(player).child("position").exists() && !LoopEnding) {
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
                            Banker_dialog.show(fm.beginTransaction().addToBackStack("dialog"), "dialog");
                            RoomRef.child(playerName).child("status").setValue("sets bank");
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
        if (!size.equals("") && parseInt(size) > 99 && parseInt(size) < 5001) {
            Bank = parseInt(size);
            available -= Bank;
            RoomRef.child("_bank").setValue(Bank);
            RoomRef.child("_bank_choose").removeValue();
            fm.beginTransaction().remove(Banker_dialog).commit();
            RoomRef.child(playerName).child("status").setValue("waiting");
            bet_flag = false;
            RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    SnapshotForBackup = snapshot;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //holy shit
                }
            });
        } else {
            Toast.makeText(Banker_dialog.getContext(), "incorrect input", Toast.LENGTH_SHORT).show();
        }
    }

    public static void SetBetSize(String gotBet) {
        if (!gotBet.equals("") && parseInt(gotBet) > 9 && parseInt(gotBet) <= Bank) {
            bet = parseInt(gotBet);
            available -= bet;
            fm.beginTransaction().remove(dialog).commit();
            RoomRef.child(playerName).child("currentBet").setValue(TwentyOneGame.bet);
            RoomRef.child(playerName).child("status").setValue("waiting");
        } else {
            Toast.makeText(dialog.getContext(), "incorrect input", Toast.LENGTH_SHORT).show();
        }
    }

    public static void RecallOfDialogBetChooseFragment(Context context) {
        fm.beginTransaction().remove(dialog).commit();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.show(fm.beginTransaction().addToBackStack("dialog"), "dialog");
                fm.beginTransaction().remove(Banker_dialog).commit();
            }
        }, 50);
        Toast.makeText(context, "you can't skip this dialog", Toast.LENGTH_SHORT).show();
    }

    public static void RecallOfDialogSetBankSize(Context context) {
        fm.beginTransaction().remove(Banker_dialog).commit();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fm.beginTransaction().remove(dialog).commit();
                Banker_dialog.show(fm.beginTransaction().addToBackStack("dialog"), "dialog");
            }
        }, 50);
        Toast.makeText(context, "you can't skip this dialog", Toast.LENGTH_SHORT).show();
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

    }

    private void SetStatusToReady() {
        PlayerRef.child("status").setValue("ready");
        binding.ready.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (IsInGame) {
            AppMethods.Disconnect(RoomRef, playerName, InGameListener);
            finish();
        } else {
            AppMethods.Disconnect(RoomRef, playerName, listener);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (IsInGame) {
            AppMethods.Disconnect(RoomRef, playerName, InGameListener);
            finish();
        } else {
            AppMethods.Disconnect(RoomRef, playerName, listener);
            finish();
        }
    }
}