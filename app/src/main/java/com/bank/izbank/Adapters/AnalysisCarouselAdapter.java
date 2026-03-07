package com.bank.izbank.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

public class AnalysisCarouselAdapter extends RecyclerView.Adapter<AnalysisCarouselAdapter.ViewHolder> {

    private List<AnalysisPage> pages;

    public static class AnalysisPage {
        public String title;
        public int type; // 0: Line, 1: Pie, 2: Text
        public List<Entry> lineEntries;
        public List<PieEntry> pieEntries;
        public String mainStat;
        public String statLabel;
        public String subDetails; // New field for extra details

        public AnalysisPage(String title, int type) {
            this.title = title;
            this.type = type;
        }
    }

    public AnalysisCarouselAdapter(List<AnalysisPage> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.analysis_card_container, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnalysisPage page = pages.get(position);
        holder.title.setText(page.title);

        // Reset visibility
        holder.lineChart.setVisibility(View.GONE);
        holder.pieChart.setVisibility(View.GONE);
        holder.textContainer.setVisibility(View.GONE);
        holder.subDetails.setVisibility(View.GONE);

        if (page.type == 0) {
            setupLineChart(holder.lineChart, page.lineEntries);
        } else if (page.type == 1) {
            setupPieChart(holder.pieChart, page.pieEntries);
        } else {
            setupTextSummary(holder, page);
        }
    }

    private void setupLineChart(LineChart chart, List<Entry> entries) {
        chart.setVisibility(View.VISIBLE);
        if (entries == null || entries.isEmpty()) return;

        LineDataSet dataSet = new LineDataSet(entries, "Activity");
        dataSet.setColor(Color.parseColor("#0071E3"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#0071E3"));
        dataSet.setFillAlpha(30);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(2.5f);

        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.invalidate();
    }

    private void setupPieChart(PieChart chart, List<PieEntry> entries) {
        chart.setVisibility(View.VISIBLE);
        if (entries == null || entries.isEmpty()) return;

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#0071E3"), Color.parseColor("#2ECC71"), Color.parseColor("#E74C3C"), Color.parseColor("#F1C40F")});
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleRadius(61f);
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(10f);
        chart.getLegend().setEnabled(false);
        chart.animateY(1400);
        chart.invalidate();
    }

    private void setupTextSummary(ViewHolder holder, AnalysisPage page) {
        holder.textContainer.setVisibility(View.VISIBLE);
        holder.mainStat.setText(page.mainStat);
        holder.statLabel.setText(page.statLabel);
        
        if (page.subDetails != null && !page.subDetails.isEmpty()) {
            holder.subDetails.setVisibility(View.VISIBLE);
            holder.subDetails.setText(page.subDetails);
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, mainStat, statLabel, subDetails;
        LineChart lineChart;
        PieChart pieChart;
        View textContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_analysis_title);
            lineChart = itemView.findViewById(R.id.card_line_chart);
            pieChart = itemView.findViewById(R.id.card_pie_chart);
            textContainer = itemView.findViewById(R.id.layout_summary_stats);
            mainStat = itemView.findViewById(R.id.text_main_stat);
            statLabel = itemView.findViewById(R.id.text_stat_label);
            subDetails = itemView.findViewById(R.id.text_sub_details);
        }
    }
}
