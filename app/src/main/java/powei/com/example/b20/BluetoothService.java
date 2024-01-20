package powei.com.example.b20;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;

public class BluetoothService extends Service {

    private static final int MESSAGE_READ = 1;
    private static final int CONNECTING_STATUS = 2;
    private final int CONNECTING_DEVICE = 3;
    private final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String MAC_ADDRESS = "98:D3:31:F5:22:F9";
    private final static String LOG_TAG = "ServiceLog";
    private BluetoothAdapter mBTAdapter;
    private BluetoothDevice device;
    private ConnectedThread mConnectedThread;
    private int deviceStatus;
    private Handler mHandler;
    private BluetoothSocket mBTSocket = null;
    private String deviceName = "Research";
    private Boolean BTAvg = false;
    private Boolean BTAvgInvalid = false;
    private Boolean Arduino = null;


    public BluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand Method");

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter != null && mBTAdapter.isEnabled()) {
            device = mBTAdapter.getRemoteDevice(MAC_ADDRESS);
            mHandler = new MsgHandler();
            new Thread() {
                public void run() {
                    boolean fail = false;
                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        onAlarm("藍牙相關問題","藍牙裝置插座建立失敗!");
                    }
                    try {
                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            onAlarm("藍牙相關問題","藍牙權限不足，藍牙連線失敗!");
                        } else {
                            mBTSocket.connect();
                        }
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                        } catch (IOException e2) {
                            onAlarm("藍牙相關問題","藍牙連線失敗!");
                        }
                    }
                    if (fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();
                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, deviceName).sendToTarget();
                        deviceStatus = CONNECTING_DEVICE;
                    }
                }
            }.start();
        } else {
            onAlarm("藍牙相關問題","裝置未支援藍牙或未開啟藍牙功能!");
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onAlarm("藍牙相關問題","權限不足，藍牙裝置插座建立失敗!");
        }

        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

    private void onAlarm(String alarmType, String alarmValue){
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND,3); //設定於3秒鐘後執行
        intent.putExtra("alarmType", alarmType);
        intent.putExtra("alarmValue", alarmValue);
        PendingIntent pi = PendingIntent.getBroadcast(getBaseContext(),1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }

    class MsgHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == MESSAGE_READ){
                byte[] bytes = (byte[]) msg.obj;

                if (bytes.length >= 10 && bytes[0] == 11) {
                    int heartRate = bytes[1];
                    if(heartRate <= 45){
                        onAlarm("心率過低", String.valueOf(heartRate));
                    }
                    if(heartRate >= 100) {
                        onAlarm("心率過高", String.valueOf(heartRate));
                    }

                    if(bytes[2] == 12) {
                        int SpO2 = bytes[3];
                        if(SpO2 < 95){
                            onAlarm("血氧數值異常", String.valueOf(SpO2));
                        }
                    }
                    if(bytes[4] == 13) {
                        float temperature = bytes[5] + (float)bytes[6] / 100;
                        if(temperature < 36.0){
                            onAlarm("體溫過低", String.valueOf(temperature));
                        }
                        if(temperature > 37.5){
                            onAlarm("體溫過高", String.valueOf(temperature));
                        }
                    }
                    BTAvg = null;
                }
                else if(bytes.length >= 1 && bytes[0] == 0){
                    Arduino = true;
                }
                else if(bytes.length >= 1 && bytes[0] == 100){
                    BTAvg = true;
                }

                if(Arduino == null){
                    try{
                        mBTSocket.getOutputStream().write("Hello Arduino".getBytes());
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }

                if(BTAvg == null){
                    try{
                        mBTSocket.getOutputStream().write("BTAvgOK".getBytes());
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }

                Log.d(LOG_TAG, "心率 : " + bytes[1] + "、血氧 : " + bytes[3] + "、體溫 : " + (bytes[5] + (float)bytes[6] / 100));
            }

            if(msg.what == CONNECTING_STATUS){
                if(msg.arg1 == 1){
                    onAlarm("藍牙相關問題","連線到裝置" + (String) msg.obj);
                    try{
                        mBTSocket.getOutputStream().write("Hello Arduino".getBytes());
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
                else
                    onAlarm("藍牙相關問題","連線失敗" + (String) msg.obj);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e){ }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run(){
            byte[] buffer = new byte[1024];
            int byteCount;
            while (true){
                try{
                    byteCount = mmInStream.available();
                    if(byteCount != 0){
                        SystemClock.sleep(100);
                        byteCount = mmInStream.available();
                        byteCount = mmInStream.read(buffer, 0, byteCount);
                        mHandler.obtainMessage(MESSAGE_READ, byteCount, -1, buffer)
                                .sendToTarget();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                    break;
                }
            }
        }
        public void write(String input){
            byte[] bytes = input.getBytes();
            try{
                mmOutStream.write(bytes);
            } catch (IOException e){ }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


}