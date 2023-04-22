package com.akscardgames.cardgamesproject.general.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.akscardgames.cardgamesproject.menu.roomSearchFragments.FoolSearchFragment;
import com.akscardgames.cardgamesproject.menu.roomSearchFragments.LiarSearchFragment;
import com.akscardgames.cardgamesproject.menu.roomSearchFragments.TwentyOneSearchFragment;

public class TabLayoutAdapter extends FragmentStateAdapter {

    public TabLayoutAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0){
            return new FoolSearchFragment();
        } else if (position == 1) {
            return new TwentyOneSearchFragment();
        }
        return new LiarSearchFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
