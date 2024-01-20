package powei.com.example.b20;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private final static String LOG_TAG = "AlarmReceiverLog";
    private int heartRate, SpO2;
    private float temperature;
    private String alarmType, message;

    @Override
    public void onReceive(Context context, Intent intent) {
        alarmType = intent.getStringExtra("alarmType"); //取得透過Intent傳送過來的msg內容，可用以判斷處理方式
        if(alarmType.equals("心率過低") || alarmType.equals("心率過高")) //若msg內容為"到期通知"，則進行通知
            heartRate = Integer.parseInt(intent.getStringExtra("alarmValue"));
        else if(alarmType.equals("血氧數值異常"))
            SpO2 = Integer.parseInt(intent.getStringExtra("alarmValue"));
        else if(alarmType.equals("體溫過低") || alarmType.equals("體溫過高"))
            temperature = Float.parseFloat(intent.getStringExtra("alarmValue"));
        else if(alarmType.equals("藍牙相關問題"))
            message = intent.getStringExtra("alarmValue");

        triggerNotification(context);
    }

    private void triggerNotification(Context context){
        String channelId = "my_channel_id"; //自訂頻道識別碼，API 26以上，通知需使用頻道處理
        int notificationId = 1; //通知識別碼

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE); //建構通知管理器
        if(notificationManager == null){ //若通知管理器無法建構，則結束執行
            Log.d(LOG_TAG, "通知管理器建立失敗!");
            return;
        }
        //Android 8.0(API 26)以上，需建立通知頻道的重要性設定
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_HIGH; //設定此通知為高重要性
            NotificationChannel notificationChannel = new NotificationChannel //使用頻道識別碼建立通知頻道物件，並加入重要性設定
                    (channelId,"NOTIFICATION_CHANNEL_NAME",importance);
            assert notificationManager != null; //若管理器不為空，則進行後續處理
            notificationManager.createNotificationChannel(notificationChannel); //使用通知頻道物件建立通知頻道
        }

        Intent notifyIntent = new Intent(context,MainActivity.class); //建立通知用的Intent，只到MainActivity
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //使用通知服務，Intent需為新工作(New Task)
        PendingIntent appIntent = PendingIntent.getActivity(context,0,notifyIntent,0); //建立隱藏Intent
        Notification notification;

        String contentText = "";
        if(alarmType.equals("心率過低") || alarmType.equals("心率過高"))
            contentText = alarmType + "，目前數值為" + heartRate + "下/分!";
        else if(alarmType.equals("血氧數值異常"))
            contentText = alarmType + "，目前數值為" + SpO2 + "%!";
        else if(alarmType.equals("體溫過低") || alarmType.equals("體溫過高"))
            contentText = alarmType + "，目前數值為" + temperature + "\u2103!";
        else if(alarmType.equals("藍牙相關問題"))
            contentText = message;

            //通過通知建立器，建立通知物件
        notification = new NotificationCompat.Builder(context,channelId) //透過頁面上下文、頻道識別碼，建構通知建立器
                .setSmallIcon(android.R.drawable.ic_dialog_info) //設定狀態列的小圖示
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.alert)) //設定狀態列的大圖示
                .setTicker("掌握健康") //設置狀態列顯示的訊息
                .setWhen(System.currentTimeMillis()) //設置顯示通知發生時間(不是設定預定通知時間)
                .setContentTitle("掌握健康") //設置狀態列的標題
                .setContentText(contentText) //設置狀態列的通知文字
                .setContentIntent(appIntent) //設置通知內容Intent，當點選狀態列時可觸發隱藏Intent
                .setOngoing(true) //設定無法手動清除，需點選狀態列方可清除
                .setAutoCancel(true) //設定點選完後，自動清除
                .setDefaults(Notification.DEFAULT_ALL) //使用通知所有預設值，例如:聲音、震動、閃燈等
                .build(); //建置建立器物件，以產生通知物件

        notificationManager.cancelAll(); //清除先前的所有通知
        notificationManager.notify(notificationId,notification); //使用通知管理器(帶入通知識別碼及通知物件)進行通知發送
    }
}