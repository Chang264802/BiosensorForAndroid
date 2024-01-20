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

public class tempraturedata extends AppCompatActivity {

    public TextView new_tem,account1,highest_tem,lowest_tem,avgtem;
    private CalendarView calenderview_temperature;
    private static final String TAG="temperaturedata";
    public String date,account;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temprature);

        new_tem=(TextView) findViewById(R.id.new_tem);
        account1=(TextView) findViewById(R.id.account);
        highest_tem=(TextView)findViewById(R.id.highest_tem);
        lowest_tem=(TextView)findViewById(R.id.lowest_tem);
        avgtem=(TextView)findViewById(R.id.avgtem);
        intent=getIntent();
        account=intent.getStringExtra("account");//傳值
        new getData2().execute();
        new getTemHighData().execute();
        new getTemlowData().execute();
        new getAVGData().execute();
        new getLineData().execute();

        //settemLineChart();

        calenderview_temperature=(CalendarView)findViewById(R.id.calendarView_temperature);
        calenderview_temperature.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int i, int i1, int i2) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(i, i1, i2);
                Calendar currentDate = Calendar.getInstance();
                if (selectedDate.after(currentDate)) {
                    calendarView.setMaxDate(currentDate.getTimeInMillis());
                } else {
                date =i+"/"+(i1+1)+"/"+i2;
                new getTemHighData().execute();
                new getTemlowData().execute();
                new getAVGData().execute();
                    new getLineData().execute();
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

    //取得體溫數據
    class getData2 extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection2 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=體溫"));
                URL url = new URL("http://"+server+"/myHealth/getdatatype1"+cmd);
                urlConnection2 = (HttpURLConnection) url.openConnection();
                urlConnection2.setRequestMethod("GET");
                urlConnection2.setDoOutput(true);
                urlConnection2.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
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
                urlConnection2.disconnect();
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
                String b = jsonObject.getString("數值");
                String z = jsonObject.getString("流水號");
                new_tem.setText(b);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //取得最高體溫數據
    class getTemHighData extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=體溫&日期="+date));
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
                    highest_tem.setText("無");
                } else {
                    // 否則，獲取數據並設置文本
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String a = jsonObject.getString("帳號");
                    String t = jsonObject.getString("類別");
                    String c = jsonObject.getString("紀錄時間");
                    String e = jsonObject.getString("紀錄日期");
                    String p = jsonObject.getString("數值");
                    String z = jsonObject.getString("流水號");
                    highest_tem.setText(p);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //取得心率數據
    class getTemlowData extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=體溫&日期="+date));
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
                    lowest_tem.setText("無");
                } else {
                    // 否則，獲取數據並設置文本
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String a = jsonObject.getString("帳號");
                    String t = jsonObject.getString("類別");
                    String c = jsonObject.getString("紀錄時間");
                    String e = jsonObject.getString("紀錄日期");
                    String p = jsonObject.getString("數值");
                    String z = jsonObject.getString("流水號");
                    lowest_tem.setText(p);
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
                cmd=String.format(("?帳號="+account+"&類別=體溫&日期="+date)); // add date to the API call
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
                    avgtem.setText("無");
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
                        avgtem.setText(String.valueOf(averageStr)); // set the text to the average value
                    }
                } }catch (JSONException e) {
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
        LineChart lineChart = (LineChart) findViewById(R.id.temchart);
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd = String.format(("?帳號=" + account + "&類別=體溫&日期=" + date)); // add date to the API call
                URL url = new URL("http://" + server + "/myHealth/getdataweek" + cmd);
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
                JSONArray jsonArray = new JSONArray(result);
                ArrayList<Entry> systolics = new ArrayList<>();
                if (jsonArray.length() <= 0) {

                } else {
                    String[] xAxisValues = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String p = jsonObject.getString("數值");
                        String f = jsonObject.getString("紀錄日期");
                        String z = jsonObject.getString("紀錄時間");
                        systolics.add(new Entry(i, Float.parseFloat(p)));
                        xAxisValues[i] = f+"\n"+z;
                    }

                    LineDataSet dataSetS = new LineDataSet(systolics, "°C");
                    lineChart.setVisibleXRangeMaximum(7);
                    lineChart.setTouchEnabled(true);
                    lineChart.setDragEnabled(true);
                    lineChart.setScaleEnabled(true);
                    // 设置 X 轴的值为字符串
                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setLabelCount(systolics.size());
                    xAxis.setAxisMinimum(0);
                    xAxis.setValueFormatter(new MyXAxisValueFormatter(xAxisValues));
                    xAxis.setLabelRotationAngle(45);
                    dataSetS.setValueTextSize(20f);
                    xAxis.setLabelCount(systolics.size());
                    dataSetS.setDrawFilled(true);
                    dataSetS.setFillAlpha(70); // 設置填充區域的透明度，範圍為 0-255
                    dataSetS.setFillColor(Color.argb(50, 220, 225, 255)); // 設置填充區域的顏色
                    dataSetS.setColor(Color.RED);
                    dataSetS.setDrawFilled(true);
                    dataSetS.setValueTextSize(10f);

                    LineData data = new LineData(dataSetS);
                    lineChart.setData(data);

                    Description description = new Description();
                    description.setTextSize(15);
                    description.setText("平均體溫統計");
                    lineChart.setDescription(description);
                    lineChart.invalidate();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}