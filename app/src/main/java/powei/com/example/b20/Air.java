package powei.com.example.b20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Air extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air);
    }

    public void btnReturnClick(View v){
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.btnreturn:
                intent.setClass(Air.this, MainActivity0.class);
                break;
        }
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }
}