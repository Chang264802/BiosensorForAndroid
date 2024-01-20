package powei.com.example.b20;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static android.os.Build.VERSION_CODES.M;





public class MainActivity extends AppCompatActivity {


    private static final int msgKey1=1;
    private  Handler mHandler1;
    private Handler mHandler;

    private final int REQUEST_ENABLE_BT=1;
    private static final int MESSAGE_READ=2;
    private static final int CONNECTING_STATUS=3;
    private final int BT_NOT_SUPPORT=4;
    private final int BT_ENABLED=5;
    private final int BT_NOTENABLE=6;
    private final int PERMISSION_REQUEST=7;
    private final int PAIRED_DEVICE=8;
    private final int DISCOVERED_DEVICE=9;
    private final int CONNECTING_DEVICE =10;

    private final UUID BT_MODULE_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private TextView txvBTStatus,time_now1;
    //private TextView txvReadData;
    private ImageButton btnBTOn;
    private ImageButton btnBTOff;
    private Button btnListBTPaired;
    private Button btnBTDiscover;
    private Button btnIntent1;
    private ListView lstBTDevices;
    private CheckBox ckbLED;

    Intent intent;
    private BluetoothAdapter mBTAdapter;
    private ConnectedThread mConnectedThread;
    private int deviceStatus;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ArrayList<String> newBTLists;

    private BluetoothSocket mBTSocket=null;
    private Boolean Arduino = null;
    private Boolean BTAvg = false;
    private Boolean BTAvgInvalid = false;
    private Boolean BTtime = null;
    private static String currentTime;
    public String account;
    MainActivity mAct;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findNeedViews();

        intent=getIntent();
        account=intent.getStringExtra("account");//傳值
        mHandler1 = new MainActivity.MsgHanlder(this);
        Thread thread = new Thread(runnable);
        thread.start();

        mBTAdapter=BluetoothAdapter.getDefaultAdapter();
        if(mBTAdapter==null){
            setBtnEnabledByCode(BT_NOT_SUPPORT);
            txvBTStatus.setText("設備不支援藍芽");
        }else {
            setBtnEnabled();
            setBtnListeners();
            setMsgHandler();
        }
        btnIntent1 = (Button) findViewById(R.id.btnIntent1);
        btnIntent1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMain();
            }
        });
    }
    private void openMain(){
        intent=new Intent(this,MainActivity0.class);
        intent.putExtra("account",account);
        startActivity(intent);
    }
    //返回鍵
    public void onBackPressed() {
        if(TextUtils.isEmpty(account))
        {
            intent.setClass(this, enter.class);
        }
        else {
            intent.setClass(this, HomeActivity.class);
            intent.putExtra("account",account);
        }

        startActivity(intent);
        finish();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            do{
                try {
                    Thread.sleep(1000);
                    Message msg=new Message();
                    msg.what=msgKey1;
                    mHandler1.sendMessage(msg);
                }catch (InterruptedException e){
                    Log.e("HandleTest",e.getMessage());
                }
            }while (true);
        }
    };

    static class MsgHanlder extends Handler{
        private WeakReference<MainActivity> mAcitvity;

        public MsgHanlder(MainActivity activity){
            mAcitvity = new WeakReference<MainActivity>(activity);
        }

        public void handleMessage(Message msg){
            MainActivity activity = mAcitvity.get();
            if(activity != null){
                switch (msg.what){
                    case msgKey1:
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd\tHH:mm:ss");
                        currentTime = dateFormat.format(calendar.getTime());
                        activity.time_now1.setText(currentTime);
                        byte[] time = currentTime.getBytes();
                        break;
                }
            }
        }
    }

    private void findNeedViews(){
        time_now1=(TextView)findViewById(R.id.time_now1);
        txvBTStatus=(TextView)findViewById((R.id.txvBTStatus));
        btnBTOn=(ImageButton)findViewById(R.id.btnBTOn);
        btnBTOff=(ImageButton)findViewById(R.id.btnBTOff);
        btnBTDiscover=(Button)findViewById(R.id.btnBTDiscover);
        btnListBTPaired=(Button)findViewById(R.id.btnListBTPaired);
        lstBTDevices=(ListView)findViewById(R.id.lstPairedDevices);

        //設定藍芽裝置清單:藍芽配士氣、陣列配適器和清單控制項
        mBTArrayAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lstBTDevices.setAdapter(mBTArrayAdapter);
        lstBTDevices.setOnItemClickListener(mDeviceClickListener);

        newBTLists=new ArrayList<String>();
    }

    private void setBtnEnabled(){
        if(mBTAdapter.isEnabled())
            setBtnEnabledByCode(BT_ENABLED);
        else
            setBtnEnabledByCode(BT_NOTENABLE);
    }

    private void setBtnEnabledByCode(int optCode){
        //先將按鈕都禁用
        btnBTOn.setEnabled(false);
        btnBTOff.setEnabled(false);
        btnListBTPaired.setEnabled(false);
        btnBTDiscover.setEnabled(false);

        switch (optCode){
            case BT_NOT_SUPPORT:
                break;
            case BT_ENABLED:
                btnBTOff.setEnabled(true);
                btnListBTPaired.setEnabled(true);
                btnBTDiscover.setEnabled(true);
                break;
            case BT_NOTENABLE:
                btnBTOn.setEnabled(true);
                break;
        }

    }

    private void setBtnListeners(){
        btnBTOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn(); //執行開啟藍牙的自定方法
                txvBTStatus.setText("開啟藍牙");
            }
        });
        btnBTOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOff(); //執行關閉藍牙的自訂方法

                txvBTStatus.setText("關閉藍牙");
            }
        });
        btnListBTPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPairDevices();
                deviceStatus = PAIRED_DEVICE; //此時列出的裝置為已配對裝置，紀錄於裝置狀態的變數中，以便後續辨別
                txvBTStatus.setText("顯示已配對裝置");
            }
        });
        btnBTDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discover();

                deviceStatus = DISCOVERED_DEVICE; //此時列出的裝置違背掃描到的新裝置，紀錄於裝置狀態變數中，以便後續辨別
                txvBTStatus.setText("掃描新藍牙裝置");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) { //若受到需求是起用藍牙的需求
            if (resultCode == RESULT_OK) { //依據使用者授權結果顯示於畫面上
                txvBTStatus.setText("藍牙已開啟");
                Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
            } else
                txvBTStatus.setText("藍牙仍未啟用");
            setBtnEnabled();
        }
    }

    @SuppressLint("MissingPermission")
    private void bluetoothOn() {
        if (!mBTAdapter.isEnabled()) { //若藍牙配適器沒有啟用則使用Intent要求開啟藍牙設定
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Toast.makeText(getApplicationContext(), "藍牙已開啟", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void bluetoothOff() {
        mBTAdapter.disable(); //進用藍牙配適器
        txvBTStatus.setText("藍牙已禁用");
        setBtnEnabled(); //重新依據藍牙啟用狀況設定個按鈕的啟用狀況
        Toast.makeText(this, "藍牙禁用中", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void listPairDevices() {
        mPairedDevices = mBTAdapter.getBondedDevices(); //由藍牙配適器取得已配對的裝置列表
        if (mBTAdapter.isEnabled()) { //先確認藍牙已啟用
            mBTArrayAdapter.clear(); //先將清單的陣列配適器清空
            for (BluetoothDevice device : mPairedDevices) //將以配對的裝置清單一筆一筆放入清單陣列配適器中
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress()); //取出名稱與Mac地址

            Toast.makeText(this, "顯示已配對裝置", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "藍牙未開啟", Toast.LENGTH_SHORT).show();
    }//列出已配對藍芽裝置

    private void discover() {
        if (Build.VERSION.SDK_INT >= M && !checkPermission()) //若手機版本超過6.0版，而且權限檢核還沒通過，則直接跳出
            return;

        if (mBTAdapter.isDiscovering()) { //若目前已正在掃描，代表使用者想要停止掃描
            mBTAdapter.cancelDiscovery(); //停止掃描
            Toast.makeText(this, "停止掃描", Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) { //先確認連牙已開啟
                mBTArrayAdapter.clear();
                mBTAdapter.startDiscovery(); //藍牙配適器開始掃描
                newBTLists.clear(); //新蘭牙裝置串列清空
                Toast.makeText(getApplicationContext(), "開始掃描", Toast.LENGTH_SHORT).show();
                //註冊廣播接受器，裡面包含兩個參數，第一個是廣播接受器物件，第二個是用來辨別廣播接受器的識別用意圖過濾器
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else
                Toast.makeText(getApplicationContext(), "藍牙未開啟", Toast.LENGTH_SHORT).show();
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); //取得觸發此廣播接受器的內建覆載函數，當掃描到一個新裝置的時候，即會觸發
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); //取得新藍牙裝置
            String address = device.getAddress(); //取得藍牙裝置的Mac地址
            if (!newBTLists.contains(address)) { //為避免相同裝置重複加入，先檢查此位置是否已存在新裝置串列中，若不存在
                newBTLists.add(address); //將裝置加入新裝置串列
                mBTArrayAdapter.add(device.getName() + "\n" + address); //將裝置名稱與位址加入清單陣列配適器中
                mBTArrayAdapter.notifyDataSetChanged(); //通知清單，告知陣列配適器已異動
            }
        }
    };

    private Boolean checkPermission() {
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION; //Android6.0之後，掃描藍牙裝置須檢核定定位權限
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) { //若沒有通過使用者權限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) { //若要逕行授權說明
                showMessageOKCancel("藍牙掃描需開啟定位權限，請進行授權!", //彈跳授權說明視，參數二為點犬確認鈕的監聽器設定
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { //當點選確認按鈕時的操作
                                doRequestPermission(); //對系統進行權限要求
                            }
                        });
            } else //若不需進行授權說明，則直接對系統進行權限要求操作
                doRequestPermission();
            return false; //回船上未完成授權檢核
        }
        return true;
    }

    private void doRequestPermission() { //要求權限授權，參數:1.操作頁面 2.要求權限陣列 3.需求代碼(在onRequestPermissionResult用以判斷授權結果)
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST) { //確認授權識別碼是否正確
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) //確認授權是否成功
                Toast.makeText(this, "定位功能授權成功!", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "定位功能未授權，將無法進行藍芽裝置掃描", Toast.LENGTH_LONG).show();
        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okLister) { //參數1.彈跳訊息 2.確認監聽器
        new AlertDialog.Builder(MainActivity.this) //建構通知對話建置器
                .setMessage(message)
                .setPositiveButton("OK", okLister) //設定訊息
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() { //設定取消鈕的內容:1.按鈕文字 2.監聽器(即時新建)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "定位為授權，無法執行藍芽裝置掃描功能", Toast.LENGTH_LONG).show();
                    }
                })
                .create() //建立對話建置器
                .show(); //顯示對話建置器
    }
    private void setMsgHandler() {
        //建立處理訊息用的Handler執行緒:1.讀取距離資訊 2.判斷藍芽連結結果
        mHandler = new MsgHandler(this);
    }
    public String HRMessage = null; //讀取訊息用的字串
    public String SpO2Message = null;
    public String TemcMessage = null;
    public String StepMessage = null;
    public String TimeMessage = null;
    //靜態的Handler類別+若參考型別可避免使用Handler時造成記憶體洩漏問題
    class MsgHandler extends Handler {



        private WeakReference<MainActivity>mActivity; //定義MainActivity的弱參考類別全域變數

        public MsgHandler(MainActivity activity ) { //建構子:傳入MainActivity的參考
            mActivity = new WeakReference<MainActivity>(activity); //將MainActivity轉成弱型別參考
        }
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get(); //取得轉成弱型別參考的本頁面
            if(msg.what == MESSAGE_READ) { //弱訊息是用來讀取的
                byte[] bytes = (byte[]) msg.obj; //透過obj屬性，取出傳送過來的位元組陣列
                if(bytes.length >= 9 && bytes[0] == 1) //第一層確認HR
                { //弱傳送過來的長度超過3，且第一位元組的值為1(此為識別用的資料)
                    HRMessage = String.valueOf(bytes[1]); //將距離資料還原
                    MainActivity0.heartdata_now.setText(HRMessage+"下/分");
                    if(bytes[2] == 2)//第一層確認SPO2
                    {
                        SpO2Message = String.valueOf((bytes[3]));
                        MainActivity0.blooddata_now.setText(SpO2Message + "%");

                    }
                    if(bytes[4] == 3)//確認Temc
                    {
                        TemcMessage = String.valueOf((bytes[5])) + "." + String.valueOf((bytes[6]));
                        MainActivity0.temdata_now.setText(TemcMessage+ "°C");
                    }
                    if(bytes[7] == 4)//確認Step
                    {
                        StepMessage = String.valueOf((bytes[8]));
                        MainActivity0.walkdata_now.setText(StepMessage + "步");
                    }
                }
                else if(bytes.length >= 10 && bytes[0] == 11) //DataAvg send to API => db 平均數
                {
                    if(BTAvgInvalid == false)
                    {
                        HRMessage = String.valueOf(bytes[1]); //將距離資料還原
                        if(bytes[2] == 12)//第一層確認HR
                        {
                            SpO2Message = String.valueOf((bytes[3]));
                        }
                        if(bytes[4] == 13)//確認Temc
                        {
                            TemcMessage = String.valueOf((bytes[5])) + "." + String.valueOf((bytes[6]));
                        }
                        if(bytes[7] == 14)//確認Step
                        {
                            StepMessage = String.valueOf((bytes[8]));
                        }
                        if(bytes[8] == 15)
                        {
                            TimeMessage = String.valueOf((bytes[9])); // send to API
                        }
                        new insertdata().execute();
                        BTAvgInvalid = true;
                        BTAvg = null;
                    }
                }
                else if(bytes.length >= 1 && bytes[0] == 0)
                {
                    Arduino = true;
                }
                else if(bytes.length >= 1 && bytes[0] == 100)
                {
                    BTAvg = true;
                    BTAvgInvalid = false;
                }
                else if(bytes.length >= 1 && bytes[0] == 101)
                {
                    BTtime = true;
                }
                if(Arduino == null)
                {
                    try{
                        mBTSocket.getOutputStream().write("Hello Arduino".getBytes());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                if(BTAvg == null)
                {
                    try {
                        mBTSocket.getOutputStream().write("BTAvgOK".getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(msg.what == CONNECTING_STATUS) { //若訊息是用來傳送藍芽裝置連結狀態
                if(msg.arg1 == 1) //透過arg1屬性，判斷是否有連結成功
                {
                    activity.txvBTStatus.setText("連結到裝置:" + (String) (msg.obj));
                    try{
                        mBTSocket.getOutputStream().write("Hello Arduino".getBytes());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                else
                    activity.txvBTStatus.setText("連線失敗");
            }

        }
    }

    class insertdata extends AsyncTask<String, String, String>{


        protected String doInBackground(String... args) {
            insertHR();
            insertSpO2();
            Temp();
            checkstep();
            return "Ok";
        }
        String tc;
        private String insertHR(){
            try {
                URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/insertdata");
                JSONObject jobj = new JSONObject();
                jobj.put("帳號", account);
                jobj.put("類別", "心率");
                jobj.put("數值", HRMessage);
                return doPostFunction(jobj,url);
            } catch (Exception ex) {
                String error = ex.getMessage();
                return "!OK";
            }
        }
        private String insertSpO2(){
            try {
                URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/insertdata");
                JSONObject jobj = new JSONObject();
                jobj.put("帳號", account);
                jobj.put("類別", "血氧");
                jobj.put("數值", SpO2Message);
                return doPostFunction(jobj,url);
            } catch (Exception ex) {
                String error = ex.getMessage();
                return "!OK";
            }
        }
        private String Temp(){
            try {
                URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/insertdata");
                JSONObject jobj = new JSONObject();
                jobj.put("帳號", account);
                jobj.put("類別", "體溫");
                jobj.put("數值", TemcMessage);
                return doPostFunction(jobj,url);
            } catch (Exception ex) {
                String error = ex.getMessage();
                return "!OK";
            }
        }
        private String step(String tc){
            //get步數日期，比對今天日期，有>update，沒有>insert
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d");
            String checkTime = dateFormat.format(new Date());
            if (checkTime.equals(tc)){
                try {
                    URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/updatestepdata");
                    JSONObject jobj = new JSONObject();
                    jobj.put("帳號", account);
                    jobj.put("類別", "步數");
                    jobj.put("數值", StepMessage);
                    return doPostFunction(jobj,url);
                } catch (Exception ex) {
                    String error = ex.getMessage();
                    return "!OK";
                }
            }
            else {
                try {
                URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/insertdata");
                JSONObject jobj = new JSONObject();
                jobj.put("帳號", account);
                jobj.put("類別", "步數");
                jobj.put("數值", StepMessage);
                return doPostFunction(jobj,url);
            } catch (Exception ex) {
                String error = ex.getMessage();
                return "!OK";
            }}

        }
        private String checkstep()
        {

            try{
                StringBuilder result = new StringBuilder();
                URL url1 = new URL("http://himhealth.mcu.edu.tw/myHealth/getdatatype1?帳號="+account+"&類別=步數");
                HttpURLConnection urlConnection3 = (HttpURLConnection) url1.openConnection();
                urlConnection3.setRequestMethod("GET");
                urlConnection3.setDoOutput(true);
                urlConnection3.connect();
                BufferedReader br=new BufferedReader(new InputStreamReader(url1.openStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line+"\n");
                }
                br.close();
                JSONArray jsonArray = new JSONArray(result.toString());
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String a = jsonObject.getString("帳號");
                String t = jsonObject.getString("類別");
                String c = jsonObject.getString("紀錄時間");
                tc = jsonObject.getString("紀錄日期");
                String p = jsonObject.getString("數值");
                String z = jsonObject.getString("流水號");
                urlConnection3.disconnect();
                return step(tc);
            }
            catch (Exception ex){
                return "!OK";
            }
        }
        private String doPostFunction(JSONObject jobj,URL url){
            try {

                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestProperty("Content-Type","application/json;charset=utf8");
                httpURLConnection.setRequestProperty("Accept","application/json");
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);//設定需要輸出資料
                httpURLConnection.connect();//連結http

                OutputStream output=httpURLConnection.getOutputStream();//取得串流

                BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(output,"utf8"));
                String jsonStr=jobj.toString();
                writer.write(jsonStr);
                writer.flush();//寫入更新
                writer.close();//關閉寫入器
                output.close();//關閉串流

                httpURLConnection.getResponseMessage();
                httpURLConnection.disconnect();
                return "ok";
            }
            catch (Exception ex) {
                String error = ex.getMessage();
                return "!OK";
            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3) {
            if (!mBTAdapter.isEnabled()) { //若藍牙未開啟，則提示後停止執行
                Toast.makeText(getBaseContext(), "藍牙未啟用", Toast.LENGTH_SHORT).show();
                setBtnEnabledByCode(BT_NOTENABLE); //設定個按鈕的啟用狀況
                return;
            }
            //取出清單項目的文字，並切割出Mac位址和裝置名稱
            String info = ((TextView) view).getText().toString(); //取出被點選的清單項目文字
            final String address = info.substring(info.length() - 17); //參數為起始索引(含)，會取出從起始索引到最後所有字串，因此會取出最後17個字
            final String name = info.substring(0, info.length() - 17); //參數圍起始和結束索引，因此會取出扣除最後17個字的所有字元
            final BluetoothDevice device = mBTAdapter.getRemoteDevice(address); //由Mac位址取得該對應的藍芽裝置

            if (deviceStatus == DISCOVERED_DEVICE) { //若清單是紀錄新掃描的裝置，則準備進行配對動作
                Boolean isBond = false; //宣告變數紀錄是否可配對
                try {
                    isBond = createBond(device); //進行配對，回傳結果為:執行配對動作是否有發生立即性的錯誤，若沒有，則會進行配對動作
                    if (!isBond)
                        txvBTStatus.setText("配對錯誤");
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "配對錯誤:" + e.getMessage(), Toast.LENGTH_LONG).show(); //若發生執行錯誤，則顯示錯誤訊息
                    return;
                }
                return; //需等候配對操作完成，因此結束後續的連線動作
            }
            txvBTStatus.setText("連線中");
            //建立一個新的執行續進行藍芽裝置連線操作，以避免主畫面卡住無法操作
            new Thread() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    boolean fail = false; //宣告是否發生錯誤的變數

                    try { //建立藍牙插座，需使用try catch進行例外保護
                        mBTSocket = creatBluetoothSocket(device); //建立連線用藍芽裝置插座
                    } catch (IOException e) {
                        fail = true; //當例外發生時，紀錄已發生錯誤
                        Toast.makeText(getBaseContext(), "藍牙裝置插座建立失敗", Toast.LENGTH_SHORT).show();
                    }
                    try { //建立藍牙連線，需使用try catch進行例外保護
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            Toast.makeText(getBaseContext(), "藍牙連線失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();
                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();

                        deviceStatus = CONNECTING_DEVICE; //紀錄此時裝置為連線中裝置
                    }
                }
            }.start();
        }
    };

    private Boolean createBond(BluetoothDevice btDevice) throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice"); //取得藍牙裝置類別
        Method createBondMethod = class1.getMethod("createBond"); //取得類別中的createBond方法
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice); //使用invoke觸發方法，並取得執行成功與否的結果
        return  returnValue.booleanValue(); //回傳執行結果
    }


    @SuppressLint("MissingPermission")
    private BluetoothSocket creatBluetoothSocket(BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID); //使用特定UUID與藍芽裝置建立安全連線
    }


    private class ConnectedThread extends Thread { //繼承Thread的自訂已連結執行緒
        private final BluetoothSocket mmSocket; //藍牙插座全域常數(僅可設定一次)
        private final InputStream mmInStream; //輸入串流全域常數(僅可設定一次)
        private final OutputStream mmOutStream; //輸出串流全域常數(僅可設定一次)

        public ConnectedThread(BluetoothSocket socket) { //帶入藍牙插座物件的建構子
            mmSocket = socket; //串入的藍牙插座設定給資料成員
            InputStream tmpIn = null; //暫存用的輸入串流(因為mmInStream為final常數僅能設定一次，先暫存取的後設定)
            OutputStream tmpOut = null; //

            try {
                tmpIn = socket.getInputStream(); //
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        @Override
        public void run() { //執行緒執行功能，主要用來讀取資料用
            byte[] buffer = new byte[1024]; //可放1024個byte的緩衝區
            int byteCount; //紀錄讀取資料的位元組數
            while (true) { //重複讀取一直到發生例外錯誤才跳出
                try { // 從輸入串流讀取資料可能發生輸出入例外錯誤，需以try catch保護
                    byteCount = mmInStream.available(); //availabel()方法可取得目前輸入串流中有多少個byte可讀取
                    if(byteCount != 0 ) {
                        SystemClock.sleep(100);
                        byteCount = mmInStream.available();
                        byteCount = mmInStream.read(buffer,0, byteCount);
                        mHandler.obtainMessage(MESSAGE_READ, byteCount,-1, buffer) //取得資料後，透過自訂Handler傳送到主畫面，參數:1.識別碼 2.位元數 3.沒用 4.讀取內容
                                .sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        public void write(String input){ //執行緒寫入功能，主要用來提供主畫面傳入資料，以寫出到藍芽裝置，參數:要傳給藍芽裝置的輸入字串
            byte[] bytes = input.getBytes(); //將字串轉成位元組(傳輸資料時，一般使用位元組傳輸)
            try {
                mmOutStream.write(bytes); //透過輸出串流寫入資料
            }catch (IOException e){
            }
        }
        public void cancel() { //提供主畫面用來關閉插作用
            try {
                mmSocket.close();
            }catch (IOException e){

            }
        }
        private boolean removeBond(BluetoothDevice btDevice) throws Exception{
            Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
            Method removeBondMethod = btClass.getMethod("removeBond");
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
            return returnValue.booleanValue();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(deviceStatus == CONNECTING_DEVICE)
            return;

        txvBTStatus.setText("");
        mBTArrayAdapter.clear();
        mBTArrayAdapter.notifyDataSetChanged();
        if(mBTSocket != null){
            try {
                mBTSocket.close();
            }
            catch (IOException e){

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mBTSocket != null) {
            try {
                mBTSocket.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}