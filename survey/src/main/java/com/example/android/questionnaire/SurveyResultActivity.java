package com.example.android.questionnaire;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class SurveyResultActivity extends AppCompatActivity {

    private LinearLayout mRootLayout;
    // 柱状图
    private BarChart mBarChart;
    // 表格
    private TableLayout mTableLayout;
    // 建议
    private TextView mAdviceTextView;
    private TextView mConclusionTextView;

    // 最后SCL-90各个子模块的平均分数
    private float[] mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_result);

        // 获取数据(MainActivity跳转到当前活动,需要传递一个result列表)
        Intent intent = getIntent();
        mData = intent.getFloatArrayExtra("data");

        // 初始化控件
        mRootLayout = findViewById(R.id.root_layout);
        mBarChart = new BarChart(this);
        mTableLayout = new TableLayout(this);
        mAdviceTextView = new TextView(this);
        mConclusionTextView = new TextView(this);

        // 设置布局参数
        mBarChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mTableLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mAdviceTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mConclusionTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // 绘制柱状图
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < mData.length; i++) {
            entries.add(new BarEntry(mData[i],i));
        }
        BarDataSet barDataSet = new BarDataSet(entries, "Survey Result");
        BarData barData = new BarData(barDataSet);
        mBarChart.setData(barData);

        // 绘制表格
        TableRow headerRow = new TableRow(this);
        TextView header1 = new TextView(this);
        TextView header2 = new TextView(this);
        TextView header3 = new TextView(this);
        header1.setText("指标");
        header2.setText("得分");
        header3.setText("评价");
        headerRow.addView(header1);
        headerRow.addView(header2);
        headerRow.addView(header3);
        mTableLayout.addView(headerRow);
        for (int i = 0; i < mData.length; i++) {
            TableRow row = new TableRow(this);
            TextView label = new TextView(this);
            TextView value = new TextView(this);
            TextView comment = new TextView(this);
            label.setText("指标" + (i + 1));
            value.setText(String.valueOf(mData[i]));
            String commentText = getComment(mData[i]);
            comment.setText(commentText);
            row.addView(label);
            row.addView(value);
            row.addView(comment);
            mTableLayout.addView(row);
        }

        // 设置用户建议和结论
        float totalScore = getTotalScore(mData);
        String advice = getAdvice(totalScore);
        String conclusion = getConclusion(totalScore);
        mAdviceTextView.setText(advice);
        mConclusionTextView.setText(conclusion);

        // 添加控件到根布局
        mRootLayout.addView(mBarChart);
        mRootLayout.addView(mTableLayout);
        mRootLayout.addView(mAdviceTextView);
        mRootLayout.addView(mConclusionTextView);
    }

    private float getTotalScore(float[] data) {
        float total = 0;
        for (float value : data) {
            total += value;
        }
        return total;
    }

    private String getAdvice(float totalScore) {
        String advice = "建议：";
        if (totalScore < 20) {
            advice += "您的情况比较严重，建议及时寻求专业医生的帮助。";
        } else if (totalScore >= 20 && totalScore < 40) {
            advice += "您的情况较为明显，需要重视并及时采取有效的心理干预措施。";
        } else if (totalScore >= 40 && totalScore < 60) {
            advice += "您的情况属于轻度抑郁，建议多参加社交活动，保持积极心态，可以适当尝试心理疏导等方法。";
        } else if (totalScore >= 60 && totalScore < 80) {
            advice += "您的情况属于中度抑郁，建议及时寻求专业心理医生的帮助，同时可以采取一些放松身心的措施。";
        } else {
            advice += "您的情况较为严重，建议及时寻求专业医生的帮助，并注意保护好自己的身心健康。";
        }
        return advice;
    }
}
