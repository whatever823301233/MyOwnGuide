package com.systekcn.guide.biz;


import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.systekcn.guide.IConstants;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.entity.BeaconBean;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.LabelBean;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.MyHttpUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Qiang on 2016/1/1.
 */
public class DataBiz implements IConstants{

    public static <T> List<T> getEntityListFromNet(Class<T> clazz,String url){

        String response= MyHttpUtil.sendGet(url);
        if(TextUtils.isEmpty(response)){return null;}
        List<T> list=JSON.parseArray(response,clazz);
        return list;
    }
    public  static<T> List<T> getEntityListLocal(Class<T> clazz){
        DbUtils db=DbUtils.create(MyApplication.get());
        List<T> list= null;
        try {
            list = db.findAll(clazz);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){db.close();}
        }
        return list;
    }

    public static <T> boolean deleteSQLiteDataFromClass(Class<T> clazz){
        boolean isSuccess=true;
        DbUtils db=DbUtils.create(MyApplication.get());
        try {
            db.deleteAll(clazz);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            if(db!=null){db.close();}
        }
        return isSuccess;
    }

    public static <T> boolean deleteSQLiteDataFromID(Class<T> clazz,String id){
        boolean isSuccess=true;
        DbUtils db=DbUtils.create(MyApplication.get());
        try {
            db.delete(clazz, WhereBuilder.b(ID, LIKE, "%" + id + "%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            if(db!=null){db.close();}
        }
        return isSuccess;
    }



    public  static<T> boolean saveListToSQLite(List<T> list){
        boolean isSuccess=true;
        if(list==null){return false; }
        List<T> listWithoutDup= new ArrayList<>(new HashSet<>(list)) ;
        DbUtils db=DbUtils.create(MyApplication.get());
        try {
            db.saveOrUpdateAll(listWithoutDup);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            if(db!=null){db.close();}
        }
        return isSuccess;
    }

    public static boolean saveEntityToSQLite(Object obj){
        boolean isSuccess=true;
        DbUtils db=DbUtils.create(MyApplication.get());
        try {
            db.save(obj);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            if(db!=null){db.close();}
        }
        return isSuccess;
    }

    public  static boolean deleteOldJsonData(String museumID){
        boolean isSuccess=true;
        DbUtils db=DbUtils.create(MyApplication.get());
        try{
            db.createTableIfNotExist(BeaconBean.class);
            List<BeaconBean> beaconList=db.findAll(Selector.from(BeaconBean.class).where(MUSEUM_ID, LIKE, "%" + museumID + "%"));
            if(beaconList!=null&&beaconList.size()>0){
                db.deleteAll(beaconList);
            }
            db.createTableIfNotExist(LabelBean.class);
            List<LabelBean> labelList=db.findAll(Selector.from(LabelBean.class).where(MUSEUM_ID, LIKE, "%" + museumID + "%"));
            if(labelList!=null&&labelList.size()>0){
                db.deleteAll(labelList);
            }
            db.createTableIfNotExist(ExhibitBean.class);
            List<ExhibitBean> exhibitBeanList=db.findAll(Selector.from(ExhibitBean.class).where(MUSEUM_ID, LIKE, "%" + museumID + "%"));
            if(exhibitBeanList!=null&&exhibitBeanList.size()>0){
                db.deleteAll(exhibitBeanList);
            }
        }catch (DbException e){
            isSuccess=false;
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
        return isSuccess;
    }
    public  static boolean saveAllJsonData(String museumID) {
        List<BeaconBean> beaconList = getEntityListFromNet(BeaconBean.class, URL_BEACON_LIST + museumID);
        List<LabelBean> labelList = getEntityListFromNet(LabelBean.class, URL_LABELS_LIST + museumID);
        List<ExhibitBean> exhibitList = getEntityListFromNet(ExhibitBean.class, URL_EXHIBIT_LIST + museumID);
        if(beaconList == null || labelList == null || exhibitList == null //|| mapList == null//|| mapList.size() == 0
                || beaconList.size() == 0 || labelList.size() == 0 || exhibitList.size() == 0 ){return false;}
        //deleteOldJsonData(museumID);
        return saveListToSQLite(beaconList) && saveListToSQLite(labelList) && saveListToSQLite(exhibitList);// && saveEntityToSQLite(mapList)

    }
    public  static<T>  List<T> getLocalListById(Class<T> clazz, String museumID) {
        DbUtils db=DbUtils.create(MyApplication.get());
        List<T>list=null;
        try {
            list =db.findAll(Selector.from(clazz).where(MUSEUM_ID,LIKE,"%"+museumID+"%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }
        return list;
    }
}
