package com.chnword.chnword.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.chnword.chnword.R;
import com.chnword.chnword.net.AbstractNet;
import com.chnword.chnword.net.DeviceUtil;
import com.chnword.chnword.net.NetConf;
import com.chnword.chnword.net.NetParamFactory;
import com.chnword.chnword.net.VerifyNet;
import com.chnword.chnword.store.LocalStore;

import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by khtc on 15/4/23.
 */
public class RegistActivity extends Activity {
    private String TAG = RegistActivity.class.getSimpleName();

    private EditText editText;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        LocalStore store = new LocalStore(this);
        if (!"0".equalsIgnoreCase(store.getDefaultUser()))
        {
            Intent intent = new Intent(this, TabActivity.class);
            startActivity(intent);
            finish();
        }
        editText = (EditText) findViewById(R.id.editText);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }



    //提交
    public void onLoginClick(View view){
        Log.i(TAG, "METHOD onLoginClick");

        String userCode = editText.getText().toString();

        LocalStore store = new LocalStore(this);
        store.removeDefaultUser();
        store.addUser(userCode);
        store.setDefaultUser(userCode);

        //这里需要进行下注册。

        userid = DeviceUtil.getPhoneNumber(this) == null ?  UUID.randomUUID().toString()  : DeviceUtil.getPhoneNumber(this) ;
        String deviceId = DeviceUtil.getDeviceId(this);
        JSONObject param = NetParamFactory.verifyParam(userid, deviceId, userid, deviceId);

        AbstractNet net = new VerifyNet(handler, param, NetConf.URL_VERIFY);
        progressDialog = ProgressDialog.show(this, "title", "loading");
        net.start();
        Log.e(TAG, param.toString());

    }

    private String userid;
    //使用
    public void onTryButtonClicked(View view){
        Log.e(TAG, "METHOD onTryButtonClicked");

        Intent i = new Intent(this, TabActivity.class);
        startActivity(i);
        finish();
    }


    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            try {
                Bundle b = msg.getData();
                String str = b.getString("responseBody");
                Log.e(TAG, str);
                if (msg.what == AbstractNet.NETWHAT_SUCESS)
                {


                    LocalStore store = new LocalStore(RegistActivity.this);
                    store.setDefaultUser(userid);
                    store.addUser(userid);

                    Intent intent = new Intent(RegistActivity.this, TabActivity.class);
                    startActivity(intent);
                    finish();

                }

                if (msg.what == AbstractNet.NETWHAT_FAIL) {
                    Toast.makeText(RegistActivity.this, "请检查网络设置。", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}