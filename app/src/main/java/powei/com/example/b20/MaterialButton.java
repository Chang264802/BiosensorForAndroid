package powei.com.example.b20;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;

public class MaterialButton extends androidx.appcompat.widget.AppCompatImageButton {

    public Paint backgroundPaint;
    private float paintX,paintY,radius;

    public MaterialButton(Context context) {
        super(context);
        init(null, 0);
    }

    public MaterialButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MaterialButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.MaterialButton, defStyle, 0);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawCircle(paintX,paintY,radius,backgroundPaint);
        canvas.restore();

        super.onDraw(canvas);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            //紀錄座標
            paintX=event.getX();
            paintY=event.getY();
            //啟動動畫
            startAnimator();
        }
        return super.onTouchEvent(event);
    }

    private Property<MaterialButton,Float> mRadiusProperty = new Property<MaterialButton, Float>(Float.class,"radius") {
        @Override
        public Float get(MaterialButton object) {
            return object.radius;
        }
        @Override
        public void set(MaterialButton object,Float value){
            object.radius=value;
            //刷新Canvas
            invalidate();
        }
    };

    private Property<MaterialButton,Integer> mBackgroundColorProperty=new Property<MaterialButton, Integer>(Integer.class,"bg_color") {
        @Override
        public Integer get(MaterialButton object) {
            return object.backgroundPaint.getColor();
        }
        public void set(MaterialButton object,Integer value){
            object.backgroundPaint.setColor(value);
        }
    };

    private void startAnimator(){
        //計算半徑變化區域
        int start,end;

        if(getHeight() < getWidth()){
           start=getHeight();
           end = getWidth();
        }else{
            start =getWidth();
            end=getHeight();
        }
        float startRadius = (start / 2 >paintY ? start-paintY:paintY)*1.15f;
        float endRadius=(end/2>paintX ? end - paintX:paintX)*0.85f;

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                //半徑變化
                ObjectAnimator.ofFloat(this,mRadiusProperty,startRadius,endRadius),
                //顏色變化 黑色到透明
                ObjectAnimator.ofObject(this,mBackgroundColorProperty,new ArgbEvaluator(),Color.BLACK,Color.TRANSPARENT)
        );
        //設置時間
        set.setDuration((long)(1200/end*endRadius));
        //先快後慢
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }
}