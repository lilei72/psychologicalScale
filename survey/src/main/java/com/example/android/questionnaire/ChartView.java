package com.example.android.questionnaire;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class ChartView extends View {

    // 柱状图标题数据
    private ArrayList<String> mTitleList=null;

    // 数据数组
    private int[] mData = {50, 80, 30, 60, 90};

    // 柱形宽度
    private float mBarWidth = 80;

    // 画笔
    private Paint mPaint;

    private Paint mTextPaint; // 新增加的画笔

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始化画笔
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        // 初始化文本画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(24);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }
    public void setTitle(ArrayList<String> mTitleList){
        this.mTitleList=mTitleList;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取View宽度
        int width = getWidth();
        // 获取View高度
        int height = getHeight();
        // 计算柱形之间的间距
        float gap = (width - mData.length * mBarWidth) / (mData.length + 1);
        // 计算柱形高度比例
        float scale = (float) height / getMaxValue();
        // 绘制柱形图
        for (int i = 0; i < mData.length; i++) {
            float left = gap + i * (mBarWidth + gap);
            float right = left + mBarWidth;
            float bottom = height - mData[i] * scale;
            float centerX = left + mBarWidth / 2; // 柱形中心位置
            canvas.drawRect(left, bottom, right, height, mPaint);
            canvas.drawText(mTitleList.get(i), centerX, bottom - 10, mPaint); // 绘制柱形标题

        }
    }

    // 获取最大值
    private int getMaxValue() {
        int max = mData[0];
        for (int i = 1; i < mData.length; i++) {
            if (mData[i] > max) {
                max = mData[i];
            }
        }
        return max;
    }

    public void setData(int[] data) {
        this.mData=data;
    }
}

