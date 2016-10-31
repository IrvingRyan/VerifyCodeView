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

/**
 * Created by yanwentao on 2016/10/20 0020.
 */

public class VerifyCodeView extends View {
    //wrap content 's width
    private static final int DEFAULT_WIDTH=600;
    //wrap content 's height
    private static final int DEFAULT_HEIGHT=200;
    private int mWidth;

    private int mHeight;
    //the code builder
    private StringBuilder codeBuilder;
    private String TAG="VerifyCodeView";
    //the paint between two solid lines
    private Paint blankPaint;
    //the paint to draw solid lines
    private Paint solidPaint;
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
    private PointF[] blankPoints;


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
            textColor = typedArray.getColor(R.styleable.VerifyCodeView_textColor, textColor);
            textSize = typedArray.getInt(R.styleable.VerifyCodeView_textSize, textSize);
            if (textSize<2)throw new IllegalArgumentException("Text size must more than 1!");
            lineWidth=typedArray.getDimensionPixelSize(R.styleable.VerifyCodeView_lineWidth, lineWidth);
            String font = typedArray.getString(R.styleable.VerifyCodeView_font);
            if (font!=null)
                typeface=Typeface.createFromAsset(context.getAssets(),font);
            typedArray.recycle();
        }
        if (codeBuilder==null)
            codeBuilder = new StringBuilder();

        blankPaint = new Paint();
        blankPaint.setColor(Color.TRANSPARENT);

        solidPaint = new Paint();
        solidPaint.setColor(textColor);
        solidPaint.setAntiAlias(true);
        solidPaint.setStrokeWidth(lineWidth);

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
            Log.d(TAG, "ACTION_DOWN");
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
        Log.i(TAG," keycode == "+keyCode);
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
        Log.i(TAG,"onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode==MeasureSpec.AT_MOST){
            mWidth=DEFAULT_WIDTH;
        }
        if (heightMode==MeasureSpec.AT_MOST){
            mHeight=DEFAULT_HEIGHT;
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
        int baseLine=Math.abs(-fontMetricsInt.bottom-fontMetricsInt.top);
        for (int i=0;i<textSize;i++){
            if (inputLength>i){
                canvas.drawText(codeBuilder.toString(), i,i+1, solidPoints[i].y-solidLine/2,mHeight/2+baseLine/2,textPaint);
            }else {
                canvas.drawLine(solidPoints[i].x,mHeight/2,solidPoints[i].y,mHeight/2,solidPaint);
            }
            canvas.drawLine(blankPoints[i].x,mHeight/2,blankPoints[i].y,mHeight/2,blankPaint);
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
        if (code.length()>4){
            throw new IllegalArgumentException("Code must less than 4 letters!");
        }
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
        blankPoints = new PointF[textSize];
        for (int i=1;i<=textSize;i++){
            solidPoints[i-1]=new PointF((i-1)*blankLine+(i-1)*solidLine,(i-1)*blankLine+i*solidLine);
            if (i==1){
                blankPoints[0]=new PointF(0,0);
                continue;
            }
            blankPoints[i-1]=new PointF((i-1)*blankLine+(i-1)*solidLine,i*blankLine+(i-1)*solidLine);
        }
    }

    public void setListener(OnTextChangListener listener) {
        this.listener = listener;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
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
}
