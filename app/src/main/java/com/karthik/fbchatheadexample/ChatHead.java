package com.karthik.fbchatheadexample;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by karthikrk on 12/09/15.
 */
public class ChatHead extends View implements SpringListener {
    private static final int NUM_ELEMS = 1;
    Spring mXSprings,mYSprings;
    private Paint mPaint = new Paint();
    Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

    public ChatHead(Context context) {
        super(context);
        intialize();

    }

    public ChatHead(Context context,AttributeSet attributeSet){
        super(context,attributeSet);
        intialize();
    }

    public ChatHead(Context context,AttributeSet attributeSet,int defStyle){
        super(context,attributeSet,defStyle);
        intialize();
    }

    private void intialize(){
        SpringSystem ss = SpringSystem.create();

        Spring s;

        s = ss.createSpring();
        s.setSpringConfig(new MySpringConfig(200, 0 == 0? 8 : 15 + 0 * 2, 0, true));
        s.addListener(this);
        mXSprings= s;

        s = ss.createSpring();
        s.setSpringConfig(new MySpringConfig(200, 0 == 0? 8 : 15 + 0 * 2, 0, false));
        s.addListener(this);
        mYSprings = s;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mXSprings.setCurrentValue(w / 2);
        mYSprings.setCurrentValue(0);

        mXSprings.setEndValue(w / 2);
        mYSprings.setEndValue(h / 2);
    }

    @Override
    public void onSpringActivate(Spring s) {
    }

    @Override
    public void onSpringAtRest(Spring s) {
    }

    @Override
    public void onSpringEndStateChange(Spring s) {
    }

    @Override
    public void onSpringUpdate(Spring s) {
        MySpringConfig cfg = (MySpringConfig) s.getSpringConfig();
        if (cfg.index < NUM_ELEMS - 1) {
            Spring springs = cfg.horizontal? mXSprings : mYSprings;
            springs.setEndValue(s.getCurrentValue());
        }
        if (cfg.index == 0) {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = NUM_ELEMS - 1; i >= 0; i--) {
            mPaint.setAlpha(i == 0? 255 : 192 - i * 128 / NUM_ELEMS);
            canvas.drawBitmap(mBitmap,
                    (float) mXSprings.getCurrentValue() - mBitmap.getWidth() / 2,
                    (float) mYSprings.getCurrentValue() - mBitmap.getHeight() / 2,
                    mPaint);
        }
    }

    class MySpringConfig extends SpringConfig {
        int index;
        boolean horizontal;
        public MySpringConfig(double tension, double friction, int index, boolean horizontal) {
            super(tension, friction);
            this.index = index;
            this.horizontal = horizontal;
        }
    }
}
