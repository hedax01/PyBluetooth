package vn.edu.sunny.myapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;



public class MainActivity extends FragmentActivity implements MyActionBar.Action, View.OnClickListener, Item_main_view.ItemInteract {

//    private boolean backkeyTouch=false;
    private MyActionBar myActionBar;
    private Bluetooth_Fragment bluetoothFragment=new Bluetooth_Fragment();
    private Home_fragment homeFragment=new Home_fragment();
    private Grarden_Info_Fragment grarden_info_fragment=new Grarden_Info_Fragment();
    private Button button_callHome, button_callBlue;
    private LinearLayout mainContents;
    private Item_main_view mainViewItem1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myActionBar=(MyActionBar)findViewById(R.id.actionBar_main);
        myActionBar.setHomeLogo(this);
        myActionBar.setTitle("Py Blue");
        button_callBlue=(Button)findViewById(R.id.button_callBlue);
        button_callHome=(Button)findViewById(R.id.button_callHome);
        button_callHome.setOnClickListener(this);
        button_callBlue.setOnClickListener(this);

        mainContents=(LinearLayout)findViewById(R.id.layout_mainContents);
        mainViewItem1=(Item_main_view)findViewById(R.id.content1);
        mainViewItem1.addCallBack(this);



    }

    @Override
    public void onBackPressed() {
        fragmentTransactionBack();
//        if(backkeyTouch) {
//            Toast.makeText(this,"Good bye I love you",Toast.LENGTH_SHORT).show();
//            super.onBackPressed();
//            return;
//        }
//        backkeyTouch=true;
//        Toast.makeText(this,"Please touch BACK again to exit", Toast.LENGTH_SHORT).show();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                backkeyTouch=false;
//            }
//        },2500);
    }

    private void callFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.move_left,R.anim.move_right);
        transaction.replace(R.id.fm_Contents,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public int getDrawable() {
        return R.drawable.home_;
    }

    @Override
    public void performAction(View view) {
        Toast.makeText(this, "Fuck! Đây là Home rồi touch cái gì?", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_callBlue:
                myActionBar.setHomeLogo(new BackAction());
                mainContents.startAnimation(AnimationUtils.loadAnimation(this, R.anim.move_right));
                mainContents.setVisibility(View.GONE);
                callFragment(bluetoothFragment);

                break;
            case R.id.button_callHome:
                mainContents.setVisibility(View.GONE);
                mainContents.startAnimation(AnimationUtils.loadAnimation(this, R.anim.move_right));
                myActionBar.setHomeLogo(new BackAction());
                callFragment(homeFragment);
                break;
        }
    }
    private void fragmentTransactionBack(){
        FragmentManager fragmentManager=getSupportFragmentManager();
        int fragmentStack=fragmentManager.getBackStackEntryCount();
        if(fragmentStack>0){
            if (fragmentStack==1){
                fragmentManager.popBackStack();
                mainContents.startAnimation(AnimationUtils.loadAnimation(this, R.anim.out_left));
                mainContents.setVisibility(View.VISIBLE);
                myActionBar.setHomeLogo(MainActivity.this);
            }else {
                fragmentManager.popBackStack();
            }
        }
        else {
            finish();
        }
    }

    @Override
    public void led1ButtonFunc() {
        mainContents.setVisibility(View.GONE);
        mainContents.startAnimation(AnimationUtils.loadAnimation(this, R.anim.move_right));
        myActionBar.setHomeLogo(new BackAction());
        callFragment(new TestKnobFragment());
    }

    @Override
    public void led2ButtonFunc() {
        myActionBar.setHomeLogo(new BackAction());
        mainContents.startAnimation(AnimationUtils.loadAnimation(this, R.anim.move_right));
        mainContents.setVisibility(View.GONE);
        callFragment(bluetoothFragment);
    }

    @Override
    public void led3ButtonFunc() {
        Toast.makeText(this, "chít vào LED 3 roi",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void led4ButtonFunc() {
        Toast.makeText(this, "chít vào LED 4 roi",Toast.LENGTH_SHORT).show();

    }

    private final class BackAction implements MyActionBar.Action{

        public BackAction(){

        }
        @Override
        public int getDrawable() {
            return R.drawable.back;
        }

        @Override
        public void performAction(View view) {
            fragmentTransactionBack();
        }
    }

}
