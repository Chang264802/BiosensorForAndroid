package powei.com.example.b20;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class updateuserdata extends AppCompatActivity {

    Intent intent;
    String account;
    public TextView txtaccount,txtemail,txtcreatedate;
    public TextView etxtphone,etxtname,etxtbirth,etxtheigh,etxtweigh;
    public Button btnupdateuserdata,btncheckbirth;
    DatePicker datePicker;
    int year=2000,month=1,day=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateuserdata);
        intent=getIntent();
        account=intent.getStringExtra("account");
        textveiws();
        new getuserdata().execute();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        etxtbirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View dpbtn = findViewById(R.id.dpbtn);
                datePicker.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams layoutParams = dpbtn.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                dpbtn.setLayoutParams(layoutParams);
                imm.hideSoftInputFromWindow(etxtbirth.getWindowToken(), 0);
                opdp();
            }

        });



        btncheckbirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dpbtn = findViewById(R.id.dpbtn);
                datePicker.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams layoutParams = dpbtn.getLayoutParams();
                layoutParams.height = 0;
                dpbtn.setLayoutParams(layoutParams);
                datePicker.setVisibility(View.GONE);
            }
        });
        btnupdateuserdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etxtphone.getText().toString().equals("")){
                    Toast.makeText(updateuserdata.this, "請輸入電話", Toast.LENGTH_SHORT).show();
                }
                else if(etxtname.getText().toString().equals("")){
                    Toast.makeText(updateuserdata.this, "請輸入名字/暱稱", Toast.LENGTH_SHORT).show();
                }
                else if(etxtbirth.getText().toString().equals("")){
                    Toast.makeText(updateuserdata.this, "請輸入生日", Toast.LENGTH_SHORT).show();
                }
                else if(etxtheigh.getText().toString().equals("")){
                    Toast.makeText(updateuserdata.this, "請輸入身高", Toast.LENGTH_SHORT).show();
                }
                else if(etxtweigh.getText().toString().equals("")){
                    Toast.makeText(updateuserdata.this, "請輸入體重", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    new updateuserdata1().execute();
                    intent.setClass(updateuserdata.this, showuserdata.class);
                    intent.putExtra("account",account);
                    startActivity(intent);
                    finish();
                }

            }
        });

    }
    public void opdp(){
        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 在這裡處理用戶選擇的生日
                String date = year + "/" + (monthOfYear + 1) + "/" + dayOfMonth;
                etxtbirth.setText(date);
            }
        });
    }
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("取消修改並返回上一頁");
        builder.setNegativeButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 处理删除操作
                finish();
                intent.setClass(updateuserdata.this, showuserdata.class);
                intent.putExtra("account",account);
                startActivity(intent);
            }
        });
        builder.setPositiveButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void textveiws() {
        txtaccount = findViewById(R.id.txtaccount);
        txtemail = findViewById(R.id.txtemail);
        etxtphone = findViewById(R.id.etxtphone);
        etxtname = findViewById(R.id.etxtname);
        etxtbirth = findViewById(R.id.etxtbirth);
        etxtheigh = findViewById(R.id.etxtheigh);
        etxtweigh = findViewById(R.id.etxtweigh);
        txtcreatedate = findViewById(R.id.txtcreatedate);
        btnupdateuserdata = findViewById(R.id.btnupdateuserdata);
        datePicker = findViewById(R.id.datepicker);
        btncheckbirth = findViewById(R.id.btncheckbirth);



    }
    class updateuserdata1 extends AsyncTask<String, String, String> {
        String bb;
        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/updateuserdata");
                JSONObject jObj = new JSONObject();
                jObj.put("帳號", account);
                jObj.put("電子信箱", txtemail.getText());
                jObj.put("姓名", etxtname.getText());
                jObj.put("生日", etxtbirth.getText());
                jObj.put("電話", etxtphone.getText());
                jObj.put("身高", etxtheigh.getText());
                jObj.put("體重", etxtweigh.getText());

                doPostFunction(jObj, url);
                return "OK";
            } catch (JSONException | MalformedURLException ex) {

                return bb="!ok";
            }
        }

        private String doPostFunction(JSONObject jObj, URL url) {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=utf8");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);//設定需要輸出資料
                httpURLConnection.connect();//連結http

                OutputStream output = httpURLConnection.getOutputStream();//取得串流

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "utf8"));
                String jsonStr = jObj.toString();
                writer.write(jsonStr);
                writer.flush();//寫入更新
                writer.close();//關閉寫入器
                output.close();//關閉串流

                httpURLConnection.getResponseMessage();
                httpURLConnection.disconnect();
                return "OK";

            } catch (IOException ex) {
                return bb="!ok";

            }

        }
        @Override
        protected void onPostExecute(String bb) {
            try {
                if(bb.equals("!ok"))
                {

                    Toast.makeText(updateuserdata.this, "更新失敗，請重新嘗試。", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(updateuserdata.this, "更新成功！", Toast.LENGTH_SHORT).show();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                if(n.equals("未填寫"))
                {

                }
                else{
                    String[] match = n.split("/");
                    year = Integer.parseInt(match[0]); // 将年份存储在year中
                    month = Integer.parseInt(match[1]); // 将月份存储在month中
                    day = Integer.parseInt(match[2]);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}