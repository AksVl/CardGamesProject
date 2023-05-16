package com.akscardgames.cardgamesproject.gamesRelated.gameFragments;


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
import com.example.cardgamesproject.databinding.PlayerItemBinding;
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
import java.util.Objects;


public class FoolGame extends Fragment {
    //region variables
    private static FragmentFoolGameBinding binding;
    private static FragmentManager fm;
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
    private boolean gameStatusUpdatePermission = true;
    private boolean[] someoneChecking = {false};
    private boolean imTheWinner = false;

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
                        /*AppMethods.foolReadyCheck(listener, InGameListener,
                                InRoomPlayers[0], RoomRef, readyCount, size[0],
                                binding, getContext(), getActivity().getWindowManager());*/
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                /*inGameListener = new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int pos;
                        View.OnDragListener dragListener1 = new View.OnDragListener() {
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
                        };
                        //region choosingPlayer reading
                        if (snapshot.child("_ChoosingPlayer").exists()) {
                            choosingPlayerPos = parseInt(snapshot.child("_ChoosingPlayer").getValue().toString());
                        }
                        //endregion choosingPlayer reading
                        //region if player wins without checking
                        if (snapshot.child(playerName).child("status").exists()
                                && snapshot.child(playerName).child("status").getValue().toString().equals("Out")) {
                            //region win
                            binding.message.setText("You have won!");
                            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                            fadeIn.setDuration(800);
                            binding.message.startAnimation(fadeIn);
                            binding.message.setVisibility(View.VISIBLE);
                            roomRef.child("_winnersPositions").child(playerName).setValue(my_pos);
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
                        if (!endGame) {
                            //region UI access managing
                            if (snapshot.child(playerName).child("status").exists()) {
                                if (my_pos == choosingPlayerPos) {
                                    roomRef.child(playerName).child("status").setValue("attacking");
                                    final int[] pairCount = {0};
                                    if (snapshot.child("_encounter").exists()) {
                                        pairCount[0] = (int) snapshot.child("_encounter").getChildrenCount();
                                    }
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
                                } else if (my_pos == nextPlayerPos(my_pos, snapshot)) {
                                    roomRef.child(playerName).child("status").setValue("defending");
                                    if (snapshot.child("_encounter").exists()) {
                                        int pairs = (int) snapshot.child("_encounter").getChildrenCount();
                                        for (int i = 0; i < pairs; i++) {
                                            Card attackingCard = AppMethods.cardLink(snapshot.child("_encounter").child(i + "")
                                                    .child("attacking").getValue().toString());
                                            View.OnDragListener listener = getDefendDragListener(attackingCard, i, snapshot);
                                            switch (i){
                                                case 0:
                                                    binding.pair1.setOnDragListener(listener);
                                                    break;
                                                case 1:
                                                    binding.pair2.setOnDragListener(listener);
                                                    break;
                                                case 2:
                                                    binding.pair3.setOnDragListener(listener);
                                                    break;
                                                case 3:
                                                    binding.pair4.setOnDragListener(listener);
                                                    break;
                                                case 4:
                                                    binding.pair5.setOnDragListener(listener);
                                                    break;
                                                case 5:
                                                    binding.pair6.setOnDragListener(listener);
                                                    break;
                                            }
                                        }
                                    }
                                } else {
                                    roomRef.child(playerName).child("status").setValue("waiting");
                                }
                            }
                        }
                        //endregion UI access managing
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
                                    if (gotStatus.equals("checking")
                                            && binding.buttonBar.getChildAt(0) != null
                                            && binding.buttonBar.getChildAt(1) != null
                                            && binding.buttonBar.getChildAt(2) != null
                                            && binding.buttonBar.getChildAt(3) != null) {
                                        someoneChecking[0] = true;
                                        binding.buttonBar.getChildAt(0).setEnabled(false);
                                        binding.buttonBar.getChildAt(1).setEnabled(false);
                                        ((TextView) binding.buttonBar.getChildAt(2)).setText("-");
                                        binding.buttonBar.getChildAt(3).setEnabled(false);
                                        if (gotStatus.equals("Out")) {
                                            status.setTextColor(Color.GREEN);
                                        } else if (gotStatus.equals("Lost")) {
                                            status.setTextColor(Color.RED);
                                        }
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
                }*/

                ;
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
                    roomRef.child("_ChoosingPlayer").setValue(1);
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
                    Card chosenLast = deck.get(deck.size() - 1);
                    roomRef.child("_chosenLast").setValue(chosenLast.toString());
                    int count = 0;
                    for (Card card : deck) {
                        roomRef.child("_stack").child("" + count).setValue(card.toString());
                        count++;
                    }
                }
            }
        }, 1700);
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
        ShapeableImageView lastCard = new ShapeableImageView(context);
        ShapeAppearanceModel shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCornerSizes(8)
                .build();
        lastCard.setShapeAppearanceModel(shapeAppearanceModel);
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
        binding.buttonBar.addView(lastCard, 1, new LinearLayout.LayoutParams((int) (60 * density), (int) (80 * density), 1.0f));
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