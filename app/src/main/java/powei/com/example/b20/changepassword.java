package powei.com.example.b20;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class changepassword extends AppCompatActivity {

    Intent intent;
    String account;
    EditText etxtoldpassword,etxtnewpassword,etxtchecknewpassword;
    Button btnchangepassword;
    String account11;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);
        intent=getIntent();
        account=intent.getStringExtra("account");
        etxtviews();
        btnchangepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etxtoldpassword.getText().toString().equals("")){
                    Toast.makeText(changepassword.this, "請輸入舊密碼", Toast.LENGTH_SHORT).show();
                }
                else if(etxtnewpassword.getText().toString().equals("")){
                    Toast.makeText(changepassword.this, "請輸入新密碼", Toast.LENGTH_SHORT).show();
                }
                else if(etxtchecknewpassword.getText().toString().equals("")){
                    Toast.makeText(changepassword.this, "請輸入確認密碼", Toast.LENGTH_SHORT).show();
                }
                else if(!etxtnewpassword.getText().toString().equals(etxtchecknewpassword.getText().toString())){
                    Toast.makeText(changepassword.this, "兩次密碼不同，請再試一次。", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    ocp();
                }

            }
        });
    }
    public void etxtviews(){
        etxtoldpassword=findViewById(R.id.etxtoldpassword);
        etxtnewpassword=findViewById(R.id.etxtnewpassword);
        etxtchecknewpassword=findViewById(R.id.etxtchecknewpassword);
        btnchangepassword=findViewById(R.id.btnchangepassword);

    }
    public void ocp(){
        new checkpassword().execute();
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("取消更新密碼並返回上一頁");
        builder.setNegativeButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 处理删除操作
                finish();
                intent.setClass(changepassword.this, showuserdata.class);
                intent.putExtra("account",account);
                startActivity(intent);
            }
        });
        builder.setPositiveButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    class changenewpassword extends AsyncTask<String, String, String> {
        String bb;
        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL("http://himhealth.mcu.edu.tw/myHealth/updatechangepassword");
                JSONObject jObj = new JSONObject();
                jObj.put("帳號", account);
                jObj.put("密碼", etxtnewpassword.getText());

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

                    Toast.makeText(changepassword.this, "更新失敗，請重新嘗試。", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(changepassword.this, "更新密碼成功！", Toast.LENGTH_SHORT).show();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private class checkpassword extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {

            HttpURLConnection urlConnection = null;
            try {
                // Connect server
                String login, url1;
                login = String.format("?帳號=" + account + "&電子信箱=" + account + "&密碼=" + etxtoldpassword.getText());
                url1 = String.format("http://himhealth.mcu.edu.tw/myHealth/getLogin" + login);
                URL url = new URL(url1);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = urlConnection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf8");
                    BufferedReader reader = new BufferedReader(isr);
                    String response = reader.readLine();
                    JSONObject jsonObject = new JSONObject(response);
                    account11 = jsonObject.getString("帳號");
                    String password11=jsonObject.getString("密碼");
                    if(account11.equals("null")) {
                        return "!ok";
                    }
                    else if(password11.equals(etxtnewpassword.getText().toString())){
                        return "!!ok";
                    }
                    else {
                        return "ok";
                    }

                }
            } catch (IOException | JSONException ex) {
                return "false";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return "false";
        }
        protected void onPostExecute(String result) {
            if (result=="ok") {
                // 登入成功
                new changenewpassword().execute();
                intent.setClass(changepassword.this, showuserdata.class);
                intent.putExtra("account",account);
                startActivity(intent);
                finish();

            }
            else if(result=="!!ok"){
                Toast.makeText(changepassword.this, "新密碼與舊密碼相同", Toast.LENGTH_SHORT).show();
            }
            else {
                // 登入失敗
                Toast.makeText(changepassword.this, "舊密碼輸入錯誤", Toast.LENGTH_SHORT).show();
            }
        }
    }

}