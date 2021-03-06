package com.chnword.chnword.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.chnword.chnword.store.LocalStore;
import com.chnword.chnword.utils.PerferenceKey;
import com.chnword.chnword.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class SplashActivity extends Activity {

    private LinearLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mLayout = (LinearLayout) findViewById(R.id.ll_splash);

        Animation alphaAnim = new AlphaAnimation(0.3f, 1.0f);
        alphaAnim.setDuration(2000);
        mLayout.startAnimation(alphaAnim);

        //初始化
        //创建默认的ImageLoader配置参数
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                .createDefault(this);
        //Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(configuration);


    }

    @Override
    protected void onStart(){
        super.onStart();

//        Intent i = new Intent(SplashActivity.this, RegistActivity.class);
//        startActivity(i);
//        return ;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                LocalStore stroe = new LocalStore(SplashActivity.this);
                if (!"0".equalsIgnoreCase(stroe.getDefaultUser())) {
                    //免费用户
                    Intent intent = new Intent(SplashActivity.this, RegistActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent i = new Intent(SplashActivity.this, GuideActivity.class);
                    startActivity(i);
                    finish();
                }

//                SharedPreferences perferences = getSharedPreferences(PerferenceKey.FirstLoginPreferences, Context.MODE_PRIVATE);
//                boolean isFirstLogin = perferences.getBoolean(PerferenceKey.firstLoginKey, true);
//                if (!isFirstLogin) {
//                    Intent intent = new Intent(SplashActivity.this, RegistActivity.class);
//                    startActivity(intent);
//                    finish();
//                } else {
//                    SharedPreferences.Editor editor = perferences.edit();
//                    editor.putBoolean(PerferenceKey.firstLoginKey, false);
//                    editor.commit();
//                    Intent i = new Intent(SplashActivity.this, GuideActivity.class);
//                    startActivity(i);
//                    finish();
//                }
            }
        }, 2000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        return super.onOptionsItemSelected(item);
    }
}
