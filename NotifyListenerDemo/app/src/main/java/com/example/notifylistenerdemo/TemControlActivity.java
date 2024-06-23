package com.example.notifylistenerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

//温度控制界面逻辑
public class TemControlActivity extends AppCompatActivity {


    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.temcontrol_activity);
    }

    public void onClickFanControl(View view) {
        //点击温度控制按钮实现跳转
        startActivity(new Intent(TemControlActivity.this, FanControlActivity.class));
        finish();
    }

    public void onClickFanStatus(View view) {
        //点击风扇状态按钮实现跳转
        startActivity(new Intent(TemControlActivity.this, MainActivity.class));
        finish();
    }
}
