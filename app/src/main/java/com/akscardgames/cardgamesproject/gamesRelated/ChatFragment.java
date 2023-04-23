package com.akscardgames.cardgamesproject.gamesRelated;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.akscardgames.cardgamesproject.gamesRelated.gameFragments.TwentyOneGame;
import com.akscardgames.cardgamesproject.general.Message;
import com.akscardgames.cardgamesproject.general.adapters.ChatRecyclerViewAdapter;
import com.example.cardgamesproject.databinding.FragmentChatBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;

public class ChatFragment extends Fragment {
    public static ValueEventListener listener;
    private FragmentChatBinding binding;
    private RecyclerView recyclerView;
    private boolean uiFlag = false;
    ChatRecyclerViewAdapter adapter;
    Handler handler = new Handler();
    private ArrayList<Message> chat = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        binding = FragmentChatBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GameFragment.viewPager2.setCurrentItem(0);
        handler.post(new Runnable() {
            @Override
            public void run() {
                binding.send.setOnClickListener(v -> sendMessage());
                recyclerView = binding.recyclerView;
                adapter = new ChatRecyclerViewAdapter(getContext(), chat, GameFragment.playerName);
                recyclerView.setAdapter(adapter);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                layoutManager.setStackFromEnd(true);
                recyclerView.setLayoutManager(layoutManager);
                showMessages();
            }
        });
    }

    private void sendMessage() {
        String text = binding.input.getText().toString();
        binding.input.setText("");
        if (!text.equals("")) {
            Instant now;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                now = Instant.now();
                long time = now.toEpochMilli();
                String id = GameFragment.RoomRef.child("_messages").push().getKey();
                Message message = new Message(GameFragment.playerName, text, time);
                GameFragment.RoomRef.child("_messages").child(id).setValue(message);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showMessages() {
        listener = GameFragment.RoomRef.child("_messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chat.clear();
                Iterable<DataSnapshot> messages = snapshot.getChildren();
                for (DataSnapshot d : messages) {
                    if (d.child("author").exists() && d.child("message").exists() && d.child("time").exists()) {
                        String author = d.child("author").getValue().toString();
                        String text = d.child("message").getValue().toString();
                        long time = Long.parseLong(d.child("time").getValue().toString());
                        Message message = new Message(author, text, time);
                        chat.add(message);
                    }
                }
                chat.sort(new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        if (o1.getTime() < o2.getTime()) {
                            return -1;
                        } else if (o1.getTime() > o2.getTime()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
                if(snapshot.exists() && !TwentyOneGame.chatUpdatePermission) {
                    TwentyOneGame.notifyPlayer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}