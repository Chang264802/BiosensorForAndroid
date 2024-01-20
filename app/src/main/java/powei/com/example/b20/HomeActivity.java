package powei.com.example.b20;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeActivity extends AppCompatActivity {
    TextView name,mail,user;
    CardView logOut;
    String account;
    Intent intent;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    CardView healthdata,watch;

    public class getuser extends AsyncTask<String, Void, String> {

        HttpURLConnection urlConnection3;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd = String.format("帳號=" + account + "&電子信箱=");
                URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/getcheckuser?" + cmd);
                urlConnection3 = (HttpURLConnection) url.openConnection();
                urlConnection3.setRequestMethod("GET");
                urlConnection3.setDoOutput(true);
                urlConnection3.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                char[] buffer = new char[1024];
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line + "\n");
                }
                br.close();
                String jsonString = result.toString();
                System.out.println("JSON:" + jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection3.disconnect();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String a = jsonObject.getString("帳號");
                String t = jsonObject.getString("電子信箱");
                String b = jsonObject.getString("密碼");
                String q = jsonObject.getString("電話");
                String x = jsonObject.getString("姓名");
                String n = jsonObject.getString("生日");
                String z = jsonObject.getString("身高");
                String zz = jsonObject.getString("體重");
                String zzz = jsonObject.getString("帳號創建日期");
                String zzzz = jsonObject.getString("頭像");
                name.setText(x);
                mail.setText(x);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onBackPressed() {
        logout();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        name=findViewById(R.id.name);
        mail=findViewById(R.id.mail);
        logOut=findViewById(R.id.logOut);

        intent=getIntent();
        account=intent.getStringExtra("account");

        new getuser().execute();

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();

            }
        });

    }


    public void OnClick(View v){

        switch (v.getId()){
            case R.id.watch:
                intent.setClass(HomeActivity.this, MainActivity.class);
                intent.putExtra("account",account);
                break;
            case R.id.watch2:
                intent.setClass(HomeActivity.this, MainActivity0.class);
                intent.putExtra("account",account);
                break;
            case R.id.healthdata:
                intent.setClass(HomeActivity.this, activitylist.class);
                intent.putExtra("account",account);
                break;
            case R.id.ProfileSetting:
                intent.setClass(HomeActivity.this, showuserdata.class);
                intent.putExtra("account",account);
                break;
            case R.id.imbtnweather:
                intent.setClass(HomeActivity.this, weather.class);
                intent.putExtra("account",account);
                break;
        }
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }

    private void logout() {
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("是否登出");
            builder.setNegativeButton("登出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 处理删除操作
                    Intent intent = new Intent(HomeActivity.this, enter.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setPositiveButton("取消", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

}