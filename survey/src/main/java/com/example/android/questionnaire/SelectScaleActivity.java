package com.example.android.questionnaire;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SelectScaleActivity extends AppCompatActivity implements View.OnClickListener {

    // 10种题目类型
    public static final String SCL_90 = "SCL_90";// 总表
    public static final String SOM = "SOM";// 身体化
    public static final String O_C = "O_C";// 强迫症状
    public static final String I_S = "I_S";// 人际关系敏感
    public static final String DEP = "DEP";// 抑郁症状
    public static final String ANX = "ANX";// 焦虑症状
    public static final String HOS = "HOS";// 敌对行为
    public static final String PHOB = "PHOB";// 恐惧症状
    public static final String PAR = "PAR";// 偏执症状
    public static final String PSY = "PSY";// 精神病性症状

    // 10个按钮对应10种类型

    // test
    private Button button;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_scale);

        button=findViewById(R.id.test_button);

        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.test_button:
                Intent intent=new Intent(SelectScaleActivity.this,MainActivity.class);
                intent.putExtra("questionType",SCL_90);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
