package powei.com.example.b20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class weather extends AppCompatActivity {

    final String SHARED_PREF_NAME = "CacheData"; //設定檔的名稱
    final String KEY_DISTRICT = "districtName"; //設定檔的鍵值

    Intent intent;
    String account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        intent=getIntent();
        account=intent.getStringExtra("account");

        String cacheName = //取得上次操作選擇的縣市名稱
                getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE).getString(KEY_DISTRICT, "");
        if (!cacheName.equals("")) //預先載入上載查詢的縣市天氣
            doWeatherInfoSet(cacheName);

        ListView lvDistrict = (ListView) findViewById(R.id.lvDistrict); //取得ListView
        lvDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() { //設定監聽器
            @Override //設定ListView項目被按下時的操作
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txvSelectedItem = (TextView) view; //取得被選到的ListView項目的文字標籤
                String selectedDistrict = txvSelectedItem.getText().toString(); //取出顯示名稱
                doWeatherInfoSet(selectedDistrict); //呼叫取得氣象資訊的方法
            }
        });
    }
    public void onBackPressed() {
        intent.setClass(this, HomeActivity.class);
        intent.putExtra("account",account);
        startActivity(intent);
        finish();
    }
    private void doWeatherInfoSet(String districtName){ //取得氣象資訊的方法
        TextView txvDistrictName = (TextView)findViewById(R.id.txvDistrictName);
        TextView txvWeatherInfo = (TextView)findViewById(R.id.txvWeather);

        txvDistrictName.setText(districtName); //設定縣市名稱

        weatherAsyncTask myAsyncTask = new weatherAsyncTask(txvWeatherInfo); //建立非同步物件，傳入顯示結果用的標籤
        myAsyncTask.execute(districtName); //執行非同步，傳入縣市名稱到doInBackground非同步方法中

        //將此次查詢的縣市名稱存到設定檔中，於下次開啟App時當作預設查詢內容(設定檔操作可參考基本練習22)
        SharedPreferences pref = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        pref.edit().putString(KEY_DISTRICT, districtName).commit();
    }
    public class weatherAsyncTask extends AsyncTask<String, Void, String> {

        private TextView txvResult; //用來接傳入的主畫面顯示氣象資訊文字標籤

        //建構子，將傳入的標籤設定到資料成員中
        public weatherAsyncTask(TextView txv) {
            txvResult = txv;
        }

        @Override
        protected void onPostExecute(String s) { //接受doInBackground的回傳結果，當作輸入參數s
            JSONObject jobjR, jobjL1, jobjL2, jobjL3; //根json物件、第一層json物件、第二層json物件、第三層json物件
            JSONArray jArrayL1, jArrayL2; //json陣列物件
            String elementName; //記錄查詢項目的名稱，例如:Wx, PoP, CI, MinT, MaxT等
            String result = ""; //用以記錄最後查詢內容的結果字串

            final int weatherNum = 3; //一般天氣預報-今明 36 小時天氣預報，預設會有三段時間，例如:(06~18, 18~06, 06~18)
            //以下為陣列類別的建立方式:1.先建立類別陣列 2.每個類別陣列元素需一一建立
            WeatherInfo36[] weatherInfo36s = new WeatherInfo36[weatherNum]; //WeatherInfo36為專門儲存此項預報的內容用類別
            for(int i = 0; i < weatherInfo36s.length; i++)
                weatherInfo36s[i] = new WeatherInfo36();

            try{
                jobjR = new JSONObject(s); //將回傳的json字串轉成json物件
                jobjR = new JSONObject(jobjR.getString("records")); //從json物件中取出名稱為records的內容，重存到json物件中
                //從json物件中取出location(位置)的內容，因位置可支援多個位置，因此回傳結果為json陣列，需使用JSONArray物件接收
                jobjL1 = new JSONArray(jobjR.getString("location")).getJSONObject(0); //目前一次只會查一個地方，取出0號元素
                jArrayL1 = new JSONArray(jobjL1.getString("weatherElement")); //從0號元素的jobjL1中取出weatherElement(查詢項目)陣列

                for(int k = 0; k < 5; k++){ //共有五項查詢內容:包含天氣現象(Wx)、舒適度(CI)、下雨機率(Pop)、最高溫(MaxT)、最低溫(MinT)
                    jobjL2 = jArrayL1.getJSONObject(k); //取出陣列元素
                    elementName = jobjL2.getString("elementName"); //取出查詢項目名稱elementName
                    jArrayL2 = new JSONArray(jobjL2.getString("time")); //取出time標籤內容，裡面共有三筆資料，因此使用JSON陣列物件接收
                    switch (elementName){ //依據查詢項目名稱個別處理
                        case "Wx": //Wx=>天氣現象
                            for(int i = 0; i < weatherInfo36s.length; i++){
                                jobjL3 = jArrayL2.getJSONObject(i); //一次處理一筆資料，取出json物件
                                weatherInfo36s[i].setStartTime(jobjL3.getString("startTime")); //讀取起始時間(startTime)內容
                                jobjL3 = new JSONObject(jobjL3.getString("parameter")); //使用json物件，取出parameter標籤內容
                                weatherInfo36s[i].setWeather(jobjL3.getString("parameterName")); //讀取parameterName標籤，取得天氣現象
                            }                                                                         //並設定到自訂物件內(透過setWeather自訂方法)
                            break;
                        case "CI": //CI=>舒適度
                            for(int i = 0; i < weatherInfo36s.length; i++){
                                jobjL3 = jArrayL2.getJSONObject(i); //一次處理一筆資料，取出json物件
                                jobjL3 = new JSONObject(jobjL3.getString("parameter")); //使用json物件，取出parameter標籤內容
                                weatherInfo36s[i].setComfort(jobjL3.getString("parameterName")); //讀取parameterName標籤，取得舒適度
                            }                                                                         //並設定到自訂物件內(透過setComfort自訂方法)
                            break;
                        case "PoP": //PoP=>下雨機率
                            for(int i = 0; i < weatherInfo36s.length; i++){
                                jobjL3 = jArrayL2.getJSONObject(i); //一次處理一筆資料，取出json物件
                                jobjL3 = new JSONObject(jobjL3.getString("parameter")); //使用json物件，取出parameter標籤內容
                                weatherInfo36s[i].setRainProb(Integer.parseInt(jobjL3.getString("parameterName"))); //讀取parameterName標籤，取得下雨機率
                            }                                 //Integer.parseInt用來將字串轉成整數                  並設定到自訂物件內(透過setRainProb自訂方法)
                            break;
                        case "MaxT": //MaxT=>最高溫度
                            for(int i = 0; i < weatherInfo36s.length; i++){
                                jobjL3 = jArrayL2.getJSONObject(i); //一次處理一筆資料，取出json物件
                                jobjL3 = new JSONObject(jobjL3.getString("parameter")); //使用json物件，取出parameter標籤內容
                                weatherInfo36s[i].setMaxTemp(Integer.parseInt(jobjL3.getString("parameterName"))); //讀取parameterName標籤，取得最高溫度
                            }                                 //Integer.parseInt用來將字串轉成整數                  並設定到自訂物件內(透過setMaxTemp自訂方法)
                            break;
                        case "MinT": //MinT=>最低溫度
                            for(int i = 0; i < weatherInfo36s.length; i++){
                                jobjL3 = jArrayL2.getJSONObject(i); //一次處理一筆資料，取出json物件
                                jobjL3 = new JSONObject(jobjL3.getString("parameter")); //使用json物件，取出parameter標籤內容
                                weatherInfo36s[i].setMinTemp(Integer.parseInt(jobjL3.getString("parameterName"))); //讀取parameterName標籤，取得最低溫度
                            }                                 //Integer.parseInt用來將字串轉成整數                  並設定到自訂物件內(透過setMinTemp自訂方法)
                            break;
                    }
                }
                for(int i = 0; i < weatherInfo36s.length; i++) //將三筆氣象的資訊串接起來
                    result += weatherInfo36s[i].getWeatherInfo();
            }
            catch (JSONException e){
                result = e.getMessage();
            }
            txvResult.setText(result); //回傳查詢的氣象結果
        }

        @Override
        protected String doInBackground(String... strings) { //表輸入為可變長度的字串陣列型別(名稱為strings)
            String districtName = strings[0]; //取出傳入的縣市名稱
            String urlString = //組成連線氣象局的查詢網址(一般天氣預報-今明 36 小時天氣預報)
                    "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=rdec-key-123-45678-011121314&format=JSON&locationName="
                            + districtName + "&elementName=";
            https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=rdec-key-123-45678-011121314
            try{ //建立URL物件，需使用try catch保護
                URL url = new URL(urlString); //使用網址，建立URL物件
                //因網址為https，所以需建立HttpsURLConnection物件
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection)url.openConnection(); //建立連線物件
                httpsURLConnection.setConnectTimeout(2000); //設定連線時限為2秒鐘(2000毫秒)
                httpsURLConnection.setReadTimeout(5000); //設定讀取時限為5秒鐘(5000毫秒)
                httpsURLConnection.connect(); //執行連線動作

                InputStream is = httpsURLConnection.getInputStream(); //由連線物件取得輸入串流(伺服器回傳內容)
                InputStreamReader isr = new InputStreamReader(is, "utf8"); //由輸入串流建立輸入串流讀取器(使用utf8編碼)
                BufferedReader reader = new BufferedReader(isr); //由輸入串流讀取器建立緩衝讀取器

                return reader.readLine(); //由緩衝讀取器，執行讀取一行的動作(因為內容不分行，相當於讀取全部內容)
            }
            catch (IOException e){
                return e.getMessage();
            }
        }//"https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=CWB-7C4B9AAF-A124-4D91-B741-0F6007DACBEC&format=JSON&locationName="
                            //+ districtName + "&elementName=";
    }
    public class WeatherInfo36 { //用來記錄三十六小時預測結果的類別

        String startTimeStr; //預測起始時間
        String weather; //天氣現象
        String comfort; //舒適度描述
        int rainProb; //下雨機率
        int minTemp; //最低溫
        int maxTemp; //最高溫

        //分別用以設定起始時間、天氣現象、舒適度、下雨機率、最低溫、最高溫的公有方法
        public void setStartTime(String sTimeStr){
            startTimeStr = sTimeStr;
        }
        public void setWeather(String weatherStr){
            weather = weatherStr;
        }
        public void setComfort(String comfortStr){
            comfort = comfortStr;
        }
        public void setRainProb(int rainProbInt){
            rainProb = rainProbInt;
        }
        public void setMinTemp(int minT){
            minTemp = minT;
        }
        public void setMaxTemp(int maxT) {
            maxTemp = maxT;
        }

        public String getWeatherInfo(){ //取得顯示的氣象資訊
            //回傳的時間格式為 : 2021-10-11 00:00:00，字元從0起算
            String dateStr = startTimeStr.substring(8,10); //從8號字開始取，取到小於10號為止(也就是取到9號)=>取得日期
            String hrStr = startTimeStr.substring(11,13); //從11號字開始取，取到小於13號為止(也就是取到12號)=>取得小時
            String result = dateStr + "日"; //加入日期資訊
            switch (hrStr){ //由小時判斷是凌晨、白天還是晚上
                case "00" :
                    result += "凌晨";
                    break;
                case "06" :
                case "12" :
                    result += "白天";
                    break;
                case "18" :
                    result += "晚上";
                    break;
            }
            //串入天氣現象、舒適度、最低溫、最高溫及降雨機率(\u2103會顯示特殊字元°C)
            result += String.format("\n%s, %s\n%d~%d\u2103 降雨機率%d％\n\n"
                    , weather, comfort, minTemp, maxTemp, rainProb);

            return  result; //回傳氣象資訊
        }

    }
}