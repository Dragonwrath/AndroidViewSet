package com.joker.andoridviewset;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;


public class CountDownView extends View implements View.OnClickListener {

    //默认的持续时间
    private final static int DEFAULT_COUNT = 5;

    //默认文字的大小
    private final static int DEFAULT_TEXT_SIZE = 16;

    //默认追加的提示文字的内容
    private final static String TEXT_SUFFIX = "秒";

    //默认的圆圈的宽度
    private final static int DEFAULT_CIRCLE_WIDTH = 4;

    private final TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private final Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Listener mFinishedListener;


    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            long timePassed = (System.currentTimeMillis() - startTime);
            if (timePassed < 5000L) {
                arc = (timePassed * 1F / (DEFAULT_COUNT * 1000));
                hintText = ((5000 - timePassed) / 1000) + 1 + TEXT_SUFFIX;
                mCircleArea.set(getPaddingLeft(), getPaddingTop(),
                        getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
                postInvalidate();
                //60毫秒刷新
                postDelayed(updateRunnable, 60);
            } else {
                if (mFinishedListener != null) {
                    mFinishedListener.onCountDownFinished();
                }
            }
        }
    };

    private long startTime = 0L;
    private String hintText = DEFAULT_COUNT + TEXT_SUFFIX;
    private float arc = 0F;
    private RectF mCircleArea = new RectF();

    public CountDownView(Context context) {
        this(context, null, 0);
    }

    public CountDownView(Context context,
                         @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CountDownView(Context context,
                         @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        int pColor = resources.getColor(R.color.colorAccent);
        DisplayMetrics metrics = resources.getDisplayMetrics();
        mCirclePaint.setColor(pColor);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CIRCLE_WIDTH, metrics));
        mTextPaint.setColor(pColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TEXT_SIZE, metrics));
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //保证宽高一致
        float text = mTextPaint.measureText(DEFAULT_COUNT + TEXT_SUFFIX);
        int spec = MeasureSpec.makeMeasureSpec((int) (text + getPaddingLeft() + getPaddingRight() + mCirclePaint.getStrokeWidth() * 2), MeasureSpec.AT_MOST);
        super.onMeasure(spec, spec);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制居中文字
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        Paint.FontMetricsInt fontMetricsInt = mTextPaint.getFontMetricsInt();
        float dy = (fontMetricsInt.bottom - fontMetricsInt.top) / 2 - fontMetricsInt.bottom;
        float baseLine = centerY + dy;
        canvas.drawText(hintText, centerX, baseLine, mTextPaint);
        //绘制外圈弧度
        float degrees = 360 * arc;
        canvas.drawArc(mCircleArea, -90 + degrees, 360 - degrees, false, mCirclePaint);
    }

    public void startCountDown() {
        removeCallbacks(updateRunnable);
        post(updateRunnable);
    }

    public void setFinishedListener(Listener listener) {
        mFinishedListener = listener;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (mFinishedListener == null) {
            throw new IllegalStateException("finish listener is not set");
        }
        super.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        postDelayed(updateRunnable,60);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(updateRunnable);
    }

    @Override
    public void onClick(View v) {
        removeCallbacks(updateRunnable);
        mFinishedListener.onCountDownFinished();
    }

    public interface Listener {
        void onCountDownFinished();
    }
}
