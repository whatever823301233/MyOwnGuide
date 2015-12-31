package com.systekcn.guide.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.systekcn.guide.IConstants;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.biz.GetDataBiz;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.ImageLoaderUtil;
import com.systekcn.guide.utils.Tools;

import java.util.List;

public class MuseumHomeActivity extends BaseActivity implements IConstants {

    /*抽屉*/
    private Drawer result;
    /*当前博物馆ID*/
    private String currentMuseumId;
    /*当前屏幕宽度*/
    private int screenWidth;
    /*对话框*/
    private AlertDialog progressDialog;
    private ImageView iv_Drawer;
    private TextView title_bar_topic;

    private final int MSG_WHAT_UPDATE_DATA=1;
    private LinearLayout ll_museum_largest_icon;
    private TextView tv_museum_introduce;
    private RelativeLayout rl_guide_home;
    private RelativeLayout rl_topic_home;
    private MyApplication application;
    private Handler handler;


    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_museum_home);
        application=MyApplication.get();
        initDrawer();
        //application.mServiceManager.connectService();/**启动播放服务*/
        currentMuseumId = application.currentMuseum.getId(); //intent.getStringExtra(INTENT_MUSEUM_ID);
        application.currentMuseumId=currentMuseumId;
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        handler=new MyHandler();
        initView();
        addListener();
        /**数据初始化好之前显示加载对话框*/
        showProgressDialog();
        initData();

    }

    private void initDrawer() {
        result = new DrawerBuilder()
                .withActivity(this)
                .withFullscreen(true)
                .withHeader(R.layout.header)
                .inflateMenu(R.menu.example_menu)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            //Toast.makeText(CityChooseActivity.this, ((Nameable) drawerItem).getName().getText(MenuDrawerActivity.this), Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }
                }).build();
        // set the selection to the item with the identifier 5
        result.setSelection(5, false);
    }



    private void addListener() {
        rl_guide_home.setOnClickListener(onClickListener);
        rl_topic_home.setOnClickListener(onClickListener);
        iv_Drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=null;
            switch (v.getId()){
                case R.id.rl_guide_home:
                    intent=new Intent(MuseumHomeActivity.this,ListAndMapActivity.class);
                    startActivity(intent);
                    break;
                case R.id.rl_topic_home:
                    intent=new Intent(MuseumHomeActivity.this,TopicActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private void initData() {

        try{
            new Thread(){
                @Override
                public void run() {
                    if(application.currentMuseum!=null){
                        currentMuseumId=application.currentMuseum.getId();
                        handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA);
                    }
                    GetDataBiz biz= (GetDataBiz) BizFactory.getDataBiz();
                    List<ExhibitBean> exhibitBeans= (List<ExhibitBean>) biz.getAllBeans(MuseumHomeActivity.this,
                            IConstants.URL_TYPE_GET_EXHIBITS_BY_MUSEUM_ID, "deadccf89ef8412a9c8a2628cee28e18");
                    if(exhibitBeans!=null&&exhibitBeans.size()>0){
                        application.totalExhibitBeanList=exhibitBeans;
                    }
                }
            }.start();

        }catch (Exception e) {
            showToast("抱歉，数据获取失败！");
            ExceptionUtil.handleException(e);
        }
    }


    private void showProgressDialog() {
       /* progressDialog = new AlertDialog.Builder(MuseumHomeActivity.this).create();
        progressDialog.show();
        Window window = progressDialog.getWindow();
        window.setContentView(R.layout.dialog_progress);
        TextView dialog_title=(TextView)window.findViewById(R.id.dialog_title);
        dialog_title.setText("正在加载...");*/
    }

    private void initView() {

        iv_Drawer = (ImageView) findViewById(R.id.title_bar_more);
        title_bar_topic = (TextView) findViewById(R.id.title_bar_topic);
        ll_museum_largest_icon = (LinearLayout) findViewById(R.id.ll_museum_largest_icon);
        tv_museum_introduce = (TextView) findViewById(R.id.tv_museum_introduce);
        rl_guide_home = (RelativeLayout) findViewById(R.id.rl_guide_home);
        rl_topic_home = (RelativeLayout) findViewById(R.id.rl_topic_home);


    }


    private void showData(){
        if(application.currentMuseum!=null){
            title_bar_topic.setText(application.currentMuseum.getName());
            /**加载博物馆介绍*/
            tv_museum_introduce.setText("      "+application.currentMuseum.getTextUrl());
            //initAudio();
            /*加载多个Icon图片*/
            String imgStr = application.currentMuseum.getImgUrl();
            String[] imgs = imgStr.split(",");
            for (int i = 0; i < imgs.length; i++) {
                String imgUrl = imgs[i];
                String imgName = imgUrl.replaceAll("/", "_");
                String localPath = LOCAL_ASSETS_PATH + currentMuseumId +"/"+ LOCAL_FILE_TYPE_IMAGE+"/"+imgName;
                boolean flag = Tools.isFileExist(localPath);
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.MATCH_PARENT));
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                ll_museum_largest_icon.addView(iv);
                if (flag) {
                    ImageLoaderUtil.displaySdcardImage(this, localPath, iv);
                } else {
                    if (MyApplication.currentNetworkType != INTERNET_TYPE_NONE) {
                        ImageLoaderUtil.displayNetworkImage(this, BASEURL + imgUrl, iv);
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_UPDATE_DATA) {
                showData();
            }
        }
    }

}
