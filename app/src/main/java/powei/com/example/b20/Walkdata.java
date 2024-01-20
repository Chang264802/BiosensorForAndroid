package powei.com.example.b20;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class Walkdata extends AppCompatActivity {

    public TextView account1,new_walk,agvwalk;
    private CalendarView calenderview_walk;
    private static final String TAG="Walkdata";
    public String date,account;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk);
        account1 = (TextView)findViewById(R.id.account);
        new_walk = (TextView) findViewById(R.id.new_walk);
        agvwalk= (TextView) findViewById(R.id.agvstep);
        intent=getIntent();
        account=intent.getStringExtra("account");//傳值
        new getData().execute();
        new getAVGData().execute();
        new getBarData().execute();

        //walkBarChart();

        calenderview_walk=(CalendarView)findViewById(R.id.calendarview_walk);
        calenderview_walk.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int i, int i1, int i2) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(i, i1, i2);
                Calendar currentDate = Calendar.getInstance();
                if (selectedDate.after(currentDate)) {
                    calendarView.setMaxDate(currentDate.getTimeInMillis());
                } else {
                    date = i + "/" + (i1 + 1) + "/" + i2;
                    new getData().execute();
                    new getAVGData().execute();
                    new getBarData().execute();
                }
            }
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

    //取得步數數據
    class getData extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=步數"));
                URL url = new URL("http://"+server+"/myHealth/getdatatype1"+cmd);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();
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
                urlConnection.disconnect();
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
                String v = jsonObject.getString("數值");
                String z = jsonObject.getString("流水號");
                new_walk.setText(v);

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
                cmd=String.format(("?帳號="+account+"&類別=步數&日期="+date)); // add date to the API call
                URL url = new URL("http://"+server+"/myHealth/getdataweek"+cmd);
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
                    agvwalk.setText(String.valueOf(average)); // set the text to the average value
                }
            } catch (JSONException e) {
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
    class getBarData extends AsyncTask<String, String, String> {
        BarChart barChart = (BarChart) findViewById(R.id.walkchart);
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=步數&日期="+date)); // add date to the API call
                URL url = new URL("http://"+server+"/myHealth/getdataweek"+cmd);
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
                ArrayList<BarEntry> systolics = new ArrayList<>();
                if(jsonArray.length()<=0) {
                }
                else {
                    String[] xAxisValues = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String p = jsonObject.getString("數值");
                        String f = jsonObject.getString("紀錄日期");
                        String z = jsonObject.getString("紀錄時間");
                        systolics.add(new BarEntry(i, Float.parseFloat(p)));
                        xAxisValues[i] = f+"\n"+z;
                    }
                    BarData dataSetS = new BarData();
                    dataSetS.addDataSet(new BarDataSet(systolics, "步"));

                    // 设置 X 轴的值为字符串
                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setValueFormatter(new MyXAxisValueFormatter(xAxisValues));
                    xAxis.setLabelRotationAngle(45);
                    xAxis.setLabelCount(systolics.size());
                    barChart.getAxisRight().setInverted(true);
                    barChart.setTouchEnabled(true);
                    barChart.setDragEnabled(true);
                    barChart.setScaleEnabled(true);
                    dataSetS.setValueTextSize(10f);
                    dataSetS.setBarWidth(0.5f);
                    barChart.setVisibleXRangeMinimum(1);
                    barChart.setVisibleXRangeMaximum(systolics.size());


                    barChart.setData(dataSetS);

                    Description description = new Description();
                    description.setText("平均步數統計");
                    barChart.setDescription(description);
                    barChart.invalidate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }}

    }

}