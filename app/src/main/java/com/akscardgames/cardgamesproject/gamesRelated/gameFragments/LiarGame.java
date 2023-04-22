package com.akscardgames.cardgamesproject.gamesRelated.gameFragments;


import static java.lang.Integer.parseInt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akscardgames.cardgamesproject.gamesRelated.ChatFragment;
import com.akscardgames.cardgamesproject.general.AppMethods;
import com.akscardgames.cardgamesproject.general.Card;
import com.akscardgames.cardgamesproject.menu.GameChooseActivity;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.FragmentLiarGameBinding;
import com.example.cardgamesproject.databinding.PlayerItemBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class LiarGame extends Fragment {
    //region variables
    private FragmentLiarGameBinding binding;
    private static FragmentManager fm;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    private static DatabaseReference RoomRef;
    private static String RoomName;
    private static String playerName;
    private static String adminName;
    private ValueEventListener listener;
    private ValueEventListener InGameListener;
    private int ChoosingPlayerPos;
    private static int available = 5000;
    private boolean MainGameLoop = false;
    private boolean OnceCheckFlag = true;
    private boolean OnceStart = true;
    private static boolean LoopEnding = false;
    private static boolean IsInGame = false;
    private final boolean[] Return = {false};
    private static boolean EndGame = false;
    private static final ArrayList<Card> deck = new ArrayList<>();
    private final static int[] size = new int[1];
    private static int my_pos;
    private final static ArrayList<String>[] InRoomPlayers = new ArrayList[]{new ArrayList<>()};

    private static final Handler handler = new Handler();
    private static String status = "";
    private static int AvailableBuff;

    public static String roomNameBuff;
    //endregion variables


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLiarGameBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //binding.available.setText(String.valueOf(available));
        fm = getParentFragmentManager();
        RoomName = roomNameBuff;
        SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", 0);
        playerName = prefs.getString("name", "");
        RoomRef = database.getReference("TwentyOneRooms/" + RoomName);
        binding.ready.setEnabled(false);
        final boolean[] OnceAnimated = {true};
        final long[] EndGameDelay = {3000};
        binding.ready.setOnClickListener(v ->
                SetStatusToReady());
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
                    InRoomPlayers[0].add(d.getKey());
                }
                int pos;
                if (snapshot.getChildrenCount() - 2 == size[0] &&
                        snapshot.child(playerName).child("status").exists() &&
                        !snapshot.child(playerName).child("status").getValue().toString().equals("ready")) {
                    binding.ready.setEnabled(true);
                } else {
                    binding.ready.setEnabled(false);
                    if (snapshot.child(playerName).child("status").exists()
                            && snapshot.child(playerName).child("status").getValue().toString().equals("ready")
                            && snapshot.getChildrenCount() - 2 != size[0]) {
                        RoomRef.child(playerName).child("status").setValue("joined");
                    }
                }
                if (snapshot.child(playerName).child("position").exists()) {
                    my_pos = parseInt(snapshot.child(playerName).child("position").getValue().toString());
                    for (String player : InRoomPlayers[0]) {
                        if (!snapshot.child(player).equals("_size") && !player.equals("_access") && snapshot.child(player).child("position").exists()) {
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
                AppMethods.liarReadyCheck(listener, InGameListener,
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
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

    public static void onGameStart(Context context, FragmentLiarGameBinding binding, WindowManager windowManager) {
        AvailableBuff = available;
        IsInGame = true;
        //TODO
    }

    @SuppressLint("ResourceAsColor")
    private static void UiCreate(Context context, FragmentLiarGameBinding binding, WindowManager windowManager) {
        //TODO
    }

    @SuppressLint("ResourceAsColor")
    private void UiDestroy(Context context, FragmentLiarGameBinding binding) {

    }

    private void SetStatusToReady() {
        RoomRef.child(playerName).child("status").setValue("ready");
        binding.ready.setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (IsInGame) {
            RoomRef.child("_offline").setValue(playerName);
            AppMethods.Disconnect(RoomRef, playerName, InGameListener, ChatFragment.listener);
            GameChooseActivity.fragmentManager.beginTransaction().remove(this).commit();
        } else {
            AppMethods.Disconnect(RoomRef, playerName, listener, ChatFragment.listener);
            GameChooseActivity.fragmentManager.beginTransaction().remove(this).commit();
        }
    }
}