package com.bank.izbank.MainScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.bank.izbank.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainScreenActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    final Fragment fragment1 = new AccountFragment();
    final Fragment fragment2 = new CreditFragment();
    final Fragment fragment_upi = new UPIFragment();
    final Fragment fragment4 = new BillFragment();
    final Fragment fragment5 = new SettingFragment();

    private Fragment tempFragment = fragment1;
    final FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        fm.beginTransaction().add(R.id.fragment_container, fragment5, "5").hide(fragment5).commit();
        fm.beginTransaction().add(R.id.fragment_container, fragment4, "4").hide(fragment4).commit();
        fm.beginTransaction().add(R.id.fragment_container, fragment_upi, "upi").hide(fragment_upi).commit();
        fm.beginTransaction().add(R.id.fragment_container, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.fragment_container, fragment1, "1").commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu1) {
                fm.beginTransaction().hide(tempFragment).show(fragment1).commit();
                tempFragment = fragment1;
            } else if (itemId == R.id.menu2) {
                fm.beginTransaction().hide(tempFragment).show(fragment2).commit();
                tempFragment = fragment2;
            } else if (itemId == R.id.menu_upi) {
                fm.beginTransaction().hide(tempFragment).show(fragment_upi).commit();
                tempFragment = fragment_upi;
            } else if (itemId == R.id.menu4) {
                fm.beginTransaction().hide(tempFragment).show(fragment4).commit();
                tempFragment = fragment4;
            } else if (itemId == R.id.menu5) {
                fm.beginTransaction().hide(tempFragment).show(fragment5).commit();
                tempFragment = fragment5;
            }
            return true;
        });
    }
}
