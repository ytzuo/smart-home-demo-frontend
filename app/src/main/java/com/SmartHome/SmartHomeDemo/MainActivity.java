package com.SmartHome.SmartHomeDemo;


import android.os.Bundle;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.SmartHome.SmartHomeDemo.database.AppDatabase;
import com.SmartHome.SmartHomeDemo.fragments.CarFragment.CarFragment;
import com.SmartHome.SmartHomeDemo.fragments.HomeFragment.HomeFragment;
import com.SmartHome.SmartHomeDemo.fragments.LogFragment.LogFragment;
import com.SmartHome.SmartHomeDemo.fragments.SettingFragment.SettingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    private AppDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 初始化数据库
        database = AppDatabase.getDatabase(this);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        loadFragment(new CarFragment());

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_car) {
                selected = new CarFragment();
            } else if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_log) {
                selected = new LogFragment();
            } else if (id == R.id.nav_setting) {
                selected = new SettingFragment();
            }
            return loadFragment(selected);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    // 提供获取数据库实例的方法
    public AppDatabase getDatabase() {
        return database;
    }
}
