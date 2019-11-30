package com.example.customview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Andrey Kudryavtsev on 2019-11-26.
 */
public class FinanceProgressView extends View {

    private static final Paint BACKGROUND_CIRCLE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint FRONT_ARC_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint CIRCLE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Path ARROW = new Path();

    private static final float STROKE_WIDTH = 50f;
    private static final float RADIUS = 300f;
    private static final float RADIUS_OF_ARROW = 200f;
    private static final float RADIUS_OF_CIRCLE = 30f;
    private static final RectF ARC_RECT = new RectF(STROKE_WIDTH / 2, STROKE_WIDTH / 2, 2 * RADIUS, 2 * RADIUS);

    private static final int MAX_PROGRESS = 200;
    private static final float MAX_ANGLE = 240f;
    private static final float START_ANGLE = -210f;
    private static final float FONT_SIZE = 64f;
    private static final float CENTER_X = ARC_RECT.width() / 2f + ARC_RECT.left;
    private static final float CENTER_Y = ARC_RECT.height() / 2f + ARC_RECT.top;

    private PointF point;


    private Rect mTextBounds = new Rect();
    private int mProgress;


    @ColorInt
    private int mFillColor;
    private int mLow_speed_color;
    private int mAverage_speed_color;
    private int mHigh_speed_color;


    public void setFillColor(int fillColor) {
        mFillColor = fillColor;
    }


    public FinanceProgressView(Context context) {
        this(context, null, 0);
    }

    public FinanceProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FinanceProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawArrow(canvas);
        canvas.drawArc(ARC_RECT, START_ANGLE, MAX_ANGLE, false, FRONT_ARC_PAINT);
        canvas.drawCircle(ARC_RECT.width() / 2f + ARC_RECT.left, ARC_RECT.height() / 2f + ARC_RECT.top, 30, CIRCLE);
        drawText(canvas);

    }

    private void drawArrow(Canvas canvas) {
        float currentAngle = START_ANGLE + (MAX_ANGLE * mProgress / MAX_PROGRESS);
        ARROW.reset();
        setPoint(currentAngle + 90, RADIUS_OF_CIRCLE);
        ARROW.moveTo(point.x, point.y);
        setPoint(currentAngle - 90, RADIUS_OF_CIRCLE);
        ARROW.lineTo(point.x, point.y);
        setPoint(currentAngle, RADIUS_OF_ARROW);
        ARROW.lineTo(point.x, point.y);
        canvas.drawPath(ARROW, CIRCLE);
    }

    private void setPoint(float currentAngle, float radius) {
        float x = (float) (radius * (Math.cos(Math.toRadians(currentAngle))) + CENTER_X);
        float y = (float) (radius * (Math.sin(Math.toRadians(currentAngle))) + CENTER_Y);
        point.x = x;
        point.y = y;
    }

    private void drawText(Canvas canvas) {
        final String progressString = formatString(mProgress);
        getTextBounds(progressString);
        float x = ARC_RECT.width() / 2f - mTextBounds.width() / 2f - mTextBounds.left + ARC_RECT.left;
        float y = ARC_RECT.height() + mTextBounds.height() / 2f - mTextBounds.bottom + ARC_RECT.top;
        canvas.drawText(progressString, x, y, TEXT_PAINT);
    }

    private String formatString(int progress) {
        return String.format("%d km/h", progress);
    }

    private void getTextBounds(@NonNull String progressString) {
        TEXT_PAINT.getTextBounds(progressString, 0, progressString.length(), mTextBounds);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        extractAttributes(context, attrs);
        configureFrontArc();
        configureText();
        configureCircle();
        configureArrow();

    }

    private void extractAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            final Resources.Theme theme = context.getTheme();
            final TypedArray typedArray = theme.obtainStyledAttributes(attrs, R.styleable.FinanceProgressView, 0, R.style.FinanceProgressViewDefault);
            try {
                mProgress = typedArray.getInt(R.styleable.FinanceProgressView_progress, 0);
                mFillColor = typedArray.getColor(R.styleable.FinanceProgressView_fill_color, Color.GREEN);
                mLow_speed_color = typedArray.getColor(R.styleable.FinanceProgressView_mLow_speed_color, Color.GREEN);
                mAverage_speed_color = typedArray.getColor(R.styleable.FinanceProgressView_mAverage_speed_color, Color.YELLOW);
                mHigh_speed_color = typedArray.getColor(R.styleable.FinanceProgressView_mHigh_speed_color, Color.RED);
            } finally {
                typedArray.recycle();
            }
        }
    }

    private void configureArrow() {
        point = new PointF();
    }

    private void configureCircle() {
        CIRCLE.setColor(mFillColor);
        CIRCLE.setStyle(Paint.Style.FILL);
        CIRCLE.setStrokeWidth(10);
    }

    private void configureText() {
        TEXT_PAINT.setColor(mFillColor);
        TEXT_PAINT.setStyle(Paint.Style.FILL);
        TEXT_PAINT.setTextSize(FONT_SIZE);
    }

    private void configureFrontArc() {
        FRONT_ARC_PAINT.setColor(mFillColor);
        FRONT_ARC_PAINT.setShader(new LinearGradient(0, ARC_RECT.top, 2 * RADIUS, getHeight(),
                new int[]{
                        mLow_speed_color,
                        mAverage_speed_color,
                        mHigh_speed_color,

                }, null, Shader.TileMode.MIRROR));

        FRONT_ARC_PAINT.setStyle(Paint.Style.STROKE);
        FRONT_ARC_PAINT.setStrokeWidth(STROKE_WIDTH);
    }

    private void configureBackground() {
        BACKGROUND_CIRCLE_PAINT.setColor(Color.GRAY);
        BACKGROUND_CIRCLE_PAINT.setStyle(Paint.Style.STROKE);
        BACKGROUND_CIRCLE_PAINT.setStrokeWidth(STROKE_WIDTH);
    }
}
