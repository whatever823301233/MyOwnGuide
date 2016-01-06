package com.systekcn.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.ExhibitAdapter;
import com.systekcn.guide.biz.DataBiz;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class TopicActivity extends BaseActivity {

    private Drawer result;
    private String currentMuseumId;
    private Handler handler;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_topic);
        Intent intent=getIntent();
        currentMuseumId =intent.getStringExtra(INTENT_MUSEUM_ID);
        init();
    }
    private TextView  tv_collection_dongwei,tv_collection_beiqi,
            tv_collection_beiwei, tv_collection_xizhou, tv_collection_shang,
            tv_collection_sui, tv_collection_tangdai, tv_collection_handai,
            tv_collection_chunqiu, tv_collection_zhanguo, tv_collection_qing,
            tv_collection_shixiang, tv_collection_qingtong,tv_collection_tongqi,
            tv_collection_shike;
    /**展品总列表*/
    private List<ExhibitBean> totalExhibitList;
    /**单个标签搜索结果列表*/
    private  List<ExhibitBean> checkExhibitList;
    /**展示列表*/
    private  List<ExhibitBean> disPlayCheckExhibitList;
    /**已选标签布局*/
    private LinearLayout ll_collection_has_choose;
    /**展示集listview*/
    private ListView lv_collection_listView;
    /**适配器*/
    private ExhibitAdapter exhibitAdapter;
    /**图片是否显示标签*/
    private boolean isPictureShow=true;
    /**已选标签控件集合*/
    private List<TextView> tvList;
    /**右上角导览按钮*/
    //private TextView iv_titleBar_toGuide;
    /**侧边栏按钮*/
    private ImageView titleBarDrawer;

    private void init() {
        initViews();
        initDrawer();
        addListener();
        initData();
    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                if(TextUtils.isEmpty(currentMuseumId)){return;}
                totalExhibitList=DataBiz.getLocalListById(ExhibitBean.class, currentMuseumId);
                if(totalExhibitList!=null&&totalExhibitList.size()>0){
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                }
            }
        }.start();

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

        titleBarDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (result.isDrawerOpen()) {
                    result.closeDrawer();
                } else {
                    result.openDrawer();
                }
            }
        });

        lv_collection_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExhibitBean exhibitBean= exhibitAdapter.getItem(position);
                String str= JSON.toJSONString(exhibitBean);
                Intent intent =new Intent();
                intent.setAction(INTENT_EXHIBIT);
                intent.putExtra(INTENT_EXHIBIT, str);
                sendBroadcast(intent);
                Intent intent1 =new Intent(TopicActivity.this,PlayActivity.class);
                intent1.putExtra(INTENT_EXHIBIT,str);
                startActivity(intent1);
                finish();
            }
        });

        /*iv_titleBar_toGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                *//**点击导览时，判断，如果当前筛选列表不为为空，向专题列表赋值，启动导览界面*//*
                if (disPlayCheckExhibitList == null || disPlayCheckExhibitList.size() <= 0) {
                    application.topicExhibitBeanList = new ArrayList<>();
                    application.currentExhibitBean = application.totalExhibitBeanList.get(0);
                } else {
                    application.topicExhibitBeanList = disPlayCheckExhibitList;
                    application.currentExhibitBean = application.topicExhibitBeanList.get(0);
                }
                application.isTopicOpen = true;
                Intent intent = new Intent(TopicActivity.this, GuideActivity.class);
                startActivity(intent);
                finish();
            }
        });*/

        lv_collection_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                isPictureShow = false;
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        isPictureShow = true;
                        LogUtil.i("OnScrollListener", "停止滚动:SCROLL_STATE_IDLE" + isPictureShow);
                        //onListViewScrollListener.onScroll(isPictureShow);
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        LogUtil.i("OnScrollListener", "手指离开屏幕，屏幕惯性滚动:SCROLL_STATE_FLING");
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        LogUtil.i("OnScrollListener", "屏幕手指正在滚动：SCROLL_STATE_TOUCH_SCROLL");
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        setManyBtnListener();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(result.isDrawerOpen()){
                result.closeDrawer();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private View.OnClickListener labelClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView tv= (TextView) v;
            TextView textView=new TextView(TopicActivity.this);
            String label= (String) tv.getText();
            textView.setText(label);
            textView.setTextSize(12);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(deleteLabelClickListener);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(15);
            ll_collection_has_choose.addView(textView, params);
            tv.setVisibility(View.GONE);
            try{
                checkExhibitList=getList(label);
                if(checkExhibitList!=null&&checkExhibitList.size()>0){
                    if(disPlayCheckExhibitList!=null&&disPlayCheckExhibitList.size()>0){
                        disPlayCheckExhibitList.removeAll(checkExhibitList);
                        disPlayCheckExhibitList.addAll(checkExhibitList);
                    }else{
                        disPlayCheckExhibitList=checkExhibitList;
                    }
                }else{
                    disPlayCheckExhibitList=new ArrayList<>();
                }
                exhibitAdapter.updateData(disPlayCheckExhibitList);
            }catch (Exception e){
                ExceptionUtil.handleException(e);
            }

        }
    };

    private View.OnClickListener deleteLabelClickListener=new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            TextView tv=(TextView)v;
            CharSequence charsTop=tv.getText();
            ll_collection_has_choose.removeView(v);
            CharSequence charDown =null;
            for(int i=0;i<tvList.size();i++){
                charDown=tvList.get(i).getText();
                if(charDown.equals(charsTop)){
                    tvList.get(i).setVisibility(View.VISIBLE);
                }
            }
            if(ll_collection_has_choose.getChildCount()>0){
                disPlayCheckExhibitList.clear();
                for(int i=0;i<ll_collection_has_choose.getChildCount();i++){
                    TextView tvLabel= (TextView) ll_collection_has_choose.getChildAt(i);
                    String text= (String) tvLabel.getText();
                    List<ExhibitBean> list=getList(text);
                    if(list!=null&&list.size()>0){
                        disPlayCheckExhibitList.removeAll(list);
                        disPlayCheckExhibitList.addAll(list);
                    }
                }
            }else{
                disPlayCheckExhibitList=new ArrayList<>();
                //disPlayCheckExhibitList=totalExhibitList;
                showToast("抱歉，没有符合您筛选条件的展品！");
            }
            /*List<ExhibitBean> removeList=getList((String)charsTop);
            if(removeList!=null&&removeList.size()>0){
                if(disPlayCheckExhibitList!=null&&removeList.size()>0){
                    disPlayCheckExhibitList.removeAll(removeList);
                }
                if(disPlayCheckExhibitList==null||disPlayCheckExhibitList.size()==0){
                    disPlayCheckExhibitList=totalExhibitList;
                }
            }*/
            exhibitAdapter.updateData(disPlayCheckExhibitList);
        }
    };

    public List<ExhibitBean> getList(String label){
        List<ExhibitBean> list = null;
        DbUtils db=DbUtils.create(this);
        try {
            list=  db.findAll(Selector.from(ExhibitBean.class).where("labels","like","%"+label+"%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
        return list;
    }

    private void initViews() {
        tvList=new ArrayList<>();
        titleBarDrawer =(ImageView)findViewById(R.id.titleBarDrawer);

        ll_collection_has_choose=(LinearLayout)findViewById(R.id.ll_collection_has_choose);
        lv_collection_listView=(ListView)findViewById(R.id.lv_collection_listView);
        //iv_titleBar_toGuide =(TextView)findViewById(R.id.iv_titlebar_toGuide);

        tv_collection_dongwei=(TextView)findViewById(R.id.tv_collection_dongwei);
        tv_collection_beiqi=(TextView)findViewById(R.id.tv_collection_beiqi);
        tv_collection_beiwei=(TextView)findViewById(R.id.tv_collection_beiwei);
        tv_collection_xizhou=(TextView)findViewById(R.id.tv_collection_xizhou);
        tv_collection_shang=(TextView)findViewById(R.id.tv_collection_shang);
        tv_collection_sui=(TextView)findViewById(R.id.tv_collection_sui);
        tv_collection_tangdai=(TextView)findViewById(R.id.tv_collection_tangdai);
        tv_collection_handai=(TextView)findViewById(R.id.tv_collection_handai);
        tv_collection_chunqiu=(TextView)findViewById(R.id.tv_collection_chunqiu);
        tv_collection_zhanguo=(TextView)findViewById(R.id.tv_collection_zhanguo);
        tv_collection_qing=(TextView)findViewById(R.id.tv_collection_qing);

        tv_collection_shixiang=(TextView)findViewById(R.id.tv_collection_shixiang);
        tv_collection_qingtong=(TextView)findViewById(R.id.tv_collection_qingtong);
        tv_collection_tongqi=(TextView)findViewById(R.id.tv_collection_tongqi);
        tv_collection_shike=(TextView)findViewById(R.id.tv_collection_shike);

        tvList.add(tv_collection_dongwei);
        tvList.add(tv_collection_beiqi);
        tvList.add(tv_collection_xizhou);
        tvList.add(tv_collection_shang);
        tvList.add(tv_collection_sui);
        tvList.add(tv_collection_tangdai);
        tvList.add(tv_collection_handai);
        tvList.add(tv_collection_chunqiu);
        tvList.add(tv_collection_zhanguo);
        tvList.add(tv_collection_qing);
        tvList.add(tv_collection_shixiang);
        tvList.add(tv_collection_qingtong);
        tvList.add(tv_collection_tongqi);
        tvList.add(tv_collection_shike);
        totalExhibitList=new ArrayList<>();
        exhibitAdapter=new ExhibitAdapter(this, totalExhibitList);
        lv_collection_listView.setAdapter(exhibitAdapter);
    }

    private void setManyBtnListener() {
        tv_collection_dongwei.setOnClickListener(labelClickListener);
        tv_collection_beiqi.setOnClickListener(labelClickListener);
        tv_collection_beiwei.setOnClickListener(labelClickListener);
        tv_collection_xizhou.setOnClickListener(labelClickListener);
        tv_collection_shang.setOnClickListener(labelClickListener);
        tv_collection_sui.setOnClickListener(labelClickListener);
        tv_collection_tangdai.setOnClickListener(labelClickListener);
        tv_collection_handai.setOnClickListener(labelClickListener);
        tv_collection_chunqiu.setOnClickListener(labelClickListener);
        tv_collection_zhanguo.setOnClickListener(labelClickListener);
        tv_collection_qing.setOnClickListener(labelClickListener);
        tv_collection_shixiang.setOnClickListener(labelClickListener);
        tv_collection_qingtong.setOnClickListener(labelClickListener);
        tv_collection_tongqi.setOnClickListener(labelClickListener);
        tv_collection_shike.setOnClickListener(labelClickListener);
        handler=new MyHandler();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_UPDATE_DATA_SUCCESS) {
                refreshView();
            }
        }
    }

    private void refreshView() {
        exhibitAdapter.updateData(totalExhibitList);// TODO: 2016/1/3
    }

}
