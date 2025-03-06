package com.example.youdo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyStatsActivity extends AppCompatActivity {

    private LinearLayout chartContainer;
    private TextView completionPercentage;
    int userId = -1;

    dbConnectToDo db;
    dbConnectToDoType dbType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stats);

        db = new dbConnectToDo(this);
        dbType = new dbConnectToDoType(this);

        chartContainer = findViewById(R.id.chartContainer);
        completionPercentage = findViewById(R.id.completionPercentage);

        Intent intent = getIntent();
        // Receive userId and curr_date from previous activity if provided
        if (intent != null && intent.hasExtra("userId")) {
            userId = intent.getIntExtra("userId", -1);
        }

        loadStatistics();
    }

    private void loadStatistics() {
        List<ToDo> completedToDos = db.getToDosBeforeTodayForUser(userId);

        Map<String, Pair<Integer, Integer>> typeStatistics = new HashMap<>();
        int maxTargetMinutes = 1;
        int maxAchievedMinutes = 1;

        for (ToDo todo : completedToDos) {
            String typeName = dbType.getToDoTypeById(todo.getTypeId()).getName();

            int target = todo.getTargetMinutes();
            int achieved = todo.getAchievedMinutes();

            maxTargetMinutes = Math.max(maxTargetMinutes, target);
            maxAchievedMinutes = Math.max(maxAchievedMinutes, achieved);

            if (!typeStatistics.containsKey(typeName)) {
                typeStatistics.put(typeName, new Pair<>(target, achieved));
            } else {
                Pair<Integer, Integer> oldValues = typeStatistics.get(typeName);
                typeStatistics.put(typeName, new Pair<>(oldValues.first + target, oldValues.second + achieved));
            }
        }

        int maxMinutes = Math.max(maxTargetMinutes, maxAchievedMinutes);

        // Completion percentage calculation
        int totalTarget = typeStatistics.values().stream().mapToInt(pair -> pair.first).sum();
        int totalAchieved = typeStatistics.values().stream().mapToInt(pair -> pair.second).sum();
        int completionRate = (totalTarget > 0) ? (totalAchieved * 100 / totalTarget) : 0;
        completionPercentage.setText(completionRate + "%");

        int screenWidth = getResources().getDisplayMetrics().widthPixels - 100;

        chartContainer.removeAllViews(); // Remove previous charts

        // Iterate through the statistics and create bars
        for (Map.Entry<String, Pair<Integer, Integer>> entry : typeStatistics.entrySet()) {
            String typeName = entry.getKey();
            int targetWidth = (entry.getValue().first * screenWidth) / maxMinutes;
            int achievedWidth = (entry.getValue().second * screenWidth) / maxMinutes;

            addBarToChart(typeName, targetWidth, achievedWidth);
        }
    }

    private void addBarToChart(String typeName, int targetWidth, int achievedWidth) {
        LinearLayout barLayout = new LinearLayout(this);
        barLayout.setOrientation(LinearLayout.VERTICAL);
        barLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        barLayout.setPadding(0, 16, 0, 16);

        // Type name
        TextView title = new TextView(this);
        title.setText(typeName);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        barLayout.addView(title);

        // Target Time (Red)
        TextView targetBar = new TextView(this);
        targetBar.setLayoutParams(new LinearLayout.LayoutParams(targetWidth, 40));
        targetBar.setBackgroundColor(Color.RED);
        barLayout.addView(targetBar);

        // Achieved Time (Green)
        TextView achievedBar = new TextView(this);
        achievedBar.setLayoutParams(new LinearLayout.LayoutParams(achievedWidth, 40));
        achievedBar.setBackgroundColor(Color.GREEN);
        barLayout.addView(achievedBar);

        chartContainer.addView(barLayout);
    }

}
