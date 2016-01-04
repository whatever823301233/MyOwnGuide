package com.systekcn.guide.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.systekcn.guide.R;
import com.systekcn.guide.fragment.ExhibitListFragment;
import com.systekcn.guide.fragment.MapFragment;
import com.systekcn.guide.manager.BluetoothManager;
import com.systekcn.guide.manager.MediaServiceManager;

public class ListAndMapActivity extends BaseActivity {

    private Drawer result;
    private RadioButton radioButtonList;
    private RadioButton radioButtonMap;
    private RadioGroup radioGroupTitle;
    private ExhibitListFragment exhibitListFragment;
    private View view;
    private TextView aaa;
    private MapFragment mapFragment;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_list_and_map);
        view =getLayoutInflater().inflate(R.layout.layout_drawer,null);
        aaa=(TextView)view.findViewById(R.id.aaa);
        aaa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("aaaaaaaaaaaaaaaaaaaa");
            }
        });
        initBlueTooth();
        initDrawer();
        initView();
        addListener();
        setDefaultFragment();
        if(application.mServiceManager==null){
            application.mServiceManager=new MediaServiceManager(getApplicationContext());
        }
        application.mServiceManager.connectService();
    }

    private void initBlueTooth() {
        BluetoothManager bluetoothManager = BluetoothManager.newInstance(this);
        bluetoothManager.initBeaconSearcher();
    }

    private void initDrawer() {
        result = new DrawerBuilder()
                .withActivity(this)
                .withFullscreen(true)
                .withCustomView(view)
                .withHeader(R.layout.header)
                        //.inflateMenu(R.menu.example_menu)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            showToast("item" + position + "被点击了");
                        }
                        return false;
                    }
                }).build();
        // set the selection to the item with the identifier 5
        result.setSelection(5, false);
    }

    private void addListener() {
        radioGroupTitle.setOnCheckedChangeListener(radioButtonCheckListener);
    }

    private void initView() {
        radioButtonList=(RadioButton)findViewById(R.id.radioButtonList);
        radioButtonMap=(RadioButton)findViewById(R.id.radioButtonMap);
        radioGroupTitle=(RadioGroup)findViewById(R.id.radioGroupTitle);
    }

    private void setDefaultFragment() {
        String flag=getIntent().getStringExtra(INTENT_FLAG_GUIDE_MAP);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        exhibitListFragment = ExhibitListFragment.newInstance();
        mapFragment = new MapFragment();
        if(flag.equals(INTENT_FLAG_GUIDE)){
            transaction.replace(R.id.llExhibitListContent, exhibitListFragment);
        }else{
            transaction.replace(R.id.llExhibitListContent, mapFragment);
        }
        transaction.commit();
    }


    private RadioGroup.OnCheckedChangeListener radioButtonCheckListener=new RadioGroup.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            FragmentManager fm = getFragmentManager();
            // 开启Fragment事务
            FragmentTransaction transaction = fm.beginTransaction();
            switch(checkedId){
                case R.id.radioButtonList:
                    if (exhibitListFragment == null)
                    {
                        exhibitListFragment = ExhibitListFragment.newInstance();
                    }
                    // 使用当前Fragment的布局替代id_content的控件
                    transaction.replace(R.id.llExhibitListContent, exhibitListFragment);
                    break;
                case R.id.radioButtonMap:
                    if (mapFragment == null)
                    {
                        mapFragment = new MapFragment();
                    }
                    transaction.replace(R.id.llExhibitListContent, mapFragment);
                    break;
            }
            // 事务提交
            transaction.commit();
        }
    };

}
