package powei.com.example.b20;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.CalendarView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Blood extends AppCompatActivity {
    TextView detailsText,detailsText2,new_blood,account1,highest_blood,lowest_blood,avgblood;
    RelativeLayout layout,layout2;

    Intent intent;
    private CalendarView calenderview_blood;
    private static final String TAG="Blood";
    public String date,account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood);

        new_blood=(TextView)findViewById(R.id.new_blood);
        highest_blood=(TextView)findViewById(R.id.highest_blood);
        lowest_blood=(TextView)findViewById(R.id.lowest_blood);
        avgblood=(TextView)findViewById(R.id.avgblood);
        account1=(TextView)findViewById(R.id.account);
        intent=getIntent();
        account=intent.getStringExtra("account");//傳值


        new getData1().execute();
        new getBloodHighData().execute();
        new getBloodlowData().execute();
        new getAVGData().execute();
        new getBarData().execute();

        calenderview_blood=(CalendarView)findViewById(R.id.calendarView_blood);
        calenderview_blood.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int i, int i1, int i2) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(i, i1, i2);
                Calendar currentDate = Calendar.getInstance();
                if (selectedDate.after(currentDate)) {
                    calendarView.setMaxDate(currentDate.getTimeInMillis());
                } else {
                    date = i + "/" + (i1 + 1) + "/" + i2;
                    new getBloodHighData().execute();
                    new getBloodlowData().execute();
                    new getAVGData().execute();
                    new getBarData().execute();
                }
            }
        });
//啟動bloodchart函式
        //bloodBarChart();
//何謂血氧
        detailsText=findViewById(R.id.t2);
        layout=findViewById(R.id.layout);
        layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
//如何改善缺氧
        detailsText2=findViewById(R.id.t4);
        layout2=findViewById(R.id.layout2);
        layout2.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void btnOnClick(View v){
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.layout:
                int n = (detailsText.getVisibility()==View.GONE) ? View.VISIBLE:View.GONE;
                TransitionManager.beginDelayedTransition(layout,new AutoTransition());
                detailsText.setVisibility(n);
                break;
            case R.id.layout2:
                int m = (detailsText2.getVisibility()==View.GONE) ? View.VISIBLE:View.GONE;
                TransitionManager.beginDelayedTransition(layout2,new AutoTransition());
                detailsText2.setVisibility(m);
                break;

        }
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
    //取得血氧數據，55行啟動
    class getData1 extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection1 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=血氧"));
                URL url = new URL("http://"+server+"/myHealth/getdatatype1"+cmd);
                urlConnection1 = (HttpURLConnection) url.openConnection();
                urlConnection1.setRequestMethod("GET");
                urlConnection1.setDoOutput(true);
                urlConnection1.connect();
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
                urlConnection1.disconnect();
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
                String d = jsonObject.getString("數值");
                String z = jsonObject.getString("流水號");
                new_blood.setText(d);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //取得High血氧數據
    class getBloodHighData extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=血氧&日期="+date));
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
                    highest_blood.setText("無");
                } else {
                    // 否則，獲取數據並設置文本
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String a = jsonObject.getString("帳號");
                    String t = jsonObject.getString("類別");
                    String c = jsonObject.getString("紀錄時間");
                    String e = jsonObject.getString("紀錄日期");
                    String p = jsonObject.getString("數值");
                    String z = jsonObject.getString("流水號");
                    highest_blood.setText(p);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //取得心率數據
    class getBloodlowData extends AsyncTask<String, String, String> {
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=血氧&日期="+date));
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
                    lowest_blood.setText("無");
                } else {
                    // 否則，獲取數據並設置文本
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String a = jsonObject.getString("帳號");
                    String t = jsonObject.getString("類別");
                    String c = jsonObject.getString("紀錄時間");
                    String e = jsonObject.getString("紀錄日期");
                    String p = jsonObject.getString("數值");
                    String z = jsonObject.getString("流水號");
                    lowest_blood.setText(p);
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
                cmd=String.format(("?帳號="+account+"&類別=血氧&日期="+date)); // add date to the API call
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
                    avgblood.setText("無");
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
                        avgblood.setText(String.valueOf(averageStr)); // set the text to the average value
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

    class getBarData extends AsyncTask<String, String, String> {
        BarChart barChart = (BarChart) findViewById(R.id.bloodchart);
        private String server = "himhealth.mcu.edu.tw";
        HttpURLConnection urlConnection3 = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                String cmd;
                cmd=String.format(("?帳號="+account+"&類別=血氧&日期="+date)); // add date to the API call
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
                    dataSetS.addDataSet(new BarDataSet(systolics, "血氧含量(SPO2)"));

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
                    description.setText("平均血氧統計");
                    barChart.setDescription(description);
                    barChart.invalidate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }}

}
}