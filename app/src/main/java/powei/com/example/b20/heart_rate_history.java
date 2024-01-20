package powei.com.example.b20;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class heart_rate_history extends ListActivity{
    private Button btnreturn;
    private TextView theDate;
    public final String server = "himhealth.mcu.edu.tw";
    public Contact[] contacts;
    public Contact contact;
    public TextView txvInfo;
    int curpos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_history);
        theDate=(TextView)findViewById(R.id.theDate);
        btnreturn=(Button)findViewById(R.id.btnreturn);

        getNeedViews();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);




        Intent incomingIntent =getIntent();
        String date = incomingIntent.getStringExtra("date");
        theDate.setText(date);



        btnreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(heart_rate_history.this,Heartrate.class);
                startActivity(intent);
            }
        });

    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        this.curpos = position;
    }

    private void getNeedViews() {
       this.txvInfo = (TextView)this.findViewById(R.id.txvInfo);
    }

}