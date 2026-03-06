package com.bank.izbank.MainScreen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.User;
import com.bank.izbank.UserInfo.UserContext;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;

public class SettingFragment extends Fragment {

    private ImageView userPhoto;
    private TextView userName,userJob,userEmail;
    private EditText editUserName,editUserEmail,editUserPassword;
    private Spinner spinner;
    private Button deleteUser,updateUser,logOut;

    private String name,email,password,job;

    private String[] jobs;
    private Job[] defaultJobs;
    private ArrayAdapter jobArrayAdapter;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        userPhoto = view.findViewById(R.id.setting_user_photo);
        userName = view.findViewById(R.id.setting_user_name);
        userJob = view.findViewById(R.id.setting_user_job);
        userEmail = view.findViewById(R.id.setting_user_email);

        editUserName = view.findViewById(R.id.setting_edit_user_name);
        editUserEmail = view.findViewById(R.id.setting_edit_user_email);
        editUserPassword = view.findViewById(R.id.setting_edit_user_password);

        spinner = view.findViewById(R.id.setting_spinner);

        deleteUser = view.findViewById(R.id.setting_delete_button);
        updateUser = view.findViewById(R.id.setting_update_button);
        logOut = view.findViewById(R.id.setting_logout_button);

        defineJobSpinner();

        return view;
    }

    public void defineJobSpinner(){

        defaultJobs = new Job[]{new Contractor(),new Doctor(),new Driver(),new Engineer(),new Entrepreneur(),
                new Farmer(),new Police(),new Soldier(),new Sportsman(),new Student(),new Teacher(),new Waiter(),new Worker()};

        jobs = new String[] {"Contractor","Doctor","Driver","Engineer","Entrepreneur","Farmer","Police","Soldier",
                "Sportsman","Student","Teacher","Waiter","Worker"};

        jobArrayAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_dropdown_item,jobs);

        spinner.setAdapter(jobArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                job = adapterView.getSelectedItem().toString();

                //change( job,"job");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
}
