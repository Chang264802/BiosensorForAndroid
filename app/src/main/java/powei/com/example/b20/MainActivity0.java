package powei.com.example.b20;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity0 extends AppCompatActivity{

    private static final int msgKey=1;
    private MsgHanlder mHandler;

    public static TextView walkdata_now;
    public TextView time_now;
    public static TextView heartdata_now;
    public static TextView blooddata_now;
    public static TextView temdata_now,account1;
    public TextView airdata_now;
    public TextView txvInfo;
    public String account;
    //MyAsyncTask myAsyncTask;
    Intent intent;
    private ImageButton openblueteeth,imbtnheart,imblood,imair;
    private AlertDialog setToast;
    //Dialog的元件
    Dialog dialog;
    View viewdialog;
    Button ok;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 1001;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main0);



        walkdata_now = (TextView) findViewById(R.id.walkdata_now);
        heartdata_now = (TextView) findViewById(R.id.heartdata_now);
        blooddata_now = (TextView) findViewById(R.id.blooddata_now);
        temdata_now = (TextView) findViewById(R.id.temdata_now);
        account1 = (TextView)findViewById(R.id.account);
        intent=getIntent();
        account=intent.getStringExtra("account");

        if(TextUtils.isEmpty(account))
        {
            CardView btnblood=findViewById(R.id.btnblood);
            btnblood.setEnabled(false);
            CardView btnheart=findViewById(R.id.btnheart);
            btnheart.setEnabled(false);
            CardView btntem=findViewById(R.id.btntem);
            btntem.setEnabled(false);
            CardView btnwalk=findViewById(R.id.btnwalk);
            btnwalk.setEnabled(false);

        }
        time_now=(TextView)findViewById(R.id.time_now);

        mHandler = new MsgHanlder(this);
        Thread thread = new Thread(runnable);
        thread.start();


        imbtnheart = (ImageButton) findViewById(R.id.imbtnheart);
        imbtnheart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity0.this);
                alertDialog.setTitle("什麼是心率?");
                alertDialog.setMessage("心率是指心臟收縮跳動的頻率和每分鐘跳動的次數（bpm）" + "\n" + "正常人平靜時心率每分鐘60到100次");
                alertDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        });

        imblood = (ImageButton) findViewById(R.id.imblood);
        imblood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity0.this);
                alertDialog.setTitle("什麼是血氧?");
                alertDialog.setMessage("血氧指的是帶氧氣的血紅素在血液中的濃度" + "\n" + "正常血氧指數 95%－100%，低於90％應就醫");
                alertDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        });

    }
    //返回鍵
    public void onBackPressed() {
        if(TextUtils.isEmpty(account))
        {
            intent.setClass(this, enter.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
                    msg.what=msgKey;
                    mHandler.sendMessage(msg);
                }catch (InterruptedException e){
                    Log.e("HandleTest",e.getMessage());
                }
            }while (true);
        }
    };

    static class MsgHanlder extends Handler{
        private WeakReference<MainActivity0> mAcitvity0;

        public MsgHanlder(MainActivity0 activity0){
            mAcitvity0 = new WeakReference<MainActivity0>(activity0);
        }

        public void handleMessage(Message msg){
            MainActivity0 activity0 = mAcitvity0.get();
            if(activity0 != null){
                switch (msg.what){
                    case msgKey:
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd\nHH:mm:ss");
                        activity0.time_now.setText(dateFormat.format(calendar.getTime()));
                        break;
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 如果權限被授予，進行相應操作
                // ...
            } else {
                // 如果權限未被授予，則提示用戶
                Toast.makeText(this, "權限未被授予", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void btnOnClick(View v){
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.openblueteeth:
                intent.setClass(MainActivity0.this, MainActivity.class);
                intent=new Intent(this,MainActivity.class);
                intent.putExtra("account",account);
                break;
            case R.id.btnwalk:
                intent.setClass(MainActivity0.this,Walkdata.class);
                intent=new Intent(this,Walkdata.class);
                intent.putExtra("account",account);
                break;
            case R.id.btnheart:
                intent.setClass(MainActivity0.this, Heartrate.class);
                intent=new Intent(this,Heartrate.class);
                intent.putExtra("account",account);
                break;
            case R.id.btnblood:
                intent.setClass(MainActivity0.this,Blood.class);
                intent=new Intent(this,Blood.class);
                intent.putExtra("account",account);
                break;
            case R.id.btntem:
                intent.setClass(MainActivity0.this, tempraturedata.class);
                intent=new Intent(this,tempraturedata.class);
                intent.putExtra("account",account);
                break;
            case R.id.btnshare:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 沒有權限，需要請求
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE_PERMISSION_CODE);
                } else {
                    // 已經有權限
                    // 執行需要權限的操作
                    View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                    rootView.setDrawingCacheEnabled(true);
                    Bitmap screenshot = Bitmap.createBitmap(rootView.getDrawingCache());
                    rootView.setDrawingCacheEnabled(false);
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/jpeg");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    screenshot.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), screenshot, "Title", null);
                    Uri imageUri = Uri.parse(path);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    startActivity(Intent.createChooser(shareIntent, "Share image using"));
                }

                break;

        }
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }
}


