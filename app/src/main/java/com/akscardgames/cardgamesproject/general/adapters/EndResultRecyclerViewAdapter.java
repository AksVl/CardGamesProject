package com.akscardgames.cardgamesproject.general.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akscardgames.cardgamesproject.gamesRelated.GameFragment;
import com.akscardgames.cardgamesproject.gamesRelated.ResultData;
import com.akscardgames.cardgamesproject.general.RoomData;
import com.example.cardgamesproject.R;

import java.util.ArrayList;

public class EndResultRecyclerViewAdapter extends RecyclerView.Adapter<EndResultRecyclerViewAdapter.ViewHolder>{
    Context context;
    ArrayList<ResultData> resultsList;

    public EndResultRecyclerViewAdapter(Context context, ArrayList<ResultData> resultsList) {
        this.context = context;
        this.resultsList = resultsList;
    }

    @NonNull
    @Override
    public EndResultRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.endgame_table_item,parent,false);
        return new EndResultRecyclerViewAdapter.ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull EndResultRecyclerViewAdapter.ViewHolder holder, int position) {
        if(resultsList.get(position).getName() != null) {
            holder.num.setText((position + 1) + "");
            holder.name.setText(resultsList.get(position).getName());
            if(resultsList.get(position).getName().equals(GameFragment.playerName)){
                holder.name.setTextColor(R.color.backgroundGreen);
                holder.num.setTextColor(R.color.backgroundGreen);
            }
            int profit = resultsList.get(position).getProfit();
            if (profit > 0) {
                holder.profit.setTextColor(Color.GREEN);
                holder.profit.setText("+" + resultsList.get(position).getProfit());
            } else if (profit < 0) {
                holder.profit.setTextColor(Color.RED);
                holder.profit.setText("" + resultsList.get(position).getProfit());
            } else {
                holder.profit.setTextColor(R.color.DarkGrey);
                holder.profit.setText("" + resultsList.get(position).getProfit());
            }
        }
    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView num;
        TextView name;
        TextView profit;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            num = (TextView) itemView.findViewById(R.id.num);
            name = (TextView) itemView.findViewById(R.id.playerName);
            profit = (TextView) itemView.findViewById(R.id.profit);
        }
    }
}
