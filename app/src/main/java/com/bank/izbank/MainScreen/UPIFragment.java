package com.bank.izbank.MainScreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Stack;

public class UPIFragment extends Fragment {

    private RecyclerView recyclerViewRecent;
    private HistoryAdapter historyAdapter;
    private TextView txtViewAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.card_scan_qr).setOnClickListener(v -> startScanning());

        view.findViewById(R.id.card_my_qr).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyQRCodeActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.card_pay_upi_id).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PayToUpiActivity.class);
            startActivity(intent);
        });

        txtViewAll = view.findViewById(R.id.txt_view_all_upi);
        txtViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UPIHistoryActivity.class);
            startActivity(intent);
        });

        recyclerViewRecent = view.findViewById(R.id.recycler_upi_recent);
        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(getContext()));

        refreshHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHistory();
    }

    private void refreshHistory() {
        if (SignIn.mainUser != null) {
            ArrayList<History> upiHistory = filterUPIHistory(SignIn.mainUser.getHistory());
            // Show only last 5 in recent
            ArrayList<History> recentUpi = new ArrayList<>();
            for (int i = 0; i < Math.min(upiHistory.size(), 5); i++) {
                recentUpi.add(upiHistory.get(i));
            }
            historyAdapter = new HistoryAdapter(recentUpi, getActivity(), getContext());
            recyclerViewRecent.setAdapter(historyAdapter);
        }
    }

    private void startScanning() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a UPI QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureActivityPortrait.class);
        barcodeLauncher.launch(options);
    }

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    handleScannedData(result.getContents());
                }
            });

    private void handleScannedData(String data) {
        if (data.startsWith("upi://pay")) {
            Intent intent = new Intent(getActivity(), ConfirmPaymentActivity.class);
            intent.putExtra("upi_uri", data);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Invalid UPI QR Code", Toast.LENGTH_SHORT).show();
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
