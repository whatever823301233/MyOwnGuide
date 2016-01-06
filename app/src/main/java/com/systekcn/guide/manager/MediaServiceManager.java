package com.systekcn.guide.manager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.systekcn.guide.IConstants;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.service.MediaPlayService;
import com.systekcn.guide.utils.ExceptionUtil;

import java.util.List;

/**
 * Created by Qiang on 2015/10/29.
 *
 */
public class MediaServiceManager implements IConstants {

    private Context mContext;
    private ServiceConnection mConn;
    public MediaPlayService.MediaServiceBinder mediaServiceBinder;
    private PlayCtrlReceiver playCtrlReceiver;

    public MediaServiceManager(Context context) {
        this.mContext = context.getApplicationContext();
        init();
    }

    public void init(){
        initConn();
        registerReceiver();
    }

    private void registerReceiver(){
        playCtrlReceiver=new PlayCtrlReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(INTENT_EXHIBIT);
        mContext.registerReceiver(playCtrlReceiver,filter);
    }

    private void initConn() {
        mConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mediaServiceBinder = (MediaPlayService.MediaServiceBinder) service;
            }
        };
    }

    public void connectService() {
        Intent intent = new Intent(mContext, MediaPlayService.class);
        mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    public void disConnectService() {
        if (mediaServiceBinder == null) {return;}
        mContext.unregisterReceiver(playCtrlReceiver);
        mContext.unbindService(mConn);
        mContext.stopService(new Intent(mContext, MediaPlayService.class));

    }

    public int getCurrentPosition(){
        if (mediaServiceBinder == null) {return 0;}
        return mediaServiceBinder.getCurrentPosition();
    }

    public void stop() {
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.stopPlay();
    }

    public boolean isPlaying() {
        return mediaServiceBinder != null && mediaServiceBinder.isPlaying();
    }

    public boolean pause() {
        if (mediaServiceBinder == null) {return false;}
        return mediaServiceBinder.pause();
    }

    public boolean next() {
        if (mediaServiceBinder == null) {return false;}
        return mediaServiceBinder.next();
    }
    public void seekTo(int progress) {
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.seekTo(progress);
    }

    public void toContinue(){
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.continuePlay();
    }

    public void notifyExhibitChange(ExhibitBean exhibitBean){
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.notifyExhibitChange(exhibitBean);
    }

    public List<ExhibitBean> getExhibitList() {
        if (mediaServiceBinder == null) {return null;}
        return mediaServiceBinder.getPlayList();
    }

    public void refreshExhibitBeanList(List<ExhibitBean> exhibitList) {
        if (exhibitList == null || mediaServiceBinder == null) {return;}
        try {
            mediaServiceBinder.setPlayList(exhibitList);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }

    public boolean play() {
        if (mediaServiceBinder == null) {return false;}
        return mediaServiceBinder.startPlay();
    }


    public int duration() {
        if (mediaServiceBinder == null) {return 0;}
        return mediaServiceBinder.getDuration();
    }


    public void setPlayMode(int mode) {
        if (mediaServiceBinder == null) {return;}
        mediaServiceBinder.setPlayMode(mode);
    }

    public int getPlayMode() {
        if (mediaServiceBinder == null) {return 0;}
        return mediaServiceBinder.getPlayMode();
    }

    public ExhibitBean getCurrentExhibit() {
        if (mediaServiceBinder == null) {return null;}
        return mediaServiceBinder.getCurrentExhibit();
    }

    public void exit() {
        if(mConn==null||mContext==null){return;}
        mContext.unbindService(mConn);
        mContext.stopService(new Intent(mContext, MediaPlayService.class));
    }

    private class PlayCtrlReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(INTENT_EXHIBIT)){
                String exhibitStr= intent.getStringExtra(INTENT_EXHIBIT);
                if(TextUtils.isEmpty(exhibitStr)){return;}
                ExhibitBean exhibitBean= JSON.parseObject( exhibitStr,ExhibitBean.class);
                if(exhibitBean==null||mediaServiceBinder==null){return;}
                mediaServiceBinder.notifyExhibitChange(exhibitBean);
            }
        }
    }



    //暂无以下方法
    /*public boolean rePlay() {
        if (mediaServiceBinder != null) {
            mediaServiceBinder.rePlay();
            return true;
        }
        return false;
    }
    public boolean prev() {
        if (mediaServiceBinder != null) {
            return mediaPlayService.prev();
        }
        return false;
    }
    public void reset(){
        mediaServiceBinder.reset();
    }
    public String getCurMusicId() {
        if (mediaServiceBinder != null) {
            return mediaServiceBinder.getCurMusicId();
        }
        return -1;
    }
 public void sendBroadcast() {
        if (mediaServiceBinder != null) {
            mediaServiceBinder.sendPlayStateBrocast();
        }
    }

    */
}
