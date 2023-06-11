package com.akscardgames.cardgamesproject.general;

import android.content.Context;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.akscardgames.cardgamesproject.gamesRelated.GameFragment;
import com.akscardgames.cardgamesproject.gamesRelated.gameFragments.FoolGame;
import com.akscardgames.cardgamesproject.gamesRelated.gameFragments.LiarGame;
import com.example.cardgamesproject.R;
import com.akscardgames.cardgamesproject.gamesRelated.gameFragments.TwentyOneGame;
import com.example.cardgamesproject.databinding.FragmentFoolGameBinding;
import com.example.cardgamesproject.databinding.FragmentLiarGameBinding;
import com.example.cardgamesproject.databinding.FragmentTwentyOneGameBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AppMethods {
    //region cards objects
    public static Card h6 = new Card("6", 'h', R.drawable.h6);
    public static Card h7 = new Card("7", 'h', R.drawable.h7);
    public static Card h8 = new Card("8", 'h', R.drawable.h8);
    public static Card h9 = new Card("9", 'h', R.drawable.h9);
    public static Card h10 = new Card("10", 'h', R.drawable.h10);
    public static Card hj = new Card("j", 'h', R.drawable.hj);
    public static Card hd = new Card("d", 'h', R.drawable.hd);
    public static Card hk = new Card("k", 'h', R.drawable.hk);
    public static Card ha = new Card("a", 'h', R.drawable.ha);
    public static Card s6 = new Card("6", 's', R.drawable.s6);
    public static Card s7 = new Card("7", 's', R.drawable.s7);
    public static Card s8 = new Card("8", 's', R.drawable.s8);
    public static Card s9 = new Card("9", 's', R.drawable.s9);
    public static Card s10 = new Card("10", 's', R.drawable.s10);
    public static Card sj = new Card("j", 's', R.drawable.sj);
    public static Card sd = new Card("d", 's', R.drawable.sd);
    public static Card sk = new Card("k", 's', R.drawable.sk);
    public static Card sa = new Card("a", 's', R.drawable.sa);
    public static Card d6 = new Card("6", 'd', R.drawable.d6);
    public static Card d7 = new Card("7", 'd', R.drawable.d7);
    public static Card d8 = new Card("8", 'd', R.drawable.d8);
    public static Card d9 = new Card("9", 'd', R.drawable.d9);
    public static Card d10 = new Card("10", 'd', R.drawable.d10);
    public static Card dj = new Card("j", 'd', R.drawable.dj);
    public static Card dd = new Card("d", 'd', R.drawable.dd);
    public static Card dk = new Card("k", 'd', R.drawable.dk);
    public static Card da = new Card("a", 'd', R.drawable.da);
    public static Card c6 = new Card("6", 'c', R.drawable.c_six);
    public static Card c7 = new Card("7", 'c', R.drawable.c_seven);
    public static Card c8 = new Card("8", 'c', R.drawable.c_eight);
    public static Card c9 = new Card("9", 'c', R.drawable.c_nine);
    public static Card c10 = new Card("10", 'c', R.drawable.c_ten);
    public static Card cj = new Card("j", 'c', R.drawable.cj);
    public static Card cd = new Card("d", 'c', R.drawable.cd);
    public static Card ck = new Card("k", 'c', R.drawable.ck);
    public static Card ca = new Card("a", 'c', R.drawable.ca);
    //endregion cards objects

    public static Card[] raw_deck = {
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

    public static void Disconnect(DatabaseReference RoomRef, String playerName, ValueEventListener listener, ValueEventListener chatListener) {
        RoomRef.removeEventListener(listener);
        RoomRef.removeEventListener(chatListener);
        RoomRef.child(playerName).removeValue();
        RoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean PlayersAreInRoom = false;
                for(DataSnapshot node :snapshot.getChildren()){
                    String element = node.getKey();
                    if(!element.equals("_size") && !element.equals("_ChoosingPlayer")
                    && !element.equals("_bank") && !element.equals("_bank_choosing")
                    && !element.equals("_offline") && !element.equals("_access")
                            && !element.equals("_messages")  && !element.equals("_stack")
                            && !element.equals("_chosenValue")  && !element.equals("_winnersPositions")
                    && !element.equals("_lastThrown") && !element.equals("_chosenLast")){
                        PlayersAreInRoom = true;
                    }
                }
                if (!PlayersAreInRoom) {
                    snapshot.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static int getAvailablePosition(DataSnapshot snapshot, int size) {
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
            if (!player.equals("_size") && !player.equals("_access") && !player.equals("_messages") && snapshot.child(player).child("position").exists()) {
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

    public static void twentyOneReadyCheck(ValueEventListener listener,
                                           ValueEventListener InGameListener,
                                           ArrayList<String> InRoomPlayers,
                                           DatabaseReference RoomRef, int readyCount, int size,
                                           FragmentTwentyOneGameBinding binding, Context context, WindowManager windowManager) {
        if (readyCount >= size) {
            binding.buttonBar.removeAllViews();
            for (String player : InRoomPlayers) {
                if (!player.equals("_size") && !player.equals("_access") && !player.equals("_messages")) {
                    RoomRef.child(player).child("status").setValue("waiting");
                }
            }
            RoomRef.removeEventListener(listener);
            RoomRef.addValueEventListener(InGameListener);
            GameFragment.listener = InGameListener;
            binding.gameStatus.setText("");
            TwentyOneGame.onGameStart(context, binding,windowManager);
        }
    }
    public static void foolReadyCheck(ValueEventListener listener,
                                      ValueEventListener InGameListener, ArrayList<String> inRoomPlayers,
                                      DatabaseReference roomRef, int readyCount, int size,
                                      FragmentFoolGameBinding binding, Context context, WindowManager windowManager) {
        if (readyCount >= size) {
            binding.buttonBar.removeAllViews();
            for (String player : inRoomPlayers) {
                if (!player.equals("_size") && !player.equals("_access") && !player.equals("_messages")) {
                    roomRef.child(player).child("status").setValue("waiting");
                }
            }
            roomRef.removeEventListener(listener);
            roomRef.addValueEventListener(InGameListener);
            GameFragment.listener = InGameListener;
            FoolGame.onGameStart(context, binding, windowManager);
        }
    }
    public static void liarReadyCheck(ValueEventListener listener,
                                      ValueEventListener InGameListener, ArrayList<String> inRoomPlayers,
                                      DatabaseReference roomRef, int readyCount, int size, FragmentLiarGameBinding binding,
                                      Context context, WindowManager windowManager) {
        if (readyCount >= size) {
            binding.buttonBar.removeAllViews();
            for (String player : inRoomPlayers) {
                if (!player.equals("_size") && !player.equals("_access") && !player.equals("_messages")) {
                    roomRef.child(player).child("status").setValue("waiting");
                }
            }
            roomRef.removeEventListener(listener);
            roomRef.addValueEventListener(InGameListener);
            GameFragment.listener = InGameListener;
            LiarGame.onGameStart(context, binding, windowManager);
        }
    }
    public static Card cardLink(String name) {
        switch (name) {
            case "c6": return raw_deck[0];
            case "c7": return raw_deck[1];
            case "c8": return raw_deck[2];
            case "c9": return raw_deck[3];
            case "c10": return raw_deck[4];
            case "cj": return raw_deck[5];
            case "cd": return raw_deck[6];
            case "ck": return raw_deck[7];
            case "ca": return raw_deck[8];
            case "d6": return raw_deck[9];
            case "d7": return raw_deck[10];
            case "d8": return raw_deck[11];
            case "d9": return raw_deck[12];
            case "d10": return raw_deck[13];
            case "dj": return raw_deck[14];
            case "dd": return raw_deck[15];
            case "dk": return raw_deck[16];
            case "da": return raw_deck[17];
            case "s6": return raw_deck[18];
            case "s7": return raw_deck[19];
            case "s8": return raw_deck[20];
            case "s9": return raw_deck[21];
            case "s10": return raw_deck[22];
            case "sj": return raw_deck[23];
            case "sd": return raw_deck[24];
            case "sk": return raw_deck[25];
            case "sa": return raw_deck[26];
            case "h6": return raw_deck[27];
            case "h7": return raw_deck[28];
            case "h8": return raw_deck[29];
            case "h9": return raw_deck[30];
            case "h10": return raw_deck[31];
            case "hj": return raw_deck[32];
            case "hd": return raw_deck[33];
            case "hk": return raw_deck[34];
            case "ha": return raw_deck[35];
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
    public static int prevPlayer(int size, int playerPos) {
        if(playerPos > 1){
            playerPos--;
        }
        else{
            playerPos = size;
        }
        return playerPos;
    }

    public static void findPlayerByPos(DatabaseReference roomRef, int i, final OnPlayerFoundListener listener) {
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String player = null;
                for (DataSnapshot node : snapshot.getChildren()) {
                    String key = node.getKey();
                    if (snapshot.child(key).child("position").exists()
                            && Integer.parseInt(snapshot.child(key).child("position").getValue().toString()) == i) {
                        player = key;
                        break;
                    }
                }
                listener.onPlayerFound(player);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onCancelled(error);
            }
        });
    }

    public interface OnPlayerFoundListener {
        void onPlayerFound(String player);

        void onCancelled(DatabaseError error);
    }
}
