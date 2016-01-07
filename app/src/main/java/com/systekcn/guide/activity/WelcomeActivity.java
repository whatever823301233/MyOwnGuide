package com.systekcn.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.systekcn.guide.R;
import com.systekcn.guide.adapter.base.ViewPagerAdapter;
import com.systekcn.guide.custom.Dot;
import com.systekcn.guide.fragment.BaseFragment;
import com.systekcn.guide.fragment.ImageFragment;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.ViewUtils;

import java.io.IOException;
import java.util.ArrayList;

public class WelcomeActivity extends BaseActivity implements ViewPager.OnPageChangeListener{

    private int lastPage = 0;
    private int dotWidth = 40;
    private int dotHeight = 35;
    private ArrayList<Dot> mDots = new ArrayList<>();
    private Button btn_into_app;
    private ViewPager viewPager;
    private LinearLayout linearLayout_dots;
    private String[] list_image;
    private Class<?> targetClass;
    private Intent intent;
    @Override
    protected void initialize(Bundle savedInstanceState) {
        ViewUtils.setStateBarToAlpha(this);
        setContentView(R.layout.activity_welcome);
        targetClass=MuseumListActivity.class;
        initView();
        //得到assets/welcome_images/目录下的所有文件的文件名，以便后面打开操作时使用
        try {
            list_image = getAssets().list("welcome_images");
        } catch (IOException e) {
            ExceptionUtil.handleException(e);
        }
        // 设置page切换监听
        viewPager.addOnPageChangeListener(this);
        // 遍历图片数组
        ArrayList<BaseFragment> baseFragments = new ArrayList<>();
        for (String aList_image : list_image) {
            Dot dot = new Dot(this);
            int unSelectColorResId = android.R.color.darker_gray;
            int selectColorResId = android.R.color.white;
            dot.setColor(unSelectColorResId, selectColorResId);
            dot.setLayoutParams(new LinearLayout.LayoutParams(dotWidth, dotHeight));
            mDots.add(dot);
            linearLayout_dots.addView(dot);
            baseFragments.add(ImageFragment.newInstance(aList_image));
        }
        // 设置适配器
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), baseFragments));
        // 默认选中第一位
        dotSelect(0);
        btn_into_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent=new Intent(WelcomeActivity.this,targetClass);
                startActivity(intent);
                finish();
            }
        });
    }
    private void initView() {
        btn_into_app=(Button)findViewById(R.id.btn_into_app);
        // 控件实例化
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        linearLayout_dots = (LinearLayout) findViewById(R.id.linearlayout_dots);
    }
    /**
     * 页面选择
     *
     * @param index
     */
    private void dotSelect(int index) {
        for (int i = 0; i < mDots.size(); i++) {
            mDots.get(i).setSelected(i == index);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (arg0 == lastPage && (lastPage + 1) < mDots.size()) {
            mDots.get(lastPage).setScale(1.0f - arg1);
            mDots.get(lastPage + 1).setScale(arg1);
        } else {
            this.lastPage = arg0;
        }
    }
    @Override
    public void onPageSelected(int arg0) {
        dotSelect(arg0);
        if(arg0==mDots.size()-1){
            btn_into_app.setVisibility(View.VISIBLE);
        }else{
            btn_into_app.setVisibility(View.GONE);
        }
    }

}
