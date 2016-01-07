package com.systekcn.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.MuseumAdapter;
import com.systekcn.guide.biz.DataBiz;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.LogUtil;
import com.systekcn.guide.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class MuseumListActivity extends BaseActivity {

    private boolean isDataShow;
    private ListView museumListView;
    /*当前所在城市*/
    private String city;
    private List<MuseumBean> museumList;
    private MuseumAdapter adapter;
    private Handler handler;
    private Drawer drawer;
    private Receiver receiver;
    private TextView titleBarTopic;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        ViewUtils.setStateBarColor(this, R.color.md_red_400);
        setContentView(R.layout.activity_museum_list);
        handler=new MyHandler();
        initView();
        initData();
        addListener();
        initDrawer();
        addReceiver();
    }

    private void addReceiver() {
        receiver=new Receiver();
        IntentFilter filter=new IntentFilter(ACTION_NET_IS_COMMING);
        filter.addAction(ACTION_NET_IS_OUT);
        registerReceiver(receiver,filter);
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
                        Intent intent=new Intent(MuseumListActivity.this,targetClass);
                        startActivity(intent);
                        return false;
                    }
                }).build();
    }

    private void addListener() {
        museumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MuseumBean museumBean = museumList.get(position - 1);
                Intent intent = new Intent(MuseumListActivity.this, MuseumHomeActivity.class);
                String museumStr=JSON.toJSONString(museumBean);
                intent.putExtra(INTENT_MUSEUM,museumStr);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initData() {
        Intent intent=getIntent();
        city=intent.getStringExtra(INTENT_CITY);
        if(!TextUtils.isEmpty(city)){
            titleBarTopic.setText(city);
        }else{
            titleBarTopic.setText("北京市");
        }
        new Thread(){
            @Override
            public void run() {
                try{
                    if(application.currentNetworkType!=INTERNET_TYPE_NONE){
                        museumList=DataBiz.getEntityListFromNet(MuseumBean.class,URL_MUSEUM_LIST);
                    }
                    if(museumList!=null&&museumList.size()>0){
                        LogUtil.i("ZHANG", "数据获取成功");
                        boolean isSaveTrue=DataBiz.deleteSQLiteDataFromClass(MuseumBean.class);
                        LogUtil.i("ZHANG","数据删除"+isSaveTrue);
                        boolean isSaveTrue2=DataBiz.saveListToSQLite(museumList);
                        LogUtil.i("ZHANG","数据保存"+isSaveTrue2);
                    }else{
                        museumList=DataBiz.getEntityListLocal(MuseumBean.class);
                    }
                }catch (Exception e){
                    ExceptionUtil.handleException(e);
                }finally {
                    if(museumList==null||museumList.size()==0){
                        onDataError();
                    }else{
                        handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                    }
                }
            }
        }.start();
    }

    private void initView() {
        titleBarTopic=(TextView)findViewById(R.id.titleBarTopic);
        museumListView=(ListView)findViewById(R.id.museumListView);
        View header=getLayoutInflater().inflate(R.layout.header_museum_list,null);
        museumListView.addHeaderView(header,null,false);
        museumList=new ArrayList<>();
        adapter=new MuseumAdapter(this,museumList);
        museumListView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_UPDATE_DATA_SUCCESS) {
                if(museumList==null||museumList.size()==0){return;}
                adapter.updateData(museumList);
                isDataShow=true;
            }else if(msg.what==MSG_WHAT_UPDATE_DATA_FAIL){
                showToast("数据获取失败，请检查网络...");
            }else if(msg.what==MSG_WHAT_REFRESH_DATA){
                initData();
            }
        }
    }

    class Receiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(ACTION_NET_IS_COMMING)){
                handler.sendEmptyMessage(MSG_WHAT_REFRESH_DATA);
            }
        }
    }

}
