package com.example.android.questionnaire;

import static com.example.android.questionnaire.data.Options.RADIOBUTTON;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.questionnaire.data.Options;
import com.example.android.questionnaire.data.Question;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.tablemanager.Connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

import org.litepal.crud.LitePalSupport;

public class MainActivity extends AppCompatActivity {
    //直接解析json文件获得数据也没存到数据库，当时偷懒后面发现要分很多表很麻烦
    //懒得改了，新增功能会改动很多，但现在也就用到一张表

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

    // 子表题号
    private int[] SOMNUM = {1, 4, 12, 27, 40, 42, 48, 49, 52, 53, 56, 58};
    private int[] O_CNUM = {3, 9, 10, 28, 38, 45, 46, 51, 55, 65};
    private int[] I_SNUM = {6, 21, 34, 36, 37, 41, 61, 69, 73};
    private int[] DEPNUM = {5, 14, 15, 20, 22, 26, 29, 30, 31, 32, 54, 71, 79};
    private int[] ANXNUM = {2, 17, 23, 33, 39, 57, 72, 78, 80, 86};
    private int[] HOSNUM = {11, 24, 63, 67, 74, 81};
    private int[] PHOBNUM = {13, 25, 47, 50, 70, 75, 82};
    private int[] PARNUM = {8, 18, 43, 68, 76, 83};
    private int[] PSYNUM = {7, 16, 35, 62, 77, 84, 85, 87, 88, 90};


    public static final String QUESTION_NUMBER = "QUESTION_NUMBER";
    public static final String QUESTIONS = "QUESTIONS";
    public static final String QUESTION_TYPE = "QUESTION_TYPE";

    private String questionType;

    private TextView questionTextView;
    private TextView numOfQuestionsTextView;
    private LinearLayout optionsLinearLayout;
    private ProgressBar progressBar;
    private TextView reviewTextView;
    private Button nextButton;
    private Button prevButton;

    private int qNumber;
    private int totalQuestions;
    private ArrayList<Question> questions;
    private boolean answered;

    private Options optionsType;
    private View optionsView;
    private Toast toast;

    // 下一题点击事件
    private View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // 保存当前答案
            saveUserAnswer();

            // 如果用户没有选择选项，提醒用户必须作答每一道题目
            if (!answered) {
                alertQuestionUnanswered();
                return;
            }

            // 当前题号+1，判断是否是最后一题，如果不是显示下一题
            // 否则键刚才+1恢复，提醒用户是否提交
            qNumber++;
            if (qNumber < questions.size()) {
                displayQuestion();
            } else {
                qNumber--;
                displayConfirmAlert(getString(R.string.submit_confirm), false);
            }
        }
    };

    // 上一题点击事件，当当前问题为第一题的时候提醒用户无法在进行上一题操作
    private View.OnClickListener prevButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // 保存当前答案
            saveUserAnswer();

            // 如果没有到第一题就显示上一题的题目，否则提醒用户当前已经是第一题
            if (qNumber > 0) {
                qNumber--;
                displayQuestion();
            } else {
                alertNoPrevQuestions();
            }

        }
    };

    @SuppressLint("MissingInflatedId")
    @Override  // 程序入口
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //obtain references to all the views in the main activity
        // 获得主界面控件的
        questionTextView = findViewById(R.id.question_text);
        numOfQuestionsTextView = findViewById(R.id.questions_remaining);
        optionsLinearLayout = findViewById(R.id.linearLayout_Options);
        progressBar = findViewById(R.id.determinantProgressBar);


        // 为 next 和 previous 设置监听器
        prevButton = findViewById(R.id.prev_button);
        prevButton.setOnClickListener(prevButtonClickListener);
        nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(nextButtonClickListener);

        // 为 mark for review 按钮设置监听器
        reviewTextView = findViewById(R.id.review_check);
        reviewTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMarkerForReview(v);
            }
        });

        // 点击预览按钮，跳转展示已标记的问题
        ImageButton reviewButton = findViewById(R.id.review_button);
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserAnswer();
                displayReviewQuestions();
            }
        });

        // 创建数据库(存储Result)
        Connector.getDatabase();

        // 加载所有的题目
        questions = getAllQuestions();
        totalQuestions = questions.size();

        //set progress bar max value
        progressBar.setMax(totalQuestions);

        displayQuestion();
    }

    // 显示题目的功能
    private void displayQuestion() {

        // 清除上一题的所有组件(可能后续会加入其它题型，组件不一样)
        optionsLinearLayout.removeAllViews();

        // 更新下面该题是否被标记组件
        // 如果被标记打勾
        if (questions.get(qNumber).isMarkedForReview()) {
            reviewTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_box, 0, 0, 0);
        } else {
            reviewTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_box_outline_blank, 0, 0, 0);
        }

        // 展示当前问题号和总问题数量
        String text = (qNumber + 1) + "/" + totalQuestions;
        numOfQuestionsTextView.setText(text);

        // 更新答题进度条
        progressBar.setProgress(qNumber);

        // 初始化选项选择
        if (answered)
            answered = false;
        // 向组件注入题目
        Question currentSet = questions.get(qNumber);
        questionTextView.setText(currentSet.getQuestion());

        // 判断当前是否为最后一题，是就将下一题按钮变为提交，否则继续下一题
        if (qNumber == questions.size() - 1) {
            nextButton.setText(R.string.submit);
        } else {
            nextButton.setText(R.string.nextQuestion);
        }

        displayOptions();

    }

    // 展示选项
    private void displayOptions() {
        // 获取当前题目
        Question question = questions.get(qNumber);
        String[] options = question.getOptions();

        RadioGroup radioGroup=new RadioGroup(this);
        radioGroup.setTag("unique_radio_group");
        for(int i=0;i<options.length;i++){
            RadioButton button = new RadioButton(this);
            button.setText(options[i]);
            button.setId(i); // 给每个选项一个id {0,1,2,3,4}
            radioGroup.addView(button);
        }

        // 获取RadioGroup的布局参数
        optionsLinearLayout.addView(radioGroup);
    }

    // 保存当前做完的题目的答案到questions列表里
    private void saveUserAnswer() {

        if (qNumber < questions.size()) {
            // currentQuestion引用当前题目
            // Java 中的基本数据类型和 String 类型是按值传递的，而对象类型则是按引用传递的。
            Question currentQuestion = questions.get(qNumber);
            // 这里使用 ArrayList来保存答案是考虑以后可能加入多选题
            ArrayList<Integer> userSelectedAnswers = new ArrayList<>();
            String answer;

            // 拿到选中单选题的id,{0,1,2,3,4}
            RadioGroup radioGroup=optionsLinearLayout.findViewWithTag("unique_radio_group");
            int selectedId = radioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = findViewById(selectedId);

            // 判断单选按钮是否存在
            if (selectedRadioButton == null) {
                return;
            } else {
                // 存在就保存按钮id到userSelectedAnswers数组，并存入对应question中，将是否已答题状态改为true
                userSelectedAnswers.add(selectedId);
                currentQuestion.setUserSetAnswerId(userSelectedAnswers);
                answered = true;
            }
        }
    }

    // 设置是否标记为可预览
    private void setMarkerForReview(View v) {
        if (!questions.get(qNumber).isMarkedForReview()) {
            // 设置为已被标记
            questions.get(qNumber).setMarkedForReview(true);
            ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_box, 0, 0, 0);
        } else {
            questions.get(qNumber).setMarkedForReview(false);
            ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_box_outline_blank, 0, 0, 0);
        }
    }

    // 保存MainActivity被销毁前的状态：在离开页面的时候用onSaveInstanceState中的outState可以保存你所需要的值
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        saveUserAnswer();

        outState.putInt(QUESTION_NUMBER, qNumber);
        outState.putSerializable(QUESTIONS, questions);
    }

    // 恢复MainActivity被销毁前的状态：在重新回到该页面的时候可以使用onRestoreInstanceState从saveInstanceState中获取保存过得值来重新初始化界面
    @Override
    @SuppressWarnings("unchecked")
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        qNumber = savedInstanceState.getInt(QUESTION_NUMBER);
        questions = (ArrayList<Question>) savedInstanceState.getSerializable(QUESTIONS);

        displayQuestion();
    }

    // 当点击下一题的时候，如果当前题目没有作答要提醒用户作答
    private void alertQuestionUnanswered() {
        cancelToast();

        toast = Toast.makeText(this, R.string.no_answer_error, Toast.LENGTH_SHORT);
        // toast.setGravity()方法用于设置Toast显示的位置。通过指定x和y坐标以及一个重心位置来决定Toast的位置
        // 这里就是让toast显示在底部垂直偏移为258处
        toast.setGravity(Gravity.BOTTOM, 0, 258);
        toast.show();
    }

    // 当点击上一题的时候，如果当前已经是第一题要提醒用户。
    private void alertNoPrevQuestions() {
        cancelToast();

        toast = Toast.makeText(MainActivity.this, R.string.no_prev_question, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 258);
        toast.show();
    }

    // 在显示新的toast之前关闭原来的，防止重叠
    private void cancelToast() {
        if (toast != null)
            toast.cancel();
    }

    // 跳转到结果页面，携带questions问题列表
    private void displayResults() {
        Intent intent = new Intent(MainActivity.this,
                SurveyResultActivity.class);
        intent.putExtra(QUESTIONS, questions);
        intent.putExtra(QUESTION_TYPE,questionType);
        startActivity(intent);
        finish();
    }

    // 跳转到标记的题目页面，携带questions问题列表
    private void displayReviewQuestions() {
        Intent intent = new Intent(MainActivity.this, ReviewAnswersActivity.class);
        intent.putExtra(QUESTIONS, questions);
        startActivity(intent);
    }

    // 当用户作答完毕最后一题，提醒用户是否提交
    private void displayConfirmAlert(String message, final boolean isBackPressed) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                    // 用户点击确认提交后如果按了返回键就退出程序，否则跳转到结果页面展示得分结果
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 只要按了返回键就会调用，isBackPressed为true
                        // 按submit键isBackPress为false
                        if(isBackPressed) {
                            finish();
                        } else {
                            displayResults();
                        }
                    }
                })
                .setNegativeButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {
                    // 当用户点击取消，关闭对话框
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(dialog != null) {
                            dialog.dismiss();
                        }
                    }
                })
                .create()
                .show();
    }

    // 当在当前活动中配置了singleTop启动模式，并且在其他活动中通过Intent对象启动当前活动时
    // 如果当前活动已经在栈顶，则会调用onNewIntent()方法。

    // 当用户从ReviewActivity选中一个题目跳转到MainActivity中重新作答的时候
    // MainActivity将接收到一个新的intent对象，该对象中包含了问题编号
    // MainActivity拿到这个问题编号后显示这个问题
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            qNumber = intent.getIntExtra(QUESTION_NUMBER, 0);
            displayQuestion();
        }
    }

    // 当程序运行时如果用户在MainActivity活动中按下返回键
    // 调用displayConfirmAlert方法提醒用户可能会失去数据
    @Override
    public void onBackPressed() {
        displayConfirmAlert(getString(R.string.exit_confirm), true);
    }

    // 加载问题
    public ArrayList<Question> getAllQuestions(){
        // 定义一个问题列表来装问题
        ArrayList<Question> questionArrayList=new ArrayList<>();
        Question question=null;
        InputStreamReader inputStreamReader=null;
        String questionData="";
        try{
            // 使用文件流读取json文件获取题目数据
            inputStreamReader=new InputStreamReader(getResources().openRawResource(R.raw.question), StandardCharsets.UTF_8);
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String line;
            StringBuilder stringBuilder=new StringBuilder();
            while ((line=bufferedReader.readLine())!=null){
                stringBuilder.append(line);
            }
            inputStreamReader.close();
            bufferedReader.close();
            questionData=stringBuilder.toString();
            // 使用JSONObject对象解析获取到的字符串
            JSONObject jsonObject=new JSONObject(questionData);

            // 判断要显示哪一类题目
            Intent intent=getIntent();
            questionType=intent.getStringExtra(QUESTION_TYPE);
            int[] questionNum=null;// 要返回的题号
            // 获取总表题目
            if(questionType.equals(SCL_90)){
                for(int i=0;i<jsonObject.length();i++){
                    String questionText=jsonObject.getString(String.valueOf(i+1));
                    question=new Question(questionText,RADIOBUTTON,getResources().getStringArray(R.array.q1_options),Collections.singletonList(0));
                    questionArrayList.add(question);
                }
            }else {
                switch (questionType){
                    case SOM:
                        questionNum=SOMNUM;
                        break;
                    case O_C:
                        questionNum=O_CNUM;
                        break;
                    case I_S:
                        questionNum=I_SNUM;
                        break;
                    case DEP:
                        questionNum=DEPNUM;
                        break;
                    case ANX:
                        questionNum=ANXNUM;
                        break;
                    case HOS:
                        questionNum=HOSNUM;
                        break;
                    case PHOB:
                        questionNum=PHOBNUM;
                        break;
                    case PAR:
                        questionNum=PARNUM;
                        break;
                    case PSY:
                        questionNum=PSYNUM;
                        break;
                    default:
                        break;
                }
                // 获取子表题目
                for(int i=0;i<questionNum.length;i++){
                    String questionText=jsonObject.getString(String.valueOf(questionNum[i]));
                    question=new Question(questionText,RADIOBUTTON,getResources().getStringArray(R.array.q1_options),Collections.singletonList(0));
                    questionArrayList.add(question);
                }
            }
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 返回对应题目
        return questionArrayList;
    }
}
