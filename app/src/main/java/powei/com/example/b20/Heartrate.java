package powei.com.example.b20;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Heartrate extends AppCompatActivity {

    public TextView new_heart_rate,highest_heart_rate,lowest_heart_rate,average_heart_rate,account1;
    private CalendarView calenderview_heart_rate;
    private static final String TAG="Hearterate";
    public String date,account;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartrate);

        //啟動連接API
        new_heart_rate = (TextView) findViewById(R.id.new_heart_rate);
        highest_heart_rate = (TextView) findViewById(R.id.highest_heart_rate);
        lowest_heart_rate = (TextView) findViewById(R.id.lowest_heart_rate);
        average_heart_rate = (TextView) findViewById(R.id.average_heart_rate);
        account1 = (TextView)findViewById(R.id.account);
        intent=getIntent();
        account=intent.getStringExtra("account");//傳值
        new getData3().execute();
        new getHighData().execute();
        new getlowData().execute();
        new getAVGData().execute();
        new getLineData().execute(date);


        calenderview_heart_rate=(CalendarView)findViewById(R.id.calendarView_heartrate);
        calenderview_heart_rate.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int i, int i1, int i2) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(i, i1, i2);
                Calendar currentDate = Calendar.getInstance();
                if (selectedDate.after(currentDate)) {
                    calendarView.setMaxDate(currentDate.getTimeInMillis());
                } else {
                date =i+"/"+(i1+1)+"/"+i2;
                    new getHighData().execute();
                    new getlowData().execute();
                    new getAVGData().execute();
                   new getLineData().execute(date);
            }}
        });
    }

    public void onBackPressed() {
        intent.setClass(this, MainActivity0.class);
        intent.putExtra("account",account);
        startActivity(intent);
        finish();
    }
    public void btnReturnClick(View v){
        switch (v.getId()){
            case R.id.btnreturn:
                onBackPressed();
                break;
        }
    }

//取得心率數據
    class getData3 extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=心率"));
                URL url = new URL("http://"+server+"/myHealth/getdatatype1"+cmd);
                urlConnection3 = (HttpURLConnection) url.openConnection();
                urlConnection3.setRequestMethod("GET");
                urlConnection3.setDoOutput(true);
                urlConnection3.connect();
                BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
                char[] buffer = new char[1024];
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line+"\n");
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
                JSONArray jsonArray = new JSONArray(result);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String a = jsonObject.getString("帳號");
                String t = jsonObject.getString("類別");
                String c = jsonObject.getString("紀錄時間");
                String e = jsonObject.getString("紀錄日期");
                String p = jsonObject.getString("數值");
                String z = jsonObject.getString("流水號");
                new_heart_rate.setText(p);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //取得最高心率數據
    class getHighData extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=心率&日期="+date));
                URL url = new URL("http://"+server+"/myHealth/getdatatypehigh"+cmd);
                urlConnection3 = (HttpURLConnection) url.openConnection();
                urlConnection3.setRequestMethod("GET");
                urlConnection3.setDoOutput(true);
                urlConnection3.connect();
                BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
                char[] buffer = new char[1024];
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line+"\n");
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
                JSONArray jsonArray = new JSONArray(result);
                if (jsonArray.length() == 0) {
                    // 如果數組為空，表示沒有數據，將文本設置為 "無"
                    highest_heart_rate.setText("無");
                } else {
                    // 否則，獲取數據並設置文本
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String a = jsonObject.getString("帳號");
                    String t = jsonObject.getString("類別");
                    String c = jsonObject.getString("紀錄時間");
                    String e = jsonObject.getString("紀錄日期");
                    String p = jsonObject.getString("數值");
                    String z = jsonObject.getString("流水號");
                    highest_heart_rate.setText(p);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //取得最低心率數據
    class getlowData extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=心率&日期="+date));
                URL url = new URL("http://"+server+"/myHealth/getdatatypelow"+cmd);
                urlConnection3 = (HttpURLConnection) url.openConnection();
                urlConnection3.setRequestMethod("GET");
                urlConnection3.setDoOutput(true);
                urlConnection3.connect();
                BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
                char[] buffer = new char[1024];
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line+"\n");
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
                JSONArray jsonArray = new JSONArray(result);
                if (jsonArray.length() == 0) {
                    // 如果數組為空，表示沒有數據，將文本設置為 "無"
                    lowest_heart_rate.setText("無");
                } else {
                    // 否則，獲取數據並設置文本
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String a = jsonObject.getString("帳號");
                    String t = jsonObject.getString("類別");
                    String c = jsonObject.getString("紀錄時間");
                    String e = jsonObject.getString("紀錄日期");
                    String p = jsonObject.getString("數值");
                    String z = jsonObject.getString("流水號");
                    lowest_heart_rate.setText(p);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class getAVGData extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=心率&日期="+date)); // add date to the API call
                URL url = new URL("http://"+server+"/myHealth/getdatatypehigh"+cmd);
                urlConnection3 = (HttpURLConnection) url.openConnection();
                urlConnection3.setRequestMethod("GET");
                urlConnection3.setDoOutput(true);
                urlConnection3.connect();
                BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
                char[] buffer = new char[1024];
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line+"\n");
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
                JSONArray jsonArray = new JSONArray(result);
                double total = 0;
                int count = 0;
                if (jsonArray.length() == 0) {
                    // 如果數組為空，表示沒有數據，將文本設置為 "無"
                    average_heart_rate.setText("無");
                } else {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String a = jsonObject.getString("帳號");
                        String t = jsonObject.getString("類別");
                        String c = jsonObject.getString("紀錄時間");
                        String e = jsonObject.getString("紀錄日期");
                        String p = jsonObject.getString("數值");
                        String z = jsonObject.getString("流水號");
                        total += Double.parseDouble(p); // add value to total
                        count++; // increment count
                    }
                    if (count > 0) {
                        double average = total / count;
                        DecimalFormat df = new DecimalFormat("0.00"); // 设置保留两位小数
                        String averageStr = df.format(average);
                        average_heart_rate.setText(String.valueOf(averageStr)); // set the text to the average value
                    }
                }} catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public class MyXAxisValueFormatter implements IAxisValueFormatter {

        private final String[] mValues;

        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int index = (int) value;
            if (index >= 0 && index < mValues.length) {
                return mValues[index];
            }
            return "";
        }
    }
    class getLineData extends AsyncTask<String, String, String> {
        LineChart lineChart = (LineChart) findViewById(R.id.heartratechart);
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=心率&日期="+args[0])); // add date to the API call
                URL url = new URL("http://"+server+"/myHealth/getdatatypehigh"+cmd);
                urlConnection3 = (HttpURLConnection) url.openConnection();
                urlConnection3.setRequestMethod("GET");
                urlConnection3.setDoOutput(true);
                urlConnection3.connect();
                BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
                char[] buffer = new char[1024];
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line+"\n");
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
                JSONArray jsonArray = new JSONArray(result);
                ArrayList<Entry> systolics = new ArrayList<>();
                if(jsonArray.length()<=0) {
                }
                else {
                    String[] xAxisValues = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String p = jsonObject.getString("數值");
                        String f = jsonObject.getString("紀錄日期");
                        String z = jsonObject.getString("紀錄時間");
                        systolics.add(new Entry(i, Float.parseFloat(p)));
                        xAxisValues[i] = f+"\n"+z;
                    }

                    //Legend legend = lineChart.getLegend();
                    LineDataSet dataSetS = new LineDataSet(systolics, "下/分");
                    lineChart.setVisibleXRangeMaximum(7);
                    lineChart.setDragXEnabled(true);
                    lineChart.setScaleEnabled(true);
                    lineChart.setTouchEnabled(true);
                    // 设置 X 轴的值为字符串
                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setLabelCount(systolics.size()+1);
                    //xAxis.setAvoidFirstLastClipping(true);
                    xAxis.setValueFormatter(new MyXAxisValueFormatter(xAxisValues));
                    xAxis.setLabelRotationAngle(45);
//                    dataSetS.setLabel();
                    dataSetS.setDrawFilled(true);
                    dataSetS.setFillAlpha(70); // 設置填充區域的透明度，範圍為 0-255
                    dataSetS.setFillColor(Color.argb(50,220, 225, 255)); // 設置填充區域的顏色
                    dataSetS.setColor(Color.RED);
                    dataSetS.setDrawFilled(true);
                    dataSetS.setValueTextSize(10f);

                    LineData data = new LineData(dataSetS);
                    lineChart.setData(data);
                    Description description = new Description();
                    description.setText("平均心率統計");
                    lineChart.setDescription(description);
                    lineChart.invalidate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }}

    }

    }