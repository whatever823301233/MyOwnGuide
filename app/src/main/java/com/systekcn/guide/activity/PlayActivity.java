package com.systekcn.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.MultiAngleImgAdapter;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.MultiAngleImg;
import com.systekcn.guide.lyric.LyricAdapter;
import com.systekcn.guide.lyric.LyricDownloadManager;
import com.systekcn.guide.lyric.LyricLoadHelper;
import com.systekcn.guide.lyric.LyricSentence;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.ImageLoaderUtil;
import com.systekcn.guide.utils.LogUtil;
import com.systekcn.guide.utils.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayActivity extends BaseActivity {

    private ListView lvLyric;
    private ImageView imgExhibitIcon;
    private ImageView imgWordCtrl;
    private MyHandler handler;
    private ArrayList<MultiAngleImg> multiAngleImgs;
    private MultiAngleImgAdapter mulTiAngleImgAdapter;
    private String currentLyricUrl;/*当前歌词路径*/
    private LyricLoadHelper mLyricLoadHelper;
    private LyricAdapter mLyricAdapter;
    private String currentMuseumId;
    private ExhibitBean currentExhibit;
    private ArrayList<Integer> imgsTimeList;
    private ImageView ivPlayCtrl;
    private int mScreenWidth;
    private TextView tvPlayTime;
    private boolean hasMultiImg;
    private boolean mIsLyricDownloading;
    private SeekBar seekBarProgress;
    private int currentProgress;
    private int currentDuration;
    private PlayStateReceiver playStateReceiver;
    private RecyclerView recycleMultiAngle;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_play);
        handler =new MyHandler();
        initView();
        initData();
        refreshView();
        registerReceiver();
    }

    private void registerReceiver() {
        playStateReceiver=new PlayStateReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(INTENT_EXHIBIT_PROGRESS);
        filter.addAction(INTENT_EXHIBIT_DURATION);
        filter.addAction(INTENT_EXHIBIT_CHANG);
        registerReceiver(playStateReceiver,filter);

    }

    private void refreshView() {
        initIcon();
        initMultiImgs();
        loadLyricByHand();
    }


    private void initIcon() {
        if(currentExhibit==null){return;}
        String iconUrl=currentExhibit.getIconurl();
        String imageName = Tools.changePathToName(iconUrl);
        String imgLocalUrl = LOCAL_ASSETS_PATH+currentMuseumId + "/" + LOCAL_FILE_TYPE_IMAGE+"/"+imageName;
        File file = new File(imgLocalUrl);
        // 判断sdcard上有没有图片
        if (file.exists()) {
            // 显示sdcard
            ImageLoaderUtil.displaySdcardImage(this, imgLocalUrl, imgExhibitIcon);
        } else {
            iconUrl = BASE_URL + iconUrl;
            ImageLoaderUtil.displayNetworkImage(this, iconUrl, imgExhibitIcon);
        }
    }



    /*初始化界面控件*/
    private void initView() {
        lvLyric=(ListView)findViewById(R.id.lvLyric);
        imgExhibitIcon=(ImageView)findViewById(R.id.imgExhibitIcon);
        seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);
        tvPlayTime=(TextView)findViewById(R.id.tvPlayTime);
        recycleMultiAngle = (RecyclerView) findViewById(R.id.recycleMultiAngle);
        ivPlayCtrl=(ImageView)findViewById(R.id.ivPlayCtrl);
        imgWordCtrl=(ImageView)findViewById(R.id.imgWordCtrl);
        multiAngleImgs=new ArrayList<>();
        mulTiAngleImgAdapter=new MultiAngleImgAdapter(this,multiAngleImgs);
        /*设置为横向*/
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleMultiAngle.setLayoutManager(linearLayoutManager);
        recycleMultiAngle.setAdapter(mulTiAngleImgAdapter);

        mLyricLoadHelper = new LyricLoadHelper();
        mLyricAdapter = new LyricAdapter(this);
        mLyricLoadHelper.setLyricListener(mLyricListener);
        lvLyric.setAdapter(mLyricAdapter);
        imgWordCtrl.setOnClickListener(onClickListener);
    }


    View.OnClickListener onClickListener=new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imgWordCtrl:
                    if(lvLyric.getVisibility()!=View.GONE){
                        lvLyric.setVisibility(View.GONE);
                        imgExhibitIcon.setAlpha(1.0f);
                    }else{
                        lvLyric.setVisibility(View.VISIBLE);
                        imgExhibitIcon.setAlpha(0.7f);
                    }
                    break;
            }

        }
    };


    private LyricLoadHelper.LyricListener mLyricListener = new LyricLoadHelper.LyricListener() {

        @Override
        public void onLyricLoaded(List<LyricSentence> lyricSentences, int index) {
            if (lyricSentences != null) {
                //LogUtil.i(TAG, "onLyricLoaded--->歌词句子数目=" + lyricSentences.size() + ",当前句子索引=" + index);
                mLyricAdapter.setLyric(lyricSentences);
                mLyricAdapter.setCurrentSentenceIndex(index);
                mLyricAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onLyricSentenceChanged(int indexOfCurSentence) {
            mLyricAdapter.setCurrentSentenceIndex(indexOfCurSentence);
            mLyricAdapter.notifyDataSetChanged();
            lvLyric.smoothScrollToPositionFromTop(indexOfCurSentence, lvLyric.getHeight() / 2, 500);
        }
    };

    /*加载数据*/
    private void initData() {

        Intent intent=getIntent();
        String exhibitStr=intent.getStringExtra(INTENT_EXHIBIT);
        currentExhibit= JSON.parseObject(exhibitStr, ExhibitBean.class);
        if(currentExhibit==null){return;}
        currentMuseumId=currentExhibit.getMuseumId();
        //*加载歌词
        currentLyricUrl = currentExhibit.getTexturl();
        handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
    }

    private void loadLyricByHand() {
        long time =System.currentTimeMillis();
        try{
            String name = currentLyricUrl.replaceAll("/", "_");
            // 取得歌曲同目录下的歌词文件绝对路径
            String lyricFilePath = LOCAL_ASSETS_PATH+currentMuseumId+"/"+LOCAL_FILE_TYPE_LYRIC+"/"+ name;
            File lyricFile = new File(lyricFilePath);
            if (lyricFile.exists()) {
                // 本地有歌词，直接读取
                mLyricLoadHelper.loadLyric(lyricFilePath);
            } else {
                mIsLyricDownloading = true;
                // 尝试网络获取歌词
                LogUtil.i("ZHANG", "loadLyric()--->本地无歌词，尝试从网络获取");
                new LyricDownloadAsyncTask().execute(currentLyricUrl);
            }
            long costTime=System.currentTimeMillis()-time;
            LogUtil.i("ZHANG", "GuideActivity_loadLyricByHand耗时" + costTime);
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }


    private class LyricDownloadAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            LyricDownloadManager mLyricDownloadManager = new LyricDownloadManager(PlayActivity.this);
            // 从网络获取歌词，然后保存到本地
            String lyricFilePath = mLyricDownloadManager.searchLyricFromWeb(params[0],
                    LOCAL_ASSETS_PATH + currentMuseumId + "/" + LOCAL_FILE_TYPE_LYRIC);
            // 返回本地歌词路径
            mIsLyricDownloading = false;
            return lyricFilePath;
        }

        @Override
        protected void onPostExecute(String lyricSavePath) {
            // Log.i(TAG, "网络获取歌词完毕，歌词保存路径:" + result);
            // 读取保存到本地的歌曲
            mLyricLoadHelper.loadLyric(lyricSavePath);
        }
    }


    //加载多角度图片
    private void initMultiImgs() {
        //当前展品为空，返回
        if(currentExhibit==null){return;}
        String imgStr=currentExhibit.getImgsurl();
        // 没有多角度图片，返回
        if(imgStr==null||imgStr.equals("")){return;}
        imgsTimeList=new ArrayList<>();
        //获取多角度图片地址数组
        String[] imgs = imgStr.split(",");
        if (imgs[0].equals("") && imgs.length != 0) {
            recycleMultiAngle.setVisibility(View.GONE);
            return;}

        for (String singleUrl : imgs) {
            String[] nameTime = singleUrl.split("\\*");
            MultiAngleImg multiAngleImg=new MultiAngleImg();
            int time=Integer.valueOf(nameTime[1]);
            multiAngleImg.setTime(time);
            multiAngleImg.setUrl(nameTime[0]);
            imgsTimeList.add(time);
            multiAngleImgs.add(multiAngleImg);
        }
        mulTiAngleImgAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(playStateReceiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            /**当信息类型为更换歌词背景*/
            if (msg.what == MSG_WHAT_CHANGE_ICON) {
                /**若信息类型为展品切换，刷新数据，刷新界面*/
            } else if (msg.what == MSG_WHAT_UPDATE_PROGRESS) {
                seekBarProgress.setProgress(currentProgress);
                mLyricLoadHelper.notifyTime(currentProgress);
            }else if(msg.what==MSG_WHAT_UPDATE_DURATION){
                seekBarProgress.setMax(currentDuration);
            }else if(msg.what==MSG_WHAT_PAUSE_MUSIC){
                /**暂停播放*/
            }else if(msg.what==MSG_WHAT_CONTINUE_MUSIC){
                //* *继续播放
            }
        }
    }

    class PlayStateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(INTENT_EXHIBIT_PROGRESS)){
                currentProgress =intent.getIntExtra(INTENT_EXHIBIT_PROGRESS,0);
                handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);
            }else if(action.equals(INTENT_EXHIBIT_DURATION)){
                currentDuration=intent.getIntExtra(INTENT_EXHIBIT_DURATION,0);
                handler.sendEmptyMessage(MSG_WHAT_UPDATE_DURATION);
            }
        }
    }


}
