package com.systekcn.guide.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.systekcn.guide.IConstants;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.receiver.LockScreenReceiver;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.MyHttpUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlayService extends Service implements IConstants {


    private MediaPlayer mediaPlayer; // 播放器*/
    private boolean isPlaying = false; //是否正在播放*/
    private Binder mediaServiceBinder = new MediaServiceBinder();///服务Binder*/
    private ExhibitBean currentExhibit; //当前展品*/
    private String  currentMuseumId;//当前博物馆id*/
    private int currentPosition;//当前位置*/
    private int duration;//当前展品总时长
    private LockScreenReceiver mReceiver;//锁屏监听器
    private Handler handler ;
    private List<ExhibitBean> recordExhibitList;
    private List<ExhibitBean> playExhibitList;
    private int playMode = PLAY_MODE_HAND; //默认设置手动点击播放
    private boolean isSendProgress;

    //private boolean hasPlay; /*是否播放过*/
    //private int errorCount;

    public void onCreate() {
        super.onCreate();
        handler=new MyHandler();
        recordExhibitList=new ArrayList<>();
        playExhibitList=new ArrayList<>();
        initMediaPlayer();
        registerReceiver();
    }

    public int toGetDuration() {
        return duration;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mediaServiceBinder;
    }
    /**
     * 锁屏广播接收器
     * */
    private void registerReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mReceiver = new LockScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    public int toGetPlayMode() {
        return playMode;
    }

    public void toSetPlayMode(int playMode) {
        this.playMode = playMode;
    }

    /**初始化播放器*/
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(preparedListener);
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.setOnErrorListener(errorListener);

    }
    /**
     * 播放完成监听器
     */
    private MediaPlayer.OnCompletionListener completionListener=new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if(playMode==PLAY_MODE_AUTO){
                toStartPlay();
            }
            mp.pause();
            Intent intent=new Intent();
            intent.setAction(INTENT_CHANGE_PLAY_STOP);
            sendBroadcast(intent);
        }
    };

    /**
     * 资源准备监听器
     */
    private  MediaPlayer.OnPreparedListener preparedListener=new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            //if(playMode==PLAY_MODE_AUTO){// TODO: 2016/1/6  }
            toStartPlay();


        }
    };

    private boolean toStartPlay() {
        boolean flag=false;
        if(mediaPlayer==null){return false;}
        try {
            mediaPlayer.start();
            Intent intent=new Intent();
            intent.setAction(INTENT_CHANGE_PLAY_PLAY);
            sendBroadcast(intent);
            isPlaying=true;
            addRecord(currentExhibit);
            duration = mediaPlayer.getDuration();
            handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);
            isSendProgress=true;
            flag=true;
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
        return flag;
    }

    /**
     * 异常监听器
     */
    private MediaPlayer.OnErrorListener errorListener=new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };

    /**
     * 设置当前展品
     * @param bean
     */
    private void  setCurrentExhibit(ExhibitBean bean) {
        currentExhibit = bean;
    }

    private void toUpdateProgress() {
        if (mediaPlayer == null ) {return;}
        handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);
    }
    private void toUpdateDuration(int time) {
        handler.sendEmptyMessage(MSG_WHAT_UPDATE_DURATION);
    }

    public boolean toPause(){
        if(!isPlaying||mediaPlayer==null){return false;}
        mediaPlayer.pause();
        isPlaying=false;
        return true;
    }

    private void play(ExhibitBean bean) {
        setCurrentExhibit(bean);
        currentMuseumId=bean.getMuseumId();
        isPlaying=false;
        currentPosition=0;
        mediaPlayer.reset();
        handler.removeCallbacksAndMessages(null);
        String url = "";
        String exURL = currentExhibit.getAudiourl();
        String localName = exURL.replaceAll("/", "_");
        String localUrl = getCurrentAudioPath()+"/"+ localName;
        File file = new File(localUrl);
        if (file.exists()) {
            try {
                mediaPlayer.setDataSource(localUrl);
            } catch (IOException e) {
                ExceptionUtil.handleException(e);
            }
            mediaPlayer.prepareAsync();
        } else {
            url = BASE_URL + exURL;
            DownloadAudioTask downloadAudioTask = new DownloadAudioTask();
            downloadAudioTask.execute(url, localName);// TODO: 2016/1/6 修改加载地址
            Toast.makeText(this,"正在加载...",Toast.LENGTH_LONG).show();
        }
    }

    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        unregisterReceiver(mReceiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public void toContinuePlay(){
        toStartPlay();
    }

    public void toSeekTo(int progress){
        if (mediaPlayer == null) {return;}
        if (isPlaying) {
            mediaPlayer.seekTo(progress);
        } else {
            play(currentExhibit);
        }
    }

    public int toGetCurrentPosition(){
        return currentPosition;
    }
    private String  getCurrentAudioPath(){
        return LOCAL_ASSETS_PATH+currentMuseumId+"/"+LOCAL_FILE_TYPE_AUDIO;
    }

    /**播放博物馆讲解*/
    private void playMuseum(MuseumBean museumBean){
        mediaPlayer.reset();
        try {
            String url = "";
            String exURL = museumBean.getAudioUrl();
            String localName = exURL.replaceAll("/", "_");
            String localUrl = getCurrentAudioPath()+ "/" + localName;
            File file = new File(localUrl);
            if (file.exists()) {
                url = localUrl;
            } else {
                url = BASE_URL + exURL;
            }
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch(Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    /**添加讲解过的记录*/
    private void addRecord(ExhibitBean bean) {
        if(recordExhibitList.contains(bean)){return;}
        recordExhibitList.add(bean);
    }

    /**停止播放*/
    private void stop() {
        if(mediaPlayer!=null&&isPlaying){
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    public void toSetPlayList(List<ExhibitBean> list){
        this.playExhibitList=list;
    }
    public List<ExhibitBean> toGetPlayList(){
        return playExhibitList;
    }

    class MyHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_UPDATE_PROGRESS:
                    doUpdateProgress();
                    break;
                /*case MSG_WHAT_UPDATE_DURATION:
                    doUpdateDuration();
                    break;*/
            }
        }
    }

    private void doUpdateDuration(){
        if(mediaPlayer==null||!isSendProgress){ return;}
        duration=mediaPlayer.getDuration();
        Intent intent=new Intent();
        intent.setAction(INTENT_EXHIBIT_DURATION);
        intent.putExtra(INTENT_EXHIBIT_DURATION,duration);
        sendBroadcast(intent);
    }

    private void doUpdateProgress() {
        if(mediaPlayer==null||!isSendProgress){ return;}
        currentPosition = mediaPlayer.getCurrentPosition();
        duration=mediaPlayer.getDuration();
        Intent intent=new Intent();
        intent.setAction(INTENT_EXHIBIT_PROGRESS);
        intent.putExtra(INTENT_EXHIBIT_PROGRESS, currentPosition);
        intent.putExtra(INTENT_EXHIBIT_DURATION,duration);
        sendBroadcast(intent);
        handler.sendEmptyMessageDelayed(MSG_WHAT_UPDATE_PROGRESS,800);
    }

    public class MediaServiceBinder extends Binder {

        public void stopPlay() {
            stop();
        }
        public boolean isPlaying() {
            return mediaPlayer != null && isPlaying;
        }
        /**暂停后开始播放*/
        public void continuePlay(){
            toContinuePlay();
        }
        /**暂停*/
        public boolean pause(){
            return toPause();
        }
        /**获取当前播放时长*/
        public int getCurrentPosition(){
            return toGetCurrentPosition();
        }
        /**播放时长跳至参数中时间*/
        public void seekTo(int progress) {
            toSeekTo(progress);
        }
        /**设置播放列表*/
        public void setPlayList(List<ExhibitBean> list) {
            toSetPlayList(list);
        }
        /**获得播放列表*/
        public List<ExhibitBean>  getPlayList() {
            return toGetPlayList();
        }

        /**通知切换展品*/
        public void notifyExhibitChange(ExhibitBean exhibitBean) {
            setCurrentExhibit(exhibitBean);
            play(exhibitBean);
        }

        public int getPlayMode() {
            return toGetPlayMode();
        }

        public void setPlayMode(int mode) {
            toSetPlayMode(mode);
        }

        public boolean startPlay(){
            return toStartPlay();
        }

        public int getDuration(){
            return toGetDuration();
        }

        public ExhibitBean getCurrentExhibit(){
            return currentExhibit;
        }

        public boolean next(){
            // TODO: 2016/1/6  
            return false;
        }
    }
    /**
     * 用于下载音频类
     */
    class DownloadAudioTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            String audioUrl=params[0];
            String audioName=params[1];
            String saveDir=getCurrentAudioPath();
            try {
                MyHttpUtil.downLoadFromUrl(audioUrl, audioName,saveDir );
            } catch (IOException e) {
                ExceptionUtil.handleException(e);
            }
            return saveDir+audioName;
        }
        @Override
        protected void onPostExecute(String savePath) {
            play(currentExhibit);
        }
    }

}
