package powei.com.example.b20;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class showuserdata extends AppCompatActivity {

    Intent intent;
    String account;
    public TextView txtaccount,txtemail,etxtphone,etxtname,etxtbirth,etxtheigh,etxtweigh,txtcreatedate;
    public Button btnupdateuserdata,btnupdatepassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showuserdata);
        intent=getIntent();
        textveiws();
        account=intent.getStringExtra("account");

        new getuserdata().execute();
        btnupdateuserdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(showuserdata.this, updateuserdata.class);
                intent.putExtra("account",account);
                startActivity(intent);
                finish();
            }
        });
        btnupdatepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(showuserdata.this, changepassword.class);
                intent.putExtra("account",account);
                startActivity(intent);
                finish();
            }
        });

    }
    public void textveiws(){
        txtaccount=findViewById(R.id.txtaccount);
        txtemail=findViewById(R.id.txtemail);
        etxtphone=findViewById(R.id.etxtphone);
        etxtname=findViewById(R.id.etxtname);
        etxtbirth=findViewById(R.id.etxtbirth);
        etxtheigh=findViewById(R.id.etxtheigh);
        etxtweigh=findViewById(R.id.etxtweigh);
        txtcreatedate=findViewById(R.id.txtcreatedate);
        btnupdateuserdata=findViewById(R.id.btnupdateuserdata);
        btnupdatepassword=findViewById(R.id.btnupdatepassword);

    }
    public void onBackPressed() {
        intent.setClass(this, HomeActivity.class);
        intent.putExtra("account",account);
        startActivity(intent);
        finish();
    }
    public class getuserdata extends AsyncTask<String, Void, String> {

        HttpURLConnection urlConnection3;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd = String.format("帳號=" + account + "&電子信箱=");
                URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/getUser?" + cmd);
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
                String q = jsonObject.getString("電話");
                String x = jsonObject.getString("姓名");
                String n = jsonObject.getString("生日");
                String z = jsonObject.getString("身高");
                String zz = jsonObject.getString("體重");
                String zzz = jsonObject.getString("帳號創建日期");
                txtaccount.setText(a);
                txtemail.setText(t);
                etxtphone.setText(q);
                etxtname.setText(x);
                etxtbirth.setText(n);
                etxtheigh.setText(z);
                etxtweigh.setText(zz);
                txtcreatedate.setText(zzz);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}