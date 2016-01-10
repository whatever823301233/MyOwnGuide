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
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.MultiAngleImgAdapter;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.MultiAngleImg;
import com.systekcn.guide.lyric.LyricAdapter;
import com.systekcn.guide.lyric.LyricDownloadManager;
import com.systekcn.guide.lyric.LyricLoadHelper;
import com.systekcn.guide.lyric.LyricSentence;
import com.systekcn.guide.manager.MediaServiceManager;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.ImageLoaderUtil;
import com.systekcn.guide.utils.LogUtil;
import com.systekcn.guide.utils.Tools;
import com.systekcn.guide.utils.ViewUtils;

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
    private Drawer drawer;
    private String currentExhibitStr;
    private String currentIconUrl;

    private MediaServiceManager mediaServiceManager;


    @Override
    protected void initialize(Bundle savedInstanceState) {
        ViewUtils.setStateBarColor(this, R.color.md_red_400);
        setContentView(R.layout.activity_play);
        handler =new MyHandler();
        mediaServiceManager=MediaServiceManager.getInstance(this);
        initDrawer();
        initView();
        addListener();
        registerReceiver();
        Intent intent=getIntent();
        String exhibitStr=intent.getStringExtra(INTENT_EXHIBIT);
        currentExhibit=mediaServiceManager.getCurrentExhibit();
        if(currentExhibit==null){
            if(currentExhibitStr!=null){
                currentExhibitStr=exhibitStr;
                ExhibitBean exhibitBean=JSON.parseObject(currentExhibitStr, ExhibitBean.class);
                if(exhibitBean!=null&&currentExhibit!=null&&!exhibitBean.equals(currentExhibit)){
                    currentExhibit=exhibitBean;
                }
            }
        }
        initData();
        LogUtil.i("ZHANG","执行了initialize");
    }


    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.i("ZHANG","执行了onNewIntent");
        super.onNewIntent(intent);
        String exhibitStr=intent.getStringExtra(INTENT_EXHIBIT);
        if(TextUtils.isEmpty(exhibitStr)){
            currentExhibit=mediaServiceManager.getCurrentExhibit();
            refreshView();
        }else{
            currentExhibitStr=exhibitStr;
            ExhibitBean exhibitBean=JSON.parseObject(currentExhibitStr, ExhibitBean.class);
            if(exhibitBean!=null&&currentExhibit!=null&&!exhibitBean.equals(currentExhibit)){
                currentExhibit=exhibitBean;
                initData();
            }else{
                refreshView();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.i("ZHANG", "执行了onStart");
    }

    @Override
    protected void onResume() {
        LogUtil.i("ZHANG","执行了onResume");
        super.onResume();
        if(mediaServiceManager.isPlaying()){
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
        }else{
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
        }

    }

    private void initDrawer() {
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withFullscreen(true)
                .withHeader(R.layout.header)
                .inflateMenu(R.menu.drawer_menu)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Class<?>  targetClass=null;
                        switch (position){
                            case 1:
                                targetClass=DownloadActivity.class;
                                break;
                            case 2:
                                targetClass=CollectionActivity.class;
                                break;
                            case 3:
                                targetClass=CityChooseActivity.class;
                                break;
                            case 4:
                                targetClass=MuseumListActivity.class;
                                break;
                            case 5:
                                targetClass=SettingActivity.class;
                                break;
                        }
                        Intent intent=new Intent(PlayActivity.this,targetClass);
                        startActivity(intent);
                        return false;
                    }
                }).build();
    }

    private void addListener() {
        ivPlayCtrl.setOnClickListener(onClickListener);
        seekBarProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mulTiAngleImgAdapter.setOnItemClickListener(new MultiAngleImgAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MultiAngleImg multiAngleImg=multiAngleImgs.get(position);
                String url=multiAngleImg.getUrl();
                initIcon(url);
            }
        });
    }



    private void registerReceiver() {
        playStateReceiver=new PlayStateReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(INTENT_EXHIBIT);
        filter.addAction(INTENT_EXHIBIT_PROGRESS);
        filter.addAction(INTENT_EXHIBIT_DURATION);
        filter.addAction(INTENT_CHANGE_PLAY_PLAY);
        filter.addAction(INTENT_CHANGE_PLAY_STOP);
        registerReceiver(playStateReceiver, filter);

    }

    private void refreshView() {
        LogUtil.i("ZHANG","执行了refreshView");
        initMultiImgs();
        loadLyricByHand();
        initIcon(currentExhibit.getIconurl());
    }

    private void initIcon(String iconUrl) {
        if(currentExhibit==null||TextUtils.isEmpty(iconUrl)){return;}
        if(currentIconUrl!=null&&currentIconUrl.equals(iconUrl)){return;}
        currentIconUrl=iconUrl;
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


    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser){return;}
            Intent intent=new Intent();
            intent.setAction(INTENT_SEEK_BAR_CHANG);
            intent.putExtra(INTENT_SEEK_BAR_CHANG,progress);
            sendBroadcast(intent);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

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
                case R.id.ivPlayCtrl:
                    Intent intent=new Intent();
                    intent.setAction(INTENT_CHANGE_PLAY_STATE);
                    sendBroadcast(intent);
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
        if(currentExhibit==null){return;}
        currentMuseumId=currentExhibit.getMuseumId();
        //*加载歌词
        currentLyricUrl = currentExhibit.getTexturl();
        handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
    }

    private void loadLyricByHand() {
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
                //LogUtil.i("ZHANG", "loadLyric()--->本地无歌词，尝试从网络获取");
                new LyricDownloadAsyncTask().execute(currentLyricUrl);
            }
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
        multiAngleImgs.clear();
        mulTiAngleImgAdapter.updateData(multiAngleImgs);
        //当前展品为空，返回
        if(currentExhibit==null){return;}
        String imgStr=currentExhibit.getImgsurl();
        // 没有多角度图片，返回
        if(imgStr==null||imgStr.equals("")){return;}
        imgsTimeList=new ArrayList<>();
        //获取多角度图片地址数组
        String[] imgs = imgStr.split(",");
        if (imgs[0].equals("") || imgs.length == 0) {
            // TODO: 2016/1/10
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
            switch (msg.what){
                case MSG_WHAT_UPDATE_PROGRESS:
                    seekBarProgress.setMax(currentDuration);
                    seekBarProgress.setProgress(currentProgress);
                    mLyricLoadHelper.notifyTime(currentProgress);
                    break;
                case MSG_WHAT_PAUSE_MUSIC:
                    break;
                case MSG_WHAT_CONTINUE_MUSIC:
                    break;
                case MSG_WHAT_CHANGE_EXHIBIT:
                    refreshView();
                    break;
                case MSG_WHAT_CHANGE_ICON:
                    break;
                case MSG_WHAT_CHANGE_PLAY_START:
                    ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.iv_play_state_open_big));
                    break;
                case MSG_WHAT_CHANGE_PLAY_STOP:
                    ivPlayCtrl.setImageDrawable(getResources().getDrawable(R.drawable.iv_play_state_close_big));
                    break;
            }
        }
    }

    class PlayStateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(INTENT_EXHIBIT_PROGRESS)){
                currentDuration=intent.getIntExtra(INTENT_EXHIBIT_DURATION,0);
                currentProgress =intent.getIntExtra(INTENT_EXHIBIT_PROGRESS,0);
                handler.sendEmptyMessage(MSG_WHAT_UPDATE_PROGRESS);
            }else if(action.equals(INTENT_EXHIBIT)){
                String exhibitStr=intent.getStringExtra(INTENT_EXHIBIT);
                if(TextUtils.isEmpty(exhibitStr)){return;}
                ExhibitBean exhibitBean=JSON.parseObject(exhibitStr, ExhibitBean.class);
                if(currentExhibit.equals(exhibitBean)){return;}
                handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
            }else if(action.equals(INTENT_CHANGE_PLAY_PLAY)){
                handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
            }else if(action.equals(INTENT_CHANGE_PLAY_STOP)){
                handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
            }
        }
    }
}
