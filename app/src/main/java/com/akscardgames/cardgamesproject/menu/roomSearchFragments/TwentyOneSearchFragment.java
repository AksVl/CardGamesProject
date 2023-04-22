package com.akscardgames.cardgamesproject.menu.roomSearchFragments;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akscardgames.cardgamesproject.gameActivities.TwentyOneGame;
import com.akscardgames.cardgamesproject.general.AppMethods;
import com.akscardgames.cardgamesproject.general.RoomData;
import com.akscardgames.cardgamesproject.menu.GameChooseActivity;
import com.akscardgames.cardgamesproject.menu.adapters.RecyclerViewAdapter;
import com.akscardgames.cardgamesproject.menu.dialogFragments.CreatePasswordFragment;
import com.akscardgames.cardgamesproject.menu.dialogFragments.PasswordRequestFragment;
import com.example.cardgamesproject.databinding.FragmentTwentyOneSearchBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class TwentyOneSearchFragment extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference GameRef;
    static DatabaseReference RoomRef;
    static String playerName = "";
    static String RoomName = "";
    ArrayList<RoomData> AvailableRooms = new ArrayList<RoomData>();
    RecyclerView recyclerView;
    public static String password;

    public static FragmentTwentyOneSearchBinding binding;
    public static RoomData roomDataBuff;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTwentyOneSearchBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = binding.list;
        binding.create.setEnabled(true);
        SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences("PREFS", 0);
        playerName = prefs.getString("name", "");
        RoomName = playerName + "_Room";
        ShowAvailable();
        binding.sizePicker.setMinValue(2);
        binding.sizePicker.setMaxValue(4);
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.sizePicker.setTextSize(px);
        }
        binding.create.setOnClickListener(v -> CreateNewRoom());
    }

    private void CreateNewRoom() {
        if(!binding.privateCheck.isChecked()) {
            RoomRef = database.getReference("TwentyOneRooms/" + RoomName);
            RoomRef.child("_size").setValue(binding.sizePicker.getValue());
            RoomRef.child("_access").setValue("public");
            binding.create.setEnabled(false);
            RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        GameChooseActivity.launchTwentyOne(RoomName, playerName);
                        RoomRef.child(playerName).child("status").setValue("joined");
                        RoomRef.child(playerName).child("position").setValue(1);
                    } else {
                        binding.create.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //idk what to do here
                }
            });
        }else{
            RoomRef = database.getReference("TwentyOneRooms/" + RoomName);
            CreatePasswordFragment dialog = new CreatePasswordFragment();
            FragmentManager fm = getParentFragmentManager();
            dialog.show(fm.beginTransaction().addToBackStack(null), null);
        }
    }

    private void ShowAvailable() {
        GameRef = database.getReference("TwentyOneRooms");
        GameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AvailableRooms.clear();
                Iterable<DataSnapshot> rooms = snapshot.getChildren();
                for (DataSnapshot d : rooms) {
                    final RoomData[] roomData = new RoomData[1];
                    if (d.exists() && d.child("_size").exists() && d.child("_access").exists()
                            && d.getKey() != null) {
                        String name = d.getKey();
                        int size = parseInt(d.child("_size").getValue().toString());
                        int playerCount = (int) (d.getChildrenCount() - 2);
                        String mode = d.child("_access").getValue().toString();
                        roomData[0] = new RoomData(name, size, playerCount, mode);
                    }
                    if (roomData[0] != null) {
                        if (roomData[0].getPlayerCount() < roomData[0].getSize()) {
                            AvailableRooms.add(roomData[0]);
                        }
                    }
                }
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(), AvailableRooms, new RecyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(RoomData roomData) {
                        RoomName = roomData.getName();
                        final int size = roomData.getSize();
                        final int count = roomData.getPlayerCount();
                        RoomRef = database.getReference("TwentyOneRooms/" + RoomName);
                        if (count < size) {
                            if (roomData.getAccess().equals("public")) {
                                RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int AvailablePosition = AppMethods.getPosition(snapshot, size);
                                        GameChooseActivity.launchTwentyOne(RoomName, playerName);
                                        RoomRef.child(playerName).child("status").setValue("joined");
                                        RoomRef.child(playerName).child("position").setValue(AvailablePosition);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            } else {
                                roomDataBuff = roomData;
                                RoomRef = database.getReference("TwentyOneRooms/" + RoomName);
                                PasswordRequestFragment dialog = new PasswordRequestFragment();
                                dialog.password = roomData.getAccess();
                                FragmentManager fm = getParentFragmentManager();
                                dialog.show(fm.beginTransaction().addToBackStack(null), null);
                            }
                        } else {
                            Toast.makeText(getContext(), "Room is full", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //idk what to do here
            }
        });
    }

    public static void connectPrivateRoom() {
        RoomName = roomDataBuff.getName();
        final int size = roomDataBuff.getSize();
        final int count = roomDataBuff.getPlayerCount();
        RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int AvailablePosition = AppMethods.getPosition(snapshot, size);
                GameChooseActivity.launchTwentyOne(RoomName, playerName);
                RoomRef.child(playerName).child("status").setValue("joined");
                RoomRef.child(playerName).child("position").setValue(AvailablePosition);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static void createPrivateRoom(String password){
        RoomRef.child("_size").setValue(binding.sizePicker.getValue());
        RoomRef.child("_access").setValue(password);
        binding.create.setEnabled(false);
        RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    GameChooseActivity.launchTwentyOne(RoomName, playerName);
                    RoomRef.child(playerName).child("status").setValue("joined");
                    RoomRef.child(playerName).child("position").setValue(1);
                } else {
                    binding.create.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //idk what to do here
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.create.setEnabled(true);
    }
}