package com.example.cardgamesproject.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.cardgamesproject.R;
import com.example.cardgamesproject.menu.adapters.TabLayoutAdapter;
import com.example.cardgamesproject.menu.roomSearchFragments.FoolSearchFragment;
import com.example.cardgamesproject.menu.roomSearchFragments.LiarSearchFragment;
import com.example.cardgamesproject.menu.roomSearchFragments.TwentyOneSearchFragment;
import com.example.cardgamesproject.databinding.ActivityGameChooseBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GameChooseActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    String playerName = "";
    DatabaseReference playerRef;
    private ActivityGameChooseBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private TabLayoutAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameChooseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        playerName = getSharedPreferences("PREFS",0).getString("name","");
        playerRef = database.getReference("playerList/" + playerName);
        binding.name.setText("logged in as "+playerName);
        binding.logout.setOnClickListener(view -> LogOut());
        tabLayout = binding.tabLayout;
        viewPager2 = binding.FragmentsContainer;
        FragmentManager fragmentManager = getSupportFragmentManager();
        adapter = new TabLayoutAdapter(fragmentManager,getLifecycle());
        viewPager2.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private void LogOut() {
        SharedPreferences prefs = getSharedPreferences("PREFS",0);
        prefs.edit().remove("name").apply();
        playerRef.removeValue();
        startActivity(new Intent(this,StartActivity.class));
    }
    @Override
    protected void onDestroy() {
        playerRef.removeValue();
        super.onDestroy();
    }
}