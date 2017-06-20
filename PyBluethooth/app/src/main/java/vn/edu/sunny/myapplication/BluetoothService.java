package vn.edu.sunny.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by mars_ on 10/6/2016.
 */

public class BluetoothService {

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    //UUID string 1:  "fa87c0d0-afac-11de-8a39-0800200c9a66"
    //UUID string 2:  "00001101-0000-1000-8000-00805f9b34fb"

    // Name for the SDP record when creating server socket
    private static final String SPD_RECORD = "BluetoothHybrid";

    private final BluetoothAdapter BA;
    private final Handler myHand;

    private int mState;
    private String answerEndingFlag = "\n";

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private AcceptThread acceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public BluetoothService(Context context, Handler handler) {
        BA = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        myHand = handler;
    }

    private synchronized void setState(int state) {
        mState = state;
        myHand.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);
        // Start the thread to listen on a BluetoothServerSocket
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }

    }

    public synchronized void connect(BluetoothDevice device) {
        // Cancel any thread attempting to make a connection
        Log.i("huy", "device Name: "+device.getName()+ "device address: "+device.getAddress());
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice remoteDevice) {

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = myHand.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, remoteDevice.getName());
        msg.setData(bundle);
        myHand.sendMessage(msg);
        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        setState(STATE_NONE);
    }

    private void connectionFailed() {
        Message msg = myHand.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        myHand.sendMessage(msg);

        BluetoothService.this.start();
    }

    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = myHand.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        myHand.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }


    public void write(byte[] send) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(send);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;


        public AcceptThread() {
            BluetoothServerSocket temp = null;
            try {
//                temp = BA.listenUsingRfcommWithServiceRecord(SPD_RECORD, MY_UUID_SECURE);
                temp = BA.listenUsingInsecureRfcommWithServiceRecord(SPD_RECORD, MY_UUID_SECURE);
            } catch (IOException e) {
                Log.d("huy", "AcceptThread temp = BA.listenUsingRfcommWithServiceRecord(SPD_RECORD, MY_UUID_SECURE);");

            }
            mmServerSocket = temp;
        }

        @Override
        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket = null;
            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.d("huy", "AcceptThread  socket = serverSocket.accept();"+e);
                    break;
                }
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e("huy", "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {

            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket temp = null;
            try {
//                temp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
//                http://stackoverflow.com/questions/16457693/the-differences-between-createrfcommsockettoservicerecord-and-createrfcommsocket
                temp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_SECURE);
            } catch (IOException e) {
                Log.d("huy", "khong the ket noi 252");
            }
            mmSocket = temp;
        }

        @Override
        public void run() {
            setName("ConnectThread");
            BA.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                //connect false
                Log.d("huy", "error connect 261" + e);
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();

                }
                connectionFailed();
                return;
            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                //socket not created
            }
            mmOutStream = tempOut;
            mmInStream = tempIn;
        }

        @Override
        public void run() {
//            byte[] buffer = new byte[1024];
//            int bytes;
            byte[] buffer = new byte[1024];
            int bytes;
            StringBuilder readMessage = new StringBuilder();
            // keep listening to the InputStream while connected
//            mState == STATE_CONNECTED
            while (true) {

                try {
                    bytes = mmInStream.read(buffer);
                    String readed = new String(buffer, 0, bytes);
                    readMessage.append(readed);
//                    myHand.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();

                    if (readed.contains(answerEndingFlag)) {
                        myHand.obtainMessage(Constants.MESSAGE_READ, bytes, -1, readMessage.toString()).sendToTarget();
                        readMessage.setLength(0);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    //disconnected
                    connectionLost();
//                    BluetoothHybridRFService.this.start();
                    break;
                }
            }
        }

        /***
         * Write to the connected OutStream
         *
         * @param buffer the bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                myHand.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
                //error send
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
