package com.example.heixingxing.dormitory_select.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.heixingxing.dormitory_select.R;
import com.example.heixingxing.dormitory_select.bean.Dorm;
import com.example.heixingxing.dormitory_select.bean.Student;
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

public class StudentInfo extends Activity implements View.OnClickListener{
    private static final int STUDENT_INFOMATION = 1;
    private static final int DORMITORY_INFOMATION = 2;

    private String student_id, vCode, dormRes, gender;
    private TextView mIDTv, mNameTv, mGenderTv, mVcodeTv, mRoomTv, mBuildTv, mLoctTv, mGradeTv;
    private TextView build5Tv, build13Tv, build14Tv, build8Tv, build9Tv;

    private Button mSelectBtn;
    private SharedPreferences sharedPreferences;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case STUDENT_INFOMATION:
                    String tmp = mGenderTv.getText().toString();
                    Log.d("dorminfo",tmp);
                    if(tmp.equals("性别：      女")){
                        queryDormInfo("2");
                    }else{
                        queryDormInfo("1");
                    }
                    updateStudentInfo((Student) msg.obj);
                    break;
                case DORMITORY_INFOMATION:
                    updateDormInfo((Dorm)msg.obj);
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_info);
        initView();
        Intent i = this.getIntent();
        student_id = i.getStringExtra("student_id");
        Log.d("stdinfo",student_id);
        queryPersonalInfo(student_id);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.selectdormBtn){
            Log.d("login","开始办理选宿舍");
            Log.d("login",student_id);
            Log.d("login",vCode);
            Log.d("login",dormRes);

            Intent i = new Intent(this, Select.class);
            i.putExtra("student_id", student_id);
            i.putExtra("vCode", vCode);
            i.putExtra("dormRes", dormRes);
            startActivityForResult(i,1);
        }
    }

    //初始化控件
    private void initView(){
        mIDTv = (TextView)findViewById(R.id.student_id);
        mNameTv = (TextView)findViewById(R.id.student_name);
        mGenderTv = (TextView)findViewById(R.id.student_gender);
        mVcodeTv = (TextView)findViewById(R.id.student_vcode);
        mRoomTv = (TextView)findViewById(R.id.student_room);
        mBuildTv = (TextView)findViewById(R.id.student_building);
        mLoctTv = (TextView)findViewById(R.id.student_location);
        mGradeTv = (TextView)findViewById(R.id.student_grade);

        build5Tv = (TextView)findViewById(R.id.building5);
        build13Tv = (TextView)findViewById(R.id.building13);
        build14Tv = (TextView)findViewById(R.id.building14);
        build8Tv = (TextView)findViewById(R.id.building8);
        build9Tv = (TextView)findViewById(R.id.building9);

        mSelectBtn = (Button)findViewById(R.id.selectdormBtn);
        mSelectBtn.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
    }

    //解析数据
    private void queryPersonalInfo(String stuid){
        final String address = "https://api.mysspku.com/index.php/V1/MobileCourse/getDetail?stuid="+stuid;
        Log.d("stdinfo",address);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                Student student = new Student();

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
                    Log.d("stdinfo",conn.getResponseCode() + " " + conn.getResponseMessage());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));//设置编码,否则中文乱码
                    StringBuilder response = new StringBuilder();

                    String lines;
                    while ((lines = reader.readLine()) != null){
                        response.append(lines);
                    }
                    String responsestr = response.toString();
                    Log.d("stdinfo", responsestr);
                    reader.close();

                    student = jsonStudent(responsestr);

                    Message message = new Message();
                    message.what = STUDENT_INFOMATION;
                    message.obj = student;
                    mHandler.sendMessage(message);
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

    //解析json数据，获取个人信息
    private Student jsonStudent(String jsondata){
        Student student = new Student();
        String jroom = null, jbuilding = null;
        Log.d("stdinfo","json");
        try{
            JSONObject jsonObj1 = new JSONObject(jsondata);
            JSONObject jsonObj2 = new JSONObject(jsonObj1.getString("data"));
            Log.d("stdinfo",jsonObj2.toString());

            if(jsonObj2.has("room")){
                jroom = jsonObj2.getString("room");
            }else{
                jroom = "暂未选择宿舍";
            }
            if(jsonObj2.has("building")){
                jbuilding = jsonObj2.getString("building");
            }else{
                jbuilding = "暂未选择宿舍楼";
            }

            student.setStd_id(jsonObj2.getString("studentid"));
            student.setName(jsonObj2.getString("name"));
            student.setGender(jsonObj2.getString("gender"));
            student.setVcode(jsonObj2.getString("vcode"));
            student.setLocation(jsonObj2.getString("location"));
            student.setGrade(jsonObj2.getString("grade"));
            student.setBuilding(jbuilding);
            student.setRoom(jroom);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return student;
    }

    //更新学生信息
    private void updateStudentInfo(Student student){
        student_id = student.getStd_id();
        vCode = student.getVcode();
        gender = student.getGender();
        mIDTv.setText("学号：      "+student.getStd_id());
        mNameTv.setText("姓名：      "+student.getName());
        mGenderTv.setText("性别：      "+student.getGender());
        mVcodeTv.setText("验证码：  "+student.getVcode());
        mRoomTv.setText("宿舍号：  "+student.getRoom());
        mBuildTv.setText("楼号：      "+student.getBuilding());
        mLoctTv.setText("校区：      "+student.getLocation());
        mGradeTv.setText("年级：      "+student.getGrade());
    }

    private void queryDormInfo(String gender){
        Log.d("dorminfo",gender);
        final String address = "https://api.mysspku.com/index.php/V1/MobileCourse/getRoom?gender="+gender;
        Log.d("stdinfo",address);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Dorm dorm = new Dorm();
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
                    Log.d("dorminfo",conn.getResponseCode() + " " + conn.getResponseMessage());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));//设置编码,否则中文乱码
                    StringBuilder response = new StringBuilder();

                    String lines;
                    while ((lines = reader.readLine()) != null){
                        response.append(lines);
                    }
                    String responsestr = response.toString();
                    Log.d("stdinfo", responsestr);
                    reader.close();

                    dorm = jsonDorm(responsestr);

                    Message message = new Message();
                    message.what = DORMITORY_INFOMATION;
                    message.obj = dorm;
                    mHandler.sendMessage(message);
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

    private Dorm jsonDorm(String jsondata) throws JSONException {
        Dorm dorm = new Dorm();
        JSONObject jsonObj1 = new JSONObject(jsondata);
        Log.d("dorminfo","jsondorm");
        try{
            if(jsonObj1.getInt("errcode")==0){
                JSONObject jsonObj2 = new JSONObject(jsonObj1.getString("data"));
                Log.d("dorminfo",jsonObj2.toString());

                dorm.setBuilding5(jsonObj2.getString("5"));
                dorm.setBuilding13(jsonObj2.getString("13"));
                dorm.setBuilding14(jsonObj2.getString("14"));
                dorm.setBuilding8(jsonObj2.getString("8"));
                dorm.setBuilding9(jsonObj2.getString("9"));
                dormRes = jsonObj2.getString("5") + ";" + jsonObj2.getString("13") + ";"
                        + jsonObj2.getString("14") + ";" + jsonObj2.getString("8") + ";"
                        + jsonObj2.getString("9");
            }else{
                Log.d("dorminfo",jsonObj1.getString("errcode"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dorm;
    }

    private void updateDormInfo(Dorm dorm){
        build5Tv.setText("5号楼：  "+dorm.getBuilding5());
        build13Tv.setText("13号楼： "+dorm.getBuilding13());
        build14Tv.setText("14号楼： "+dorm.getBuilding14());
        build8Tv.setText("8号楼：  "+dorm.getBuilding8());
        build9Tv.setText("9号楼：  "+dorm.getBuilding9());
    }
}
