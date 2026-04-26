package com.bank.izbank.MainScreen;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.Adapters.HistoryAdapter;
import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.History;

import java.util.ArrayList;
import java.util.Stack;

public class UPIHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upi_history);

        ImageButton btnBack = findViewById(R.id.btn_back);
        RecyclerView recyclerView = findViewById(R.id.recycler_upi_history_full);

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (SignIn.mainUser != null) {
            ArrayList<History> upiHistory = filterUPIHistory(SignIn.mainUser.getHistory());
            HistoryAdapter adapter = new HistoryAdapter(upiHistory, this, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private ArrayList<History> filterUPIHistory(Stack<History> historyStack) {
        ArrayList<History> filtered = new ArrayList<>();
        Stack<History> temp = new Stack<>();
        
        while (!historyStack.isEmpty()) {
            History h = historyStack.pop();
            if (h.getProcess() != null && (h.getProcess().toLowerCase().contains("upi") || h.getProcess().toLowerCase().contains("scan"))) {
                filtered.add(h);
            }
            temp.push(h);
        }
        
        // Restore stack
        Stack<History> restore = new Stack<>();
        while(!temp.isEmpty()) restore.push(temp.pop());
        while(!restore.isEmpty()) historyStack.push(restore.pop());
        
        return filtered;
    }
}
