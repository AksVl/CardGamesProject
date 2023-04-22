package com.akscardgames.cardgamesproject.menu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akscardgames.cardgamesproject.general.RoomData;
import com.example.cardgamesproject.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Context context;
    ArrayList<RoomData> roomList;
    ItemClickListener itemClickListener;

    public RecyclerViewAdapter(Context context, ArrayList<RoomData> roomList,ItemClickListener itemClickListener) {
        this.context = context;
        this.roomList = roomList;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_room_item,parent,false);
        return new RecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        if(roomList.get(position)!=null) {
            if (roomList.get(position).getName() != null && holder.name != null) {
                holder.name.setText(roomList.get(position).getName());
            }
            int size = roomList.get(position).getSize();
            int playerCounter = roomList.get(position).getPlayerCount();
            holder.playerCount.setText(String.valueOf(playerCounter) + "/" + String.valueOf(size));
            if (roomList.get(position).getAccess().equals("public")) {
                holder.mode.setImageResource(R.drawable.opened);
            } else {
                holder.mode.setImageResource(R.drawable.locked);
            }
            holder.itemView.setOnClickListener(v -> {
                itemClickListener.onItemClick(roomList.get(position));
            });
        }
    }
    public interface ItemClickListener{
        void onItemClick(RoomData roomData);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView playerCount;
        ImageView mode;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.roomName);
            playerCount = (TextView) itemView.findViewById(R.id.playerCounter);
            mode = (ImageView) itemView.findViewById(R.id.mode);
        }
    }
}
