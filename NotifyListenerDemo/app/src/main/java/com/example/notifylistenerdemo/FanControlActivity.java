package com.example.notifylistenerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

//风扇控制界面逻辑
public class FanControlActivity extends AppCompatActivity {


    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.fancontrol_activity);
    }

    public void onClickTemControl(View view) {
        //点击温度控制按钮实现跳转
        startActivity(new Intent(FanControlActivity.this, TemControlActivity.class));
        finish();
    }

    public void onClickFanStatus(View view) {
        //点击风扇状态按钮实现跳转
        startActivity(new Intent(FanControlActivity.this,MainActivity.class));
        finish();
    }
}
