package com.example.youdo.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.example.youdo.Database.dbConnectToDo;
import com.example.youdo.Database.dbConnectToDoType;
import com.example.youdo.Models.ToDo;
import com.example.youdo.Models.Type;
import com.example.youdo.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyStatsActivity extends AppCompatActivity {

    private LinearLayout chartContainer;
    private TextView completionPercentage, motivationalText;
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
        motivationalText = findViewById(R.id.motivationalText);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId")) {
            userId = intent.getIntExtra("userId", -1);
        }

        loadStatistics();
    }

    private void loadStatistics() {
        List<ToDo> completedToDos = db.getToDosBeforeTodayForUser(userId);

        Map<Type, Pair<Integer, Integer>> typeStatistics = new HashMap<>();
        int totalTarget = 0;
        int totalAchieved = 0;
        int totalPercentage = 0;
        int typeCount = 0;

        for (ToDo todo : completedToDos) {
            Type curr_type = dbType.getToDoTypeById(todo.getTypeId());

            if (curr_type == null) {
                continue;
            }

            int target = todo.getTargetMinutes();
            int achieved = todo.getAchievedMinutes();

            int percentage;
            if (curr_type.isRewardOverAchievement()) {
                percentage = (target > 0) ? (achieved * 100 / target) : 0;
            } else {
                percentage = (achieved > 0) ? (target * 100 / achieved) : 0;
            }

            if (!typeStatistics.containsKey(curr_type)) {
                typeStatistics.put(curr_type, new Pair<>(target, achieved));
            } else {
                Pair<Integer, Integer> oldValues = typeStatistics.get(curr_type);
                typeStatistics.put(curr_type, new Pair<>(oldValues.first + target, oldValues.second + achieved));
            }

            totalTarget += target;
            totalAchieved += achieved;

            totalPercentage += percentage;
            typeCount++;
        }

        int overallCompletionRate = (totalTarget > 0) ? (totalAchieved * 100 / totalTarget) : 0;
        int averagePercentage = (typeCount > 0) ? (totalPercentage / typeCount) : 0;

        completionPercentage.setText( averagePercentage + "%");

        String message = getMotivationalMessage(overallCompletionRate);
        motivationalText.setText(message);

        int screenWidth = getResources().getDisplayMetrics().widthPixels - 100;
        chartContainer.removeAllViews();

        int maxMinutes = typeStatistics.values().stream().mapToInt(pair -> Math.max(pair.first, pair.second)).max().orElse(1);

        for (Map.Entry<Type, Pair<Integer, Integer>> entry : typeStatistics.entrySet()) {
            Type curr_type = entry.getKey();
            int target = entry.getValue().first;
            int achieved = entry.getValue().second;

            int percentage;
            if (curr_type.isRewardOverAchievement()) {
                percentage = (target > 0) ? (achieved * 100 / target) : 0;
            } else {
                percentage = (achieved > 0) ? (target * 100 / achieved) : 0;
            }

            String displayName = curr_type.getName() + " - " + percentage + "%";

            int targetWidth = (target * screenWidth) / maxMinutes;
            int achievedWidth = (achieved * screenWidth) / maxMinutes;

            addBarToChart(displayName, targetWidth, achievedWidth);
        }
    }

    private void addBarToChart(String typeName, int targetWidth, int achievedWidth) {
        LinearLayout barLayout = new LinearLayout(this);
        barLayout.setOrientation(LinearLayout.VERTICAL);
        barLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        barLayout.setPadding(0, 16, 0, 16);

        TextView title = new TextView(this);
        title.setText(typeName);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        title.setTypeface(null, Typeface.BOLD);
        barLayout.addView(title);

        TextView targetBar = new TextView(this);
        targetBar.setLayoutParams(new LinearLayout.LayoutParams(targetWidth, 40));
        targetBar.setBackgroundColor(Color.parseColor("#FF007A"));
        barLayout.addView(targetBar);

        TextView achievedBar = new TextView(this);
        achievedBar.setLayoutParams(new LinearLayout.LayoutParams(achievedWidth, 40));
        achievedBar.setBackgroundColor(Color.parseColor("#04E4DB"));
        barLayout.addView(achievedBar);

        chartContainer.addView(barLayout);
    }

    private String getMotivationalMessage(int completionRate) {
        if (completionRate > 150) {
            return "Okay, slow down!😳";
        } else if (completionRate > 125) {
            return "Wao, you are unstoppable🤪";
        } else if (completionRate > 100) {
            return "You are a true over-achiever!🥳";
        } else if (completionRate == 100) {
            return "Perfectionist...🙄";
        } else if (completionRate >= 75) {
            return "Almost there!😃";
        } else if (completionRate >= 50) {
            return "You got this!😉";
        } else if (completionRate >= 25) {
            return "Keep pushing!💪";
        } else if (completionRate >= 1) {
            return "At least you are trying🙄";
        } else {
            return "'Dolce far niente' means the sweetness of doing nothing🤷‍♀️";
        }
    }
}
