package com.bank.izbank.MainScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
git import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

public class MainScreenActivity extends AppCompatActivity {

    private static final String TAG = "MainScreenActivity";
    private static final String CHANNEL_ID = "UPI_PAYMENT_CHANNEL";
    private static final int PERMISSION_REQUEST_CODE = 112;
    private BottomNavigationView bottomNavigationView;

    final Fragment fragment1 = new AccountFragment();
    final Fragment fragment2 = new CreditFragment();
    final Fragment fragment_upi = new UPIFragment();
    final Fragment fragment4 = new BillFragment();
    final Fragment fragment5 = new SettingFragment();

    private Fragment tempFragment = fragment1;
    final FragmentManager fm = getSupportFragmentManager();
    
    private Handler pollingHandler = new Handler();
    private Date lastCheckDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        createNotificationChannel();
        requestNotificationPermission();
        
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
        
        // Start fallback polling just in case LiveQuery is not enabled on server
        startPollingFallback();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "UPI Payment Notifications";
            String description = "Notifications for money received";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setupPaymentNotifications() {
        if (SignIn.mainUser == null) return;

        try {
            // Trying multiple common Back4App LiveQuery URL patterns
            String liveQueryUrl = "wss://parseapi.back4app.com"; // Standard proxy
            
            ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI(liveQueryUrl));
            
            ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("History");
            parseQuery.whereEqualTo("userId", SignIn.mainUser.getId());
            parseQuery.whereEqualTo("isIncome", true);
            
            SubscriptionHandling<ParseObject> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);
            
            subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, (query, history) -> {
                Log.d(TAG, "LiveQuery: New Payment Received!");
                handleNewTransaction(history.getString("process"));
            });
            
            subscriptionHandling.handleError((query, exception) -> {
                Log.e(TAG, "LiveQuery Error: " + exception.getMessage());
                // If standard proxy fails, we rely on polling
            });
            
        } catch (URISyntaxException e) {
            Log.e(TAG, "LiveQuery URI Error: " + e.getMessage());
        }
    }

    private void startPollingFallback() {
        pollingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SignIn.mainUser != null) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("History");
                    query.whereEqualTo("userId", SignIn.mainUser.getId());
                    query.whereEqualTo("isIncome", true);
                    query.whereGreaterThan("createdAt", lastCheckDate);
                    query.findInBackground((objects, e) -> {
                        if (e == null && objects != null && !objects.isEmpty()) {
                            for (ParseObject obj : objects) {
                                if (obj.getCreatedAt().after(lastCheckDate)) {
                                    handleNewTransaction(obj.getString("process"));
                                    lastCheckDate = obj.getCreatedAt();
                                }
                            }
                        }
                        pollingHandler.postDelayed(this, 5000); // Poll every 5 seconds
                    });
                } else {
                    pollingHandler.postDelayed(this, 5000);
                }
            }
        }, 5000);
    }

    private void handleNewTransaction(String details) {
        runOnUiThread(() -> {
            showPaymentReceivedPopup(details);
            showSystemNotification("Money Received! 💰", details);
            
            // Refresh UI
            if (tempFragment instanceof AccountFragment) {
                ((AccountFragment) tempFragment).onResume();
            }
        });
    }

    private void showSystemNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_bank)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
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
            dialog.getWindow().getAttributes().windowAnimations = R.style.ModernDialogAnimation;
        }

        popupView.findViewById(R.id.btn_dismiss_popup).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollingHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
