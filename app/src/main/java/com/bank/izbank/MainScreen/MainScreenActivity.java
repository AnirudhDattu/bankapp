package com.bank.izbank.MainScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainScreenActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    Fragment fragment1 = new AccountFragment();
    final Fragment fragment2 = new CreditFragment();
    final Fragment fragment4 = new BillFragment();
    final Fragment fragment5 = new SettingFragment();

    private Fragment tempFragment=fragment1;

    final FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        Gson gson=new GsonBuilder().setLenient().create();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        fm.beginTransaction().add(R.id.fragment_container,fragment5,"5").hide(fragment5).commit();
        fm.beginTransaction().add(R.id.fragment_container,fragment4,"4").hide(fragment4).commit();
        fm.beginTransaction().add(R.id.fragment_container,fragment2,"2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.fragment_container,fragment1,"1").commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.menu1) {
                fragment1 = new AccountFragment();
                fm.beginTransaction().add(R.id.fragment_container,fragment1,"1").commit();
                fm.beginTransaction().hide(tempFragment).show(fragment1).commit();
                tempFragment = fragment1;
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