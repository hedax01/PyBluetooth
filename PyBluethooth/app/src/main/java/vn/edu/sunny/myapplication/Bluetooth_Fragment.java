package vn.edu.sunny.myapplication;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class Bluetooth_Fragment extends Fragment{

//    private String jsonString="{"+"hi:"+"one,"+"lao:"+"two"+"}";
//    JSONObject jsonObject;
private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter BA = null;
    private BluetoothService hService = null;
    private String deviceName=null;
    private int sendHandle=0;
    private Handler huyHand=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity=getActivity();
            switch (msg.what){
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case BluetoothService.STATE_CONNECTED:
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] sendBuf=(byte[])msg.obj;
                    String sendMessage=new String(sendBuf);

                    break;
                case Constants.MESSAGE_READ:
//                    byte[] receivedBuf=(byte[])msg.obj;
//                    String receivedMessage=new String(receivedBuf);
                    final String receivedMessage=(String)msg.obj;
                    Toast.makeText(getActivity(),"receiver: "+receivedMessage, Toast.LENGTH_SHORT).show();

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    deviceName=msg.getData().getString(Constants.DEVICE_NAME);
                    if(null!=activity){
                        Toast.makeText(activity,"connected to "+deviceName,Toast.LENGTH_LONG).show();
                    }
                    break;

                case Constants.MESSAGE_TOAST:
                    if(null!=activity){
                        Toast.makeText(activity,msg.getData().getString(Constants.TOAST),Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BA=BluetoothAdapter.getDefaultAdapter();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!BA.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else if (hService == null) {
            setupService();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(hService!=null){
            if(hService.getState()==BluetoothService.STATE_NONE){
                hService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hService != null) hService.stop();
    }
    public void stopSevice(){
        if (hService != null) hService.stop();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button buttonSearch=(Button)view.findViewById(R.id.button_search);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
            }
        });

        final RadioGroup LED_groupRadio=(RadioGroup)view.findViewById(R.id.radioLEDID);
        LED_groupRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d("huy", "int la "+i + "  id "+getLED_ID(i));
            }
        });
        Button buttonSend=(Button)view.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject object=new JSONObject();
                try {
                    object.put("name","Quoc Huy");
                    object.put("age",34);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
             //   sendMessage(object.toString());
                int ledID=LED_groupRadio.getCheckedRadioButtonId();

                Log.d("huy",object.toString()+" LED ID "+ledID);
            }
        });

        RotorKnob knob=(RotorKnob)view.findViewById(R.id.knobBlue);
        knob.setMinVal(0);
        knob.setMaxVal(100);
        knob.setOnDataChange(new RotorKnob.onDataChangeEvent() {
            @Override

            public void onDataChange(float progress) {
                int  sendHandleL=Math.abs(sendHandle-(int)progress);
                if(sendHandleL>10){
                    if(progress<10) progress=0;
                int ledID=LED_groupRadio.getCheckedRadioButtonId();
                JSONObject mObject=new JSONObject();
                try{
                    mObject.put("LEDID",getLED_ID(ledID));
                    mObject.put("level",(int)progress);
                }catch (JSONException e){
                    e.printStackTrace();
                }
//                sendMessage(" "+(int)progress);
                    sendMessage(" "+mObject.toString());
                    sendHandle=(int)progress;
                }
            }
        });

    }

    private void setupService() {

        hService = new BluetoothService(getActivity(), huyHand);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data);
            }
        }
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                setupService();
            } else {
                Toast.makeText(getActivity(), "Bluetooth was not enable", Toast.LENGTH_LONG).show();

            }
        }
    }
    private void connectDevice(Intent data) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = BA.getRemoteDevice(address);
        Log.i("huy", "device Name: "+device.getName()+ "device address: "+device.getAddress());
        hService.connect(device);
    }

    private void sendMessage(String string) {
        //check sevice status
        if (hService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), getResources().getString(R.string.not_connect), Toast.LENGTH_LONG).show();
            return;
        }
        if(string.length()>0){
            byte[] send=string.getBytes();
            hService.write(send);
        }
    }

    private int getLED_ID(int i){
        switch (i){
            case R.id.radioLed1:
                return  0;
            case R.id.radioLed2:
                return  1;
            case R.id.radioLed3:
                return  2;
            case R.id.radioLed4:
                return  3;
            default:
                return -1;
        }
    }
}
