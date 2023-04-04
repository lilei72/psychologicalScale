package com.example.android.questionnaire;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
// 选择题目页面
public class SelectScaleActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String QUESTION_TYPE = "QUESTION_TYPE";
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
    private Button SCLButton;
    private Button SOMButton;
    private Button O_CButton;
    private Button I_SButton;
    private Button DEPButton;
    private Button ANXButton;
    private Button HOSButton;
    private Button PHOBButton;
    private Button PARButton;
    private Button PSYButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_scale);

        SCLButton=findViewById(R.id.scl_90);
        SOMButton=findViewById(R.id.som);
        O_CButton=findViewById(R.id.o_c);
        I_SButton=findViewById(R.id.i_s);
        DEPButton=findViewById(R.id.dep);
        ANXButton=findViewById(R.id.anx);
        HOSButton=findViewById(R.id.hos);
        PHOBButton=findViewById(R.id.phob);
        PARButton=findViewById(R.id.par);
        PSYButton=findViewById(R.id.psy);

        SCLButton.setOnClickListener(this);
        SOMButton.setOnClickListener(this);
        O_CButton.setOnClickListener(this);
        I_SButton.setOnClickListener(this);
        DEPButton.setOnClickListener(this);
        ANXButton.setOnClickListener(this);
        HOSButton.setOnClickListener(this);
        PHOBButton.setOnClickListener(this);
        PARButton.setOnClickListener(this);
        PSYButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent=new Intent(SelectScaleActivity.this,MainActivity.class);
        switch (view.getId()){
            case R.id.scl_90:
                intent.putExtra(QUESTION_TYPE,SCL_90);
                break;
            case R.id.som:
                intent.putExtra(QUESTION_TYPE,SOM);
                break;
            case R.id.o_c:
                intent.putExtra(QUESTION_TYPE,O_C);
                break;
            case R.id.i_s:
                intent.putExtra(QUESTION_TYPE,I_S);
                break;
            case R.id.dep:
                intent.putExtra(QUESTION_TYPE,DEP);
                break;
            case R.id.anx:
                intent.putExtra(QUESTION_TYPE,ANX);
                break;
            case R.id.hos:
                intent.putExtra(QUESTION_TYPE,HOS);
                break;
            case R.id.phob:
                intent.putExtra(QUESTION_TYPE,PHOB);
                break;
            case R.id.par:
                intent.putExtra(QUESTION_TYPE,PAR);
                break;
            case R.id.psy:
                intent.putExtra(QUESTION_TYPE,PSY);
                break;
            default:
                break;
        }
        startActivity(intent);
    }

}
