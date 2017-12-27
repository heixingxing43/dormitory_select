package com.example.heixingxing.dormitory_select.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.heixingxing.dormitory_select.R;
import com.example.heixingxing.dormitory_select.bean.Student;

public class Map extends Activity implements View.OnClickListener{
    ImageView map_back;
    String student_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        Intent intent = this.getIntent();
        student_id = intent.getStringExtra("student_id");

        map_back = (ImageView) findViewById(R.id.map_back);
        map_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.map_back){
            Intent intent = new Intent(Map.this, Student.class);
            intent.putExtra("student_id", student_id);
            startActivity(intent);
            finish();
        }
    }
}
