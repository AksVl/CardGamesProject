package com.akscardgames.cardgamesproject.gamesRelated.gameFragments;


import static com.akscardgames.cardgamesproject.gamesRelated.GameFragment.RoomRef;
import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akscardgames.cardgamesproject.gamesRelated.ChatFragment;
import com.akscardgames.cardgamesproject.gamesRelated.GameFragment;
import com.akscardgames.cardgamesproject.gamesRelated.ResultData;
import com.akscardgames.cardgamesproject.general.AppMethods;
import com.akscardgames.cardgamesproject.general.Card;
import com.akscardgames.cardgamesproject.general.adapters.EndResultRecyclerViewAdapter;
import com.akscardgames.cardgamesproject.menu.GameChooseActivity;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.CardLayoutBinding;
import com.example.cardgamesproject.databinding.FragmentFoolGameBinding;
import com.example.cardgamesproject.databinding.NoimgPlayerItemBinding;
import com.example.cardgamesproject.databinding.PlayerItemBinding;
import com.example.cardgamesproject.databinding.SmallCardLayoutBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;
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
import java.util.HashSet;
import java.util.Objects;


public class FoolGame extends Fragment {
    //region variables
    private static FragmentFoolGameBinding binding;
    private static FragmentManager fm;
    private static SmallCardLayoutBinding lastCard;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    private static DatabaseReference roomRef;
    private static String roomName;
    private static String playerName;
    private static String adminName;
    private ValueEventListener listener;
    private ValueEventListener inGameListener;
    private int choosingPlayerPos;
    private static boolean endGame = false;
    private static final ArrayList<Card> deck = new ArrayList<>();
    private final static int[] size = new int[1];
    private static int my_pos;
    private final static ArrayList<String>[] InRoomPlayers = new ArrayList[]{new ArrayList<>()};
    private static final Handler handler = new Handler();
    public static String roomNameBuff;
    private static boolean gameStarted = false;
    private boolean handUpdatePermission = true;
    private boolean statusUpdatePermission = true;
    private boolean someoneOffline = false;
    //private boolean gameStatusUpdatePermission = true;
    private boolean imTheWinner = false;
    private int oldPairCount = 0;
    private String oldAttackingPlayer = null;
    private boolean tossPermission = false;
    private ArrayList<String> usedValues = new ArrayList<>();
    private boolean statusPaused = false;
    private boolean onceTake = false;
    private boolean okClicked = false;
    private boolean onceHandOut = true;
    private boolean onceLose = true;
    private boolean secondOnceTake = true;

    //endregion variables


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFoolGameBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GameFragment.viewPager2.setCurrentItem(1);
        adminName = null;
        choosingPlayerPos = 0;
        endGame = false;
        gameStarted = false;
        handUpdatePermission = true;
        statusUpdatePermission = true;
        someoneOffline = false;
        imTheWinner = false;
        oldPairCount = 0;
        oldAttackingPlayer = null;
        tossPermission = false;
        statusPaused = false;
        onceTake = false;
        okClicked = false;
        onceHandOut = true;
        onceLose = true;
        secondOnceTake = true;
        usedValues.clear();
        handler.post(new Runnable() {
            @Override
            public void run() {
                fm = getParentFragmentManager();
                roomName = roomNameBuff;
                SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", 0);
                playerName = prefs.getString("name", "");
                roomRef = database.getReference("FoolRooms/" + roomName);
                binding.ready.setEnabled(false);
                binding.ready.setOnClickListener(v ->
                        SetStatusToReady());
                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.playersContainer.removeAllViews();
                        ViewGroup.LayoutParams params = new LinearLayout.
                                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                        if (snapshot.child("_size").exists()) {
                            size[0] = parseInt(snapshot.child("_size").getValue().toString());
                            for (int i = 0; size[0] - 1 > i; i++) {
                                NoimgPlayerItemBinding playerItem = NoimgPlayerItemBinding.inflate(getLayoutInflater());
                                binding.playersContainer.addView(playerItem.getRoot(), params);
                                binding.playersContainer.invalidate();
                            }
                        }
                        roomRef.addValueEventListener(listener);
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
                                roomRef.child(playerName).child("status").setValue("joined");
                            }
                        }
                        if (snapshot.child(playerName).child("position").exists()) {
                            my_pos = parseInt(snapshot.child(playerName).child("position").getValue().toString());
                            if (my_pos == 1) {
                                adminName = playerName;
                            }
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
                        AppMethods.foolReadyCheck(listener, inGameListener,
                                InRoomPlayers[0], roomRef, readyCount, size[0],
                                binding, getContext(), getActivity().getWindowManager());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                inGameListener = new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int pos;
                        if (!snapshot.child("_encounter").exists()) {
                            usedValues.clear();
                            statusUpdatePermission = true;
                            okClicked = false;
                            if (onceTake) {
                                onceTake = false;
                                //region takeFromStack
                                int handCount = 0;
                                if (snapshot.child(playerName).child("hand").exists()) {
                                    handCount = (int) snapshot.child(playerName).child("hand").getChildrenCount();
                                }
                                if (handCount < 6) {
                                    roomRef.child("_takers").child(playerName).setValue(my_pos);
                                }
                                //endregion takeFromStack
                            }
                        }
                        //region choosingPlayer reading
                        if (snapshot.child("_ChoosingPlayer").exists()) {
                            int currentChoosingPlayer = parseInt(snapshot.child("_ChoosingPlayer").getValue().toString());
                            if (currentChoosingPlayer != choosingPlayerPos) {
                                choosingPlayerPos = parseInt(snapshot.child("_ChoosingPlayer").getValue().toString());
                                if (choosingPlayerPos != my_pos && choosingPlayerPos != AppMethods.nextPlayer(size[0], my_pos)) {
                                    roomRef.child(playerName).child("status").setValue("waiting");
                                }
                            }
                        }
                        //endregion choosingPlayer reading
                        //region if only one is left in the game
                        if (snapshot.child("_winnersPositions").exists()
                                && snapshot.child("_winnersPositions").getChildrenCount() == (size[0] - 1)) {
                            endGame = true;
                            if (!imTheWinner) {
                                adminName = playerName;
                                if (snapshot.child(playerName).child("status").exists()
                                        && !snapshot.child(playerName).child("status").getValue().toString().equals("Out")) {
                                    //region lose
                                    binding.message.setText("You have lost!");
                                    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                                    fadeIn.setDuration(800);
                                    binding.message.startAnimation(fadeIn);
                                    binding.message.setVisibility(View.VISIBLE);
                                    roomRef.child(playerName).child("profit").setValue(-5000);
                                    binding.ShowBet.setText("" + (-5000));
                                    binding.betText.setText("profit");
                                    binding.ShowBet.setTextColor(Color.RED);
                                    //region recounting avg
                                    SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", 0);
                                    int gamesCount = prefs.getInt("gamesCount", 0);
                                    int avgProfit = prefs.getInt("avgProfit", 0);
                                    int profit = -5000;
                                    avgProfit = (avgProfit * gamesCount + profit) / (gamesCount + 1);
                                    gamesCount++;
                                    prefs.edit().putInt("gamesCount", gamesCount).apply();
                                    prefs.edit().putInt("avgProfit", avgProfit).apply();
                                    //endregion recounting avg
                                    uiDestroy(getContext(), binding);
                                    //endregion lose
                                    roomRef.child(playerName).child("status").setValue("Lost");
                                }
                            }
                        }
                        //endregion if only one is left in the game
                        //region if someone offline
                        if (snapshot.child("_offline").exists()) {
                            someoneOffline = true;
                            String offlinePlayerName = snapshot.child("_offline").getValue().toString();
                            roomRef.removeEventListener(inGameListener);
                            binding.message.setText(offlinePlayerName + " went offline, results won't be counted");
                            binding.message.setVisibility(View.VISIBLE);
                            uiDestroy(getContext(), binding);
                        }
                        //endregion if someone offline
                        //region handing out from stack
                        //region takingPlayer init
                        if (snapshot.child("_encounter").exists()) {
                            onceHandOut = true;
                        } else {
                            onceLose = true;
                        }
                        if (snapshot.child("_takers").exists() && my_pos == choosingPlayerPos) {
                            if (onceHandOut) {
                                onceHandOut = false;
                                int min = 999999;
                                for (DataSnapshot taker : snapshot.child("_takers").getChildren()) {
                                    int got = parseInt(taker.getValue().toString());
                                    if (got < min) {
                                        min = got;
                                    }
                                }
                                if (snapshot.child("_stack").exists()) {
                                    roomRef.child("_takingPlayer").setValue(min);
                                }
                            }
                        }
                        //endregion takingPlayer init
                        //region taking from stack by takingPlayer
                        if (snapshot.child("_takingPlayer").exists()) {
                            int takingPlayerPos = parseInt(snapshot.child("_takingPlayer").getValue().toString());
                            if (my_pos == takingPlayerPos) {
                                int handCount = (int) snapshot.child(playerName).child("hand").getChildrenCount();
                                if (handCount < 6 && secondOnceTake) {
                                    secondOnceTake = false;
                                    int required = 6 - handCount;
                                    for (int i = 0; i < required; i++) {
                                        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                int handCount = (int) snapshot.child(playerName).child("hand").getChildrenCount();
                                                int stackCount = ((int) snapshot.child("_stack").getChildrenCount()) - 1;
                                                String card = snapshot.child("_stack").child("" + stackCount).getValue().toString();
                                                roomRef.child("_stack").child("" + stackCount).removeValue();
                                                roomRef.child(playerName).child("hand").child("" + handCount).setValue(card);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    roomRef.child("_takers").child(playerName).removeValue();
                                    roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            int min = 999999;
                                            for (DataSnapshot taker : snapshot.child("_takers").getChildren()) {
                                                int got = parseInt(taker.getValue().toString());
                                                if (got < min) {
                                                    min = got;
                                                }
                                            }
                                            if (snapshot.child("_stack").exists()) {
                                                roomRef.child("_takingPlayer").setValue(min);
                                            }
                                            if (min == 999999) {
                                                roomRef.child("_takingPlayer").removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        }
                        //endregion taking from stack by takingPlayer
                        //endregion handing out from stack
                        if (!endGame && !snapshot.child("_takers").exists()) {
                            //region UI access managing
                            if (snapshot.child(playerName).child("status").exists()) {
                                //region if you are attacking
                                if (my_pos == choosingPlayerPos) {
                                    final int[] pairCount = {0};
                                    if (statusUpdatePermission) {
                                        statusUpdatePermission = false;
                                        statusPaused = true;
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!okClicked) {
                                                    roomRef.child(playerName).child("status").setValue("attacking");
                                                }
                                                statusPaused = false;
                                            }
                                        }, 2200);
                                    }
                                    if (snapshot.child("_encounter").exists()) {
                                        int attackingCounter = 0;
                                        int defendingCounter = 0;
                                        int pairs = (int) snapshot.child("_encounter").getChildrenCount();
                                        for (int i = 0; i < pairs; i++) {
                                            if (snapshot.child("_encounter").child("" + i).child("attacking").exists()) {
                                                attackingCounter++;
                                            }
                                            if (snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                                defendingCounter++;
                                            }
                                        }
                                        boolean allDefended = attackingCounter == defendingCounter;
                                        if (allDefended) {
                                            if (binding.buttonBar.getChildAt(1) != null) {
                                                binding.buttonBar.getChildAt(1).setEnabled(true);
                                                binding.buttonBar.getChildAt(1).setOnClickListener(v -> {
                                                    binding.buttonBar.getChildAt(1).setEnabled(false);
                                                    statusUpdatePermission = false;
                                                    roomRef.child(playerName).child("status").setValue("ok");
                                                });
                                            }
                                        } else {
                                            if (binding.buttonBar.getChildAt(1) != null) {
                                                binding.buttonBar.getChildAt(1).setEnabled(false);
                                            }
                                        }
                                    } else {
                                        if (binding.buttonBar.getChildAt(1) != null) {
                                            binding.buttonBar.getChildAt(1).setEnabled(false);
                                        }
                                    }
                                    pairCount[0] = (int) snapshot.child("_encounter").getChildrenCount();
                                    if (snapshot.child(playerName).child("status").getValue().toString().equals("ok")) {
                                        if (pairCount[0] != oldPairCount && !statusPaused) {
                                            roomRef.child(playerName).child("status").setValue("tossing up");
                                            okClicked = true;
                                        }
                                    }
                                    oldPairCount = (int) snapshot.child("_encounter").getChildrenCount();
                                    if (!snapshot.child(playerName).child("status").getValue().toString().equals("ok")) {
                                        if (pairCount[0] < 6) {
                                            binding.linearLayout3.setOnDragListener(new View.OnDragListener() {
                                                @Override
                                                public boolean onDrag(View v, DragEvent event) {
                                                    switch (event.getAction()) {
                                                        case DragEvent.ACTION_DRAG_STARTED:
                                                            return true;
                                                        case DragEvent.ACTION_DRAG_ENTERED:
                                                            return true;
                                                        case DragEvent.ACTION_DRAG_EXITED:
                                                            return true;
                                                        case DragEvent.ACTION_DRAG_LOCATION:
                                                            return true;
                                                        case DragEvent.ACTION_DROP:
                                                            handUpdatePermission = false;
                                                            String cardName = event.getClipData().getItemAt(0).getText().toString();
                                                            Card draggedCard = AppMethods.cardLink(cardName);
                                                            if (usedValues.size() == 0 || usedValues.contains(draggedCard.value)) {
                                                                ArrayList<String> rearrangedHand = new ArrayList<>();
                                                                for (DataSnapshot cardSnapshot : snapshot.child(playerName).child("hand").getChildren()) {
                                                                    if (!cardSnapshot.getValue().toString().equals(draggedCard.toString())) {
                                                                        rearrangedHand.add(cardSnapshot.getValue().toString());
                                                                    }
                                                                }
                                                                roomRef.child(playerName).child("hand").removeValue();
                                                                for (String card : rearrangedHand) {
                                                                    roomRef.child(playerName).child("hand")
                                                                            .child(String.valueOf(rearrangedHand.indexOf(card))).setValue(card);
                                                                }
                                                                roomRef.child("_encounter").child("" + pairCount[0])
                                                                        .child("attacking").setValue(draggedCard.toString());
                                                                handUpdatePermission = true;
                                                                return true;
                                                            } else {
                                                                return false;
                                                            }
                                                        default:
                                                            break;
                                                    }
                                                    return false;
                                                }
                                            });
                                            if (binding.buttonBar.getChildAt(1) != null) {
                                                binding.buttonBar.getChildAt(1).setOnClickListener(v -> {
                                                    binding.buttonBar.getChildAt(1).setEnabled(false);
                                                    statusUpdatePermission = false;
                                                    roomRef.child(playerName).child("status").setValue("ok");
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            statusUpdatePermission = true;
                                                        }
                                                    }, 2000);
                                                });
                                            }
                                        }
                                    } else {
                                        binding.linearLayout3.setOnDragListener(null);
                                    }
                                    binding.buttonBar.getChildAt(0).setEnabled(false);
                                }
                                //endregion if you are attacking
                                //region if you are defending
                                else if (my_pos == nextPlayerPos(choosingPlayerPos, snapshot)) {
                                    if (binding.buttonBar.getChildAt(1) != null) {
                                        binding.buttonBar.getChildAt(1).setEnabled(false);
                                    }
                                    if (statusUpdatePermission) {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                roomRef.child(playerName).child("status").setValue("defending");
                                            }
                                        }, 2000);
                                    }
                                    statusUpdatePermission = false;
                                    if (snapshot.child("_encounter").exists()) {
                                        binding.buttonBar.getChildAt(0).setEnabled(true);
                                        binding.buttonBar.getChildAt(0).setOnClickListener(v -> {
                                            binding.buttonBar.getChildAt(0).setEnabled(false);
                                            statusUpdatePermission = false;
                                            roomRef.child(playerName).child("status").setValue("taking");
                                        });
                                        if (snapshot.child(playerName).child("status").getValue().toString().equals("taking")) {
                                            binding.buttonBar.getChildAt(0).setEnabled(false);
                                            boolean allOked = true;
                                            for (String player : InRoomPlayers[0]) {
                                                if (snapshot.child(player).child("status").exists()
                                                        && !(snapshot.child(player).child("status").getValue().toString().equals("taking")
                                                        || snapshot.child(player).child("status").getValue().toString().equals("ok"))) {
                                                    allOked = false;
                                                }
                                            }
                                            if (allOked && onceLose) {
                                                statusUpdatePermission = true;
                                                onceLose = false;
                                                for (DataSnapshot pair : snapshot.child("_encounter").getChildren()) {
                                                    long count = snapshot.child(playerName).child("hand").getChildrenCount();
                                                    String attacking = pair.child("attacking").getValue().toString();
                                                    roomRef.child(playerName).child("hand").child(count + "").setValue(attacking);
                                                    if (pair.child("defending").exists()) {
                                                        count++;
                                                        String defending = pair.child("defending").getValue().toString();
                                                        roomRef.child(playerName).child("hand").child(count + "").setValue(defending);
                                                    }
                                                }
                                                choosingPlayerPos = nextPlayerPos(my_pos, snapshot);
                                                roomRef.child("_ChoosingPlayer").setValue(choosingPlayerPos);
                                                roomRef.child(playerName).child("status").setValue("waiting");
                                                roomRef.child("_encounter").removeValue();
                                                statusUpdatePermission = true;

                                            }
                                        }
                                        boolean allDefended = true;
                                        int pairs = (int) snapshot.child("_encounter").getChildrenCount();
                                        for (int i = 0; i < pairs; i++) {
                                            Card attackingCard = AppMethods.cardLink(snapshot.child("_encounter").child(i + "")
                                                    .child("attacking").getValue().toString());
                                            View.OnDragListener listener = getDefendDragListener(attackingCard, i, snapshot);
                                            switch (i) {
                                                case 0:
                                                    if (!snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                                        binding.pair1.setOnDragListener(listener);
                                                        allDefended = false;
                                                    }
                                                    break;
                                                case 1:
                                                    if (!snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                                        binding.pair2.setOnDragListener(listener);
                                                        allDefended = false;
                                                    }
                                                    break;
                                                case 2:
                                                    if (!snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                                        binding.pair3.setOnDragListener(listener);
                                                        allDefended = false;
                                                    }
                                                    break;
                                                case 3:
                                                    if (!snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                                        binding.pair4.setOnDragListener(listener);
                                                        allDefended = false;
                                                    }
                                                    break;
                                                case 4:
                                                    if (!snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                                        binding.pair5.setOnDragListener(listener);
                                                        allDefended = false;
                                                    }
                                                    break;
                                                case 5:
                                                    if (!snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                                        binding.pair6.setOnDragListener(listener);
                                                        allDefended = false;
                                                    }
                                                    break;
                                                default:
                                                    break;
                                            }
                                            if (allDefended) {
                                                binding.buttonBar.getChildAt(0).setEnabled(false);
                                            } else {
                                                binding.buttonBar.getChildAt(0).setEnabled(true);
                                            }
                                        }
                                        boolean allOked = true;
                                        for (String player : InRoomPlayers[0]) {
                                            if (snapshot.child(player).child("status").exists()
                                                    && !(snapshot.child(player).child("status").getValue().toString().equals("ok")
                                                    || snapshot.child(player).child("status").getValue().toString().equals("defending"))) {
                                                allOked = false;
                                            }
                                        }
                                        if (allOked && allDefended) {
                                            statusUpdatePermission = true;
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    choosingPlayerPos = nextPlayerPos(choosingPlayerPos, snapshot);
                                                    roomRef.child("_ChoosingPlayer").setValue(choosingPlayerPos);
                                                    roomRef.child(playerName).child("status").setValue("waiting");
                                                }
                                            }, 1000);
                                            roomRef.child("_encounter").removeValue();

                                        }
                                    } else {
                                        if (binding.buttonBar.getChildAt(0) != null) {
                                            binding.buttonBar.getChildAt(0).setEnabled(false);
                                        }
                                        if (statusUpdatePermission) {
                                            roomRef.child(playerName).child("status").setValue("waiting");
                                        }
                                    }
                                }
                                //endregion if you are defending
                                //region if you are neither attacking or defending
                                else {
                                    AppMethods.findPlayerByPos(roomRef, choosingPlayerPos, new AppMethods.OnPlayerFoundListener() {
                                        @Override
                                        public void onPlayerFound(String player) {
                                            if (oldAttackingPlayer != null) {
                                                if (!oldAttackingPlayer.equals(player)) {
                                                    tossPermission = false;
                                                }
                                                final int[] pairCount = {0};
                                                if (snapshot.child("_encounter").exists()) {
                                                    pairCount[0] = (int) snapshot.child("_encounter").getChildrenCount();
                                                    if (snapshot.child(playerName).child("status").getValue().toString().equals("ok")) {
                                                        if (pairCount[0] != oldPairCount && !statusPaused) {
                                                            roomRef.child(playerName).child("status").setValue("tossing up");
                                                        }
                                                    }
                                                    oldPairCount = (int) snapshot.child("_encounter").getChildrenCount();
                                                } else {
                                                    roomRef.child(playerName).child("status").setValue("waiting");
                                                }
                                                if (snapshot.child(player).child("status").getValue().toString().equals("ok")) {
                                                    tossPermission = true;
                                                }
                                                if (!statusPaused && tossPermission && !snapshot.child(playerName).child("status").getValue().toString().equals("ok")) {
                                                    roomRef.child(playerName).child("status").setValue("tossing up");
                                                    if (pairCount[0] < 6) {
                                                        binding.linearLayout3.setOnDragListener(new View.OnDragListener() {
                                                            @Override
                                                            public boolean onDrag(View v, DragEvent event) {
                                                                switch (event.getAction()) {
                                                                    case DragEvent.ACTION_DRAG_STARTED:
                                                                        return true;
                                                                    case DragEvent.ACTION_DRAG_ENTERED:
                                                                        return true;
                                                                    case DragEvent.ACTION_DRAG_EXITED:
                                                                        return true;
                                                                    case DragEvent.ACTION_DRAG_LOCATION:
                                                                        return true;
                                                                    case DragEvent.ACTION_DROP:
                                                                        handUpdatePermission = false;
                                                                        String cardName = event.getClipData().getItemAt(0).getText().toString();
                                                                        Card draggedCard = AppMethods.cardLink(cardName);
                                                                        ArrayList<String> rearrangedHand = new ArrayList<>();
                                                                        for (DataSnapshot cardSnapshot : snapshot.child(playerName).child("hand").getChildren()) {
                                                                            if (!cardSnapshot.getValue().toString().equals(draggedCard.toString())) {
                                                                                rearrangedHand.add(cardSnapshot.getValue().toString());
                                                                            }
                                                                        }
                                                                        roomRef.child(playerName).child("hand").removeValue();
                                                                        for (String card : rearrangedHand) {
                                                                            roomRef.child(playerName).child("hand")
                                                                                    .child(String.valueOf(rearrangedHand.indexOf(card))).setValue(card);
                                                                        }
                                                                        roomRef.child("_encounter").child("" + pairCount[0])
                                                                                .child("attacking").setValue(draggedCard.toString());
                                                                        handUpdatePermission = true;
                                                                        return true;
                                                                    default:
                                                                        break;
                                                                }

                                                                return false;

                                                            }
                                                        });
                                                    } else {
                                                        binding.linearLayout3.setOnDragListener(null);
                                                    }
                                                    binding.buttonBar.getChildAt(1).setOnClickListener(v -> {
                                                        statusUpdatePermission = false;
                                                        roomRef.child(playerName).child("status").setValue("ok");
                                                        binding.buttonBar.getChildAt(1).setEnabled(false);
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                statusUpdatePermission = true;
                                                            }
                                                        }, 2000);
                                                    });
                                                }
                                            }
                                            oldAttackingPlayer = player;
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {

                                        }
                                    });
                                }
                                //endregion if you are neither attacking or defending
                            }
                            //endregion UI access managing
                        }
                        for (String player : InRoomPlayers[0]) {
                            if (!player.equals("_size") && !player.equals("_access") && !player.equals("_messages")
                                    && snapshot.child(player).child("position").exists()) {
                                //region player's status update
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
                                    if (gotStatus.equals("taking")) {
                                        if (binding.buttonBar.getChildAt(1) != null) {
                                            binding.buttonBar.getChildAt(1).setEnabled(true);
                                            binding.buttonBar.getChildAt(1).setOnClickListener(v -> {
                                                statusUpdatePermission = false;
                                                roomRef.child(playerName).child("status").setValue("ok");
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        statusUpdatePermission = true;
                                                    }
                                                }, 2000);
                                            });
                                        }
                                    }
                                    if (gotStatus.equals("Out")) {
                                        status.setTextColor(Color.GREEN);
                                    } else if (gotStatus.equals("Lost")) {
                                        status.setTextColor(Color.RED);
                                    }
                                }
                                //endregion player's status update
                            }
                            //region player's hand
                            if (handUpdatePermission) {
                                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ArrayList<String> hand = new ArrayList<>();
                                        if (snapshot.child(playerName).child("hand").exists() && !snapshot.child("_offline").exists()) {
                                            for (int i = 0; i < snapshot.child(playerName).child("hand").getChildrenCount(); i++) {
                                                if (snapshot.child(playerName).child("hand").child(String.valueOf(i)).exists()) {
                                                    hand.add(snapshot.child(playerName).child("hand").child(String.valueOf(i)).getValue().toString());
                                                }
                                            }
                                        } else {
                                            binding.hand.removeAllViews();
                                        }
                                        //region if player is out
                                        if (!someoneOffline) {
                                            if (hand.size() == 0 && gameStarted && handUpdatePermission) {
                                                if (statusUpdatePermission) {
                                                    roomRef.child(playerName).child("status").setValue("finished");
                                                    statusUpdatePermission = false;
                                                }
                                                imTheWinner = true;
                                                GameFragment.isInGame = false;
                                                //region win
                                                binding.message.setText("You have won!");
                                                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                                                fadeIn.setDuration(800);
                                                binding.message.startAnimation(fadeIn);
                                                binding.message.setVisibility(View.VISIBLE);
                                                roomRef.child("_winnersPositions").child(playerName).setValue(my_pos);
                                                roomRef.child(playerName).child("status").setValue("Out");
                                                roomRef.child(playerName).child("profit").setValue(5000 / (size[0] - 1));
                                                binding.ShowBet.setText("+" + (5000 / (size[0] - 1)));
                                                binding.betText.setText("profit");
                                                binding.ShowBet.setTextColor(Color.GREEN);
                                                //region recounting avg
                                                SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", 0);
                                                int gamesCount = prefs.getInt("gamesCount", 0);
                                                int avgProfit = prefs.getInt("avgProfit", 0);
                                                int profit = 5000 / (size[0] - 1);
                                                avgProfit = (avgProfit * gamesCount + profit) / (gamesCount + 1);
                                                gamesCount++;
                                                prefs.edit().putInt("gamesCount", gamesCount).apply();
                                                prefs.edit().putInt("avgProfit", avgProfit).apply();
                                                //endregion recounting avg
                                                uiDestroy(getContext(), binding);
                                                //endregion win
                                            } else {
                                                statusUpdatePermission = true;
                                            }
                                        }
                                        //endregion if player is out
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (handUpdatePermission) {
                                                    binding.hand.removeAllViews();
                                                    hand.sort(new Comparator<String>() {
                                                        @Override
                                                        public int compare(String o1, String o2) {
                                                            char chosenSuit = 's';
                                                            if (snapshot.child("_chosenLast").exists()) {
                                                                Card chosenCard = AppMethods.cardLink(snapshot.child("_chosenLast").getValue().toString());
                                                                if (chosenCard != null) {
                                                                    chosenSuit = chosenCard.suit;
                                                                }
                                                            }
                                                            Card c1 = AppMethods.cardLink(o1);
                                                            Card c2 = AppMethods.cardLink(o2);
                                                            char[] suits = {'d', 'c', 'h', 's'};
                                                            ArrayList<Character> suit_value = new ArrayList<Character>();
                                                            for (char suit : suits) {
                                                                if (suit != chosenSuit) {
                                                                    suit_value.add(suit);
                                                                }
                                                            }
                                                            suit_value.add(chosenSuit);
                                                            if (suit_value.indexOf(c1.suit) - suit_value.indexOf(c2.suit) != 0) {
                                                                return suit_value.indexOf(c1.suit) - suit_value.indexOf(c2.suit);
                                                            } else {
                                                                return getValueOfCard(c1) - getValueOfCard(c2);
                                                            }
                                                        }

                                                        ;
                                                    });
                                                    for (String got : hand) {
                                                        Card card = AppMethods.cardLink(got);
                                                        if (card != null && !endGame) {
                                                            CardLayoutBinding image = CardLayoutBinding.inflate(getLayoutInflater());
                                                            image.image.setImageResource(card.img_res);
                                                            View viewForCard = image.getRoot();
                                                            binding.hand.addView(viewForCard, ViewGroup.LayoutParams.WRAP_CONTENT
                                                                    , ViewGroup.LayoutParams.MATCH_PARENT);
                                                            binding.hand.invalidate();
                                                            viewForCard.setTag(card.toString());
                                                            viewForCard.setOnLongClickListener(new View.OnLongClickListener() {
                                                                @Override
                                                                public boolean onLongClick(View v) {
                                                                    ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
                                                                    String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                                                                    ClipData dragData = new ClipData(v.getTag().toString(), mimeTypes, item);
                                                                    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);
                                                                    v.startDragAndDrop(dragData, myShadow, null, 0);
                                                                    return true;
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                        }, 100);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            //endregion player's hand
                        }
                        //region encounter UI
                        if (snapshot.child("_encounter").exists()) {
                            onceTake = true;
                            secondOnceTake = true;
                            long pairCount = snapshot.child("_encounter").getChildrenCount();
                            for (int i = 0; i < pairCount; i++) {
                                switch (i) {
                                    case 0:
                                        if (snapshot.child("_encounter").child("" + i).child("attacking").exists()) {
                                            Card attacking = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("attacking").getValue().toString());
                                            if (!usedValues.contains(attacking.value)) {
                                                usedValues.add(attacking.value);
                                            }
                                            binding.card11.setImageResource(attacking.img_res);
                                        }
                                        if (snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                            Card defending = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("defending").getValue().toString());
                                            if (!usedValues.contains(defending.value)) {
                                                usedValues.add(defending.value);
                                            }
                                            binding.card12.setImageResource(defending.img_res);
                                        }
                                        break;
                                    case 1:
                                        if (snapshot.child("_encounter").child("" + i).child("attacking").exists()) {
                                            Card attacking = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("attacking").getValue().toString());
                                            if (!usedValues.contains(attacking.value)) {
                                                usedValues.add(attacking.value);
                                            }
                                            binding.card21.setImageResource(attacking.img_res);
                                        }
                                        if (snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                            Card defending = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("defending").getValue().toString());
                                            if (!usedValues.contains(defending.value)) {
                                                usedValues.add(defending.value);
                                            }
                                            binding.card22.setImageResource(defending.img_res);
                                        }
                                        break;
                                    case 2:
                                        if (snapshot.child("_encounter").child("" + i).child("attacking").exists()) {
                                            Card attacking = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("attacking").getValue().toString());
                                            if (!usedValues.contains(attacking.value)) {
                                                usedValues.add(attacking.value);
                                            }
                                            binding.card31.setImageResource(attacking.img_res);
                                        }
                                        if (snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                            Card defending = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("defending").getValue().toString());
                                            if (!usedValues.contains(defending.value)) {
                                                usedValues.add(defending.value);
                                            }
                                            binding.card32.setImageResource(defending.img_res);
                                        }
                                        break;
                                    case 3:
                                        if (snapshot.child("_encounter").child("" + i).child("attacking").exists()) {
                                            Card attacking = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("attacking").getValue().toString());
                                            if (!usedValues.contains(attacking.value)) {
                                                usedValues.add(attacking.value);
                                            }
                                            binding.card41.setImageResource(attacking.img_res);
                                        }
                                        if (snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                            Card defending = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("defending").getValue().toString());
                                            if (!usedValues.contains(defending.value)) {
                                                usedValues.add(defending.value);
                                            }
                                            binding.card42.setImageResource(defending.img_res);
                                        }
                                        break;
                                    case 4:
                                        if (snapshot.child("_encounter").child("" + i).child("attacking").exists()) {
                                            Card attacking = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("attacking").getValue().toString());
                                            if (!usedValues.contains(attacking.value)) {
                                                usedValues.add(attacking.value);
                                            }
                                            binding.card51.setImageResource(attacking.img_res);
                                        }
                                        if (snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                            Card defending = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("defending").getValue().toString());
                                            if (!usedValues.contains(defending.value)) {
                                                usedValues.add(defending.value);
                                            }
                                            binding.card52.setImageResource(defending.img_res);
                                        }
                                        break;
                                    case 5:
                                        if (snapshot.child("_encounter").child("" + i).child("attacking").exists()) {
                                            Card attacking = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("attacking").getValue().toString());
                                            if (!usedValues.contains(attacking.value)) {
                                                usedValues.add(attacking.value);
                                            }
                                            binding.card61.setImageResource(attacking.img_res);
                                        }
                                        if (snapshot.child("_encounter").child("" + i).child("defending").exists()) {
                                            Card defending = AppMethods.cardLink(snapshot.child("_encounter").child("" + i)
                                                    .child("defending").getValue().toString());
                                            if (!usedValues.contains(defending.value)) {
                                                usedValues.add(defending.value);
                                            }
                                            binding.card62.setImageResource(defending.img_res);
                                        }
                                        break;
                                }
                            }
                        } else {
                            binding.card11.setImageDrawable(null);
                            binding.card12.setImageDrawable(null);
                            binding.card21.setImageDrawable(null);
                            binding.card22.setImageDrawable(null);
                            binding.card31.setImageDrawable(null);
                            binding.card32.setImageDrawable(null);
                            binding.card41.setImageDrawable(null);
                            binding.card42.setImageDrawable(null);
                            binding.card51.setImageDrawable(null);
                            binding.card52.setImageDrawable(null);
                            binding.card61.setImageDrawable(null);
                            binding.card62.setImageDrawable(null);
                        }
                        //endregion encounter UI
                        //region lastChosen UI
                        if (snapshot.child("_chosenLast").exists()) {
                            Card lastChosen = AppMethods.cardLink(snapshot.child("_chosenLast").getValue().toString());
                            View view = binding.buttonBar.getChildAt(2);
                            if (view instanceof ViewGroup) {
                                ImageView imageView = ((ViewGroup) view).findViewById(R.id.image);
                                if (imageView != null) {
                                    imageView.setImageResource(lastChosen.img_res);
                                }
                            }

                        }
                        //endregion lastChosen UI
                        //region leftInStack UI
                        if (snapshot.child("_stack").exists()) {
                            long count = snapshot.child("_stack").getChildrenCount();
                            binding.leftInStack.setText("" + count);
                        }
                        //endregion leftInStack UI
                        //region endGame, Loser
                        if (endGame) {
                            boolean allEnded = true;
                            for (String player : InRoomPlayers[0]) {
                                if (!snapshot.child(player).child("profit").exists()) {
                                    allEnded = false;
                                }
                            }
                            if (allEnded) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        roomRef.removeEventListener(inGameListener);
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
                                        //endregion endGameTable
                                        if (playerName.equals(adminName)) {
                                            roomRef.removeValue();
                                        }
                                    }
                                }, 2000);
                            }
                        }
                        //endregion endGame, Loser
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
            }
        });
    }

    private View.OnDragListener getDefendDragListener(Card attackingCard, int pair, DataSnapshot snapshot) {
        return new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        return true;
                    case DragEvent.ACTION_DROP:
                        if (snapshot.child("_chosenLast").exists()) {
                            boolean beats;
                            Card chosen = AppMethods.cardLink(snapshot.child("_chosenLast").getValue().toString());
                            char chosenSuit = chosen.suit;
                            handUpdatePermission = false;
                            String cardName = event.getClipData().getItemAt(0).getText().toString();
                            Card draggedCard = AppMethods.cardLink(cardName);
                            assert draggedCard != null;
                            if (attackingCard.suit == draggedCard.suit) {
                                beats = getValueOfCard(draggedCard) > getValueOfCard(attackingCard);
                            } else {
                                beats = draggedCard.suit == chosenSuit;
                            }
                            if (beats) {
                                ArrayList<String> rearrangedHand = new ArrayList<>();
                                for (DataSnapshot cardSnapshot : snapshot.child(playerName).child("hand").getChildren()) {
                                    if (!cardSnapshot.getValue().toString().equals(draggedCard.toString())) {
                                        rearrangedHand.add(cardSnapshot.getValue().toString());
                                    }
                                }
                                roomRef.child(playerName).child("hand").removeValue();
                                for (String card : rearrangedHand) {
                                    roomRef.child(playerName).child("hand")
                                            .child(String.valueOf(rearrangedHand.indexOf(card))).setValue(card);
                                }
                                roomRef.child("_encounter").child("" + pair)
                                        .child("defending").setValue(draggedCard.toString());
                                handUpdatePermission = true;
                                return true;
                            }
                        }
                        return false;
                    default:
                        break;
                }

                return false;

            }
        };
    }

    private int nextPlayerPos(int pos, DataSnapshot snapshot) {
        ArrayList<Integer> winnersPosition = new ArrayList<>();
        if (snapshot.child("_winnersPositions").exists()) {
            for (DataSnapshot winnerSnapshot : snapshot.child("_winnersPositions").getChildren()) {
                winnersPosition.add(parseInt(winnerSnapshot.getValue().toString()));
            }
        }
        boolean flag = false;
        int new_pos = AppMethods.nextPlayer(size[0], pos);
        while (!flag) {
            if (winnersPosition.contains(new_pos)) {
                new_pos = AppMethods.nextPlayer(size[0], new_pos);
            } else {
                flag = true;
            }
        }
        return new_pos;
    }

    public static void onGameStart(Context context, FragmentFoolGameBinding binding, WindowManager windowManager) {
        uiCreate(context, binding, windowManager);
        GameFragment.isInGame = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (playerName.equals(adminName)) {
                    roomRef.child("_ChoosingPlayer").setValue((int) (Math.random() * (size[0] - 1 + 1)) + 1);
                    deck.clear();
                    deck.addAll(Arrays.asList(AppMethods.raw_deck));
                    Collections.shuffle(deck);
                    for (String player : InRoomPlayers[0]) {
                        if (!player.equals("_access") && !player.equals("_size") && !player.equals("_messages") && !player.equals("_stack")) {
                            for (int i = 0; i < 6; i++) {
                                Card chosen = deck.get(0);
                                deck.remove(chosen);
                                roomRef.child(player).child("hand").child(String.valueOf(i))
                                        .setValue(chosen.toString());
                            }
                        }
                    }
                    Card chosenLast = deck.get(0);
                    roomRef.child("_chosenLast").setValue(chosenLast.toString());
                    int count = 0;
                    for (Card card : deck) {
                        roomRef.child("_stack").child("" + count).setValue(card.toString());
                        count++;
                    }
                }
                binding.leftInStackText.setVisibility(View.VISIBLE);
            }
        }, 1700);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gameStarted = true;
            }
        }, 3500);
        binding.message.setText("game has started!");
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(2400);
        binding.message.startAnimation(fadeOut);
        binding.message.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("ResourceAsColor")
    private static void uiCreate(Context context, FragmentFoolGameBinding binding, WindowManager windowManager) {
        LinearLayout.LayoutParams params = new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(8, 8, 8, 8);
        binding.buttonBar.removeAllViews();
        MaterialButton take = new MaterialButton(context);
        MaterialButton ok = new MaterialButton(context);
        lastCard = SmallCardLayoutBinding.inflate(LayoutInflater.from(context));
        take.setCornerRadius(9999);
        ok.setCornerRadius(9999);
        take.setText("TAKE");
        ok.setText("OK");
        take.setTextColor(R.color.black);
        ok.setTextColor(R.color.black);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;
        float smallestWidth = Math.min(widthDp, heightDp);
        float density = context.getResources().getDisplayMetrics().density;
        if (smallestWidth > 600) {
            take.setTextSize(25);
            ok.setTextSize(25);
        }
        ok.setEnabled(false);
        take.setEnabled(false);
        binding.buttonBar.addView(take, 0, params);
        binding.buttonBar.addView(ok, 1, params);
        binding.buttonBar.addView(lastCard.getRoot(), 2, params);
        binding.ShowBet.setText("" + 5000);
    }

    @SuppressLint("ResourceAsColor")
    private void uiDestroy(Context context, FragmentFoolGameBinding binding) {
        binding.buttonBar.removeAllViews();
        binding.hand.removeAllViews();
        ViewGroup.LayoutParams params = new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        MaterialButton Disconnect = new MaterialButton(context);
        Disconnect.setCornerRadius((int) context.getResources().getDimension(android.R.dimen.thumbnail_height));
        Disconnect.setTextColor(R.color.black);
        Disconnect.setText("Disconnect");
        Disconnect.setTextSize(20);
        binding.buttonBar.addView(Disconnect, params);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.buttonBar.getChildAt(0).setEnabled(true);
            }
        }, 1500);
        roomRef.child(playerName).child("status").setValue("ended");
        binding.buttonBar.getChildAt(0).setOnClickListener(view -> {
            AppMethods.Disconnect(roomRef, playerName, inGameListener, ChatFragment.listener);
            assert getParentFragment() != null;
            GameChooseActivity.fragmentManager.beginTransaction().remove(getParentFragment()).commit();
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
                return 11;
            case "d":
                return 12;
            case "k":
                return 13;
            case "a":
                return 14;
            default:
                return 0;
        }
    }

    private void SetStatusToReady() {
        roomRef.child(playerName).child("status").setValue("ready");
        binding.ready.setEnabled(false);
    }

    public static void notifyPlayer() {
        if (GameFragment.viewPager2.getCurrentItem() != 1) {
            handler.post(new Runnable() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void run() {
                    Snackbar snackbar = Snackbar.make(binding.scrollView2, "New message received", Snackbar.LENGTH_LONG);
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