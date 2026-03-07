package com.bank.izbank.MainScreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.Adapters.HistoryAdapter;
import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.History;

import java.util.ArrayList;
import java.util.Stack;

public class UPIFragment extends Fragment {

    private RecyclerView recyclerViewRecent;
    private HistoryAdapter historyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.card_scan_qr).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Camera permission required for QR Scan", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.card_pay_upi_id).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Feature coming soon: Pay to UPI ID", Toast.LENGTH_SHORT).show());

        recyclerViewRecent = view.findViewById(R.id.recycler_upi_recent);
        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(getContext()));

        // Filter history for UPI specific transactions
        ArrayList<History> upiHistory = filterUPIHistory(SignIn.mainUser.getHistory());
        historyAdapter = new HistoryAdapter(upiHistory, getActivity(), getContext());
        recyclerViewRecent.setAdapter(historyAdapter);
    }

    private ArrayList<History> filterUPIHistory(Stack<History> historyStack) {
        ArrayList<History> filtered = new ArrayList<>();
        Stack<History> temp = new Stack<>();
        
        while (!historyStack.isEmpty()) {
            History h = historyStack.pop();
            if (h.getProcess().toLowerCase().contains("upi") || h.getProcess().toLowerCase().contains("scan")) {
                filtered.add(h);
            }
            temp.push(h);
        }
        
        while (!temp.isEmpty()) {
            historyStack.push(temp.pop());
        }
        return filtered;
    }
}
