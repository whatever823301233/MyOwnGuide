package com.systekcn.guide.activity;

import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.systekcn.guide.R;
import com.systekcn.guide.utils.NetworkUtil;
import com.systekcn.guide.utils.Tools;
import com.systekcn.guide.utils.ViewUtils;

public class BeginActivity extends BaseActivity {

    private View view;
    private Class<?> targetClass;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        connectWIFI();
        ViewUtils.setStateBarColor(this,R.color.md_red_200);
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

    private void connectWIFI() {

        new Thread(){
            @Override
            public void run() {
                WifiConfiguration wifiConfig = new WifiConfiguration();

                wifiConfig . status =  WifiConfiguration . Status . DISABLED ;
                wifiConfig . priority =  40 ;

                wifiConfig.SSID = String.format("\"%s\"", WIFI_SSID);
                wifiConfig.preSharedKey = String.format("\"%s\"", WIFI_PASSWORD);

                wifiConfig . allowedProtocols . set ( WifiConfiguration . Protocol . RSN );
                wifiConfig . allowedProtocols . set ( WifiConfiguration . Protocol . WPA );
                wifiConfig . allowedKeyManagement . set ( WifiConfiguration . KeyMgmt . WPA_PSK );
                wifiConfig . allowedPairwiseCiphers . set ( WifiConfiguration . PairwiseCipher . CCMP );
                wifiConfig . allowedPairwiseCiphers . set ( WifiConfiguration . PairwiseCipher . TKIP );
                wifiConfig . allowedGroupCiphers . set ( WifiConfiguration . GroupCipher . WEP40 );
                wifiConfig . allowedGroupCiphers . set ( WifiConfiguration . GroupCipher . WEP104 );
                wifiConfig . allowedGroupCiphers . set ( WifiConfiguration . GroupCipher . CCMP );
                wifiConfig . allowedGroupCiphers . set ( WifiConfiguration . GroupCipher . TKIP );

                WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);//remember id
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
            }
        }.start();


       /* wifiUtils.startScan();
        List<WifiConfiguration> configurations= wifiUtils.getConfiguration();
        if(configurations.size()>0){
           for(int i=0;i<configurations.size();i++){
               if(configurations.get(i).SSID.equals("systek")){
                   wifiUtils.connectConfiguration(i);
               }
           }
        }*/
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
