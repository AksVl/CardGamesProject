package com.akscardgames.cardgamesproject.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.akscardgames.cardgamesproject.gamesRelated.GameFragment;
import com.akscardgames.cardgamesproject.gamesRelated.gameFragments.TwentyOneGame;
import com.akscardgames.cardgamesproject.general.adapters.GameViewPagerAdapter;
import com.akscardgames.cardgamesproject.general.adapters.TabLayoutAdapter;
import com.akscardgames.cardgamesproject.menu.roomSearchFragments.TwentyOneSearchFragment;
import com.example.cardgamesproject.databinding.ActivityGameChooseBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameChooseActivity extends AppCompatActivity {
    private static FragmentManager fm;
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://cardgamesproject-6d467-default-rtdb.europe-west1.firebasedatabase.app/");
    String playerName = "";
    DatabaseReference playerRef;
    private ActivityGameChooseBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private TabLayoutAdapter adapter;
    public static FragmentManager fragmentManager;
    private boolean shutDownFlag = false;
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
        fragmentManager = getSupportFragmentManager();
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
    public static void launchTwentyOne(String roomName, String playerName){
        //TwentyOneGame fragment = new TwentyOneGame();
        TwentyOneGame.roomNameBuff = roomName;
        TwentyOneGame.playerNameBuff = playerName;
        GameFragment.playerName = playerName;
        GameFragment.RoomRef = database.getReference("TwentyOneRooms/" + roomName);
        GameViewPagerAdapter.gameType = "TwentyOne";
        GameFragment fragment = new GameFragment();
        fragmentManager.beginTransaction().addToBackStack(null).replace(android.R.id.content, fragment).commit();
        TwentyOneSearchFragment.binding.create.setEnabled(true);
    }

    private void LogOut() {
        SharedPreferences prefs = getSharedPreferences("PREFS",0);
        prefs.edit().remove("name").apply();
        playerRef.removeValue();
        startActivity(new Intent(this,StartActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(shutDownFlag) {
            shutDownFlag = false;
            playerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!playerName.equals("")) {
                        if (!snapshot.exists()) {
                            playerRef.setValue("");
                            SharedPreferences prefs = getSharedPreferences("PREFS", 0);
                            prefs.edit().putString("name", playerName).apply();
                        } else {
                            Toast.makeText(GameChooseActivity.this, "This player already exists", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(GameChooseActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        shutDownFlag = true;
        playerRef.removeValue();
        super.onPause();
    }
}