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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.PasswordAuthentication;

/**
 * @author Andrey Kudryavtsev on 2019-11-26.
 */
public class FinanceProgressView extends View {

    private static final String TAG = "FinanceProgressView";

    private static final Paint BACKGROUND_CIRCLE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint FRONT_ARC_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint CIRCLE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Path ARROW = new Path();

    private static final float STROKE_WIDTH = 50f;
    // private static final float RADIUS = 300f;
    // private static final float RADIUS_OF_ARROW = 200f;
    //private static final float RADIUS_OF_CIRCLE = 30f;
    // private static final RectF ARC_RECT = new RectF(STROKE_WIDTH / 2, STROKE_WIDTH / 2, 2 * RADIUS, 2 * RADIUS);

    private static final int MAX_PROGRESS = 200;
    private static final float MAX_ANGLE = 240f;
    private static final float START_ANGLE = -210f;
    private static final float FONT_SIZE = 64f;
    //private static final float CENTER_X = ARC_RECT.width() / 2f + ARC_RECT.left;
    //private static final float CENTER_Y = ARC_RECT.height() / 2f + ARC_RECT.top;

    private PointF point;
    private int mRadius;
    private int mRadius_of_arrow;
    private int mRadius_of_circle;
    private RectF ARC_RECT = new RectF();
    private float CENTER_X;
    private float CENTER_Y;

    private Rect mTextBounds = new Rect();
    private int mProgress;


    @ColorInt
    private int mFillColor;
    private int mLow_speed_color;
    private int mAverage_speed_color;
    private int mHigh_speed_color;
    private static final float MAGIC_MULTIPLIER = 1.05f;

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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure() called with: widthMeasureSpec = [" + MeasureSpec.toString((widthMeasureSpec)) + "], heightMeasureSpec = [" + MeasureSpec.toString((heightMeasureSpec)) + "]");

        final String maxProgressString = formatString(MAX_PROGRESS);
        getTextBounds(maxProgressString);
        float desiredWidth = Math.max(mTextBounds.width() + 2 * STROKE_WIDTH, getSuggestedMinimumWidth()) + getPaddingLeft() + getPaddingRight();
        float desiredHeight = Math.max(mTextBounds.height() + 2 * STROKE_WIDTH, getSuggestedMinimumHeight()) + getPaddingTop() + getPaddingBottom();
        int desiredSize =  (int) (MAGIC_MULTIPLIER * Math.max(desiredHeight, desiredWidth));
        int height = resolveSize( desiredSize, widthMeasureSpec);
        int width = resolveSize( desiredSize, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h + "], oldw = [" + oldw + "], oldh = [" + oldh + "]");
        super.onSizeChanged(w, h, oldw, oldh);
        final int size = Math.min(h, w);

        ARC_RECT = new RectF(getPaddingLeft() + STROKE_WIDTH / 2, getPaddingTop() + STROKE_WIDTH / 2, size - STROKE_WIDTH / 2 - getPaddingRight(), size - STROKE_WIDTH / 2 - getPaddingBottom());
        CENTER_X = ARC_RECT.width() / 2f + ARC_RECT.left;
        CENTER_Y = ARC_RECT.height() / 2f + ARC_RECT.top;
        float current_height = size - STROKE_WIDTH - getPaddingBottom() - getPaddingTop();
        float current_width = size - STROKE_WIDTH - getPaddingRight() - getPaddingLeft();

        mRadius = Math.min((int) current_height, (int) current_width) / 2;
        mRadius_of_arrow = mRadius / 3 * 2;
        mRadius_of_circle = mRadius / 10;
        configureBackground();
        configureFrontArc();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        savedState.mProgress = mProgress;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mProgress = savedState.mProgress;

    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawArrow(canvas);
        canvas.drawArc(ARC_RECT, START_ANGLE, MAX_ANGLE, false, FRONT_ARC_PAINT);
        canvas.drawCircle(ARC_RECT.width() / 2f + ARC_RECT.left, ARC_RECT.height() / 2f + ARC_RECT.top, mRadius_of_circle, CIRCLE);
        drawText(canvas);

    }

    private void drawArrow(Canvas canvas) {
        float currentAngle = START_ANGLE + (MAX_ANGLE * mProgress / MAX_PROGRESS);
        ARROW.reset();
        setPoint(currentAngle + 90, mRadius_of_circle);
        ARROW.moveTo(point.x, point.y);
        setPoint(currentAngle - 90, mRadius_of_circle);
        ARROW.lineTo(point.x, point.y);
        setPoint(currentAngle, mRadius_of_arrow);
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
        FRONT_ARC_PAINT.setShader(new LinearGradient(0, ARC_RECT.top, 2 * mRadius, 2 * mRadius - STROKE_WIDTH,
                new int[]{
                        mLow_speed_color,
                        mAverage_speed_color,
                        mHigh_speed_color,

                }, null, Shader.TileMode.CLAMP));

        FRONT_ARC_PAINT.setStyle(Paint.Style.STROKE);
        FRONT_ARC_PAINT.setStrokeWidth(STROKE_WIDTH);
    }

    private void configureBackground() {
        BACKGROUND_CIRCLE_PAINT.setColor(Color.GRAY);
        BACKGROUND_CIRCLE_PAINT.setStyle(Paint.Style.STROKE);
        BACKGROUND_CIRCLE_PAINT.setStrokeWidth(STROKE_WIDTH);
    }

    private  static final class SavedState extends BaseSavedState {
        private int mProgress;
        public SavedState(Parcel source) {
            super(source);
            mProgress = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mProgress);
        }
    }
}
