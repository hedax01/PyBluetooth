package vn.edu.sunny.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by mars_ on 9/29/2016.
 */

public class MyActionBar extends RelativeLayout implements View.OnClickListener {

    private LayoutInflater inflater;
    private RelativeLayout mBar;
    private LinearLayout mActionView;
    private ImageButton mMenuImg;
    private TextView acTitle;
//    private Handler bHand = new Handler();
//    //    private Timer timer=new Timer();
//    Thread updateTimeThread = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            updateTime();
//            bHand.postDelayed(updateTimeThread,30000);
//        }
//    });

    public MyActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBar = (RelativeLayout) inflater.inflate(R.layout.actionbar, null);
        addView(mBar);
        mMenuImg = (ImageButton) mBar.findViewById(R.id.actionBarMenu);
        acTitle = (TextView) mBar.findViewById(R.id.acTitle);
        mActionView = (LinearLayout) mBar.findViewById(R.id.actionbar_action);


    }



    @Override
    public void onClick(View view) {
        final Object tag = view.getTag();
        if (tag instanceof Action) {
            final Action action = (Action) tag;
            action.performAction(view);
        }
    }

    public interface Action {
        public int getDrawable();

        public void performAction(View view);
    }

    public static abstract class AbstractAction implements Action {
        final private int mDrawable;

        public AbstractAction(int mDrawable) {
            this.mDrawable = mDrawable;
        }

        @Override
        public int getDrawable() {
            return mDrawable;
        }
    }

    public static class IntentAction extends AbstractAction {
        private Context mContext;
        private Intent mIntent;

        public IntentAction(Context mContext, Intent mIntent, int mDrawable) {
            super(mDrawable);
            this.mContext = mContext;
            this.mIntent = mIntent;
        }


        @Override
        public void performAction(View view) {
            try {
                mContext.startActivity(mIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, "not found activity", Toast.LENGTH_LONG).show();
            }

        }
    }

    /**
     * Create view for action layout
     */
    private View inflateAction(Action action) {
        View view = inflater.inflate(R.layout.actionbar_item, mActionView, false);
        ImageButton labelView = (ImageButton) view.findViewById(R.id.actionbar_item);
        labelView.setImageResource(action.getDrawable());
        view.setTag(action);
        view.setOnClickListener(this);
        return view;
    }

    public void addAction(Action action) {
        final int index = mActionView.getChildCount();
        addAtion(action, index);
    }

    public void removeAction(Action action) {
        int childCount = mActionView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mActionView.getChildAt(i);
            if (view != null) {
                final Object tag = view.getTag();
                if (tag instanceof Action && tag.equals(action)) {
                    mActionView.removeView(view);
                }
            }
        }
    }

    private void addAtion(Action action, int index) {
        mActionView.addView(inflateAction(action), index);
    }

    public void setTitle(String title) {
        acTitle.setText(title);
    }

    public void setHomeLogo(Action action) {
        mMenuImg.setOnClickListener(this);
        mMenuImg.setTag(action);
        mMenuImg.setImageResource(action.getDrawable());
//        mMenuImg.setBackground(context.getDrawable(action.getDrawable()));
    }

}
