package com.systekcn.guide.activity;

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

import com.systekcn.guide.IConstants;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.MultiAngleImgAdapter;
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

public class PlayActivity extends BaseActivity implements IConstants{

    private ListView lvLyric;
    private ImageView imgExhibitIcon;
    private SeekBar seekBarProgress;
    private TextView tvPlayTime;
    private RecyclerView recycleMultiAngle;
    private ImageView ivPlayCtrl;
    private ImageView imgWordCtrl;
    private final int MSG_WHAT_CHANGE_ICON=2;
    private final int MSG_WHAT_CHANGE_EXHIBIT=3;
    private final int MSG_WHAT_PAUSE_MUSIC=4;
    private final int MSG_WHAT_CONTINUE_MUSIC=5;
    private MyHandler handler;
    private ArrayList<MultiAngleImg> multiAngleImgs;
    private ArrayList<Integer> imgsTimeList;
    private boolean hasMultiImg;
    private MultiAngleImgAdapter mulTiAngleImgAdapter;
    private int mScreenWidth;
    /*当前歌词路径*/
    private String currentLyricUrl;
    private LyricLoadHelper mLyricLoadHelper;
    private LyricAdapter mLyricAdapter;
    private boolean mIsLyricDownloading;
    private LyricDownloadManager mLyricDownloadManager;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_play);
        handler =new MyHandler();
        initView();
        initData();
        refreshView();
    }

    private void refreshView() {
        initIcon();
        initMultiImgs();
        loadLyricByHand();
    }


    private void initIcon() {
        if(application.currentExhibitBean==null){return;}
        String iconUrl=application.currentExhibitBean.getIconurl();
        String imageName = Tools.changePathToName(iconUrl);
        String imgLocalUrl = LOCAL_ASSETS_PATH+application.getCurrentMuseumId() + "/" + LOCAL_FILE_TYPE_IMAGE+"/"+imageName;
        File file = new File(imgLocalUrl);
        // 判断sdcard上有没有图片
        if (file.exists()) {
            // 显示sdcard
            ImageLoaderUtil.displaySdcardImage(this, imgLocalUrl, imgExhibitIcon);
        } else {
            // 服务器上存的imageUrl有域名如http://www.systek.com.cn/1.png
            iconUrl = BASEURL+ iconUrl;
            ImageLoaderUtil.displayNetworkImage(this, iconUrl, imgExhibitIcon);
        }}



    /*初始化界面控件*/
    private void initView() {
        lvLyric=(ListView)findViewById(R.id.lvLyric);
        imgExhibitIcon=(ImageView)findViewById(R.id.imgExhibitIcon);
        seekBarProgress=(SeekBar)findViewById(R.id.seekBarProgress);
        tvPlayTime=(TextView)findViewById(R.id.tvPlayTime);
        recycleMultiAngle=(RecyclerView)findViewById(R.id.recycleMultiAngle);
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


        imgWordCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lvLyric.getVisibility()!=View.GONE){
                    lvLyric.setVisibility(View.GONE);
                    imgExhibitIcon.setAlpha(1.0f);
                }else{
                    lvLyric.setVisibility(View.VISIBLE);
                    imgExhibitIcon.setAlpha(0.7f);
                }

            }
        });
    }


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
        if(application.currentExhibitBean==null){return;}
        /**加载歌词*/
        currentLyricUrl = application.currentExhibitBean.getTexturl();
        handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
    }

    private void loadLyricByHand() {
        long time =System.currentTimeMillis();
        try{
            String name = currentLyricUrl.replaceAll("/", "_");
            // 取得歌曲同目录下的歌词文件绝对路径
            String lyricFilePath = application.getCurrentLyricDir() + name;
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
            mLyricDownloadManager = new LyricDownloadManager(PlayActivity.this);
            // 从网络获取歌词，然后保存到本地
            String lyricFilePath = mLyricDownloadManager.searchLyricFromWeb(params[0],application.getCurrentLyricDir());
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


    /*加载多角度图片*/
    private void initMultiImgs() {
        long startT=System.currentTimeMillis();
        /*当前展品为空，返回*/
        if(application.currentExhibitBean==null){return;}
        String imgStr=application.currentExhibitBean.getImgsurl();
        /*没有多角度图片，返回*/
        if(imgStr==null||imgStr.equals("")){return;}
        imgsTimeList=new ArrayList<>();
        /*获取多角度图片地址数组*/
        String[] imgs = imgStr.split(",");
        if (imgs[0].equals("") && imgs.length != 0) {return;}

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
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            /**当信息类型为更换歌词背景*/
            if (msg.what == MSG_WHAT_CHANGE_ICON) {
                /*String imgPath = (String) msg.obj;
                String currentIconPath=(String)iv_frag_largest_img.getTag();
                String imgLocalPath = application.getCurrentImgDir() + Tools.changePathToName(imgPath);
                *//**若歌词路径不为空，判断图片url,加载图片*//*
                if(currentIconPath!=null&&!imgPath.equals(currentIconPath)&&!imgPath.equals(imgLocalPath)){
                    if (Tools.isFileExist(imgLocalPath)) {
                        iv_frag_largest_img.setTag(imgLocalPath);
                        ImageLoaderUtil.displaySdcardImage(activity, imgLocalPath, iv_frag_largest_img);
                    } else {
                        String httpPath = BASEURL + imgPath;
                        iv_frag_largest_img.setTag(httpPath);
                        ImageLoaderUtil.displayNetworkImage(activity, httpPath, iv_frag_largest_img);
                    }
                }else{
                    currentIconPath=null;
                    imgPath=null;
                }*/
                /**若信息类型为展品切换，刷新数据，刷新界面*/
            } else if (msg.what == MSG_WHAT_CHANGE_EXHIBIT) {
                /**数据初始化好之前显示加载对话框*/
               /* showProgressDialog();
                refreshData();
                refreshView();
                mediaServiceManager.notifyAllDataChange();
                if(progressDialog!=null&&progressDialog.isShowing()){
                    progressDialog.dismiss();
                }*/
            }else if(msg.what==MSG_WHAT_PAUSE_MUSIC){
                /**暂停播放*//*
                if (mediaServiceManager != null && mediaServiceManager.isPlaying()) {
                    music_play_and_ctrl.setBackgroundResource(R.mipmap.iv_media_stop);
                    mediaServiceManager.pause();
                }*/
            }else if(msg.what==MSG_WHAT_CONTINUE_MUSIC){
               /* *//**继续播放*//*
                music_play_and_ctrl.setBackgroundResource(R.mipmap.iv_media_play);
                mediaServiceManager.toContinue();*/
            }
        }
    }

}
