package powei.com.example.b20;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class activitylist extends ListActivity implements View.OnClickListener{


    public String server ="himhealth.mcu.edu.tw";
    public Contact[] healths;
    public TextView txvInfo;
    Intent intent;

    FloatingActionButton fab;

    public TextView account1;
    public String account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        intent=getIntent();
        account=intent.getStringExtra("account");//傳值

        account1=(TextView) findViewById(R.id.txvaccount);
        txvInfo=(TextView) findViewById(R.id.txvInfo);


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(activitylist.this, activitychangedata.class);
                intent.putExtra("account",account);
                startActivity(intent);
                finish();
            }
        });


        new getdatalist().execute();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }



    public void onBackPressed() {
        intent.setClass(this, HomeActivity.class);
        intent.putExtra("account",account);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {

    }

    class getdatalist extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url=new URL("http://"+server+"/myHealth/getdatatype2?帳號="+account);
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setConnectTimeout(2000);
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.connect();

                InputStream is=httpURLConnection.getInputStream();
                InputStreamReader isr=new InputStreamReader(is,"utf8");
                BufferedReader reader=new BufferedReader(isr);
                String result=reader.readLine();
                reader.close();

                return result;
            }catch (IOException ex){
                txvInfo.setText(ex.getMessage());
                return "!OK";
            }
            //查詢所有聯絡人
        }
        protected  void  onPostExecute(String s) {
            if(s.equals("!OK"))
                return;
            doSetListAdapter(s);
        }
        private void doSetListAdapter(String jsonStr) {
            List<Contact> list=new ArrayList<Contact>();
            Contact contact;

            try{
                JSONArray jAry=new JSONArray(jsonStr);
                JSONObject jObj;
                for(int i=0;i<jAry.length();i++){
                    contact=new Contact();
                    jObj=jAry.getJSONObject(i);
                    contact.date=jObj.getString("紀錄日期");
                    contact.types= jObj.getString("類別");
                    contact.data=jObj.getString("數值");
                    contact.data2=jObj.getString("數值2");
                    if(!contact.data2.equals("0.0"))
                    {
                        contact.data=String.format(contact.data+"/"+ contact.data2);
                    }
                    contact.time=jObj.getString("紀錄時間");


                    list.add(contact);
                }

            } catch (Exception e) {
                txvInfo.setText(e.getMessage());
                return;
            }
            if(list.size()==0)
                return;
            healths=new Contact[list.size()];
            healths=list.toArray(healths);
            MyAdapter adapter=new MyAdapter(activitylist.this,healths);
            setListAdapter(adapter);
        }
    }
}