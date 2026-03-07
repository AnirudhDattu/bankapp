package com.bank.izbank.splashScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bank.izbank.MainScreen.AdminPanelActivity;
import com.bank.izbank.MainScreen.MainScreenActivity;
import com.bank.izbank.R;
import com.bank.izbank.UserInfo.Admin;
import com.bank.izbank.UserInfo.User;
import com.bank.izbank.UserInfo.UserContext;
import com.bank.izbank.UserInfo.UserTypeState;

import static com.bank.izbank.Sign.SignIn.mainUser;

public class splashScreen extends AppCompatActivity {

    private ImageView brandLogo;
    private ProgressBar progressBar;
    private UserContext userContext;
    public static UserTypeState adminUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        brandLogo = findViewById(R.id.brand_logo);
        progressBar = findViewById(R.id.progress_barr);
        
        userContext = new UserContext();
        adminUser = new Admin();

        // Pulsing animation for the logo instead of a GIF video
        Animation pulse = new AlphaAnimation(0.4f, 1.0f);
        pulse.setDuration(1000);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        brandLogo.startAnimation(pulse);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mainUser != null) {
                    if ("admin".equals(mainUser.getId()) || ("9999".equals(mainUser.getId()) && "admin".equals(mainUser.getName()))) {
                        userContext.setState(adminUser);
                        userContext.TypeChange(mainUser);
                        Intent intent = new Intent(splashScreen.this, AdminPanelActivity.class);
                        startActivity(intent);
                    } else {
                        userContext.setState(mainUser);
                        Intent splashIntent = new Intent(splashScreen.this, MainScreenActivity.class);
                        startActivity(splashIntent);
                    }
                } else {
                    // Fallback to MainScreen if mainUser is somehow null
                    startActivity(new Intent(splashScreen.this, MainScreenActivity.class));
                }
                finish();
            }
        }, 3000); // Reduced to 3 seconds for better UX
    }
}
