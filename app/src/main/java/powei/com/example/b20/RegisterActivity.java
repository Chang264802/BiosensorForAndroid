package powei.com.example.b20;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {
    private EditText mEmail, mAccount, mPassword, mcheckpassword;
    private Button mSignUpBtn;
    public String ckname, ckemail;


    class RegisterTask extends AsyncTask<String, String, String> {
        String bb;
        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/insertuser");
                JSONObject jObj = new JSONObject();
                jObj.put("帳號", mAccount.getText());
                jObj.put("電子信箱", mEmail.getText());
                jObj.put("密碼", mPassword.getText());

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
                Toast.makeText(RegisterActivity.this, "註冊失敗，請重新嘗試。", Toast.LENGTH_SHORT).show();
                return bb="!ok";

            }

        }
        @Override
        protected void onPostExecute(String bb) {
            try {
                if(bb.equals("!ok"))
                {

                    Toast.makeText(RegisterActivity.this, "註冊失敗，請重新嘗試。", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(RegisterActivity.this, "註冊成功！", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), enter.class));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class checkaccount extends AsyncTask<String, String, String>{
        HttpURLConnection urlConnection3;
        Activity activity = null;
        private Context context;
        String aa;
        @Override
        protected String doInBackground(String... agvs) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format("?帳號="+mAccount.getText()+"&電子信箱="+mEmail.getText());
                URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/getcheckuser"+cmd);
                urlConnection3 = (HttpURLConnection) url.openConnection();
                urlConnection3.setRequestMethod("GET");
                urlConnection3.setDoOutput(true);
                urlConnection3.connect();
                BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                line = br.readLine();
                result.append(line);
                br.close();
                doStr(result.toString());


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection3.disconnect();
            }
            return aa;
        }
        protected String doStr(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String t = jsonObject.getString("電子信箱");
                ckemail = String.format(t);

                if(ckemail.equals("null")) {
                    new RegisterTask().execute();
                    return aa="OK";
                }
                else{
                    return aa="!ok";
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return "!ok";
            }
        }
        @Override
        protected void onPostExecute(String aa) {
            try {
                if(aa.equals("!ok"))
                {
                    Toast.makeText(RegisterActivity.this, "帳號或信箱重複。", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("提示");
        builder.setMessage("是否要回到登入畫面");
        builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 处理删除操作
                startActivity(new Intent(getApplicationContext(), enter.class));
                finish();
            }
        });
        builder.setPositiveButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail =(EditText) findViewById(R.id.signupmail);
        mAccount =(EditText) findViewById(R.id.signupaccount);
        mPassword =(EditText) findViewById(R.id.signuppassword);
        mcheckpassword =(EditText) findViewById(R.id.signupcheckpassword);
        mSignUpBtn =(Button) findViewById(R.id.signupbtn);


        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEmail.getText().toString().equals("")){
                    Toast.makeText(RegisterActivity.this, "請輸入信箱", Toast.LENGTH_SHORT).show();
                }
                else if(mAccount.getText().toString().equals("")){
                    Toast.makeText(RegisterActivity.this, "請輸入帳號", Toast.LENGTH_SHORT).show();
                }
                else if(mPassword.getText().toString().equals("")){
                    Toast.makeText(RegisterActivity.this, "請輸入密碼", Toast.LENGTH_SHORT).show();
                }
                else if(mcheckpassword.getText().toString().equals("")){
                    Toast.makeText(RegisterActivity.this, "請輸入確認密碼", Toast.LENGTH_SHORT).show();
                }
                else if(!mPassword.getText().toString().equals(mcheckpassword.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "兩次密碼不同，請再試一次。", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    new checkaccount().execute();
                }

            }
        });
    }
}