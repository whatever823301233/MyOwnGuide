package com.systekcn.guide.biz;

import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.systekcn.guide.IConstants;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.LogUtil;
import com.systekcn.guide.utils.MyHttpUtil;
import com.systekcn.guide.utils.Tools;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * Created by Qiang on 2015/11/17.
 */
public class DownloadBiz implements IConstants {

    /* 下载开始时间 */
    long startTime;
    private String assetsJson;
    private int totalSize;
    public  boolean downloadPause;
    /**下载文件个数*/
    private int count;
    public boolean downloadOver;
    public int getProgress(){

        return count*100/totalSize>100?100:count;
    }

    /** 获得assets资源json */
    public String getAssetsJSON(String id) {
        long startTime=System.currentTimeMillis();
        assetsJson=MyHttpUtil.sendGet(URL_ALL_MUSEUM_ASSETS + id);
        while (assetsJson == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                ExceptionUtil.handleException(e);
            }
            if(System.currentTimeMillis()-startTime>8000){
                LogUtil.i("ZHANG","getAssetsJSON失败");
                break;
            }
        }
        return assetsJson;
    }

    /** 解析assets资源json */
    public Vector<String> parseAssetsJson(String assetsJson2) {
        JSONObject jsonObj = JSON.parseObject(assetsJson2);
        String jsonString = jsonObj.getString("url");
        List<String> list = JSON.parseArray(jsonString, String.class);
        return new Vector<>(list);
    }

    public void pause(){
        downloadPause=true;
    }
    public void toContinue(){
        downloadPause=false;
    }

    String str = "";
    String savePath = "";
    /* 下载assets中数据 */
    public void downloadAssets(Vector<String> assetsList, int start, int end,String museumId) {
        startTime = System.currentTimeMillis();
        totalSize=assetsList.size();
        LogUtil.i("ZHANG", "downloadAssets开始执行------当前时间为" + startTime + "文件个数" + (totalSize-count));
        String url="";
		/* 遍历集合并下载 */
        for (int i = start; i < end; i++) {
            str = assetsList.get(i);
            if (str.endsWith(".jpg")||str.endsWith(".png")) {
                savePath = LOCAL_ASSETS_PATH +museumId+"/"+LOCAL_FILE_TYPE_IMAGE+"/"+ str.replaceAll("/","_");
                url = BASE_URL + assetsList.get(i);
            } else if (str.endsWith(".lrc")) {
                savePath = LOCAL_ASSETS_PATH+museumId+"/" +LOCAL_FILE_TYPE_LYRIC+"/"+ str.replaceAll("/", "_");
                url = BASE_URL + assetsList.get(i);
            } else if (str.endsWith(".mp3") || str.endsWith(".wav")) {
                savePath = LOCAL_ASSETS_PATH+museumId+"/" +LOCAL_FILE_TYPE_AUDIO+"/"+ str.replaceAll("/","_");
                url = BASE_URL + assetsList.get(i);
            } else {
                LogUtil.i("ZHANG", "文件后缀异常-----------------");
                continue;
            }
            if(Tools.isFileExist(savePath)){
                count++;
                continue;
            }
            while(downloadPause){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    ExceptionUtil.handleException(e);
                }
            }
            downloadFile(savePath, url);
//            DataBiz.saveTempValue(MyApplication.get(),SP_DOWNLOAD_MUSEUM_COUNT,totalSize-count);
        }
        downloadOver=true;
    }

    private void downloadFile(String savePath, final String url) {
        try {
            MyHttpUtil.downLoadFromUrl(url,url.substring(url.lastIndexOf("/") + 1),savePath);
            count++;
        } catch (IOException e) {
            ExceptionUtil.handleException(e);
        }
        if (url.endsWith(".jpg")) {
            LogUtil.i("jpg文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + count);
        }else if (url.endsWith(".png")) {
            LogUtil.i("png文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + count);
        }else if (url.endsWith(".lrc")) {
            LogUtil.i("lrc文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + count);
        } else if (url.endsWith(".mp3")) {
            LogUtil.i("mp3文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + count);
        } else if (url.endsWith(".wav")) {
            LogUtil.i("wav文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + count);
        }
        if (count == totalSize) {
            long cost = System.currentTimeMillis() - startTime;
            LogUtil.i("下载执行完毕", "用时----------" + cost / 1000 + "秒");
            Intent in = new Intent();
            in.setAction(ACTION_PROGRESS);
            in.putExtra(ACTION_PROGRESS, 100);// currentSize*100/totalSize
            MyApplication.get().sendBroadcast(in);
        }
    }
}
