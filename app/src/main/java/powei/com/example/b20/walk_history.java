package powei.com.example.b20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class walk_history extends AppCompatActivity {
    private Button btnreturn;
    private TextView theDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_history);

        theDate=(TextView)findViewById(R.id.theDate);
        btnreturn=(Button)findViewById(R.id.btnreturn);

        Intent incomingIntent =getIntent();
        String date = incomingIntent.getStringExtra("date");
        theDate.setText(date);

        btnreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(walk_history.this,Walkdata.class);
                startActivity(intent);
            }
        });
    }
}