package vn.edu.sunny.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.util.AttributeSet;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by mars_ on 11/25/2016.
 */

public class RotorKnob extends View implements View.OnTouchListener {
    private Context mContext;
    private int height, width;
    private Drawable knobOn, knobOff, knobCover;
    private Rect viewBounds = new Rect();
    private float minVal = 0, maxVal=0,scaleVal, currentVal = 0;
    private GestureDetector mDetector;
    private onDataChangeEvent eventChange;


    public RotorKnob(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
//        knobOn = context.getResources().getDrawable(R.drawable.rotoron, context.getTheme());
        knobOn = context.getResources().getDrawable(R.drawable.rotoron);
        knobOff = context.getResources().getDrawable(R.drawable.rotoroff);
        knobCover = context.getResources().getDrawable(R.drawable.stator);
        mDetector = new GestureDetector(context, new GestureListener());
        mDetector.setIsLongpressEnabled(false);
        setOnTouchListener(this);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        viewBounds = new Rect(width / 2 - height / 2, 0, width / 2 + height / 2, height);
        setMeasuredDimension(width, height);

    }


    @Override
    protected void onDraw(Canvas canvas) {

        knobCover.setBounds(viewBounds);
        knobCover.draw(canvas);
        knobOn.setBounds(viewBounds);
        canvas.save();
        canvas.rotate(currentVal, width / 2, height / 2);
        knobOn.draw(canvas);
        canvas.restore();

    }
    public void release(){
        destroyDrawingCache();
    }


    public void setMinVal(float minVal) {
        this.minVal = minVal;
        if (maxVal != 0) {
            scaleVal = (maxVal - minVal) / (float) 24;

        }
        currentVal = minVal;
    }

    public void setMaxVal(float maxVal) {
        this.maxVal = maxVal;
        scaleVal = (maxVal - minVal) / (float) 288;

    }
    public void setCurrent(float currentVal){
        this.currentVal=(currentVal-minVal)/scaleVal;
        invalidate();
    }
    public float getCurrent(){return currentVal;}


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d("huy", "chit me a?");
        boolean result = mDetector.onTouchEvent(event);
        return result;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            currentVal = currentVal - vectorToScalarScroll(distanceX, distanceY, e2.getX() - viewBounds.centerX(), e2.getY() - viewBounds.centerY()) / 3;
            if (currentVal < 0) currentVal = 0;
            if (currentVal > 288) currentVal = 288;
            float data=currentVal*scaleVal+minVal;
            eventChange.onDataChange(data);
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {

            return true;
        }

    }
    public interface onDataChangeEvent{
        void onDataChange(float progress);
    }

    public void setOnDataChange(onDataChangeEvent eventChange){
        this.eventChange=eventChange;
    }
    private static float vectorToScalarScroll(float dx, float dy, float x, float y) {
        // get the length of the vector
        float l = (float) Math.sqrt(dx * dx + dy * dy);

        // decide if the scalar should be negative or positive by finding
        // the dot product of the vector perpendicular to (x,y).
        float crossX = -y;
        float crossY = x;

        float dot = (crossX * dx + crossY * dy);
        float sign = Math.signum(dot);

        return l * sign;
    }

}
