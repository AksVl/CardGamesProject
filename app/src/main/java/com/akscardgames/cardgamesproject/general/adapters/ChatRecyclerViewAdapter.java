package com.akscardgames.cardgamesproject.general.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.akscardgames.cardgamesproject.general.Message;
import com.example.cardgamesproject.R;
import com.example.cardgamesproject.databinding.MessageItemBinding;

import java.util.ArrayList;
import java.util.TreeMap;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder> {
    Context context;
    ArrayList<Message> messages;
    String playerName;

    public ChatRecyclerViewAdapter(Context context, ArrayList<Message> messages, String playerName) {
        this.context = context;
        this.messages = messages;
        this.playerName = playerName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.message_item,parent,false);
        return new ChatRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(messages.get(position).getAuthor().equals(playerName)){
            holder.parent.setGravity(Gravity.END);
        } else{
            holder.parent.setGravity(Gravity.START);
        }
        holder.author.setText(messages.get(position).getAuthor());
        holder.message.setText(messages.get(position).getMessage());
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout parent;
        TextView author;
        TextView message;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = (LinearLayout) itemView.findViewById(R.id.parent);
            author = (TextView) itemView.findViewById(R.id.author);
            message = (TextView) itemView.findViewById(R.id.message);
        }
    }
}
