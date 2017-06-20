package vn.edu.sunny.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by mars_ on 11/24/2016.
 */

public class Item_main_view extends LinearLayout implements View.OnClickListener {

    private LayoutInflater inflater;
    private LinearLayout mLayout;
    private LinearLayout mainBanner;
    private Context context;
    private ImageButton buttonLed1, buttonLed2, buttonLed3, buttonLed4;
    private ItemInteract itemInteract;

    public Item_main_view(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout=(LinearLayout)inflater.inflate(R.layout.main_item,null);
        addView(mLayout);
        addCallBack(new InstanceItemInteract(context));
        mainBanner=(LinearLayout)mLayout.findViewById(R.id.main_banner);
        buttonLed1 =(ImageButton)mLayout.findViewById(R.id.button_led1);
        buttonLed2 =(ImageButton)mLayout.findViewById(R.id.button_led2);
        buttonLed3 =(ImageButton)mLayout.findViewById(R.id.button_led3);
        buttonLed4 =(ImageButton)mLayout.findViewById(R.id.button_led4);
        TypedArray a=context.getTheme().obtainStyledAttributes(attrs,R.styleable.Item_main_view,0,0);
        try {
            Drawable drawable=a.getDrawable(R.styleable.Item_main_view_bannerImage);
            if(drawable!=null){
                mainBanner.setBackground(drawable);
            }
            Drawable ledButtonImage=a.getDrawable(R.styleable.Item_main_view_ledButtonImage);
            if(ledButtonImage!=null) buttonLed1.setBackground(ledButtonImage);
        }finally {
            a.recycle();
        }

        buttonLed1.setOnClickListener(this);
        buttonLed2.setOnClickListener(this);
        buttonLed3.setOnClickListener(this);
        buttonLed4.setOnClickListener(this);

    }
    public void addCallBack(ItemInteract itemInteract){
    this.itemInteract=itemInteract;
}
    public void setMainBanerImage(int drawable){
        mainBanner.setBackground(context.getDrawable(drawable));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_led1:
                itemInteract.led1ButtonFunc();
                break;
            case R.id.button_led2:
                itemInteract.led2ButtonFunc();
                break;
            case R.id.button_led3:
                itemInteract.led3ButtonFunc();
                break;
            case R.id.button_led4:
                itemInteract.led4ButtonFunc();
                break;
        }
    }

    public interface ItemInteract{
        void led1ButtonFunc();
        void led2ButtonFunc();
        void led3ButtonFunc();
        void led4ButtonFunc();
    }

    private final static class InstanceItemInteract implements ItemInteract{
        private Context mContext;
        public InstanceItemInteract(Context context){
            mContext=context;
        }

        @Override
        public void led1ButtonFunc() {
            Toast.makeText(mContext,"please add callback for this button",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void led2ButtonFunc() {
            Toast.makeText(mContext,"please add callback for this button",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void led3ButtonFunc() {
            Toast.makeText(mContext,"please add callback for this button",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void led4ButtonFunc() {
            Toast.makeText(mContext,"please add callback for this button",Toast.LENGTH_SHORT).show();
        }
    }
}
