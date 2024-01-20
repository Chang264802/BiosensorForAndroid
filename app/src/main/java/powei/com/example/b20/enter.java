package powei.com.example.b20;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class enter extends AppCompatActivity {
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private final int PERMISSION_REQUEST = 1;
    public String account;


    ImageView google_login_btn;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        TextView btn=findViewById(R.id.signup);

        TextView nolog=findViewById(R.id.tonologapp);

        mUsernameEditText = findViewById(R.id.username);
        mPasswordEditText = findViewById(R.id.password);
        mLoginButton = findViewById(R.id.loginbtn);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                new LoginTask().execute(username, password);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(enter.this,RegisterActivity.class));
                finish();
            }
        });
        nolog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(enter.this,MainActivity0.class));
                finish();
            }
        });


        google_login_btn=findViewById(R.id.google_login_btn);

        gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        gsc= GoogleSignIn.getClient(this,gso);

        google_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermission()){
            Toast.makeText(enter.this, "藍牙連線未授權，無法執行藍牙傳輸功能!", Toast.LENGTH_LONG).show();
        }

        Intent intent = new Intent(enter.this, BluetoothService.class);
        startService(intent);
    }

    private Boolean checkPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (ContextCompat.checkSelfPermission(enter.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(enter.this, permission)) {
                showMessageOKCancel("藍牙掃描需開啟定位權限，請進行授權!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                doRequestPermission();
                            }
                        });
            } else
                doRequestPermission();
            return false;
        }
        return true;
    }

    private void doRequestPermission() {
        ActivityCompat.requestPermissions(enter.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(enter.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(enter.this, "定位未授權，無法執行藍牙裝置掃描功能", Toast.LENGTH_LONG).show();
                    }
                })
                .create()
                .show();
    }

    private void SignIn() {
        Intent intent=gsc.getSignInIntent();
        startActivityForResult(intent,100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_REQUEST){
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(enter.this, "藍牙定位功能授權成功!", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(enter.this, "藍牙定位未授權，無法執行藍牙裝置掃描功能!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                HomeActivity();
            } catch (ApiException e) {
                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void HomeActivity() {
        Intent intent=new Intent(getApplicationContext(),HomeActivity.class);
        intent.putExtra("account",account);
        startActivity(intent);
        finish();
    }


    public void onBackPressed()
    {
        finish();
    }
    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {

            HttpURLConnection urlConnection = null;
            try {
                // Connect server
                String username = args[0];
                String password = args[1];
                String login, url1;
                login = String.format("?帳號=" + username + "&電子信箱=" + username + "&密碼=" + password + "");
                url1 = "http://himhealth.mcu.edu.tw/myHealth/getLogin" + login;
                URL url = new URL(url1);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = urlConnection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf8");
                    BufferedReader reader = new BufferedReader(isr);
                    String response = reader.readLine();


                    JSONObject jsonObject = new JSONObject(response);
                    account = jsonObject.getString("帳號");
                    String 密碼 = jsonObject.getString("密碼");
                    if(account=="null")
                    {
                        return "false";
                    }
                    else {
                        return "ok";


                    }
                }
            } catch (IOException | JSONException ex) {
                return "false";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return "false";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result=="ok") {
                // 登入成功
                Toast.makeText(enter.this, "登入成功", Toast.LENGTH_SHORT).show();

                HomeActivity();

            } else {
                // 登入失敗
                Toast.makeText(enter.this, "登入失敗，請確認帳號密碼", Toast.LENGTH_SHORT).show();
            }
        }
    }
}