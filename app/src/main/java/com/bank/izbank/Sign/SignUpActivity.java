package com.bank.izbank.Sign;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bank.izbank.Job.Contractor;
import com.bank.izbank.Job.Doctor;
import com.bank.izbank.Job.Driver;
import com.bank.izbank.Job.Engineer;
import com.bank.izbank.Job.Entrepreneur;
import com.bank.izbank.Job.Farmer;
import com.bank.izbank.Job.Job;
import com.bank.izbank.Job.Police;
import com.bank.izbank.Job.Soldier;
import com.bank.izbank.Job.Sportsman;
import com.bank.izbank.Job.Student;
import com.bank.izbank.Job.Teacher;
import com.bank.izbank.Job.Waiter;
import com.bank.izbank.Job.Worker;
import com.bank.izbank.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    private ImageView userPhoto;
    private EditText editUserName, editUserIdNumber, editUserPassword, editUserPhone;
    private Spinner spinner;
    private Button signUpButton;

    private String name, password, job, addressSum;
    private ParseFile parseFile;

    private String[] jobs;
    private Job[] defaultJobs;
    private ArrayAdapter jobArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userPhoto = findViewById(R.id.sign_up_user_photo);
        editUserName = findViewById(R.id.sign_up_edit_user_name);
        editUserIdNumber = findViewById(R.id.sign_up_edit_user_email);
        editUserPassword = findViewById(R.id.sign_up_edit_user_password);
        editUserPhone = findViewById(R.id.edittext_phone_sign_up);
        spinner = findViewById(R.id.sign_up_spinner);
        signUpButton = findViewById(R.id.sign_up_button);

        defineJobSpinner();

    }

    public void defineJobSpinner() {

        defaultJobs = new Job[]{new Contractor(), new Doctor(), new Driver(), new Engineer(), new Entrepreneur(),
                new Farmer(), new Police(), new Soldier(), new Sportsman(), new Student(), new Teacher(), new Waiter(), new Worker()};

        jobs = new String[]{"Contractor", "Doctor", "Driver", "Engineer", "Entrepreneur", "Farmer", "Police", "Soldier",
                "Sportsman", "Student", "Teacher", "Waiter", "Worker"};

        jobArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jobs);

        spinner.setAdapter(jobArrayAdapter);
        job = jobs[0];

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                job = adapterView.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public void selectImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                userPhoto.setImageBitmap(bitmap);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
                byte[] bytes = outputStream.toByteArray();
                parseFile = new ParseFile("profile.png", bytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createAddress(View view) {
        // street neighborhood apartNo floor homeNo province city country
        addressSum = "MainStreet Central 10 2 45 Istanbul Marmara Turkey";
        if (view instanceof TextView) {
            ((TextView) view).setText("Address Set");
        }
        Toast.makeText(this, "Default Address Applied", Toast.LENGTH_SHORT).show();
    }

    public void signUp(View view) {
        name = editUserName.getText().toString().trim();
        String username = editUserIdNumber.getText().toString().trim();
        password = editUserPassword.getText().toString().trim();
        String phone = editUserPhone.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill required fields (Name, ID, Password)", Toast.LENGTH_SHORT).show();
            return;
        }

        ParseUser.logOut();
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    saveUserInfo(username, name, phone);
                } else {
                    Toast.makeText(SignUpActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveUserInfo(String username, String realName, String phone) {
        ParseObject userInfo = new ParseObject("UserInfo");
        userInfo.put("username", username);
        userInfo.put("userRealName", realName);
        userInfo.put("phone", phone);
        userInfo.put("address", addressSum != null ? addressSum : "Street Neighborhood 0 0 0 Province City Country");
        userInfo.put("job", job != null ? job : "Worker");

        // Set job defaults
        Job selectedJob = null;
        for (Job j : defaultJobs) {
            if (j.getName().equalsIgnoreCase(job)) {
                selectedJob = j;
                break;
            }
        }

        if (selectedJob != null) {
            userInfo.put("maxCreditAmount", selectedJob.getMaxCreditAmount());
            userInfo.put("maxCreditInstallment", selectedJob.getMaxCreditInstallment());
            userInfo.put("interestRate", selectedJob.getInterestRate());
        } else {
            userInfo.put("maxCreditAmount", "5000");
            userInfo.put("maxCreditInstallment", "12");
            userInfo.put("interestRate", "5");
        }

        if (parseFile != null) {
            userInfo.put("images", parseFile);
        }

        userInfo.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(SignUpActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Profile Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
