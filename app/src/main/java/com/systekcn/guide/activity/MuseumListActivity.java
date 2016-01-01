package com.systekcn.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.systekcn.guide.IConstants;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.MuseumAdapter;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.biz.GetDataBiz;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class MuseumListActivity extends BaseActivity implements IConstants {


    private ListView museumListView;
    /*当前所在城市*/
    private String city;
    private List<MuseumBean> museumList;
    private MuseumAdapter adapter;
    private Handler handler;
    MyApplication application;
    private Drawer result;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_museum_list);
        handler=new MyHandler();
        application=MyApplication.get();
        initView();
        initData();
        adddListener();
        initDrawer();
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
        //result.setSelection(5, false);
    }

    private void adddListener() {
        museumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MuseumBean museumBean=museumList.get(position-1);
                application.currentMuseum=museumBean;
                Intent intent =new Intent(MuseumListActivity.this,MuseumHomeActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                GetDataBiz biz= (GetDataBiz) BizFactory.getDataBiz();
                museumList= (List<MuseumBean>) biz.getAllBeansFromNet(URL_TYPE_GET_MUSEUM_LIST, "");
                if(museumList!=null){
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                    LogUtil.i("ZHANG","数据获取成功");
                }else{
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_FAIL);
                    LogUtil.i("ZHANG", "数据获取失败");
                }
            }
        }.start();

    }

    private void initView() {
        museumListView=(ListView)findViewById(R.id.museumListView);
        View header=getLayoutInflater().inflate(R.layout.header_museum_list,null);
        museumListView.addHeaderView(header);
        museumList=new ArrayList<>();
        adapter=new MuseumAdapter(this,museumList);
        museumListView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_UPDATE_DATA_SUCCESS) {
                if(museumList==null||museumList.size()==0){return;}
                adapter.updateData(museumList);
            }else if(msg.what==MSG_WHAT_UPDATE_DATA_FAIL){
                showToast("数据获取失败，请检查网络...");
            }
        }
    }




}
