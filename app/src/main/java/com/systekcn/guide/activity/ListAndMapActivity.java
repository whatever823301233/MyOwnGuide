package com.systekcn.guide.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.systekcn.guide.R;
import com.systekcn.guide.fragment.ExhibitListFragment;

public class ListAndMapActivity extends BaseActivity {

    private Drawer result;
    private RadioButton radioButtonList;
    private RadioButton radioButtonMap;
    private RadioGroup radioGroupTitle;
    private ExhibitListFragment exhibitListFragment;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_list_and_map);
        initDrawer();
        initView();
        addListener();
        setDefaultFragment();
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
        radioGroupTitle.setOnCheckedChangeListener(radioButtonCheckListener);
    }

    private void initView() {
        radioButtonList=(RadioButton)findViewById(R.id.radioButtonList);
        radioButtonMap=(RadioButton)findViewById(R.id.radioButtonMap);
        radioGroupTitle=(RadioGroup)findViewById(R.id.radioGroupTitle);
    }

    private void setDefaultFragment()
    {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        exhibitListFragment = ExhibitListFragment.newInstance();
        transaction.replace(R.id.llExhibitListContent, exhibitListFragment);
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
                    break;
            }
            // 事务提交
            transaction.commit();
        }
    };

}
