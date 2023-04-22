package com.akscardgames.cardgamesproject.menu.roomSearchFragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akscardgames.cardgamesproject.gameActivities.FoolGame;
import com.akscardgames.cardgamesproject.general.AppMethods;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.FragmentFoolSearchBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class FoolSearchFragment extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference GameRef;
    DatabaseReference RoomRef;
    String playerName = "";
    String RoomName = "";
    ArrayList<String> AvailableRooms = new ArrayList<String>();
    ListView listView;
    private FragmentFoolSearchBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFoolSearchBinding.inflate(getLayoutInflater());
        //return inflater.inflate(R.layout.fragment_fool_search, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = requireView().findViewById(R.id.list);
        binding.sizePicker.setMinValue(2);
        binding.sizePicker.setMaxValue(6);
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.sizePicker.setTextSize(px);
        }
        SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences("PREFS", 0);
        playerName = prefs.getString("name","");
        RoomName = playerName+"_Room";
        ShowAvailable();
        //temporary
        binding.create.setEnabled(false);
        //binding.create.setOnClickListener(v -> CreateNewRoom());
        //temporary
        binding.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RoomName = AvailableRooms.get(i);
                final int[] size = new int[1];
                final int[] count = {0};
                final int[] AvailablePosition = new int[1];
                RoomRef = database.getReference("FoolRooms/" + RoomName);
                RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        size[0] = Integer.parseInt(snapshot.child("_size").getValue().toString());
                        count[0] = (int) snapshot.getChildrenCount();
                        AvailablePosition[0] = AppMethods.getPosition(snapshot,size[0]);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //nothing
                    }
                });
                RoomRef = database.getReference("FoolRooms/" + RoomName + "/" + playerName);
                RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(count[0] - 1 < size[0]){
                            Intent intent = new Intent(getContext(), FoolGame.class);
                            intent.putExtra("RoomName", RoomName);
                            intent.putExtra("playerName", playerName);
                            startActivity(intent);
                            RoomRef.child("status").setValue("joined");
                            RoomRef.child("position").setValue(AvailablePosition[0]);
                        }
                        else{
                            Toast.makeText(getContext(),"Room is full", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //idk what to do here
                    }
                });
            }
        });
    }
    private void CreateNewRoom() {
        RoomRef = database.getReference("FoolRooms/" + RoomName + "/_size");
        RoomRef.setValue(binding.sizePicker.getValue());
        binding.create.setEnabled(false);
        RoomRef = database.getReference("FoolRooms/" + RoomName + "/" + playerName);
        RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.create.setEnabled(false);
                Intent intent = new Intent(getActivity(), FoolGame.class);
                intent.putExtra("RoomName",RoomName);
                intent.putExtra("playerName",playerName);
                startActivity(intent);
                RoomRef.child("status").setValue("joined");
                RoomRef.child("position").setValue(1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //idk what to do here
            }
        });
    }

    private void ShowAvailable(){
        GameRef = database.getReference("FoolRooms");
        GameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AvailableRooms.clear();
                Iterable<DataSnapshot> rooms = snapshot.getChildren();
                for (DataSnapshot d : rooms) {
                    AvailableRooms.add(d.getKey());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,AvailableRooms);
                listView.setAdapter(adapter);
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
        binding.create.setEnabled(false);
    }
}
