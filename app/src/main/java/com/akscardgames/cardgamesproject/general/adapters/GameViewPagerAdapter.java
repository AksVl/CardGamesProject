package com.akscardgames.cardgamesproject.general.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.akscardgames.cardgamesproject.gamesRelated.ChatFragment;
import com.akscardgames.cardgamesproject.gamesRelated.GameFragment;
import com.akscardgames.cardgamesproject.gamesRelated.gameFragments.FoolGame;
import com.akscardgames.cardgamesproject.gamesRelated.gameFragments.LiarGame;
import com.akscardgames.cardgamesproject.gamesRelated.gameFragments.TwentyOneGame;

public class GameViewPagerAdapter extends FragmentStateAdapter {
    Fragment game;
    ChatFragment chat;
    public static String gameType;


    public GameViewPagerAdapter(@NonNull GameFragment fragment) {
        super(fragment);
        chat = new ChatFragment(gameType);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0){
            switch (gameType){
                case "TwentyOne":
                    return new TwentyOneGame();
                case "Fool":
                    return new FoolGame();
                case "Liar":
                    return new LiarGame();
            }
        }
        return chat;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
