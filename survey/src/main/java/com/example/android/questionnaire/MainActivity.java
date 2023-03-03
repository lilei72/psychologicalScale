package com.example.android.questionnaire;

import static com.example.android.questionnaire.data.Options.RADIOBUTTON;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.questionnaire.data.Options;
import com.example.android.questionnaire.data.Question;
import com.example.android.questionnaire.data.QuestionSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

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

            //if the current question is unanswered, alert the user as all questions are mandatory
            if (!answered) {
                alertQuestionUnanswered();
                return;
            }

            /*
            increment the question number and display the next question
            or display the results if it's the last question after confirming for submission from the user
             */
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

            //save current set answer before displaying the previous question
            saveUserAnswer();

            /*
            decrement the question number and display the previous question
            if this is the first question, display a toast message stating there are no previous questions
             */
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

        // 加载所有的题目
        questions = getAllQuestions();
        totalQuestions = questions.size();

        //set progress bar max value
        progressBar.setMax(totalQuestions);

        displayQuestion();
    }

    /**
     * Display the questions from the set along with it's options, each question can
     * have different number of options and different type of views for the inputs
     */
    private void displayQuestion() {

        //remove previous question and it's corresponding options
        optionsLinearLayout.removeAllViews();

        //update the state of 'mark for review' TextView appropriately
        if (questions.get(qNumber).isMarkedForReview()) {
            reviewTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_box, 0, 0, 0);
        } else {
            reviewTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_box_outline_blank, 0, 0, 0);
        }

        //display the current question number and total number of questions
        String text = (qNumber + 1) + "/" + totalQuestions;
        numOfQuestionsTextView.setText(text);

        //update the progress bar status
        progressBar.setProgress(qNumber);

        if (answered)
            answered = false;

        Question currentSet = questions.get(qNumber);
        questionTextView.setText(currentSet.getQuestion());

        //set the button for last question to be 'Submit', rather than 'Next'
        if (qNumber == questions.size() - 1) {
            nextButton.setText(R.string.submit);
        } else {
            nextButton.setText(R.string.nextQuestion);
        }

        displayOptions();

    }

    /**
     * display options for each question - type could be Radiobuttons, checkboxes or edittext
     * restore answers from question object that were saved on activity reload (orientation change, minimize app)
     * or on clicking next, previous or review buttons
     */
    private void displayOptions() {

        //get the current question and it's options
        Question question = questions.get(qNumber);
        String[] options = question.getOptions();
        Options currentOptionsType = question.getOptionsType();

        switch (currentOptionsType) {

            case RADIOBUTTON:
                //For the case of radiobuttons, create a RadioGroup and add each option as a RadioButton
                //to the group - set an ID for each RadioButton to be referred later
// 获取圆形按钮的 drawable
                RadioGroup radioGroup=new RadioGroup(this);
                for(int i=0;i<options.length;i++){
                    RadioButton button = new RadioButton(this);
                    button.setText(options[i]);
                    button.setId(i);
                    radioGroup.addView(button);
                }
                // 调整整个单选框样式
                // 获取RadioGroup的布局参数
                optionsLinearLayout.addView(radioGroup);

                //restore saved answers
                if (question.getUserSetAnswerId() != null && question.getUserSetAnswerId().size() > 0) {
                    RadioButton radioButton = (RadioButton) radioGroup.getChildAt(question.getUserSetAnswerId().get(0));
                    radioButton.setChecked(true);
                }

                optionsView = radioGroup;
                break;


            case CHECKBOX:
                //For the case of check boxes, create a new CheckBox for each option
                for (String option : options) {
                    CheckBox checkbox = new CheckBox(this);
                    checkbox.setText(option);
                    optionsLinearLayout.addView(checkbox);
                }

                //restore saved answers
                if (question.getUserSetAnswerId() != null && question.getUserSetAnswerId().size() > 0) {
                    for (int index : question.getUserSetAnswerId()) {
                        ((CheckBox) optionsLinearLayout.getChildAt(index)).setChecked(true);
                    }
                }
                optionsView = optionsLinearLayout;
                break;

            case EDITTEXT:
                //For the case of edit text, display an EditText for the user to enter the answer
                EditText editText = new EditText(this);
                //set the InputType to display digits only keyboard if applicable
                if (TextUtils.isDigitsOnly(questions.get(qNumber).getAnswer())) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }

                //restore saved answers, set hint text if answer is empty/remains unanswered
                if (!TextUtils.isEmpty(question.getUserAnswer())) {
                    editText.setText(question.getUserAnswer());
                    editText.setSelection(question.getUserAnswer().length());
                } else {
                    editText.setHint(R.string.editText_hint);
                }

                optionsLinearLayout.addView(editText);

                optionsView = editText;
                break;
        }
        optionsType = currentOptionsType;
    }

    // 利用saveInstance保存当前做完的题目的答案，防止用户不小心推出程序后丢失数据
    private void saveUserAnswer() {

        if (qNumber < questions.size()) {
            Question currentQuestion = questions.get(qNumber);
            ArrayList<Integer> userSelectedAnswers = new ArrayList<>();

            String answer;

            switch (optionsType) {
                case RADIOBUTTON:
                    //save the selected RadioButton IDs
                    int selectedId = ((RadioGroup) optionsView).getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    Log.d("LiLei", "saveUserAnswer: "+selectedId);

                    if (selectedRadioButton == null) {
                        return;
                    } else {
                        userSelectedAnswers.add(selectedId);
                        currentQuestion.setUserSetAnswerId(userSelectedAnswers);
                        answered = true;
                    }
                    break;

                case CHECKBOX:
                    //save checkbox IDs that have been checked by the user
                    LinearLayout parentLayout = (LinearLayout) optionsView;
                    int numOfCheckBox = parentLayout.getChildCount();
                    for (int i = 0; i < numOfCheckBox; i++) {
                        CheckBox childCheckBox = (CheckBox) parentLayout.getChildAt(i);
                        if (childCheckBox.isChecked()) {
                            userSelectedAnswers.add(i);
                            answered = true;
                        }
                    }
                    currentQuestion.setUserSetAnswerId(userSelectedAnswers);
                    break;

                case EDITTEXT:
                    //save the EditText answer
                    EditText answerText = (EditText) optionsView;
                    answer = answerText.getText().toString();
                    if (!TextUtils.isEmpty(answer)) {
                        currentQuestion.setUserAnswer(answer);
                        answered = true;
                    } else {
                        currentQuestion.setUserAnswer(null);
                    }
                    break;
            }
        }
    }

    /**
     * called when `mark for review` option is checked ot unchecked by the user
     *
     * @param v `marker for review` textview reference
     */
    // 设置是否标记为可预览
    private void setMarkerForReview(View v) {
        if (!questions.get(qNumber).isMarkedForReview()) {
            questions.get(qNumber).setMarkedForReview(true);
            ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_box, 0, 0, 0);
        } else {
            questions.get(qNumber).setMarkedForReview(false);
            ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_box_outline_blank, 0, 0, 0);
        }
    }

    /**
     * Save the status of the quiz on activity stop/pause and restore the values
     * again when recreated
     * @param outState Bundle object used to save the state of the Activity
     */
    // 保存Acticivity被销毁前的状态：在离开页面的时候用onSaveInstanceState中的outState可以保存你所需要的值
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        saveUserAnswer();

        outState.putInt(QUESTION_NUMBER, qNumber);
        outState.putSerializable(QUESTIONS, questions);
    }


    /**
     * restore state of the quiz on activity resumes after stop/pause
     * @param savedInstanceState provides access to the data prior to activity resume
     */
    // 恢复Acticivity被销毁前的状态：在重新回到该页面的时候可以使用onRestoreInstanceState从saveInstanceState中获取保存过得值来重新初始化界面
    @Override
    @SuppressWarnings("unchecked")
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        qNumber = savedInstanceState.getInt(QUESTION_NUMBER);
        questions = (ArrayList<Question>) savedInstanceState.getSerializable(QUESTIONS);

        displayQuestion();
    }

    /**
     * display an alert toast message to the user
     * if the next button is clicked without answering the current question
     */
    private void alertQuestionUnanswered() {
        cancelToast();

        toast = Toast.makeText(this, R.string.no_answer_error, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 258);
        toast.show();
    }

    /**
     * when the previous button is clicked while in the first questions
     * display a toast message stating there are no previous questions
     */
    private void alertNoPrevQuestions() {
        cancelToast();

        toast = Toast.makeText(MainActivity.this, R.string.no_prev_question, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 258);
        toast.show();
    }

    /**
     * method to handle canceling of any previously displayed toast before displaying new one
     */
    private void cancelToast() {
        if (toast != null)
            toast.cancel();
    }

    /**
     * Navigate to new activity when user presses submit button
     * pass the questions object in the intent to be used by the ResultsActivity
     */
    // 跳转到结果页面
    private void displayResults() {
        Intent intent = new Intent(MainActivity.this,
                SelectScaleActivity.class);
        intent.putExtra(QUESTIONS, questions);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to new activity when user presses review list button
     * - pass the questions object in the intent to be used by the ReviewAnswersActivity
     */
    // 跳转到标记的题目页面
    private void displayReviewQuestions() {
        Intent intent = new Intent(MainActivity.this, ReviewAnswersActivity.class);
        intent.putExtra(QUESTIONS, questions);
        startActivity(intent);
    }

    /**
     * display submission confirmation dialog to the user after all questions have been answered
     * display quiz activity exit confirmation if the back is pressed while the quiz is still ongoing
     */
    private void displayConfirmAlert(String message, final boolean isBackPressed) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isBackPressed) {
                            finish();
                        } else {
                            displayResults();
                        }
                    }
                })
                .setNegativeButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {
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

    /**
     * new intent will be received when the user clicks on a `Go to question` button from the Review Answers activity
     * @param intent intent object received from ReviewAnswersActivity - contains the question number to be displayed
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            qNumber = intent.getIntExtra(QUESTION_NUMBER, 0);
            displayQuestion();
        }
    }

    /**
     * if back is pressed while taking the quiz, alert the user that the answers will be lost
     * and confirm exiting the quiz activity
     */
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
            String questionType=intent.getStringExtra("questionType");
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
