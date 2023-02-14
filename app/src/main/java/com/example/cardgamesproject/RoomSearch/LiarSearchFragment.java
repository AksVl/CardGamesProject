package com.example.cardgamesproject.RoomSearch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cardgamesproject.GameActivities.FoolGame;
import com.example.cardgamesproject.GameActivities.LiarGame;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.FragmentFoolSearchBinding;
import com.example.cardgamesproject.databinding.FragmentLiarSearchBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class LiarSearchFragment extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference GameRef;
    DatabaseReference RoomRef;
    String playerName = "";
    String RoomName = "";
    ArrayList<String> AvailableRooms = new ArrayList<String>();
    ListView listView;
    FragmentLiarSearchBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLiarSearchBinding.inflate(getLayoutInflater());
        //return inflater.inflate(R.layout.fragment_fool_search, container, false);
        return binding.getRoot();
        //
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = requireView().findViewById(R.id.list);
        binding.create.setEnabled(true);
        SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences("PREFS", 0);
        playerName = prefs.getString("name","");
        RoomName = playerName;
        ShowAvailable();
        binding.create.setOnClickListener(v -> CreateNewRoom());
        binding.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RoomName = AvailableRooms.get(i);
                RoomRef = database.getReference("LiarRooms/" + RoomName + "/" + playerName);
                RoomRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Intent intent = new Intent(getContext(), LiarGame.class);
                        intent.putExtra("RoomName/",RoomName);
                        startActivity(intent);
                        RoomRef.setValue("joined");
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
        //it crashes here
        binding.create.setEnabled(false);
        RoomRef = database.getReference("LiarRooms/"+RoomName);
        RoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.create.setEnabled(false);
                Intent intent = new Intent(getContext(), LiarGame.class);
                intent.putExtra("RoomName",RoomName);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //idk what to do here
            }
        });
        RoomRef.setValue(playerName);
    }

    private void ShowAvailable(){
        GameRef = database.getReference("LiarRooms");
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
}