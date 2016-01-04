package com.systekcn.guide.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ListView;

import com.systekcn.guide.R;
import com.systekcn.guide.adapter.ExhibitAdapter;
import com.systekcn.guide.biz.DataBiz;
import com.systekcn.guide.entity.ExhibitBean;

import java.util.ArrayList;
import java.util.List;

public class CollectionActivity extends BaseActivity {


    private ListView collectionListView;
    private List<ExhibitBean> collectionExhibitList;
    private ExhibitAdapter exhibitAdapter;
    private MyHandler handler;
    private String museumId;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_collection);
        initView();
        initData();
    }

    private void initData() {
        museumId =getIntent().getStringExtra(INTENT_MUSEUM_ID);
        new Thread(){
            @Override
            public void run() {
                if(TextUtils.isEmpty(museumId)){return;}
                collectionExhibitList=DataBiz.getCollectionExhibitListFromDB(museumId);
                if(collectionExhibitList==null){return;}
                if(collectionExhibitList.size()==0){
                    showToast("您暂未收藏展品。");
                }else{
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
                }
            }
        }.start();
    }

    private void initView() {
        collectionListView=(ListView)findViewById(R.id.collectionListView);
        collectionExhibitList=new ArrayList<>();
        exhibitAdapter=new ExhibitAdapter(this,collectionExhibitList);
        collectionListView.setAdapter(exhibitAdapter);
        handler=new MyHandler();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
                exhibitAdapter.updateData(collectionExhibitList);
            }
        }
    }

}
