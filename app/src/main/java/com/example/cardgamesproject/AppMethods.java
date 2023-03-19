package com.example.cardgamesproject;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.cardgamesproject.GameActivities.Card;
import com.example.cardgamesproject.GameActivities.TwentyOneGame;
import com.example.cardgamesproject.databinding.ActivityTwentyOneGameBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AppMethods {
    static Card h6 = new Card("6", 'h', R.drawable.h6);
    static Card h7 = new Card("7", 'h', R.drawable.h7);
    static Card h8 = new Card("8", 'h', R.drawable.h8);
    static Card h9 = new Card("9", 'h', R.drawable.h9);
    static Card h10 = new Card("10", 'h', R.drawable.h10);
    static Card hj = new Card("j", 'h', R.drawable.hj);
    static Card hd = new Card("d", 'h', R.drawable.hd);
    static Card hk = new Card("k", 'h', R.drawable.hk);
    static Card ha = new Card("a", 'h', R.drawable.ha);

    static Card s6 = new Card("6", 's', R.drawable.s6);
    static Card s7 = new Card("7", 's', R.drawable.s7);
    static Card s8 = new Card("8", 's', R.drawable.s8);
    static Card s9 = new Card("9", 's', R.drawable.s9);
    static Card s10 = new Card("10", 's', R.drawable.s10);
    static Card sj = new Card("j", 's', R.drawable.sj);
    static Card sd = new Card("d", 's', R.drawable.sd);
    static Card sk = new Card("k", 's', R.drawable.sk);
    static Card sa = new Card("a", 's', R.drawable.sa);

    static Card d6 = new Card("6", 'd', R.drawable.d6);
    static Card d7 = new Card("7", 'd', R.drawable.d7);
    static Card d8 = new Card("8", 'd', R.drawable.d8);
    static Card d9 = new Card("9", 'd', R.drawable.d9);
    static Card d10 = new Card("10", 'd', R.drawable.d10);
    static Card dj = new Card("j", 'd', R.drawable.dj);
    static Card dd = new Card("d", 'd', R.drawable.dd);
    static Card dk = new Card("k", 'd', R.drawable.dk);
    static Card da = new Card("a", 'd', R.drawable.da);

    static Card c6 = new Card("6", 'c', R.drawable.c_six);
    static Card c7 = new Card("7", 'c', R.drawable.c_seven);
    static Card c8 = new Card("8", 'c', R.drawable.c_eight);
    static Card c9 = new Card("9", 'c', R.drawable.c_nine);
    static Card c10 = new Card("10", 'c', R.drawable.c_ten);
    static Card cj = new Card("j", 'c', R.drawable.cj);
    static Card cd = new Card("d", 'c', R.drawable.cd);
    static Card ck = new Card("k", 'c', R.drawable.ck);
    static Card ca = new Card("a", 'c', R.drawable.ca);

    public static Card[] TwentyOne_raw_deck = {
            c6, c7, c8, c9, c10, cj, cd, ck, ca,
            d6, d7, d8, d9, d10, dj, dd, dk, da,
            s6, s7, s8, s9, s10, sj, sd, sk, sa,
            h6, h7, h8, h9, h10, hj, hd, hk, ha};

    public static int getUiPosition(int my_position, int position, int size) {
        int uiPosition;
        if (position > my_position) {
            uiPosition = position - my_position - 1;
        }
        else {
            uiPosition = size - my_position + position - 1;
        }
        return uiPosition;
    }

    public static void Disconnect(DatabaseReference RoomRef, String playerName, ValueEventListener listener) {
        RoomRef.removeEventListener(listener);
        RoomRef.child(playerName).child("status").setValue("empty");
        RoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.child(playerName).getRef().removeValue();
                if (snapshot.getChildrenCount() == 1) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static int getPosition(DataSnapshot snapshot, int size) {
        ArrayList<Integer> PlayersPositions = new ArrayList<>();
        ArrayList<Integer> RoomPositions = new ArrayList<>();
        for (int j = 0; j < size; j++) {
            RoomPositions.add(j + 1);
        }
        ArrayList<String> InRoomPlayers = new ArrayList<>();
        for (DataSnapshot d : snapshot.getChildren()) {
            InRoomPlayers.add(d.getKey());
        }
        for (String player : InRoomPlayers) {
            if (!player.equals("_size") && snapshot.child(player).child("position").exists()) {
                PlayersPositions.add(Integer.parseInt(snapshot.child(player).child("position").getValue().toString()));
            }
        }

        for (int j = 0; j < RoomPositions.size(); j++) {
            if (!PlayersPositions.contains(RoomPositions.get(j))) {
                return RoomPositions.get(j);
            }
        }
        return -1;
    }

    public static void readyCheck(String game, ValueEventListener listener,
                                  ValueEventListener InGameListener,
                                  ArrayList<String> InRoomPlayers,
                                  DatabaseReference RoomRef, int readyCount, int IntentSize,
                                  ActivityTwentyOneGameBinding binding, Context context) {
        if (readyCount >= IntentSize) {
            binding.buttonBar.removeAllViews();
            for (String player : InRoomPlayers) {
                if (!player.equals("_size")) {
                    RoomRef.child(player).child("status").setValue("waiting");
                }
            }
            RoomRef.removeEventListener(listener);
            RoomRef.addValueEventListener(InGameListener);
            binding.gameStatus.setText("");
            switch (game) {
                case "TwentyOne":
                    TwentyOneGame.onGameStart(context, binding);
                case "Fool":
                    //Fool.onGameStart(context, binding);
                case "Liar":
                    //Liar.onGameStart(context, binding);
            }
        }
    }

    public static Card CardLink(String name) {
        switch (name) {
            case "c6": return TwentyOne_raw_deck[0];
            case "c7": return TwentyOne_raw_deck[1];
            case "c8": return TwentyOne_raw_deck[2];
            case "c9": return TwentyOne_raw_deck[3];
            case "c10": return TwentyOne_raw_deck[4];
            case "cj": return TwentyOne_raw_deck[5];
            case "cd": return TwentyOne_raw_deck[6];
            case "ck": return TwentyOne_raw_deck[7];
            case "ca": return TwentyOne_raw_deck[8];
            case "d6": return TwentyOne_raw_deck[9];
            case "d7": return TwentyOne_raw_deck[10];
            case "d8": return TwentyOne_raw_deck[11];
            case "d9": return TwentyOne_raw_deck[12];
            case "d10": return TwentyOne_raw_deck[13];
            case "dj": return TwentyOne_raw_deck[14];
            case "dd": return TwentyOne_raw_deck[15];
            case "dk": return TwentyOne_raw_deck[16];
            case "da": return TwentyOne_raw_deck[17];
            case "s6": return TwentyOne_raw_deck[18];
            case "s7": return TwentyOne_raw_deck[19];
            case "s8": return TwentyOne_raw_deck[20];
            case "s9": return TwentyOne_raw_deck[21];
            case "s10": return TwentyOne_raw_deck[22];
            case "sj": return TwentyOne_raw_deck[23];
            case "sd": return TwentyOne_raw_deck[24];
            case "sk": return TwentyOne_raw_deck[25];
            case "sa": return TwentyOne_raw_deck[26];
            case "h6": return TwentyOne_raw_deck[27];
            case "h7": return TwentyOne_raw_deck[28];
            case "h8": return TwentyOne_raw_deck[29];
            case "h9": return TwentyOne_raw_deck[30];
            case "h10": return TwentyOne_raw_deck[31];
            case "hj": return TwentyOne_raw_deck[32];
            case "hd": return TwentyOne_raw_deck[33];
            case "hk": return TwentyOne_raw_deck[34];
            case "ha": return TwentyOne_raw_deck[35];
            default:
                return null;
        }
    }

    public static int nextPlayer(int size, int PlayerPos) {
        if(PlayerPos < size){
            PlayerPos++;
        }
        else{
            PlayerPos = 1;
        }
        return PlayerPos;
    }
}
