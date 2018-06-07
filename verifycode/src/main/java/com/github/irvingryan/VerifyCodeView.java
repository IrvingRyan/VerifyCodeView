package com.github.irvingryan;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.IntDef;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.github.irvingryan.utils.UIUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by yanwentao on 2016/10/20 0020.
 */

public class VerifyCodeView extends View {
    private String TAG="VerifyCodeView";
    private int mWidth;

    private int mHeight;
    //the code builder
    private StringBuilder codeBuilder;
    //the paint to draw solid lines
    private Paint linePaint;
    //the paint to draw text
    private Paint textPaint;
    //text font
    private Typeface typeface=Typeface.DEFAULT;
    private OnTextChangListener listener;

    private int textColor=Color.CYAN;
    //how many words to show
    private int textSize=4;
    //transparent line between solid lines
    private int blankLine;
    private int solidLine;
    //solid line's width
    private int lineWidth=5;

    private PointF[] solidPoints;


    public static final int INPUT_NO_LINE=0;

    public static final int INPUT_LINE_UNDER_TEXT=1;

    @IntDef({INPUT_NO_LINE, INPUT_LINE_UNDER_TEXT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LineStyle {
    }
    @LineStyle private int lineStyle=INPUT_NO_LINE;

    private int mLinePosY;
    public VerifyCodeView(Context context) {
        super(context);
        init(context,null);
    }

    public VerifyCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public VerifyCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VerifyCodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs!=null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerifyCodeView);
            textColor = typedArray.getColor(R.styleable.VerifyCodeView_vcTextColor, textColor);
            textSize = typedArray.getInt(R.styleable.VerifyCodeView_vcTextSize, textSize);
            if (textSize<2)throw new IllegalArgumentException("Text size must more than 1!");
            lineWidth=typedArray.getDimensionPixelSize(R.styleable.VerifyCodeView_vcLineWidth, lineWidth);
            String font = typedArray.getString(R.styleable.VerifyCodeView_vcFont);
            if (font!=null)
                typeface=Typeface.createFromAsset(context.getAssets(),font);
            switch (typedArray.getInt(R.styleable.VerifyCodeView_vcLineStyle, INPUT_NO_LINE)) {
                case INPUT_NO_LINE:
                    lineStyle = INPUT_NO_LINE;
                    break;

                case INPUT_LINE_UNDER_TEXT:
                    lineStyle = INPUT_LINE_UNDER_TEXT;
                    break;
            }
            typedArray.recycle();
        }
        if (codeBuilder==null)
            codeBuilder = new StringBuilder();

        linePaint = new Paint();
        linePaint.setColor(textColor);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(lineWidth);

        textPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(typeface);
        setFocusableInTouchMode(true); // allows the keyboard to pop up on
                                        // touch down
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        requestFocus();//must have focus to show the keyboard
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // touch down
//            Log.d(TAG, "ACTION_DOWN");
            // show the keyboard so we can enter text
            InputMethodManager imm = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        }
        return true;
    }
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        //define keyboard to number keyboard
        BaseInputConnection fic = new BaseInputConnection(this, false);
        outAttrs.actionLabel = null;
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_NEXT;
        return fic;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.i(TAG," keycode == "+keyCode);
        if (codeBuilder==null)codeBuilder=new StringBuilder();
        //67 is backspace,7-16 are 0-9
        if (keyCode == 67 && codeBuilder.length() > 0) {
            codeBuilder.deleteCharAt(codeBuilder.length() - 1);
            if (listener!=null){
                listener.afterTextChanged(codeBuilder.toString());
            }
            invalidate();
        } else if (keyCode >= 7 && keyCode <= 16 && codeBuilder.length() < textSize) {
            codeBuilder.append(keyCode - 7);
            if (listener!=null){
                listener.afterTextChanged(codeBuilder.toString());
            }
            invalidate();
        }
        //hide soft keyboard
        if (codeBuilder.length() >= textSize||keyCode==66) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Log.i(TAG,"onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode==MeasureSpec.AT_MOST){
            mWidth= UIUtils.getWidth(getContext())*2/3;
        }
        if (heightMode==MeasureSpec.AT_MOST){
            mHeight=UIUtils.getWidth(getContext())/4;
        }

        //calculate line's length
        blankLine = mWidth / (4*textSize-1);    //short one
        solidLine = mWidth / (4*textSize-1)*3;  //long one

        if (textPaint!=null)
            textPaint.setTextSize(solidLine);
        calculateStartAndEndPoint(textSize);
        setMeasuredDimension(mWidth,mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLine(canvas);
    }

    private void drawLine(Canvas canvas) {
        if (codeBuilder==null)return;
        int inputLength=codeBuilder.length();
        Paint.FontMetricsInt fontMetricsInt = textPaint.getFontMetricsInt();
        //text's vertical center is view's center
        int baseLine=mHeight/2 + (fontMetricsInt.bottom - fontMetricsInt.top)/2 - fontMetricsInt.bottom;
        switch (lineStyle){
            case INPUT_NO_LINE:
                mLinePosY=mHeight/2;
                for (int i=0;i<textSize;i++){
                    if (inputLength>i){
                        canvas.drawText(codeBuilder.toString(), i,i+1, solidPoints[i].y-solidLine/2,baseLine,textPaint);
                    }else {
                        canvas.drawLine(solidPoints[i].x,mLinePosY,solidPoints[i].y,mLinePosY, linePaint);
                    }
                }
                break;
            case INPUT_LINE_UNDER_TEXT:
                mLinePosY=baseLine+lineWidth;
                for (int i=0;i<textSize;i++){
                    if (inputLength>i){
                        canvas.drawText(codeBuilder.toString(), i,i+1, solidPoints[i].y-solidLine/2,baseLine,textPaint);
                    }
                    canvas.drawLine(solidPoints[i].x,mLinePosY,solidPoints[i].y,mLinePosY, linePaint);
                }
                break;

        }
    }

    /**
     * get verify code string
     * @return code
     */
    public String getText(){
        return codeBuilder!=null?codeBuilder.toString():"";
    }

    /**
     * set verify code (must less than 4 letters)
     * @param code code
     */
    public void setText(String code){
        if (code==null)
            throw new NullPointerException("Code must not null!");
//         if (code.length()>4){
//             throw new IllegalArgumentException("Code must less than 4 letters!");
//         }
        codeBuilder=new StringBuilder();
        codeBuilder.append(code);
        invalidate();
    }

    public interface OnTextChangListener {
        void afterTextChanged(String text);
    }

    /**
     * calculate every points
     * @param textSize code length
     */
    private void calculateStartAndEndPoint(int textSize){
        solidPoints = new PointF[textSize];
        for (int i=1;i<=textSize;i++){
            solidPoints[i-1]=new PointF((i-1)*blankLine+(i-1)*solidLine,(i-1)*blankLine+i*solidLine);
        }
    }

    public void setListener(OnTextChangListener listener) {
        this.listener = listener;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(@ColorRes int textColor) {
        this.textColor = textColor;
    }

    public int getTextSize() {
        return textSize;
    }

    /**
     * set the code's length
     * @param textSize code length
     */
    public void setTextSize(int textSize) {
        if (textSize<2)throw new IllegalArgumentException("Text size must more than 1!");
        this.textSize = textSize;
    }


    /**
     * custom font
     * @param typeface font
     */
    public void setFont(Typeface typeface) {
        this.typeface = typeface;
    }

    /**
     * custom font
     * @param path assets' path
     */
    public void setFont(String path) {
        typeface=Typeface.createFromAsset(getContext().getAssets(),path);
    }

    /**
     * define input line's style
     * @param lineStyle
     * In addition, the lineStyle variation must be one of
     * {@link VerifyCodeView#INPUT_NO_LINE},
     * {@link VerifyCodeView#INPUT_LINE_UNDER_TEXT}
     */
    public void setLineStyle(@LineStyle int lineStyle) {
        this.lineStyle = lineStyle;
    }
}
