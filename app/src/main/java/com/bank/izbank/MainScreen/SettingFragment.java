package com.bank.izbank.MainScreen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
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
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SettingFragment extends Fragment {

    private ImageView userPhoto;
    private TextView userName, userJob, userEmail, textViewDate;
    private EditText editUserName, editUserEmail, editUserPassword;
    private Spinner spinner;
    private View updateButton, logoutButton, deleteButton;

    private String selectedJob;
    private String[] jobs;
    private Job[] defaultJobs;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            userPhoto.setImageBitmap(bitmap);
                            SignIn.mainUser.setPhoto(bitmap);
                            savePhotoToDb(bitmap);
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
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
        
        userPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void populateUserData() {
        if (SignIn.mainUser != null) {
            userName.setText(SignIn.mainUser.getName());
            userJob.setText(SignIn.mainUser.getJob().getName());
            userEmail.setText(String.format("%s@izbank.com", SignIn.mainUser.getId()));

            editUserName.setText(SignIn.mainUser.getName());
            editUserEmail.setText(String.format("%s@izbank.com", SignIn.mainUser.getId()));

            if (SignIn.mainUser.getPhoto() != null) {
                userPhoto.setImageBitmap(SignIn.mainUser.getPhoto());
            }
        }
    }

    private void savePhotoToDb(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] bytes = outputStream.toByteArray();
        
        ParseFile parseFile = new ParseFile("profile.png", bytes);
        parseFile.saveInBackground((SaveCallback) e -> {
            if (e == null) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("UserInfo");
                query.whereEqualTo("username", SignIn.mainUser.getId());
                query.findInBackground((objects, e1) -> {
                    if (e1 == null && !objects.isEmpty()) {
                        ParseObject userInfo = objects.get(0);
                        userInfo.put("images", parseFile);
                        userInfo.saveInBackground(e2 -> {
                            if (e2 == null) {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Profile photo updated permanently!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void setDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());
        textViewDate.setText(format.format(Calendar.getInstance().getTime()));
    }

    public void defineJobSpinner() {
        defaultJobs = new Job[]{new Contractor(), new Doctor(), new Driver(), new Engineer(), new Entrepreneur(),
                new Farmer(), new Police(), new Soldier(), new Sportsman(), new Student(), new Teacher(), new Waiter(), new Worker()};

        jobs = new String[]{"Contractor", "Doctor", "Driver", "Engineer", "Entrepreneur", "Farmer", "Police", "Soldier",
                "Sportsman", "Student", "Teacher", "Waiter", "Worker"};

        if (getContext() != null) {
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
            if (currentUser != null) {
                currentUser.setPassword(newPass);
                currentUser.saveInBackground();
            }
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
        if (getContext() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void deleteAccount() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.deleteInBackground(e -> {
                if (e == null) {
                    logOut();
                } else {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
