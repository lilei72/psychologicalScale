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


import com.example.android.questionnaire.data.Question;
import com.example.android.questionnaire.data.Result;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
// 结果页面
public class SurveyResultActivity extends AppCompatActivity {

    // 题型
    private String questionType;

    //问题列表
    private ArrayList<Question> questions=new ArrayList<>();

    // 结果
    private Result result;

    // 各个题型题号
    int[][] numbers = {
            {1, 4, 12, 27, 40, 42, 48, 49, 52, 53, 56, 58},
            {3, 9, 10, 28, 38, 45, 46, 51, 55, 65},
            {6, 21, 34, 36, 37, 41, 61, 69, 73},
            {5, 14, 15, 20, 22, 26, 29, 30, 31, 32, 54, 71, 79},
            {2, 17, 23, 33, 39, 57, 72, 78, 80, 86},
            {11, 24, 63, 67, 74, 81},
            {13, 25, 47, 50, 70, 75, 82},
            {8, 18, 43, 68, 76, 83},
            {7, 16, 35, 62, 77, 84, 85, 87, 88, 90}
    };



    // 柱状图
    private BarChart mBarChart;
    // 表格
    private TableLayout mTableLayout;
    // 建议
    private TextView mAdviceTextView;
    private TextView mConclusionTextView;

    // 最后SCL-90各个子模块的总分
    ArrayList<Float> totals=new ArrayList<Float>();
    // GSI
    private float GSI;
    // 平均分
    private float averageScore;

    // 柱状图x轴标签
    private List<String> labels = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_result);

        // 获取数据(MainActivity跳转到当前活动,需要传递一个result列表)
        Intent intent = getIntent();
        questions= (ArrayList<Question>) intent.getSerializableExtra(MainActivity.QUESTIONS);
        questionType=intent.getStringExtra(MainActivity.QUESTION_TYPE);

        // 初始化控件
//        mRootLayout = findViewById(R.id.root_layout);
        mBarChart = (BarChart) findViewById(R.id.chart);
        mTableLayout = (TableLayout) findViewById(R.id.table);
        mAdviceTextView = (TextView) findViewById(R.id.advice_textview);
        mConclusionTextView = (TextView) findViewById(R.id.advice_textview2);

        // 获取数据展示
        display();

        // 绘制柱状图
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < totals.size(); i++) {
            entries.add(new BarEntry(totals.get(i),i));
        }
        // 柱状图数据
        BarDataSet barDataSet = new BarDataSet(entries, "各项总分");
        //BarData构造函数选一个list<BarDataSet>类型的集合
        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);
        // x轴标签
        for(int i=0; i<totals.size(); i++){
            labels.add(result.getQuestionType());
        }

        BarData barData = new BarData(labels,dataSets);
        mBarChart.setData(barData);

        // 绘制表格
        TableRow headerRow = new TableRow(this);
        TextView header1 = new TextView(this);
        TextView header2 = new TextView(this);
        TextView header3 = new TextView(this);
        headerRow.addView(header1);
        headerRow.addView(header2);
        headerRow.addView(header3);
        mTableLayout.addView(headerRow);
        for (int i = 0; i < totals.size(); i++) {
            TableRow row = new TableRow(this);
            TextView label = new TextView(this);
            TextView value = new TextView(this);
            TextView comment = new TextView(this);
            label.setText(result.getQuestionType());
            value.setText(String.valueOf(totals.get(i)));
            String commentText = result.getDescription();
            comment.setText(commentText);
            row.addView(label);
            row.addView(value);
            row.addView(comment);
            mTableLayout.addView(row);
        }

        // 设置结论和建议
        String conclusion = getExplain(); // 结论
        String advice=null;
        if(questionType.equals(MainActivity.SCL_90)){
            advice = getAdvice(GSI);
        } else {
            advice = getAdvice(totals.get(0));
        }

        mAdviceTextView.setText(advice);
        mConclusionTextView.setText(conclusion);
    }

    // 计算子模块的总分
    private int getTotalScore(ArrayList<Question> questions){
        int totalScore=0; // 总分
        int singleScore=0; // 单题得分
        List<Integer> singleScoreList=null; // 单题分数列表
        Question question=null;
        // 遍历所有题目，计算总分
        for(int i=0;i<questions.size();i++){
            question= questions.get(i);
            singleScoreList=question.getUserSetAnswerId();
            singleScore=singleScoreList.get(0);
            totalScore+=singleScore;
        }
        // 记录各子表总分
        totals.add((float)totalScore);
        // 记录各表平均分
        averageScore=totalScore/questions.size();

        return totalScore;
    }
    // 计算标准化分数
    private float getStandardizedScore(int totalScore, int length) {
        float score = (totalScore * 25.0f / length) + 50.0f;
        DecimalFormat df = new DecimalFormat("#.00");   // 保留两位小数
        return Float.parseFloat(df.format(score));
    }


    // 计算GSI
    private float getGSI(ArrayList<Question> questions){
        // 各个维度的标准化分数×该维度题目数量
        float totalStandardizedScore=0.0f;
        // 子问题列表
        ArrayList<Question> childQuestions=new ArrayList<>();
        for(int i=0;i<numbers.length;i++){

            for(int j=0;j<numbers[i].length;j++){
                childQuestions.add(questions.get(numbers[i][j]));
            }
            // 子表总分
            int totalScore=0;
            // 子表标准化分数
            float standardizedScore=0.0f;
            // 计算总分
            totalScore=getTotalScore(childQuestions);
            standardizedScore=getStandardizedScore(totalScore,childQuestions.size());
            totalStandardizedScore+=standardizedScore*childQuestions.size();

            // 清除子列表
            childQuestions.clear();
        }
        // 最终得到GSI(totalStandardizedScore/90)
        DecimalFormat df = new DecimalFormat("#.00");   // 保留两位小数
        float GSI=totalStandardizedScore/90;
        return Float.parseFloat(df.format(GSI));
    }

    // 展示结果
    private void display(){
        Result result=getResult(questionType);

        // 向数据库中添加数据
        result.save();

        List<Result> resultList = LitePal.findAll(Result.class);
        for (Result mResult : resultList) {
            Log.d("SurveyResultActivity", "questionType is " + mResult.getQuestionType());
            Log.d("MainActivity", "score is " + mResult.getScore());
            Log.d("MainActivity", "description is " + mResult.getDescription());
        }
    }
    // 提供描述
    private String getDescription(float Score) {
        String advice = "";
        if ((Score < 15 && averageScore<1.1) || Score<0.40) {
            advice += "正常范围";
        } else if ((Score >= 15 && Score < 25)||(Score>=0.40&&Score<0.79)) {
            advice += "轻度异常范围";
        } else if ((Score >= 25 && Score < 35)||(Score>=0.80&&Score<1.59)) {
            advice += "中度异常范围";
        } else {
            advice += "重度异常范围";
        }
        return advice;
    }
    // 分数说明
    private String getExplain() {
        String advice = "说明: GSI得分范围为0~4，得分越高表明受测者的症状越严重。常用的临床分级标准是: 0.40~0.79:轻度症状," +
                "·0.80~1.59:中度症状," +
                "·1.60以上:重度症状"+
                "单个维度的得分评估可以参考标准：" +
                "15-25分为轻度异常，25-35分为中度异常,35分以上为重度异常";
        return advice;
    }
    // 提供建议
    private String getAdvice(float Score) {
        String advice = "";

        if ((Score < 15&& averageScore<1.1) || Score<0.40) {
            advice += "您的"+questionType+"得分属于正常范围";
        } else if ((Score >= 15 && Score < 25)||(Score>=0.40&&Score<0.79)) {
            advice += "您的"+questionType+"得分属于轻度异常范围，建议多参加社交活动，保持积极心态，可以适当尝试心理疏导等方法。";
        } else if ((Score >= 25 && Score < 35)||(Score>=0.80&&Score<1.59)) {
            advice += "您的"+questionType+"得分属于中度异常范围，需要重视并及时采取有效的心理干预措施。";
        } else {
            advice += "您的"+questionType+"得分属于重度异常范围，建议及时寻求专业医生的帮助，并注意保护好自己的身心健康。";
        }
        return advice;
    }

    // 封装结果数据
    private Result getResult(String rQuestionType){
        if(rQuestionType.equals(MainActivity.SCL_90)){
            GSI=getGSI(questions);
            result=new Result(questionType,GSI,getDescription(GSI));
        } else {
            // 因为柱状图数据需要先从 getTotalScore()方法获取再存到totals中
            // 所以这里又调用了一次
            float totalScore = getTotalScore(questions);
            result=new Result(questionType,totalScore,getDescription(totalScore));
        }
        return result;
    }
}
