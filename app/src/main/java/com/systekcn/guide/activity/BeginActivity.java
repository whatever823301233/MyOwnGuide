package com.systekcn.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.systekcn.guide.R;
import com.systekcn.guide.utils.NetworkUtil;
import com.systekcn.guide.utils.Tools;

public class BeginActivity extends BaseActivity {

    private View view;
    private Class<?> targetClass;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        view = View.inflate(this, R.layout.activity_begin, null);
        NetworkUtil.checkNet(this);
        setContentView(view);
        boolean isFirstLogin= (boolean) Tools.getValue(this, SP_NOT_FIRST_LOGIN, false);
        if(!isFirstLogin){
            Tools.saveValue(this,SP_NOT_FIRST_LOGIN,true);
            targetClass=WelcomeActivity.class;
        }else{
            /*默认跳转界面为城市选择*/
            targetClass=MuseumListActivity.class;
        }
        initData();
    }
    private void initData() {
        AlphaAnimation startAnimation = new AlphaAnimation(1.0f, 1.0f);
        startAnimation.setDuration(2500);
        view.startAnimation(startAnimation);
        startAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                redirectTo();
            }
        });
    }
    protected void redirectTo() {
        startActivity(new Intent(getApplicationContext(),targetClass));
        finish();
    }
}
