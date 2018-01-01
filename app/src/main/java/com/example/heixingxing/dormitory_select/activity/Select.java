package com.example.heixingxing.dormitory_select.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.heixingxing.dormitory_select.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Select extends Activity implements View.OnClickListener{
    private static final int DORM_SELECT_INFOMATION = 3;

    private EditText mId1, mId2, mId3, mId4, mVcode1, mVcode2, mVcode3, mVcode4;
    private Spinner mSpiNum, mSpiBuilding;
    private LinearLayout mInfo1, mInfo2, mInfo3, mInfo4;
    private TextView mErrorHint;
    private ImageView mBack;
    private Button mConfirmTv;

    private String id1, id2, id3, id4;
    private String vcode1, vcode2, vcode3, vcode4;
    private String dormRes1;
    private String[] dormRes;
    private int countStd, building;

    private ArrayList<String> renshu, roomNum;
    private ArrayAdapter<String> adapterRenshu, adapterRoom;

    private SharedPreferences sharedPreferences;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DORM_SELECT_INFOMATION:
                    int errorCode = (int) msg.obj;
                    confirmBackHint(errorCode);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_dorm);
        initView();
        setSpinInfo();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                Intent intent = new Intent(Select.this, StudentInfo.class);
                intent.putExtra("student_id", id1);
                startActivityForResult(intent,1);
                finish();
                break;
            case R.id.confirm:
                Log.d("dormselect","clickconfirm");
                queryConfirmInfo(makeJsonData());
                break;
            default:
                break;
        }
    }

    private void initView(){
        mBack = (ImageView)findViewById(R.id.back);
        mBack.setOnClickListener(this);

        mInfo1 = (LinearLayout) findViewById(R.id.info_1);
        mId1 = (EditText) findViewById(R.id.id_1);
        mVcode1 = (EditText) findViewById(R.id.vcode_1);
        mInfo2 = (LinearLayout) findViewById(R.id.info_2);
        mId2 = (EditText) findViewById(R.id.id_2);
        mVcode2 = (EditText) findViewById(R.id.vcode_2);
        mInfo3 = (LinearLayout) findViewById(R.id.info_3);
        mId3 = (EditText) findViewById(R.id.id_3);
        mVcode3 = (EditText) findViewById(R.id.vcode_3);
        mInfo4 = (LinearLayout) findViewById(R.id.info_4);
        mId4 = (EditText) findViewById(R.id.id_4);
        mVcode4 = (EditText) findViewById(R.id.vcode_4);

        mSpiNum = (Spinner) findViewById(R.id.renshukuang);
        mSpiBuilding = (Spinner) findViewById(R.id.sushekuang);
    }

    private void setSpinInfo(){
        Intent intent = this.getIntent();
        id1 = intent.getStringExtra("student_id");
        vcode1 = intent.getStringExtra("vCode");
        Log.d("dormselect",vcode1);

        mId1.setText(id1);
        mVcode1.setText(vcode1);
        mId1.setFocusable(false);
        mVcode1.setFocusable(false);

        mConfirmTv = (Button) findViewById(R.id.confirm);
        mConfirmTv.setOnClickListener(this);
        mErrorHint = (TextView) findViewById(R.id.errorHint);
        //办理人数下拉菜单处理
        renshu = new ArrayList<String>();
        renshu.add("单人办理");
        renshu.add("两人办理");
        renshu.add("三人办理");
        renshu.add("四人办理");
        adapterRenshu = new ArrayAdapter<String>(Select.this, R.layout.support_simple_spinner_dropdown_item, renshu);
        adapterRenshu.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSpiNum.setAdapter(adapterRenshu);
        mSpiNum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        countStd = 1;
                        mInfo1.setVisibility(View.VISIBLE);
                        mInfo2.setVisibility(View.INVISIBLE);
                        mInfo3.setVisibility(View.INVISIBLE);
                        mInfo4.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        countStd = 2;
                        mInfo1.setVisibility(View.VISIBLE);
                        mInfo2.setVisibility(View.VISIBLE);
                        mInfo3.setVisibility(View.INVISIBLE);
                        mInfo4.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        countStd = 3;
                        mInfo1.setVisibility(View.VISIBLE);
                        mInfo2.setVisibility(View.VISIBLE);
                        mInfo3.setVisibility(View.VISIBLE);
                        mInfo4.setVisibility(View.INVISIBLE);
                        break;
                    case 3:
                        countStd = 4;
                        mInfo1.setVisibility(View.VISIBLE);
                        mInfo2.setVisibility(View.VISIBLE);
                        mInfo3.setVisibility(View.VISIBLE);
                        mInfo4.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //楼号选择下拉菜单处理
        dormRes1 = intent.getStringExtra("dormRes");
        dormRes = dormRes1.split(";");
        Log.d("dorminfo",dormRes1);

        roomNum = new ArrayList<String>();
        if(Integer.parseInt(dormRes[0])>0){
            roomNum.add("5号楼");
        }
        if(Integer.parseInt(dormRes[1])>0){
            roomNum.add("13号楼");
        }
        if(Integer.parseInt(dormRes[2])>0){
            roomNum.add("14号楼");
        }
        if(Integer.parseInt(dormRes[3])>0){
            roomNum.add("8号楼");
        }
        if(Integer.parseInt(dormRes[4])>0){
            roomNum.add("9号楼");
        }
        adapterRoom = new ArrayAdapter<String>(Select.this, R.layout.support_simple_spinner_dropdown_item, roomNum);
        mSpiBuilding.setAdapter(adapterRoom);
        mSpiBuilding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        building = 5;
                        break;
                    case 1:
                        building = 13;
                        break;
                    case 2:
                        building = 14;
                        break;
                    case 3:
                        building = 8;
                        break;
                    case 4:
                        building = 9;
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String makeJsonData(){
        id1 = mId1.getText().toString();
        vcode1 = mVcode1.getText().toString();
        id2 = mId2.getText().toString();
        vcode2 = mVcode2.getText().toString();
        id3 = mId3.getText().toString();
        vcode3 = mVcode3.getText().toString();
        id4 = mId4.getText().toString();
        vcode4 = mVcode4.getText().toString();

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("num", countStd);
            jsonObj.put("stuid", id1);
            jsonObj.put("stu1id", id2);
            jsonObj.put("v1code", vcode2);
            jsonObj.put("stu2id", id3);
            jsonObj.put("v2code", vcode3);
            jsonObj.put("stu3id", id4);
            jsonObj.put("v3code", vcode4);
            jsonObj.put("buildingNo", building);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("dormselect", jsonObj.toString());
        return jsonObj.toString();
    }

    private void queryConfirmInfo(final String jsondata){
        final String address = "https://api.mysspku.com/index.php/V1/MobileCourse/SelectRoom";
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                int errorCode = -1;

                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
                    public X509Certificate[] getAcceptedIssuers(){return null;}
                    public void checkClientTrusted(X509Certificate[] certs, String authType){}
                    public void checkServerTrusted(X509Certificate[] certs, String authType){}
                }};
                try {
                    URL url = new URL(address);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(4000);

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
                    BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                    bufferedWriter.write(jsondata);
                    bufferedWriter.flush();

                    InputStream inputStream = conn.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = bufferedReader.readLine()) != null) {
                        response.append(str);
                    }
                    String responseStr = response.toString();
                    Log.d("dormselect", responseStr);
                    bufferedReader.close();
                    inputStream.close();
                    bufferedWriter.close();
                    outputStreamWriter.close();
                    errorCode = getErrorCode(responseStr);
                    Log.d("dormselect", errorCode+" errorCode");

                    Message message = new Message();
                    message.what = DORM_SELECT_INFOMATION;
                    message.obj = errorCode;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public int getErrorCode(String jsonData) {
        int errorCode = -1;
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            if (jsonObject != null) {
                errorCode = jsonObject.getInt("errcode");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return errorCode;
    }

    protected void confirmBackHint(int errorCode) {
        if (errorCode == 0) {
            if(Integer.parseInt(id1.substring(id1.length()-2, id1.length()-1)) %2 ==0){
                Log.d("dormselect", "成功");
                Toast.makeText(Select.this, "选择宿舍成功", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Select.this, StudentInfo.class);
                intent.putExtra("student_id", id1);
                startActivityForResult(intent,1);
            }else{
                Toast.makeText(Select.this, "选择宿舍失败", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Select.this, StudentInfo.class);
                intent.putExtra("student_id", id1);
                startActivityForResult(intent,1);
            }
        } else if (errorCode == 40001) {
            Log.d("dormselect", "40001");
            mErrorHint.setText("学号不存在，请重新填写");
            mErrorHint.setVisibility(View.VISIBLE);
        } else if (errorCode == 40002) {
            Log.d("dormselect", "40002");
            mErrorHint.setText("验证码错误，请重新填写");
            mErrorHint.setVisibility(View.VISIBLE);
        } else if (errorCode == 40009) {
            Log.d("dormselect", "40009");
            mErrorHint.setText("参数错误");
            mErrorHint.setVisibility(View.VISIBLE);
        }
    }
}
