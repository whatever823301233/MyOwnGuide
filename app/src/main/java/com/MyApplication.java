package com;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.systekcn.guide.IConstants;
import com.systekcn.guide.R;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.manager.MediaServiceManager;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.LogUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Qiang on 2015/12/30.
 */
public class MyApplication extends Application implements IConstants{

    private static MyApplication myApplication;
    /*软件是否开发完毕*/
    public static final boolean isRelease = false;
    /*所有的activity都放入此集合中*/
    public static ArrayList<Activity> listActivity = new ArrayList<>();
    /*当前网络状态*/
    public static int currentNetworkType= INTERNET_TYPE_NONE;
    public static final int GUIDE_MODEL_AUTO=2;
    public static final int GUIDE_MODEL_HAND=3;
    public static int guideModel=GUIDE_MODEL_HAND;
    public MediaServiceManager mServiceManager;
    /**当前展品*/
    public ExhibitBean currentExhibitBean;
    /**当前博物馆*/
    public MuseumBean currentMuseum;
    /**展品总集合*/
    public List<ExhibitBean> totalExhibitBeanList;
    /**当前要加入展品（蓝牙扫描周边等）集合*/
    public List<ExhibitBean> currentExhibitBeanList;
    /**看过的展品集合*/
    public List<ExhibitBean> everSeenExhibitBeanList;
    /**专题展品集合*/
    public List<ExhibitBean> topicExhibitBeanList;
    /***/
    public List<ExhibitBean> recordExhibitBeanList;
    /**附近展品框中显示的展品*/
    public List<ExhibitBean> nearlyExhibitBeanList;

    public String currentMuseumId;
    public String currentBeaconId;
    public String currentExhibitId;

    @Override
    public void onCreate() {
        myApplication = this;
        // 防止重启两次,非相同名字的则返回
        if (!isSameAppName()) {
            return;
        }
        super.onCreate();


        /*
        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });
        */

        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }

                //we use the default one for
                //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()

                return super.placeholder(ctx, tag);
            }
        });
    }

    public  static Context getAppContext(){
        return myApplication.getApplicationContext();
    }

    public void refreshData(){
        if(currentExhibitBean!=null){
            currentExhibitId=currentExhibitBean.getId();
        }
        currentMuseumId=currentExhibitBean.getMuseumId();
        currentBeaconId=currentExhibitBean.getBeaconId();
    }

    public  String getCurrentLyricDir(){
        return LOCAL_ASSETS_PATH+currentMuseumId+"/"+LOCAL_FILE_TYPE_LYRIC+"/";
    }

    public String getCurrentAudioDir(){
        return LOCAL_ASSETS_PATH+currentMuseumId+"/"+LOCAL_FILE_TYPE_AUDIO+"/";
    }
    public  String getCurrentMuseumId(){
        if(currentExhibitBean!=null){
            return currentExhibitBean.getMuseumId();
        }else{
            return "";
        }
    }
    public  String getCurrentImgDir(){
        return LOCAL_ASSETS_PATH+currentExhibitBean.getMuseumId()+"/"+LOCAL_FILE_TYPE_IMAGE+"/";
    }


    /**
     * 判断是否为相同app名
     *
     * @return
     */
    private boolean isSameAppName() {
        int pid = android.os.Process.myPid();
        String processAppName = getProcessAppName(pid);
        return !(TextUtils.isEmpty(processAppName) || !processAppName.equalsIgnoreCase(getPackageName()));
    }
    /**
     * 获取processAppName
     *
     * @param pid
     * @return
     */
    private String getProcessAppName(int pid) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        Iterator<ActivityManager.RunningAppProcessInfo> iterator = activityManager.getRunningAppProcesses().iterator();
        while (iterator.hasNext()) {
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo = (ActivityManager.RunningAppProcessInfo) (iterator.next());
            try {
                if (runningAppProcessInfo.pid == pid) {
                    return runningAppProcessInfo.processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
    private void initBaiduSDK() {
        try {
            // 初始化百度地图
            //SDKInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }
    /*退出程序 */
    public  void exit() {
        for (Activity activity : listActivity) {
            if(activity!=null){
                try {
                    activity.finish();
                    LogUtil.i("退出", activity.toString() + "退出了");
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }
        }
        /*if(bluetoothManager!=null){
            bluetoothManager.disConnectBluetoothService();
        }*/
        System.exit(0);
    }

    /**
     * 获取application对象
     *
     * @return JApplication
     */
    public static MyApplication get() {
        return myApplication;
    }
}
