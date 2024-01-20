package powei.com.example.b20;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class activitychangedata extends AppCompatActivity implements View.OnClickListener {
    public String server = "himhealth.mcu.edu.tw",account;
    public TextView txvInfo;
    public TextView account1,txvtypewr;

    Intent intent;

    EditText data,data2;
    Button btnInsert, btnspo2select, btnhrselect,btntempselect,btnbpselect;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changedata);

        intent=getIntent();
        account=intent.getStringExtra("account");//傳值

        getNeedViews();
        data.setEnabled(false);
        data2.setEnabled(false);
        data.setVisibility(View.GONE);
        data2.setVisibility(View.GONE);
    }

    public void onbtnclick(View view) {
        switch (view.getId()){
            case R.id.btnspo2select:
                txvtypewr.setText("血氧");
                data.setHint("%");
                data.setEnabled(true);
                data2.setEnabled(false);
                data.setVisibility(View.VISIBLE);
                data2.setVisibility(View.GONE);
                break;
            case R.id.btnhrselect:
                txvtypewr.setText("心率");
                data.setHint("每分鐘/下");
                data.setEnabled(true);
                data2.setEnabled(false);
                data.setVisibility(View.VISIBLE);
                data2.setVisibility(View.GONE);
                break;
            case R.id.btntempselect:
                txvtypewr.setText("體溫");
                data.setHint("度數℃");
                data.setEnabled(true);
                data2.setEnabled(false);
                data.setVisibility(View.VISIBLE);
                data2.setVisibility(View.GONE);
                break;
            case R.id.btnbpselect:
                txvtypewr.setText("收縮/舒張壓");
                data.setHint("收縮壓");
                data2.setHint("舒張壓");
                data.setEnabled(true);
                data2.setEnabled(true);
                data2.setVisibility(View.VISIBLE);
                break;
            case R.id.btnInsert:
                new updatedatalist().execute();
                break;
        }

    }
    class updatedatalist extends AsyncTask<String,String,String> {

        String cmd;
        @Override
        protected String doInBackground(String... strings) {
            try {

                JSONObject jObj=new JSONObject();
                if(!data.getText().toString().equals("")){
                    if(txvtypewr.getText().equals("收縮/舒張壓")&&!data2.getText().toString().equals(""))
                    {
                        cmd=String.format("insertbpdata");
                        URL url=new URL("http://"+server+"/myHealth/"+cmd);
                        jObj.put("帳號",account);
                        jObj.put("類別","收縮/舒張壓");
                        jObj.put("數值",data.getText());
                        jObj.put("數值2",data2.getText());
                        return doPostFunction(jObj,url);
                    }
                    else if(txvtypewr.getText()!="收縮/舒張壓")
                    {
                        cmd=String.format("insertdata");
                        URL url=new URL("http://"+server+"/myHealth/"+cmd);
                        jObj.put("帳號",account);
                        jObj.put("類別",txvtypewr.getText().toString());
                        jObj.put("數值",data.getText());
                        return doPostFunction(jObj,url);
                    }
                    else {
                        txvInfo.setText("新增失敗");
                        return "!ok";
                    }
                }
                else {
                    txvInfo.setText("新增失敗");
                    return "!ok";
                }


            } catch (JSONException | MalformedURLException ex) {
                txvInfo.setText(ex.getMessage());
                return "!OK";
            }
        }
        private String doPostFunction(JSONObject jObj, URL url){
            try {
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestProperty("Content-Type","application/json;charset=utf8");
                httpURLConnection.setRequestProperty("Accept","application/json");
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);//設定需要輸出資料
                httpURLConnection.connect();//連結http

                OutputStream output=httpURLConnection.getOutputStream();//取得串流

                BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(output,"utf8"));
                String jsonStr=jObj.toString();
                writer.write(jsonStr);
                writer.flush();//寫入更新
                writer.close();//關閉寫入器
                output.close();//關閉串流

                httpURLConnection.getResponseMessage();
                httpURLConnection.disconnect();
                txvInfo.setText("新增成功");
                onBackPressed();
                return "OK";

            }catch (IOException ex) {
                txvInfo.setText("新增失敗");
                return  "!OK";
            }
        }

    }
    public void onBackPressed() {
        intent.setClass(this, activitylist.class);
        intent.putExtra("account",account);
        startActivity(intent);
        finish();
    }
    private void getNeedViews(){
        data=(EditText)findViewById(R.id.etxtdata1wr);
        data2=(EditText)findViewById(R.id.etxtdata2wr);
        btnInsert=(Button) findViewById(R.id.btnInsert);
        btnspo2select=(Button) findViewById(R.id.btnspo2select);
        btnbpselect=(Button) findViewById(R.id.btnbpselect);
        btnhrselect=(Button) findViewById(R.id.btnhrselect);
        btntempselect=(Button) findViewById(R.id.btntempselect);
        txvtypewr=(TextView)findViewById(R.id.txvtypewr);
        txvInfo=(TextView) findViewById(R.id.txvInfo);
    }
    @Override
    public void onClick(View view) {

    }




}