package com.bruce.travel.mine.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.usage.UsageEvents;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bruce.travel.R;
import com.bruce.travel.db.MyDbHelper;
import com.bruce.travel.desktop.ui.DesktopActivity;

import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import static android.view.View.*;

/**
 * Created by 梦亚 on 2016/8/4.
 */
public class LoginActivity extends Activity implements OnClickListener {

    private RadioButton account_login_btn, dynamic_login_btn;
    private EditText account_login_name_et, account_lognin_password_et;
    private EditText dynamic_login_number_et, dynamic_login_password_et;
    private Button send_dynamic_password_btn;
    private Button register_new_account_btn, find_password_btn;
    private Button close_btn, login_btn;
    private LinearLayout account_login_ll, dynamic_login_ll;
    private RadioGroup login_rg;

    private MyDbHelper db;
    SharedPreferences sp;
    private boolean flag = false;
    private EventHandler eventHandler;
    private String user_phone;

    private static final int CODE_ING = 1;   //已发送，倒计时
    private static final int CODE_REPEAT = 2;  //重新发送
    private static final int SMSDDK_HANDLER = 3;  //短信回调
    private int TIME = 60;//倒计时60s


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_layout);
        initViews();
        initSDK();
        initListeners();

    }

    private void initViews() {
        login_rg = (RadioGroup) findViewById(R.id.login_rg);
        close_btn = (Button) findViewById(R.id.close_btn);
        account_login_btn = (RadioButton) findViewById(R.id.account_login_radio_btn);
        dynamic_login_btn = (RadioButton) findViewById(R.id.dynamic_login_radio_btn);

        account_login_name_et = (EditText) findViewById(R.id.account_login_name_et);
        account_lognin_password_et = (EditText) findViewById(R.id.account_login_password_et);
        dynamic_login_number_et = (EditText) findViewById(R.id.dynamic_login_phone_number_et);
        dynamic_login_password_et = (EditText) findViewById(R.id.dynamic_login_password_et);
        send_dynamic_password_btn = (Button) findViewById(R.id.send_dynamic_password_btn);

        register_new_account_btn = (Button) findViewById(R.id.register_new_account_btn);
        find_password_btn = (Button) findViewById(R.id.find_password_btn);
        login_btn = (Button) findViewById(R.id.login_btn);

        account_login_ll = (LinearLayout) findViewById(R.id.account_login_layout);
        dynamic_login_ll = (LinearLayout) findViewById(R.id.dynamic_login_layout);
    }

    private void initSDK() {
        SMSSDK.initSDK(this, "15cff88565474", "617f22f30590ae8a24434a25b2ed4e86");
        eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                msg.what = SMSDDK_HANDLER;
                handler.sendMessage(msg);
            }
        };

        SMSSDK.registerEventHandler(eventHandler);
    }

    private void initListeners() {
        account_login_ll.setOnClickListener(this);
        dynamic_login_ll.setOnClickListener(this);

        account_login_btn.setOnClickListener(this);
        dynamic_login_ll.setOnClickListener(this);
        send_dynamic_password_btn.setOnClickListener(this);
        register_new_account_btn.setOnClickListener(this);
        find_password_btn.setOnClickListener(this);
        login_btn.setOnClickListener(this);

        login_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.account_login_radio_btn:
                        account_login_ll.setVisibility(View.VISIBLE);
                        dynamic_login_ll.setVisibility(View.GONE);
                        break;
                    case R.id.dynamic_login_radio_btn:
                        account_login_ll.setVisibility(View.GONE);
                        dynamic_login_ll.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        db = new MyDbHelper(this);
        sp = this.getSharedPreferences("userInfo", LoginActivity.MODE_WORLD_READABLE);
        switch(v.getId()) {
            case R.id.close_btn:
                finish();
                break;
            case R.id.login_btn:

                if(account_login_btn.isChecked()) {
                    String account_login_name = account_login_name_et.getText().toString();
                    String account_login_password = account_lognin_password_et.getText().toString();
                    String dynamic_login_number = dynamic_login_number_et.getText().toString();
                    String dynamic_login_password = dynamic_login_number_et.getText().toString();

                    if(TextUtils.isEmpty(account_login_name)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_user_empty), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(TextUtils.isEmpty(account_login_password)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_password_empty), Toast.LENGTH_LONG).show();
                        return;
                    }


                    Cursor cursor = db.select();
                    cursor.moveToFirst();
                    while(cursor.moveToNext()) {
                        if(cursor.getString(cursor.getColumnIndex("username")).equals(account_login_name)){
                            if(cursor.getString(cursor.getColumnIndex("password")).equals(account_login_password)){
//                            记住用户名、密码
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("USER_NAME", account_login_name);
                                editor.putString("PASSWORD", account_login_password);
                                editor.commit();
                                flag = true;

                            }
                        }
                    }
                    if(!flag){
                        Toast.makeText(getApplicationContext(), "用户名密码不正确", Toast.LENGTH_SHORT).show();
                    }else{
                        Intent intent = new Intent(LoginActivity.this, DesktopActivity.class);
                        intent.putExtra("loginState",true);
                        intent.putExtra("username",account_login_name);
                        startActivity(intent);
                    }
                } else {
                    SMSSDK.submitVerificationCode("86", user_phone, dynamic_login_password_et.getText().toString());//对验证码进行验证->回调函数

                }

                break;

            case R.id.send_dynamic_password_btn:

                user_phone  = dynamic_login_number_et.getText().toString();
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("发送短信")
                        .setMessage("我们将把验证码发送到以下号码:\n"+"+86:" + user_phone)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                SMSSDK.getVerificationCode("86", user_phone);
                                send_dynamic_password_btn.setClickable(false);
                                send_dynamic_password_btn.setText("重新发送(" + TIME-- + "s)");
                                new Thread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        for (int i = 60; i > 0; i--)
                                        {
                                            handler.sendEmptyMessage(CODE_ING);
                                            if (i <= 0)
                                            {
                                                break;
                                            }
                                            try
                                            {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                        TIME = 60;
                                        handler.sendEmptyMessage(CODE_REPEAT);
                                    }
                                }).start();
                            }
                        })
                        .create()
                        .show();

                break;
            case R.id.register_new_account_btn:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
        }

    }

    Handler handler  = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case CODE_ING:
                        send_dynamic_password_btn.setText("重新发送(" + TIME-- + "s)");
                    break;
                case CODE_REPEAT:
                    send_dynamic_password_btn.setText("发送动态密码");
                    send_dynamic_password_btn.setClickable(true);
                    break;
                case SMSDDK_HANDLER:
                    int event  = msg.arg1;
                    int result = msg.arg2;
                    Object data = msg.obj;
                    if (result == SMSSDK.RESULT_COMPLETE)
                    {
                        //验证码验证成功
                        if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE)
                        {
                            Toast.makeText(LoginActivity.this, "验证成功", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, DesktopActivity.class);
                            startActivity(intent);

                        }
                        //已发送验证码
                        else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE)
                        {
                            Toast.makeText(getApplicationContext(), "验证码已经发送",
                                    Toast.LENGTH_SHORT).show();
                        } else
                        {
                            ((Throwable) data).printStackTrace();
                        }
                    }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }
}
