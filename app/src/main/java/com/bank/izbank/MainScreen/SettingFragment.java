package com.bank.izbank.MainScreen;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.User;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.parse.Parse.getApplicationContext;

public class SettingFragment extends Fragment {

    private ImageView userPhoto;
    private TextView userName, userJob, userEmail, textViewDate;
    private EditText editUserName, editUserEmail, editUserPassword;
    private Spinner spinner;
    private View updateButton, logoutButton, deleteButton;

    private String selectedJob;
    private String[] jobs;
    private Job[] defaultJobs;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        userPhoto = view.findViewById(R.id.setting_user_photo);
        userName = view.findViewById(R.id.setting_user_name);
        userJob = view.findViewById(R.id.setting_user_job);
        userEmail = view.findViewById(R.id.setting_user_email);
        textViewDate = view.findViewById(R.id.text_view_date_profile);

        editUserName = view.findViewById(R.id.setting_edit_user_name);
        editUserEmail = view.findViewById(R.id.setting_edit_user_email);
        editUserPassword = view.findViewById(R.id.setting_edit_user_password);

        spinner = view.findViewById(R.id.setting_spinner);

        updateButton = view.findViewById(R.id.setting_update_button);
        logoutButton = view.findViewById(R.id.setting_logout_button_card);
        deleteButton = view.findViewById(R.id.setting_delete_button_card);

        // Set Current Data
        populateUserData();
        setDate();
        defineJobSpinner();

        // Listeners
        updateButton.setOnClickListener(v -> updateProfile());
        logoutButton.setOnClickListener(v -> logOut());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void populateUserData() {
        if (SignIn.mainUser != null) {
            userName.setText(SignIn.mainUser.getName());
            userJob.setText(SignIn.mainUser.getJob().getName());
            userEmail.setText(SignIn.mainUser.getId() + "@izbank.com");

            editUserName.setText(SignIn.mainUser.getName());
            editUserEmail.setText(SignIn.mainUser.getId() + "@izbank.com");

            if (SignIn.mainUser.getPhoto() != null) {
                userPhoto.setImageBitmap(SignIn.mainUser.getPhoto());
            }
        }
    }

    private void setDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM");
        textViewDate.setText(format.format(Calendar.getInstance().getTime()));
    }

    public void defineJobSpinner() {
        defaultJobs = new Job[]{new Contractor(), new Doctor(), new Driver(), new Engineer(), new Entrepreneur(),
                new Farmer(), new Police(), new Soldier(), new Sportsman(), new Student(), new Teacher(), new Waiter(), new Worker()};

        jobs = new String[]{"Contractor", "Doctor", "Driver", "Engineer", "Entrepreneur", "Farmer", "Police", "Soldier",
                "Sportsman", "Student", "Teacher", "Waiter", "Worker"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, jobs);
        spinner.setAdapter(adapter);

        // Set selection to current job
        if (SignIn.mainUser != null) {
            for (int i = 0; i < jobs.length; i++) {
                if (jobs[i].equalsIgnoreCase(SignIn.mainUser.getJob().getName())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedJob = jobs[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void updateProfile() {
        String newName = editUserName.getText().toString().trim();
        String newPass = editUserPassword.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserInfo");
        query.whereEqualTo("username", SignIn.mainUser.getId());
        query.findInBackground((objects, e) -> {
            if (e == null && !objects.isEmpty()) {
                ParseObject userInfo = objects.get(0);
                userInfo.put("userRealName", newName);
                userInfo.put("job", selectedJob);
                
                // Update job specifics
                for (Job j : defaultJobs) {
                    if (j.getName().equalsIgnoreCase(selectedJob)) {
                        userInfo.put("maxCreditAmount", j.getMaxCreditAmount());
                        userInfo.put("maxCreditInstallment", j.getMaxCreditInstallment());
                        userInfo.put("interestRate", j.getInterestRate());
                        SignIn.mainUser.setJob(j);
                        break;
                    }
                }

                userInfo.saveInBackground(e1 -> {
                    if (e1 == null) {
                        SignIn.mainUser.setName(newName);
                        userName.setText(newName);
                        userJob.setText(selectedJob);
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        if (!newPass.isEmpty()) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            currentUser.setPassword(newPass);
            currentUser.saveInBackground();
        }
    }

    private void logOut() {
        ParseUser.logOutInBackground(e -> {
            if (e == null) {
                Intent intent = new Intent(getActivity(), SignIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        ParseUser.getCurrentUser().deleteInBackground(e -> {
            if (e == null) {
                logOut();
            } else {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
