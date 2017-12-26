package com.example.heixingxing.dormitory_select.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.heixingxing.dormitory_select.R;
import com.example.heixingxing.dormitory_select.util.NetUtil;
import com.example.heixingxing.dormitory_select.util.NullHostNameVerifier;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by heixingxing on 2017/10/26.
 */

public class MainActivity extends Activity implements View.OnClickListener{
    private static final int LOGIN_SUCCESS=1;
    private EditText mEditCode;
    private EditText mEditPsword;

    private Button mSubmitBtn;
    private String username, password;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case LOGIN_SUCCESS:
                    String errcode = (String)msg.obj;
                    Log.d("login",errcode);
                    ifLoginSuccess(errcode);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        initView();
    }

    @Override
    public void onClick(View v){
        if(v.getId()==R.id.submit){
            Log.d("login","submit");
            queryLogin(username, password);
        }
    }

    //初始化
    private void initView(){
        mSubmitBtn = (Button)findViewById(R.id.submit);
        mSubmitBtn.setOnClickListener(this);
        mEditCode = (EditText)findViewById(R.id.input_number);
        mEditCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //解析登录数据
                username = s.toString();
                Log.d("login", username);
            }
        });
        mEditPsword = (EditText)findViewById(R.id.input_password);
        mEditPsword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mEditPsword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                password = s.toString();
                Log.d("login", password);
            }
        });

        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
            Log.d("network","Network is OK1.");
            Toast.makeText(MainActivity.this,"Network is OK.", Toast.LENGTH_LONG).show();
        }else{
            Log.d("network","Network is not OK.");
            Toast.makeText(MainActivity.this,"Network is not OK.", Toast.LENGTH_LONG).show();
        }
    }

    //从api中获取参数，参数为输入的学号和密码
    private void queryLogin(String username, String password){
        final String address = "https://api.mysspku.com/index.php/V1/MobileCourse/Login?username="+username+"&password="+password;
        Log.d("login",address);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;

                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
                    public X509Certificate[] getAcceptedIssuers(){return null;}
                    public void checkClientTrusted(X509Certificate[] certs, String authType){}
                    public void checkServerTrusted(X509Certificate[] certs, String authType){}
                }};

                // Install the all-trusting trust manager
                try {// 注意这部分一定要
                    HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustAllCerts, new SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                    URL url = new URL(address);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    Log.d("login",conn.getResponseCode() + " " + conn.getResponseMessage());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));//设置编码,否则中文乱码
                    StringBuilder response = new StringBuilder();

                    String lines;
                    while ((lines = reader.readLine()) != null){
                        response.append(lines);
                    }
                    String responsestr = response.toString();
                    Log.d("login", responsestr);
                    reader.close();
                    String result = jsonlogin(responsestr);
                    Log.d("login","return "+result);

                    Message msg =new Message();
                    msg.what = LOGIN_SUCCESS;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally{
                    if(conn != null){
                        // 断开连接
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

    //解析获取到的json数据
    private String jsonlogin(String jsondata){
        Log.d("login","json");
        String res=null;
        try{
            JSONObject jsonObj = new JSONObject(jsondata);
            res = jsonObj.getString("errcode");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }

    //根据传值判断是否登录成功
    private void ifLoginSuccess(String errcode){
        if(errcode.equals("0")){
//            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
//            String student_id = sharedPreferences.getString("student_id","1300000001");
            Intent i = new Intent(this, StudentInfo.class);
            i.putExtra("student_id", username);
            startActivityForResult(i,1);
        }else{
            Toast.makeText(MainActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            mEditCode.setText(null);
            mEditPsword.setText(null);
        }
    }

}
