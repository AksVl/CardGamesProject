package com.akscardgames.cardgamesproject.gamesRelated.gameFragments;


import static java.lang.Integer.parseInt;
import static java.lang.Integer.signum;

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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
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
import com.example.cardgamesproject.databinding.FragmentLiarGameBinding;
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


public class LiarGame extends Fragment {
    //region variables
    private static FragmentLiarGameBinding binding;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    private static DatabaseReference roomRef;
    private static String roomName;
    private static String playerName;
    private static String adminName;
    private ValueEventListener listener;
    private ValueEventListener inGameListener;
    private static int choosingPlayerPos;
    private static String chosenValue;
    private static boolean endGame = false;
    private boolean loopStarted = false;
    private static final ArrayList<Card> deck = new ArrayList<>();
    private final static int[] size = new int[1];
    private static int my_pos;
    private final static ArrayList<String>[] InRoomPlayers = new ArrayList[]{new ArrayList<>()};

    private static Handler handler = new Handler();
    private static int bet = 5000;
    public static String roomNameBuff;
    private static boolean gameStarted = false;
    private boolean handUpdatePermission = true;
    private boolean statusUpdatePermission = true;
    private boolean someoneOffline = false;
    private boolean gameStatusUpdatePermission = true;
    private boolean[] someoneChecking = {false};
    private boolean imTheWinner = false;

    //endregion variables


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLiarGameBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GameFragment.viewPager2.setCurrentItem(1);
        adminName = null;
        choosingPlayerPos = 0;
        chosenValue = "";
        endGame = false;
        loopStarted = false;
        bet = 5000;
        gameStarted = false;
        handUpdatePermission = true;
        statusUpdatePermission = true;
        someoneOffline = false;
        gameStatusUpdatePermission = true;
        someoneChecking[0] = false;
        imTheWinner = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                roomName = roomNameBuff;
                SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", 0);
                playerName = prefs.getString("name", "");
                roomRef = database.getReference("LiarRooms/" + roomName);
                binding.ready.setEnabled(false);
                binding.ready.setOnClickListener(v -> {
                    roomRef.child(playerName).child("status").setValue("ready");
                    binding.ready.setEnabled(false);
                });
                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                            status.setText(getString(R.string.empty));
                            name.setText(getString(R.string.none));
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
                                            String outputStatus = getOutputStatus(gotStatus);
                                            status.setText(outputStatus);
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
                        AppMethods.liarReadyCheck(listener, inGameListener,
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
                        //region choosingPlayer reading
                        if (snapshot.child("_ChoosingPlayer").exists()) {
                            choosingPlayerPos = parseInt(snapshot.child("_ChoosingPlayer").getValue().toString());
                        }
                        //endregion choosingPlayer reading
                        //region if player wins without checking
                        if (snapshot.child(playerName).child("status").exists()
                                && snapshot.child(playerName).child("status").getValue().toString().equals("Out")) {
                            //region win
                            binding.message.setText(getString(R.string.you_have_won));
                            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                            fadeIn.setDuration(800);
                            binding.message.startAnimation(fadeIn);
                            binding.message.setVisibility(View.VISIBLE);
                            roomRef.child("_winnersPositions").child(playerName).setValue(my_pos);
                            roomRef.child(playerName).child("profit").setValue(5000 / (size[0] - 1));
                            binding.ShowBet.setText("+" + (5000 / (size[0] - 1)));
                            binding.betText.setText(getString(R.string.profit));
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
                        }
                        //endregion if player wins without checking
                        //region if only one is left in the game
                            if (snapshot.child("_winnersPositions").exists()
                                    && snapshot.child("_winnersPositions").getChildrenCount() == (size[0] - 1)) {
                                endGame = true;
                                if (!imTheWinner) {
                                    adminName = playerName;
                                    if (snapshot.child(playerName).child("status").exists()
                                            && !snapshot.child(playerName).child("status").getValue().toString().equals("Out")) {
                                        //region lose
                                        binding.message.setText(getString(R.string.you_have_lost));
                                        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                                        fadeIn.setDuration(800);
                                        binding.message.startAnimation(fadeIn);
                                        binding.message.setVisibility(View.VISIBLE);
                                        roomRef.child(playerName).child("profit").setValue(-5000);
                                        binding.ShowBet.setText("" + (-5000));
                                        binding.betText.setText(getString(R.string.profit));
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
                        //region chosenValue reading
                        if (snapshot.child("_chosenValue").exists()) {
                            gameStarted = true;
                            loopStarted = true;
                            chosenValue = snapshot.child("_chosenValue").getValue().toString();
                        } else {
                            loopStarted = false;
                            chosenValue = "-";
                        }
                        //endregion chosenValue reading
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
                        if (!endGame) {
                            //region UI access managing
                            if (snapshot.child(playerName).child("status").exists()) {
                                if (my_pos == choosingPlayerPos) {
                                    if (statusUpdatePermission) {
                                        if (loopStarted) {
                                            roomRef.child(playerName).child("status").setValue("choosing");
                                        } else {
                                            roomRef.child(playerName).child("status").setValue("starting");
                                        }
                                    }
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
                                                    int prevPos = AppMethods.prevPlayer(size[0], my_pos);
                                                    AppMethods.findPlayerByPos(roomRef, prevPos, new AppMethods.OnPlayerFoundListener() {
                                                        @Override
                                                        public void onPlayerFound(String player) {
                                                            if (snapshot.child(player).child("status").exists()
                                                                    && snapshot.child(player).child("status").getValue().toString().equals("finished")) {
                                                                roomRef.child(player).child("status").setValue("Out");
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError error) {

                                                        }
                                                    });
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
                                                    int count = 0;
                                                    if (snapshot.child("_stack").exists()) {
                                                        count = (int) snapshot.child("_stack").getChildrenCount();
                                                    } else {
                                                        String value = ((TextView) binding.buttonBar.getChildAt(2)).getText().toString();
                                                        roomRef.child("_chosenValue").setValue(value);
                                                    }
                                                    roomRef.child("_stack").child("" + count).setValue(draggedCard.toString());
                                                    roomRef.child("_lastThrown").setValue(playerName);
                                                    choosingPlayerPos = AppMethods.nextPlayer(size[0], choosingPlayerPos);
                                                    roomRef.child("_ChoosingPlayer").setValue(choosingPlayerPos);
                                                    binding.linearLayout3.setOnDragListener(null);
                                                    handUpdatePermission = true;
                                                    return true;
                                                default:
                                                    break;
                                            }

                                            return false;

                                        }
                                    });
                                    if (!loopStarted && binding.buttonBar.getChildAt(0) != null
                                            && binding.buttonBar.getChildAt(1) != null
                                            && binding.buttonBar.getChildAt(2) != null
                                            && binding.buttonBar.getChildAt(3) != null) {
                                        binding.buttonBar.getChildAt(0).setEnabled(false);
                                        binding.buttonBar.getChildAt(1).setEnabled(true);
                                        ((TextView) binding.buttonBar.getChildAt(2)).setText("6");
                                        binding.buttonBar.getChildAt(3).setEnabled(true);
                                    }
                                } else {
                                    if (statusUpdatePermission) {
                                        roomRef.child(playerName).child("status").setValue("waiting");
                                    }
                                    if (binding.buttonBar.getChildAt(2) != null
                                            && binding.buttonBar.getChildAt(1) != null
                                            && binding.buttonBar.getChildAt(3) != null) {
                                        ((TextView) binding.buttonBar.getChildAt(2)).setText("-");
                                        binding.buttonBar.getChildAt(1).setEnabled(false);
                                        binding.buttonBar.getChildAt(3).setEnabled(false);
                                    }
                                }
                            }
                            if (loopStarted
                                    && binding.buttonBar.getChildAt(0) != null
                                    && binding.buttonBar.getChildAt(1) != null
                                    && binding.buttonBar.getChildAt(2) != null
                                    && binding.buttonBar.getChildAt(3) != null) {
                                binding.buttonBar.getChildAt(1).setEnabled(false);
                                ((TextView) binding.buttonBar.getChildAt(2)).setText("-");
                                binding.buttonBar.getChildAt(3).setEnabled(false);
                                if (AppMethods.prevPlayer(size[0], choosingPlayerPos) != my_pos) {
                                    binding.buttonBar.getChildAt(0).setEnabled(true);
                                    binding.buttonBar.getChildAt(0).setOnClickListener(v -> {
                                        if (snapshot.child("_stack").exists() && snapshot.child("_chosenValue").exists()) {
                                            roomRef.child(playerName).child("status").setValue("checking");
                                            roomRef.child("_ChoosingPlayer").removeValue();
                                            long cardCount = snapshot.child("_stack").getChildrenCount();
                                            Card lastCard = AppMethods.cardLink(snapshot.child("_stack")
                                                    .child((cardCount - 1) + "").getValue().toString());
                                            String requiredValue = snapshot.child("_chosenValue").getValue().toString();
                                            gameStatusUpdatePermission = false;
                                            someoneChecking[0] = true;
                                            if (lastCard.value.equals(requiredValue)) {
                                                binding.gameStatus.setText(getString(R.string.missed));
                                            } else {
                                                binding.gameStatus.setText(getString(R.string.you_have_caught));
                                            }
                                            binding.stackImageView.setImageResource(lastCard.img_res);
                                            binding.stackSize.setVisibility(View.INVISIBLE);
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    someoneChecking[0] = false;
                                                    gameStatusUpdatePermission = true;
                                                    String lastThrown = snapshot.child("_lastThrown").getValue().toString();
                                                    if (snapshot.child("_stack").exists() && snapshot.child("_chosenValue").exists()
                                                            && snapshot.child("_lastThrown").exists()) {
                                                        binding.stackImageView.setImageResource(R.drawable.other_side);
                                                        binding.stackSize.setVisibility(View.VISIBLE);
                                                        String requiredValue = snapshot.child("_chosenValue").getValue().toString();
                                                        if (lastCard.value.equals(requiredValue)) {
                                                            int count = 0;
                                                            if (snapshot.child(playerName).child("hand").exists()) {
                                                                count = (int) snapshot.child(playerName).child("hand").getChildrenCount();
                                                            }
                                                            for (DataSnapshot d : snapshot.child("_stack").getChildren()) {
                                                                roomRef.child(playerName).child("hand").child("" + count).setValue(d.getValue().toString());
                                                                count++;
                                                            }
                                                            setNextChoosingPlayerPos(my_pos, snapshot);
                                                        } else {
                                                            if (snapshot.child(lastThrown).child("position").exists() &&
                                                                    snapshot.child(lastThrown).child("status").exists()) {
                                                                int lastThrownPos = parseInt(snapshot.child(lastThrown).child("position").getValue().toString());
                                                                int count = 0;
                                                                if (snapshot.child(lastThrown).child("hand").exists()) {
                                                                    count = (int) snapshot.child(lastThrown).child("hand").getChildrenCount();
                                                                }
                                                                for (DataSnapshot d : snapshot.child("_stack").getChildren()) {
                                                                    roomRef.child(lastThrown).child("hand").child("" + count).setValue(d.getValue().toString());
                                                                    count++;
                                                                }
                                                                setNextChoosingPlayerPos(lastThrownPos, snapshot);
                                                            }
                                                        }
                                                        if (statusUpdatePermission) {
                                                            roomRef.child(playerName).child("status").setValue("waiting");
                                                        }
                                                        roomRef.child("_stack").removeValue();
                                                        roomRef.child("_chosenValue").removeValue();
                                                    }
                                                }
                                            }, 1500);
                                        }
                                    });
                                } else {
                                    binding.buttonBar.getChildAt(0).setEnabled(false);
                                }
                            }
                            //endregion UI access managing
                            for (String player : InRoomPlayers[0]) {
                                if (!player.equals("_size") && !player.equals("_access") && !player.equals("_messages")
                                        && snapshot.child(player).child("position").exists()) {
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
                                        String outputStatus = getOutputStatus(gotStatus);
                                        status.setText(outputStatus);
                                        status.setTextColor(Color.WHITE);
                                        if (gotStatus.equals("checking")
                                                && binding.buttonBar.getChildAt(0) != null
                                                && binding.buttonBar.getChildAt(1) != null
                                                && binding.buttonBar.getChildAt(2) != null
                                                && binding.buttonBar.getChildAt(3) != null) {
                                            someoneChecking[0] = true;
                                            loopStarted = false;
                                            binding.buttonBar.getChildAt(0).setEnabled(false);
                                            binding.buttonBar.getChildAt(1).setEnabled(false);
                                            ((TextView) binding.buttonBar.getChildAt(2)).setText("-");
                                            binding.buttonBar.getChildAt(3).setEnabled(false);
                                            if (snapshot.child("_stack").exists() && snapshot.child("_chosenValue").exists()) {
                                                long cardCount = snapshot.child("_stack").getChildrenCount();
                                                Card lastCard = AppMethods.cardLink(snapshot.child("_stack")
                                                        .child((cardCount - 1) + "").getValue().toString());
                                                String requiredValue = snapshot.child("_chosenValue").getValue().toString();
                                                statusUpdatePermission = false;
                                                binding.gameStatus.setVisibility(View.VISIBLE);
                                                if (lastCard.value.equals(requiredValue)) {
                                                    binding.gameStatus.setText(getString(R.string.checked_one_wasnt_the_liar));
                                                } else {
                                                    binding.gameStatus.setText(getString(R.string.liar_has_been_caught));
                                                }
                                                binding.stackImageView.setImageResource(lastCard.img_res);
                                            }
                                            binding.stackSize.setVisibility(View.INVISIBLE);
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    someoneChecking[0] = false;
                                                    statusUpdatePermission = true;
                                                }
                                            }, 1000);
                                        }
                                        if (gotStatus.equals("Out")) {
                                            status.setTextColor(Color.GREEN);
                                        } else if (gotStatus.equals("Lost")) {
                                            status.setTextColor(Color.RED);
                                        }
                                    }
                                    // endregion player's status update
                                }
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
                                                if (!loopStarted && !someoneChecking[0]) {
                                                    imTheWinner = true;
                                                    GameFragment.isInGame = false;
                                                    //region win
                                                    binding.message.setText(getString(R.string.you_have_won));
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
                                                }
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
                                                            Card c1 = AppMethods.cardLink(o1);
                                                            Card c2 = AppMethods.cardLink(o2);
                                                            int[] real_value = {6, 7, 8, 9, 10, 11, 12, 13, 14};
                                                            String[] string_value = {"6", "7", "8", "9", "10", "j", "d", "k", "a"};
                                                            int index1 = Arrays.asList(string_value).indexOf(c1.value);
                                                            int index2 = Arrays.asList(string_value).indexOf(c2.value);
                                                            return real_value[index1] - real_value[index2];
                                                        }
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
                                                            if (my_pos == choosingPlayerPos) {
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
                                                            } else {
                                                                viewForCard.setOnLongClickListener(null);
                                                            }
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
                            //region stack/chosenValue UI
                            binding.chosenValue.setText(chosenValue);
                            if (loopStarted) {
                                binding.valueLayout.setVisibility(View.VISIBLE);
                            } else {
                                binding.valueLayout.setVisibility(View.INVISIBLE);
                            }
                            if (snapshot.child("_stack").exists()) {
                                int stackSize = (int) snapshot.child("_stack").getChildrenCount();
                                binding.stackLayout.setVisibility(View.VISIBLE);
                                binding.stackSize.setText(stackSize + "");
                            } else {
                                binding.stackImageView.setImageResource(R.drawable.other_side);
                                binding.stackSize.setVisibility(View.VISIBLE);
                                binding.stackLayout.setVisibility(View.INVISIBLE);
                                binding.stackLayout.setVisibility(View.INVISIBLE);
                            }
                            //endregion stack/chosenValue UI
                            //region gameStatus UI
                            String status;
                            if (snapshot.child(playerName).child("status").exists()) {
                                status = snapshot.child(playerName).child("status").getValue().toString();
                                if (gameStatusUpdatePermission) {
                                    if (status.equals("starting")) {
                                        binding.gameStatus.setVisibility(View.VISIBLE);
                                        binding.gameStatus.setText(getString(R.string.start_a_stack));
                                    } else if (status.equals("choosing")) {
                                        binding.gameStatus.setVisibility(View.VISIBLE);
                                        binding.gameStatus.setText(getString(R.string.your_turn_to_lie));
                                    } else if (someoneChecking[0]) {
                                        binding.gameStatus.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.gameStatus.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                            //endregion gameStatus UI
                        }
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
    private String getOutputStatus(String gotStatus) {
        switch (gotStatus) {
            case "joined":
                return getString(R.string.joined);
            case "ready":
                return getString(R.string.ready);
            case "waiting":
                return getString(R.string.waiting);
            case "ended":
                return getString(R.string.ended);
            case "ending":
                return getString(R.string.ending);
            case "choosing":
                return getString(R.string.choosing);
            case "starting":
                return getString(R.string.starting);
            case "takes more":
                return "-";
            case "passed":
                return getString(R.string.passed);
            case "betting":
                return getString(R.string.betting);
            case "gets more":
                return "-";
            case "TwentyOne":
                return getString(R.string.twenty_one);
            case "Won":
                return getString(R.string.won);
            case "Won all":
                return getString(R.string.won_all);
            case "Lost":
                return getString(R.string.lost);
            case "Lost all":
                return getString(R.string.lost_all);
            case "out":
                return getString(R.string.out);
            case "finished":
                return getString(R.string.finished);
            case "Out":
                return getString(R.string.out);
            case "checking":
                return getString(R.string.checking);
            case "sets bank":
                return getString(R.string.sets_bank);
            case "sets a bank":
                return "-";
            default:
                return null;
        }
    }

    private void setNextChoosingPlayerPos(int pos, DataSnapshot snapshot) {
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
        roomRef.child("_ChoosingPlayer").setValue(new_pos);
    }

    public static void onGameStart(Context context, FragmentLiarGameBinding binding, WindowManager windowManager) {
        uiCreate(context, binding, windowManager);
        GameFragment.isInGame = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (playerName.equals(adminName)) {
                    choosingPlayerPos = (int) ((int) (Math.random() * (size[0] - 1 + 1)) + 1);
                    roomRef.child("_ChoosingPlayer").setValue(choosingPlayerPos);
                    deck.clear();
                    deck.addAll(Arrays.asList(AppMethods.raw_deck));
                    Collections.shuffle(deck);
                    int amount = deck.size() / size[0];
                    for (String player : InRoomPlayers[0]) {
                        if (!player.equals("_access") && !player.equals("_size") && !player.equals("_messages") && !player.equals("_chosenValue")) {
                            for (int i = 0; i < amount; i++) {
                                Card chosen = deck.get(0);
                                deck.remove(chosen);
                                roomRef.child(player).child("hand").child(String.valueOf(i))
                                        .setValue(chosen.toString());
                            }
                        }
                    }
                }
            }
        }, 1700);
        binding.message.setText(R.string.game_has_started);
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(2400);
        binding.message.startAnimation(fadeOut);
        binding.message.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("ResourceAsColor")
    private static void uiCreate(Context context, FragmentLiarGameBinding binding, WindowManager windowManager) {
        LinearLayout.LayoutParams params = new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(8, 8, 8, 8);
        binding.buttonBar.removeAllViews();
        MaterialButton check = new MaterialButton(context);
        MaterialButton next = new MaterialButton(context);
        MaterialButton back = new MaterialButton(context);
        TextView value = new TextView(context);
        value.setGravity(Gravity.CENTER);
        check.setCornerRadius(9999);
        next.setCornerRadius(9999);
        back.setCornerRadius(9999);
        check.setText(context.getString(R.string.check));
        next.setText(">");
        back.setText("<");
        check.setTextColor(R.color.black);
        next.setTextColor(R.color.black);
        back.setTextColor(R.color.black);
        String[] values = {"6", "7", "8", "9", "10", "j", "d", "k", "a"};
        String[] valuesUI = {"6", "7", "8", "9", "10", "j", "d", "k", "a"};
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;
        float smallestWidth = Math.min(widthDp, heightDp);
        next.setTextSize(25);
        back.setTextSize(25);
        value.setTextColor(Color.WHITE);
        if (smallestWidth > 600) {
            check.setTextSize(25);
            value.setTextSize(40);
        } else {
            value.setTextSize(30);
        }
        value.setText(values[0]);
        next.setOnClickListener(v -> {
            int index = Arrays.asList(values).indexOf(value.getText().toString());
            if (index < Arrays.asList(values).size() - 1) {
                index++;
            } else {
                index = 0;
            }
            value.setText(values[index]);
        });
        back.setOnClickListener(v -> {
            int index = Arrays.asList(values).indexOf(value.getText().toString());
            if (index > 0) {
                index--;
            } else {
                index = 8;
            }
            value.setText(values[index]);
        });
        back.setEnabled(false);
        next.setEnabled(false);
        check.setEnabled(false);
        binding.buttonBar.addView(check, 0, params);
        binding.buttonBar.addView(back, 1, params);
        binding.buttonBar.addView(value, 2, params);
        binding.buttonBar.addView(next, 3, params);
        binding.ShowBet.setText("" + bet);
    }

    @SuppressLint("ResourceAsColor")
    private void uiDestroy(Context context, FragmentLiarGameBinding binding) {
        binding.buttonBar.removeAllViews();
        binding.gameStatus.setText("");
        binding.hand.removeAllViews();
        binding.stackLayout.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams params = new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        MaterialButton Disconnect = new MaterialButton(context);
        Disconnect.setCornerRadius((int) context.getResources().getDimension(android.R.dimen.thumbnail_height));
        Disconnect.setTextColor(R.color.black);
        Disconnect.setText(R.string.disconnect);
        Disconnect.setTextSize(20);
        binding.buttonBar.addView(Disconnect, params);
        binding.buttonBar.getChildAt(0).setOnClickListener(view -> {
            AppMethods.Disconnect(roomRef, playerName, inGameListener, ChatFragment.listener);
            assert getParentFragment() != null;
            GameChooseActivity.fragmentManager.beginTransaction().remove(getParentFragment()).commit();
        });
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