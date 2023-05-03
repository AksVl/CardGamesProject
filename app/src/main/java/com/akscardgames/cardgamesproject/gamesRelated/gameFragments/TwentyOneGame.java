package com.akscardgames.cardgamesproject.gamesRelated.gameFragments;


import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akscardgames.cardgamesproject.gamesRelated.ChatFragment;
import com.akscardgames.cardgamesproject.gamesRelated.GameFragment;
import com.akscardgames.cardgamesproject.gamesRelated.ResultData;
import com.akscardgames.cardgamesproject.general.AppMethods;
import com.akscardgames.cardgamesproject.general.Card;
import com.akscardgames.cardgamesproject.gamesRelated.dialogFragments.DialogBetChooseFragment;
import com.akscardgames.cardgamesproject.gamesRelated.dialogFragments.DialogSetBankSize;
import com.akscardgames.cardgamesproject.general.adapters.EndResultRecyclerViewAdapter;
import com.akscardgames.cardgamesproject.menu.GameChooseActivity;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.CardLayoutBinding;
import com.example.cardgamesproject.databinding.FragmentTwentyOneGameBinding;
import com.example.cardgamesproject.databinding.PlayerItemBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;


public class TwentyOneGame extends Fragment {
    //region variables
    private static FragmentTwentyOneGameBinding binding;
    private static FragmentManager fm;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
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
    private static int BackUpBank;
    private static int available = 5000;
    private static boolean bet_flag = true;
    private boolean MainGameLoop = false;
    private boolean HandOutStart = true;
    private boolean OnceCheckFlag = true;
    private boolean OnceStart = true;
    private static boolean LoopEnding = false;
    boolean BankerEndingPermission = false;
    private final boolean[] Return = {false};
    private static boolean EndGame = false;
    private static boolean StartBankReading = true;
    private static boolean KnockKnock = false;
    private boolean OnceEndBank = true;
    private boolean alreadyCounted = false;
    private static final ArrayList<Card> deck = new ArrayList<>();
    private final static int[] size = new int[1];
    private static int my_pos;
    private final static ArrayList<String>[] InRoomPlayers = new ArrayList[]{new ArrayList<>()};
    private static DialogSetBankSize Banker_dialog;
    private static DialogBetChooseFragment dialog;
    private static final Handler handler = new Handler();
    private static String status = "";
    private static int StartBank;
    private static int AvailableBuff;

    public static String roomNameBuff;
    public static String playerNameBuff;
    public static boolean chatUpdatePermission = false;
    private boolean OnceAddProfit = false;

    //endregion variables


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTwentyOneGameBinding.inflate(getLayoutInflater());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        //region init
        adminName = null;
        bankerName = null;
        SnapshotForBackup = null;
        bet = 0;
        ChoosingPlayerPos = 0;
        PlayerBet = 0;
        Bank = 0;
        BackUpBank = 0;
        available = 5000;
        bet_flag = true;
        MainGameLoop = false;
        HandOutStart = true;
        OnceCheckFlag = true;
        OnceStart = true;
        OnceAddProfit = false;
        alreadyCounted = false;
        LoopEnding = false;
        BankerEndingPermission = false;
        Return[0] = false;
        EndGame = false;
        StartBankReading = true;
        KnockKnock = false;
        OnceEndBank = true;
        ArrayList<Card> deck = new ArrayList<>();
        size[0] = 0;
        my_pos = 0;
        ArrayList<String>[] InRoomPlayers = new ArrayList[]{new ArrayList<>()};
        Handler handler = new Handler();
        status = "";
        StartBank = 0;
        AvailableBuff = 0;
        chatUpdatePermission = false;
        //endregion init
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, null);
        GameFragment.viewPager2.setCurrentItem(1);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Banker_dialog = new DialogSetBankSize();
                dialog = new DialogBetChooseFragment();
                binding.available.setText(String.valueOf(available));
                fm = getParentFragmentManager();
                //Intent inputIntent = getIntent();
                RoomName = roomNameBuff;
                SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", 0);
                playerName = prefs.getString("name", "");
                RoomRef = database.getReference("TwentyOneRooms/" + RoomName);
                binding.ready.setEnabled(false);
                binding.textBankersFirstCard.setVisibility(View.INVISIBLE);
                binding.betText.setVisibility(View.INVISIBLE);
                final boolean[] OnceAnimated = {true};
                final long[] EndGameDelay = {3000};
                binding.ready.setOnClickListener(v ->
                        setStatusToReady());
                // endregion onCreate init
                RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.playersContainer.removeAllViews();
                        ViewGroup.LayoutParams params = new LinearLayout.
                                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                        if (snapshot.child("_size").exists()) {
                            size[0] = parseInt(snapshot.child("_size").getValue().toString());
                            for (int i = 0; size[0] - 1 > i; i++) {
                                PlayerItemBinding playerItem = PlayerItemBinding.inflate(getLayoutInflater());
                                binding.playersContainer.addView(playerItem.getRoot(), params);
                                binding.playersContainer.invalidate();
                            }
                        }
                        RoomRef.addValueEventListener(listener);
                        GameFragment.listener = listener;
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
                        for (int i = 0; i < binding.playersContainer.getChildCount(); i++) {
                            TextView name = binding.playersContainer.getChildAt(i).findViewById(R.id.name);
                            TextView status = binding.playersContainer.getChildAt(i).findViewById(R.id.status);
                            status.setText("empty");
                            name.setText("none");
                        }
                        InRoomPlayers[0].clear();
                        for (DataSnapshot d : snapshot.getChildren()) {
                            if (!Objects.equals(d.getKey(), "_size")
                                    && !Objects.equals(d.getKey(), "_messages")
                                    && !Objects.equals(d.getKey(), "_access")) {
                                InRoomPlayers[0].add(d.getKey());
                            }
                        }
                        int pos;
                        int noPlayersCount = 2;
                        if (snapshot.child("_messages").exists()) {
                            noPlayersCount = 3;
                        }
                        if (snapshot.getChildrenCount() - noPlayersCount == size[0] &&
                                snapshot.child(playerName).child("status").exists() &&
                                !snapshot.child(playerName).child("status").getValue().toString().equals("ready")) {
                            binding.ready.setEnabled(true);
                        } else {
                            binding.ready.setEnabled(false);
                            if (snapshot.child(playerName).child("status").exists()
                                    && snapshot.child(playerName).child("status").getValue().toString().equals("ready")
                                    && snapshot.getChildrenCount() - noPlayersCount != size[0]) {
                                RoomRef.child(playerName).child("status").setValue("joined");
                            }
                        }
                        if (snapshot.child(playerName).child("position").exists()) {
                            my_pos = parseInt(snapshot.child(playerName).child("position").getValue().toString());
                            for (String player : InRoomPlayers[0]) {
                                if (!snapshot.child(player).equals("_size") && !player.equals("_access") && !player.equals("_messages")
                                        && snapshot.child(player).child("position").exists()) {
                                    String gotStatus = snapshot.child(player).child("status").getValue().toString();
                                    if (!player.equals(playerName)) {
                                        pos = parseInt(snapshot.child(player).child("position").getValue().toString());
                                        if (binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])) != null) {
                                            TextView name = binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.name);
                                            TextView status = binding.playersContainer.getChildAt(AppMethods.getUiPosition(my_pos, pos, size[0])).findViewById(R.id.status);
                                            name.setText(player);
                                            status.setText(gotStatus);
                                            status.setTextColor(Color.WHITE);
                                            if (gotStatus.equals("ready")) {
                                                status.setTextColor(Color.GREEN);
                                            }
                                        }
                                    }
                                    if (gotStatus.equals("ready")) {
                                        readyCount++;
                                    }
                                }
                            }
                        }
                        AppMethods.twentyOneReadyCheck(listener, InGameListener,
                                InRoomPlayers[0], RoomRef, readyCount, size[0],
                                binding, getContext(), getActivity().getWindowManager());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                InGameListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.available.setText(String.valueOf(available));
                        if (!playerName.equals(bankerName)) {
                            binding.ShowBet.setText(String.valueOf(bet));
                        }
                        binding.betText.setVisibility(View.VISIBLE);
                        String offlinePlayerName = "";
                        int my_pos;
                        int pos;
                        //region if someone offline
                        if (snapshot.child("_offline").exists()) {
                            offlinePlayerName = snapshot.child("_offline").getValue().toString();
                            available = AvailableBuff;
                            binding.available.setText(String.valueOf(available));
                            RoomRef.removeEventListener(InGameListener);
                            binding.message.setText(offlinePlayerName + " went offline, all bets were returned");
                            binding.message.setVisibility(View.VISIBLE);
                            uiDestroy(getContext(), binding);
                        }
                        //endregion if someone offline
                        // region bank reading
                        if (snapshot.child("_bank").exists() && !snapshot.child("_offline").exists()) {
                            Bank = parseInt(snapshot.child("_bank").getValue().toString());
                            if (StartBankReading) {
                                StartBank = Bank;
                                StartBankReading = false;
                            }
                            if (Bank == 0) {
                                RoomRef.child(playerName).child("status").setValue("ended");
                                alreadyCounted = true;
                                EndGame = true;
                                RoomRef.child(playerName).child("hand").removeValue();
                                HandOutStart = false;
                                bet_flag = false;
                                EndGameDelay[0] = 0;
                                binding.message.setText("Bank has no money!");
                                binding.message.setVisibility(View.VISIBLE);
                            } else if (StartBank * 3 <= Bank) {
                                KnockKnock = true;
                                if (OnceAnimated[0]) {
                                    OnceAnimated[0] = false;
                                    binding.message.setText("knock - knock!");
                                    binding.message.setVisibility(View.VISIBLE);
                                    AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                                    fadeOut.setDuration(2400);
                                    binding.message.startAnimation(fadeOut);
                                    binding.message.setVisibility(View.INVISIBLE);
                                }
                            }
                            if (LoopEnding) {
                                BackUpBank = parseInt(snapshot.child("_bank").getValue().toString());
                            }
                        }
                        // endregion bank reading
                        // region Counters
                        int FinishedCounter = 0;
                        int LostCounter = 0;
                        int TwentyOnesCounter = 0;
                        for (String player1 : InRoomPlayers[0]) {
                            if (snapshot.child(player1).child("status").exists()
                                    && snapshot.child(player1).child("status").getValue().toString().equals("Lost")) {
                                LostCounter++;
                            }
                            if (snapshot.child(player1).child("status").exists()
                                    && snapshot.child(player1).child("status").getValue().toString().equals("TwentyOne")) {
                                TwentyOnesCounter++;
                            }
                            if (snapshot.child(player1).child("status").exists()
                                    && (snapshot.child(player1).child("status").getValue().toString().equals("passed")
                                    || snapshot.child(player1).child("status").getValue().toString().equals("Lost")
                                    || snapshot.child(player1).child("status").getValue().toString().equals("TwentyOne"))) {
                                FinishedCounter++;
                            }
                            if (bankerName != null && snapshot.child(bankerName).child("status").exists()
                                    && (snapshot.child(bankerName).child("status").getValue().toString().equals("Lost all")
                                    || snapshot.child(bankerName).child("status").getValue().toString().equals("Won all"))) {
                                FinishedCounter = size[0];
                            }
                        }
                        if (snapshot.child(playerName).child("hand").exists()) {
                            MainGameLoop = true;
                        }
                        if (FinishedCounter == size[0]
                                || LostCounter == size[0] - 1
                                || TwentyOnesCounter == size[0] - 1 && !EndGame) {
                            OnceCheckFlag = true;
                            MainGameLoop = false;
                            LoopEnding = true;
                            if (snapshot.child(bankerName).child("status").exists()
                                    && !snapshot.child(bankerName).child("status").getValue().toString().equals("Lost all")
                                    && !snapshot.child(bankerName).child("status").getValue().toString().equals("Won all")) {
                                if (BankerEndingPermission && playerName.equals(bankerName)) {
                                    RoomRef.child(bankerName).child("status").setValue("ending");
                                }
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
                        if (binding.buttonBar.getChildAt(2) != null && binding.buttonBar.getChildAt(3) != null) {
                            if (MainGameLoop) {
                                if (snapshot.child(playerName).child("position").exists()
                                        && parseInt(snapshot.child(playerName).child("position").getValue().toString()) != ChoosingPlayerPos
                                        && !LoopEnding) {
                                    ((Button) binding.buttonBar.getChildAt(2)).setEnabled(false);
                                    ((Button) binding.buttonBar.getChildAt(3)).setEnabled(false);
                                } else if (snapshot.child(playerName).child("position").exists()
                                        && parseInt(snapshot.child(playerName).child("position").getValue().toString()) == ChoosingPlayerPos
                                        && !LoopEnding) {
                                    if (!snapshot.child(playerName).child("status").getValue().toString().equals("passed")
                                            && !snapshot.child(playerName).child("status").getValue().toString().equals("Lost")
                                            && !snapshot.child(playerName).child("status").getValue().toString().equals("Lost all")
                                            && !snapshot.child(playerName).child("status").getValue().toString().equals("TwentyOne")
                                            && !snapshot.child(playerName).child("status").getValue().toString().equals("Won all")
                                            && (snapshot.child(playerName).child("currentBet").exists() || playerName.equals(bankerName))) {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (parseInt(snapshot.child(playerName).child("position").getValue().toString()) == ChoosingPlayerPos
                                                        && !LoopEnding) {
                                                    if (binding.buttonBar.getChildAt(2) != null && binding.buttonBar.getChildAt(3) != null) {
                                                        ((Button) binding.buttonBar.getChildAt(2)).setEnabled(true);
                                                        ((Button) binding.buttonBar.getChildAt(3)).setEnabled(true);
                                                    }

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
                                    } else {
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
                        }
                        // endregion player's choosing case
                        // region banker's shown card
                        if (bankerName != null && !snapshot.child("_offline").exists()) {
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
                        if (MainGameLoop && playerName.equals(adminName)) {
                            boolean AllHaveCurrentBet = true;
                            for (String player1 : InRoomPlayers[0]) {
                                if (!player1.equals("_size") && !player1.equals("_bank") && !player1.equals("_messages") && !player1.equals("_access") && !player1.equals(bankerName)) {
                                    if (!snapshot.child(player1).child("currentBet").exists()) {
                                        AllHaveCurrentBet = false;
                                        break;
                                    }
                                }
                            }
                            if (AllHaveCurrentBet && OnceStart) {
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ChoosingPlayerPos = AppMethods.nextPlayer(size[0],
                                                parseInt(snapshot.child(bankerName).child("position").getValue().toString()));
                                        RoomRef.child("_ChoosingPlayer").setValue(ChoosingPlayerPos);
                                        OnceStart = false;
                                    }
                                });
                                thread.start();
                            }
                        }
                        // endregion MainGameLoop(everyone's role)
                        // region first handing out
                        if (HandOutStart) {
                            OnceAddProfit = true;
                            chatUpdatePermission = false;
                            if (snapshot.child("_bank").exists()) {
                                HandOutStart = false;
                                if (playerName.equals(bankerName)) {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            deck.clear();
                                            deck.addAll(Arrays.asList(AppMethods.raw_deck));
                                            Collections.shuffle(deck);
                                            for (String player : InRoomPlayers[0]) {
                                                if (!player.equals("_access") && !player.equals("_size") && !player.equals("_messages") && !player.equals("_bank")) {
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
                                            MainGameLoop = true;
                                            OnceCheckFlag = true;
                                        }
                                    }, 50);
                                }
                            }
                        }
                        // endregion first handing out
                        if (snapshot.child(playerName).child("status").exists()) {
                            // region betting
                            if (snapshot.child(playerName).child("hand").getChildrenCount() == 1
                                    && !playerName.equals(bankerName)) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bet_flag && !EndGame) {
                                            bet = 0;
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
                            my_pos = parseInt(snapshot.child(playerName)
                                    .child("position").getValue().toString());
                            int AddedCounter = 0;
                            for (String player : InRoomPlayers[0]) {
                                if (!player.equals("_size") && !player.equals("_access") && !player.equals("_messages")
                                        && snapshot.child(player).child("position").exists()
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
                                    // region game status update
                                    if (snapshot.child("_bank").exists()) {
                                        if (!playerName.equals(bankerName)) {
                                            Bank = parseInt(snapshot.child("_bank").getValue().toString());
                                            if (!playerName.equals(bankerName)) {
                                                Bank = parseInt(snapshot.child("_bank").getValue().toString());
                                                GameStatus = "bank : " + Bank + "\n";
                                            }
                                            binding.gameStatus.setText(GameStatus);
                                        }
                                    }
                                    if (!player.equals(playerName)) {
                                        if (snapshot.child(player).child("currentBet").exists()) {
                                            PlayerBet = parseInt(snapshot.child(player).child("currentBet").getValue().toString());
                                            if (!LoopEnding) {
                                                GameStatus += player + "'s bet : " + PlayerBet + "\n";
                                                binding.gameStatus.setText(GameStatus);
                                            }
                                            if (LoopEnding) {
                                                if (!playerName.equals(bankerName)) {
                                                    if (snapshot.child(player).child("status").exists()) {
                                                        if (snapshot.child(player).child("status").getValue().toString().equals("Won")
                                                                || snapshot.child(player).child("status").getValue().toString().equals("TwentyOne")) {
                                                            GameStatus += player + " has won " + PlayerBet + "!\n";
                                                        } else if (snapshot.child(player).child("status").getValue().toString().equals("Lost")) {
                                                            GameStatus += player + " has lost " + PlayerBet + "!\n";
                                                        }
                                                        binding.gameStatus.setText(GameStatus);
                                                    }
                                                } else {
                                                    if (snapshot.child(player).child("status").exists()) {
                                                        if (snapshot.child(player).child("status").getValue().toString().equals("Won")
                                                                || snapshot.child(player).child("status").getValue().toString().equals("TwentyOne")) {
                                                            GameStatus += "You have lost " + PlayerBet + " as " + player + "'s bet" + "!\n";
                                                        } else if (snapshot.child(player).child("status").getValue().toString().equals("Lost")) {
                                                            GameStatus += "You have won " + PlayerBet + " as " + player + "'s bet" + "!\n";
                                                        }
                                                        binding.gameStatus.setText(GameStatus);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // endregion game status update
                                    // region back to start bank's counting
                                    if (playerName.equals(bankerName) && LoopEnding && OnceCheckFlag && OnceEndBank) {
                                        if (snapshot.child(player).child("status").exists() && snapshot.child(player).child("currentBet").exists()) {
                                            int Bet = parseInt(snapshot.child(player).child("currentBet").getValue().toString());
                                            if (snapshot.child(player).child("status").getValue().toString().equals("Won")
                                                    || snapshot.child(player).child("status").getValue().toString().equals("TwentyOne")) {
                                                if (AddedCounter == size[0] - 1) {
                                                    OnceCheckFlag = false;
                                                }
                                                BackUpBank -= Bet;
                                                AddedCounter++;
                                                if (!KnockKnock) {
                                                    Return[0] = true;
                                                }
                                            } else if (snapshot.child(player).child("status").getValue().toString().equals("Lost")) {
                                                if (AddedCounter == size[0] - 1) {
                                                    OnceCheckFlag = false;
                                                }
                                                BackUpBank += Bet;
                                                AddedCounter++;
                                                if (!KnockKnock) {
                                                    Return[0] = true;
                                                }
                                            } else {
                                                AddedCounter = 0;
                                                OnceCheckFlag = true;
                                                Return[0] = false;
                                            }
                                        }
                                    }
                                    // endregion back to start bank's counting
                                }
                            }
                        }
                        // region game status extension
                        if (playerName.equals(bankerName)) {
                            binding.ShowBet.setText(String.valueOf(Bank));
                        } else {
                            binding.ShowBet.setText(String.valueOf(bet));
                        }
                        if (LoopEnding) {
                            if (snapshot.child(playerName).child("status").exists()) {
                                if (snapshot.child(playerName).child("status").getValue().toString().equals("Won")
                                        || snapshot.child(playerName).child("status").getValue().toString().equals("TwentyOne")) {
                                    GameStatus += "You have won " + bet + "!\n";
                                } else if (snapshot.child(playerName).child("status").getValue().toString().equals("Lost")) {
                                    GameStatus += "You have lost " + bet + "!\n";
                                }
                                binding.gameStatus.setText(GameStatus);
                            }
                        }
                        // endregion game status extension
                        // region user's hand
                        int total = 0;
                        binding.hand.removeAllViews();
                        ArrayList<String> Hand = new ArrayList<>();
                        if (snapshot.child(playerName).child("hand").exists() && !snapshot.child("_offline").exists()) {
                            for (int i = 0; i < snapshot.child(playerName).child("hand").getChildrenCount(); i++) {
                                if (snapshot.child(playerName).child("hand").child(String.valueOf(i)).exists()) {
                                    Hand.add(snapshot.child(playerName).child("hand").child(String.valueOf(i)).getValue().toString());
                                }
                            }
                        } else {
                            binding.hand.removeAllViews();
                        }
                        Collections.reverse(Hand);
                        for (String got : Hand) {
                            Card card = AppMethods.CardLink(got);
                            total += getValueOfCard(card);
                            if (card != null && !EndGame) {
                                CardLayoutBinding image = CardLayoutBinding.inflate(getLayoutInflater());
                                image.image.setImageResource(card.img_res);
                                binding.hand.addView(image.getRoot(), ViewGroup.LayoutParams.WRAP_CONTENT
                                        , ViewGroup.LayoutParams.MATCH_PARENT);
                                binding.hand.invalidate();
                            }
                        }
                        if (binding.buttonBar.getChildAt(1) != null) {
                            ((TextView) (binding.buttonBar.getChildAt(1))).setText(String.valueOf(total));
                            ((TextView) (binding.buttonBar.getChildAt(1))).setTextColor(Color.WHITE);
                        }
                        if (total == 21) {
                            ((TextView) (binding.buttonBar.getChildAt(1))).setTextColor(Color.GREEN);
                            if (!playerName.equals(bankerName) && snapshot.child(playerName).child("status").exists()
                                    && !snapshot.child(playerName).child("status").getValue().toString().equals("Lost")) {
                                RoomRef.child(playerName).child("status").setValue("TwentyOne");
                                status = "TwentyOne";
                                Return[0] = true;
                            } else {
                                RoomRef.child(playerName).child("status").setValue("Won all");
                                Return[0] = true;
                            }
                        } else if (total > 21) {
                            ((TextView) (binding.buttonBar.getChildAt(1))).setTextColor(Color.RED);
                            if (!playerName.equals(bankerName)) {
                                if (snapshot.child(bankerName).child("status").exists()
                                        && !snapshot.child(bankerName).child("status").getValue().toString().equals("Lost all")) {
                                    RoomRef.child(playerName).child("status").setValue("Lost");
                                    status = "Lost";
                                }
                                Return[0] = true;
                            } else {
                                RoomRef.child(playerName).child("status").setValue("Lost all");
                                Return[0] = true;
                            }
                        }
                        // endregion user's hand
                        if (snapshot.child(playerName).child("hand").exists()) {
                            // region loop ending
                            if (LoopEnding) {
                                BankerEndingPermission = true;
                                if (binding.buttonBar.getChildAt(2) != null && binding.buttonBar.getChildAt(3) != null) {
                                    ((Button) binding.buttonBar.getChildAt(2)).setEnabled(false);
                                    ((Button) binding.buttonBar.getChildAt(3)).setEnabled(false);
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
                                    bankerScore += getValueOfCard(card);
                                }
                                if (snapshot.child(bankerName).child("status").getValue().toString().equals("Lost all")) {
                                    if (snapshot.child(playerName).child("status").exists()
                                            && !snapshot.child(playerName).child("status").getValue().toString().equals("TwentyOne")) {
                                        RoomRef.child(playerName).child("status").setValue("Won");
                                        status = "Won";
                                    }
                                    Return[0] = true;
                                } else if (snapshot.child(bankerName).child("status").getValue().toString().equals("Won all")) {
                                    RoomRef.child(playerName).child("status").setValue("Lost");
                                    status = "Lost";
                                    Return[0] = true;
                                } else if (total < 21 && bankerScore < 21) {
                                    if (total > bankerScore) {
                                        if (snapshot.child(playerName).child("status").exists()
                                                && !snapshot.child(playerName).child("status").getValue().toString().equals("TwentyOne")) {
                                            RoomRef.child(playerName).child("status").setValue("Won");
                                            status = "Won";
                                        }

                                        Return[0] = true;
                                    } else {
                                        RoomRef.child(playerName).child("status").setValue("Lost");
                                        status = "Lost";
                                        Return[0] = true;
                                    }
                                }
                                if (KnockKnock) {
                                    Return[0] = false;
                                    EndGame = true;
                                    LoopEnding = false;
                                }
                            }
                            if (LoopEnding && playerName.equals(bankerName) && KnockKnock && !EndGame) {
                                LoopEnding = false;
                                Return[0] = false;
                                EndGame = true;
                            }
                            // endregion loop ending
                            // region is money still out there?
                            if (available == 0 && bet == 0 && !playerName.equals(bankerName)) {
                                RoomRef.child(playerName).child("status").setValue("out");
                                RoomRef.child(playerName).child("hand").removeValue();
                                EndGame = true;
                                bet_flag = false;
                                EndGameDelay[0] = 0;
                                binding.message.setText("You have run out of money!");
                                binding.message.setVisibility(View.VISIBLE);
                            }
                            for (String player : InRoomPlayers[0]) {
                                if (snapshot.child(player).child("status").exists()
                                        && snapshot.child(player).child("status").getValue().toString().equals("out")) {
                                    RoomRef.child(playerName).child("hand").removeValue();
                                    EndGame = true;
                                    bet_flag = false;
                                    EndGameDelay[0] = 0;
                                    binding.message.setText(player + " has run out of money!");
                                    binding.message.setVisibility(View.VISIBLE);
                                    break;
                                }
                            }
                            // endregion is money still out there?
                            // region back to start
                            if (LoopEnding && Return[0] && !EndGame) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (LoopEnding && Return[0]) {
                                            bet_flag = true;
                                            Return[0] = false;
                                            LoopEnding = false;
                                            if (playerName.equals(bankerName)) {
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        chatUpdatePermission = true;
                                                        binding.gameStatus.setText("");
                                                        DataSnapshot chatSnapshot = snapshot.child("_messages");
                                                        RoomRef.setValue(SnapshotForBackup.getValue());
                                                        RoomRef.child("_messages").setValue(chatSnapshot.getValue());
                                                        RoomRef.child("_bank").setValue(BackUpBank);
                                                        OnceCheckFlag = false;
                                                        HandOutStart = true;
                                                    }
                                                }, 2000);
                                            } else {
                                                //profit counting
                                                if (status.equals("Won") || status.equals("TwentyOne")) {
                                                    if (!EndGame) {
                                                        available += 2 * bet;
                                                        HandOutStart = true;
                                                    }
                                                }
                                            }
                                            if (playerName.equals(adminName)) {
                                                OnceStart = true;
                                            }
                                            AvailableBuff = available;
                                        }
                                    }
                                }, 3000);
                            }
                            // endregion back to start
                        }
                        // region endgame
                        if (EndGame) {
                            uiDestroy(getContext(),binding);
                            LoopEnding = false;
                            Return[0] = false;
                            bet_flag = false;
                            HandOutStart = false;
                            MainGameLoop = false;
                            OnceCheckFlag = false;
                            OnceStart = false;
                            if (playerName.equals(adminName)) {
                                RoomRef.child("_ChoosingPlayer").removeValue();
                            }
                            boolean[] AllEnded = {true};
                            for (String player : InRoomPlayers[0]) {
                                if (snapshot.child(player).child("status").exists()
                                        && !snapshot.child(player).child("status").getValue().toString().equals("ended")) {
                                    AllEnded[0] = false;
                                    break;
                                }
                            }
                            if (AllEnded[0] && !playerName.equals(bankerName)) {
                                if ((status.equals("Won") || status.equals("TwentyOne")) && OnceAddProfit) {
                                    OnceAddProfit = false;
                                    if(!alreadyCounted) {
                                        available = AvailableBuff + bet;
                                    }
                                }
                                RoomRef.child(playerName).child("profit").setValue(available - 5000);
                            }
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    RoomRef.child(playerName).child("status").setValue("ended");
                                    LoopEnding = false;
                                    if (OnceEndBank && playerName.equals(bankerName)) {
                                        OnceEndBank = false;
                                        Bank = BackUpBank;
                                        available = BackUpBank;
                                        RoomRef.child(playerName).child("profit").setValue(available - 5000);
                                    }
                                    if (AllEnded[0]) {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                RoomRef.removeEventListener(InGameListener);
                                                if (playerName.equals(adminName)) {
                                                    RoomRef.removeValue();
                                                }
                                                //region endGameTable
                                                ArrayList<ResultData> results = new ArrayList<>();
                                                results.clear();
                                                binding.endGameTable.setVisibility(View.INVISIBLE);
                                                for (String player : InRoomPlayers[0]) {
                                                    boolean playerInList = false;
                                                    for (ResultData data : results) {
                                                        if (data.getName().equals(player)) {
                                                            playerInList = true;
                                                        }
                                                    }
                                                    if (snapshot.child(player).child("profit").exists() && !playerInList) {
                                                        int profit = parseInt(snapshot.child(player).child("profit").getValue().toString());
                                                        ResultData playerResult = new ResultData(player, profit);
                                                        results.add(playerResult);
                                                    }
                                                }
                                                results.sort(new Comparator<ResultData>() {
                                                    @Override
                                                    public int compare(ResultData o1, ResultData o2) {
                                                        if (o1.getProfit() < o2.getProfit())
                                                            return 1;
                                                        else if (o1.getProfit() > o2.getProfit())
                                                            return -1;
                                                        else return 0;
                                                    }
                                                });
                                                RecyclerView recyclerView = binding.endGameRecyclerView;
                                                EndResultRecyclerViewAdapter adapter = new EndResultRecyclerViewAdapter(getContext(), results);
                                                recyclerView.setAdapter(adapter);
                                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                                                fadeIn.setDuration(2400);
                                                binding.endGameTable.startAnimation(fadeIn);
                                                binding.endGameTable.setVisibility(View.VISIBLE);
                                            }
                                        }, 3000);
                                        //endregion endGameTable
                                        //region recounting avg
                                        SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", 0);
                                        int gamesCount = prefs.getInt("gamesCount", 0);
                                        int avgProfit = prefs.getInt("avgProfit", 0);
                                        int profit = available - 5000;
                                        avgProfit = (avgProfit * gamesCount + profit) / (gamesCount + 1);
                                        gamesCount++;
                                        prefs.edit().putInt("gamesCount", gamesCount).apply();
                                        prefs.edit().putInt("avgProfit", avgProfit).apply();
                                        //endregion recounting avg

                                    }
                                }
                            }, EndGameDelay[0]);
                        }
                        // endregion endgame
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
            }
        });

    }

    private int getValueOfCard(Card card) {
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

    public static void onGameStart(Context context, FragmentTwentyOneGameBinding binding, WindowManager windowManager) {
        AvailableBuff = available;
        GameFragment.isInGame = true;
        uiCreate(context, binding, windowManager);
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
                    if (!snapshot.child(player).equals("_size") && !snapshot.child(player).equals("_access")
                            && !snapshot.child(player).equals("_messages")) {
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
                                binding.crown.setVisibility(View.VISIBLE);
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
                binding.message.startAnimation(fadeOut);
                binding.message.setVisibility(View.INVISIBLE);
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
                AppMethods.Disconnect(RoomRef, playerName, null, ChatFragment.listener);
            }
        });
        //triggers reading if bank_choose exists in database
    }

    public static void setBankSize(String size) {
        if (!size.equals("") && !(size.length() >= 6)) {
            if (parseInt(size) > 99 && parseInt(size) < 5001 && parseInt(size) <= available) {
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
            }
        } else {
            Toast.makeText(Banker_dialog.getContext(), "incorrect input", Toast.LENGTH_SHORT).show();
        }
    }

    public static void setBetSize(String gotBet) {
        if (!gotBet.equals("") && !(gotBet.length() >= 6)) {
            if (parseInt(gotBet) > 9 && parseInt(gotBet) <= Bank / (size[0] - 1) && parseInt(gotBet) <= available) {
                bet = parseInt(gotBet);
                available -= bet;
                fm.beginTransaction().remove(dialog).commit();
                RoomRef.child(playerName).child("currentBet").setValue(TwentyOneGame.bet);
                RoomRef.child(playerName).child("status").setValue("waiting");
            }
        } else {
            Toast.makeText(dialog.getContext(), "incorrect input", Toast.LENGTH_SHORT).show();
        }
    }

    public static void recallOfDialogBetChooseFragment(Context context) {
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

    public static void recallOfDialogSetBankSize(Context context) {
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

    @SuppressLint("ResourceAsColor")
    private static void uiCreate(Context context, FragmentTwentyOneGameBinding binding, WindowManager windowManager) {
        LinearLayout.LayoutParams params = new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(8, 8, 8, 8);
        MaterialButton OneMore = new MaterialButton(context);
        MaterialButton Pass = new MaterialButton(context);
        TextView text = new TextView(context);
        OneMore.setText("More");
        Pass.setText("Pass");
        OneMore.setCornerRadius((int) context.getResources().getDimension(android.R.dimen.thumbnail_height));
        Pass.setCornerRadius((int) context.getResources().getDimension(android.R.dimen.thumbnail_height));
        OneMore.setTextColor(R.color.black);
        Pass.setTextColor(R.color.black);
        text.setText("TOTAL :");
        TextView total = new TextView(context);
        total.setText("0");
        total.setGravity(Gravity.CENTER);
        text.setTextColor(Color.WHITE);
        text.setGravity(Gravity.CENTER);
        total.setTextColor(Color.WHITE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;
        float smallestWidth = Math.min(widthDp, heightDp);
        if (smallestWidth > 600) {
            text.setTextSize(40);
            total.setTextSize(60);
            OneMore.setTextSize(25);
            Pass.setTextSize(25);
        } else {
            text.setTextSize(20);
            total.setTextSize(30);
        }
        binding.buttonBar.addView(text, 0, params);
        binding.buttonBar.addView(total, 1, params);
        binding.buttonBar.addView(OneMore, 2, params);
        binding.buttonBar.addView(Pass, 3, params);

    }

    @SuppressLint("ResourceAsColor")
    private void uiDestroy(Context context, FragmentTwentyOneGameBinding binding) {
        if (dialog.isVisible()) {
            fm.beginTransaction().remove(dialog).commit();
        }
        if (Banker_dialog.isVisible()) {
            fm.beginTransaction().remove(Banker_dialog).commit();
        }
        binding.buttonBar.removeAllViews();
        binding.gameStatus.setText("");
        binding.hand.removeAllViews();
        binding.shownCard.setImageDrawable(null);
        binding.textBankersFirstCard.setVisibility(View.INVISIBLE);
        binding.ShowBet.setVisibility(View.INVISIBLE);
        binding.betText.setVisibility(View.INVISIBLE);
        binding.crown.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams params = new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        MaterialButton Disconnect = new MaterialButton(context);
        Disconnect.setCornerRadius((int) context.getResources().getDimension(android.R.dimen.thumbnail_height));
        Disconnect.setTextColor(R.color.black);
        Disconnect.setText("Disconnect");
        Disconnect.setTextSize(20);
        binding.buttonBar.addView(Disconnect, params);
        binding.buttonBar.getChildAt(0).setOnClickListener(view -> {
            AppMethods.Disconnect(RoomRef, playerName, InGameListener, ChatFragment.listener);
            assert getParentFragment() != null;
            GameChooseActivity.fragmentManager.beginTransaction().remove(getParentFragment()).commit();
        });
    }

    private void setStatusToReady() {
        RoomRef.child(playerName).child("status").setValue("ready");
        binding.ready.setEnabled(false);
    }

    public static void notifyPlayer() {
        if (GameFragment.viewPager2.getCurrentItem() != 1) {
            handler.post(new Runnable() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void run() {
                    Snackbar snackbar = Snackbar.make(binding.scrollView2, "New message recived", Snackbar.LENGTH_LONG);
                    snackbar.setAnchorView(binding.linearLayout);
                    snackbar.setAction("To the chat ->", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            GameFragment.viewPager2.setCurrentItem(1);
                        }
                    });
                    snackbar.show();
                }
            });
        }
    }
}