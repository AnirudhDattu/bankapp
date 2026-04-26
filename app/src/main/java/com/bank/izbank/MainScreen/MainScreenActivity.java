package com.bank.izbank.MainScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.net.URISyntaxException;

public class MainScreenActivity extends AppCompatActivity {

    private static final String TAG = "MainScreenActivity";
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

        // Initialize Real-time Payment Notifications
        setupPaymentNotifications();
    }

    private void setupPaymentNotifications() {
        if (SignIn.mainUser == null) return;

        try {
            // Note: Replace with your actual LiveQuery server URL if different
            ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI("wss://izbank.b4a.io"));
            
            ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("History");
            parseQuery.whereEqualTo("userId", SignIn.mainUser.getId());
            parseQuery.whereEqualTo("isIncome", true);
            
            SubscriptionHandling<ParseObject> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);
            
            subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, (query, history) -> {
                runOnUiThread(() -> {
                    showPaymentReceivedPopup(history.getString("process"));
                });
            });
            
        } catch (URISyntaxException e) {
            Log.e(TAG, "LiveQuery URI Error: " + e.getMessage());
        }
    }

    private void showPaymentReceivedPopup(String processDetails) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.layout_payment_received_popup, null);
        builder.setView(popupView);

        TextView detailsText = popupView.findViewById(R.id.txt_payment_details);
        detailsText.setText(processDetails);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        popupView.findViewById(R.id.btn_dismiss_popup).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
}
