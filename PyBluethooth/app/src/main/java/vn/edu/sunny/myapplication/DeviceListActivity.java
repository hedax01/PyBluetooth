package vn.edu.sunny.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class DeviceListActivity extends Activity {

    private BluetoothAdapter BA;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private ArrayAdapter<String> newDevicesArrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);
        setResult(Activity.RESULT_CANCELED);
        BA=BluetoothAdapter.getDefaultAdapter();

        //setting for paired devices list view
        ArrayAdapter<String> pairedListDeviceAdapter= new ArrayAdapter<String>(this,R.layout.history_view);
        ListView pairedListView=(ListView)findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedListDeviceAdapter);
        pairedListView.setOnItemClickListener( mDeviceClickLisenner);
        // show paired devices list
        Set<BluetoothDevice> pairedDevice=BA.getBondedDevices();
        if(pairedDevice.size()>0){
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for(BluetoothDevice device:pairedDevice){
                pairedListDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        else {
            pairedListDeviceAdapter.add("No devices have paired");
        }

        //setting for search new devices
        newDevicesArrayAdapter=new ArrayAdapter<String>(this,R.layout.history_view);
        ListView lvNewDevices=(ListView)findViewById(R.id.new_devices);
        lvNewDevices.setAdapter(newDevicesArrayAdapter);
        lvNewDevices.setOnItemClickListener(mDeviceClickLisenner);

        //setting for searching event
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        Button scanButton=(Button)findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchForDevices();
                view.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(BA!=null){
            BA.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
    }


    private void searchForDevices() {
        setProgressBarIndeterminateVisibility(true);
        setTitle(getResources().getString(R.string.scanning));
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        if(BA.isDiscovering()){
            BA.cancelDiscovery();
        }
        BA.startDiscovery();
    }
    /**
     * Get device mac address and sen to BlueFragment
     *
     * */
    AdapterView.OnItemClickListener mDeviceClickLisenner=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();

            // When discovery finds a device
            if(BluetoothDevice.ACTION_FOUND.equals((action))){
                //get the BluetoothDevice object from the Intent
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState()!=BluetoothDevice.BOND_BONDED){
                    newDevicesArrayAdapter.add(device.getName() +"\n" + device.getAddress());
                }
            }else  if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                setProgressBarIndeterminateVisibility(false);
                if(newDevicesArrayAdapter.getCount()==0) {
                    setTitle(getResources().getString(R.string.select_device));
                    newDevicesArrayAdapter.add("No device found");
                }
            }

        }
    };
}
