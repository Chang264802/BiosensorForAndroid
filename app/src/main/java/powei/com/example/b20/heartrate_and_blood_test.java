package powei.com.example.b20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

public class heartrate_and_blood_test extends AppCompatActivity {

private Button btntest;
private RelativeLayout ll;

int count=11;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartrate_and_blood_test);

        btntest=findViewById(R.id.btntest);
    }



    public void btnOnClick(View v){

        RelativeLayout ll =(RelativeLayout)findViewById(R.id.ll) ;
        switch (v.getId()){
            case R.id.btntest:
                ll.setVisibility(View.VISIBLE);
                btntest.setText("開始偵測，手指請不要離開手環!");

                break;
        }
    }

}