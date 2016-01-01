package com.systekcn.guide.biz;


import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.MyHttpUtil;

import java.util.List;

/**
 * Created by Qiang on 2016/1/1.
 */
public class DataBiz {

    public <T> List<T> getEntityListFromNet(Class<T> clazz,String url){
        List<T> list=null;
        String response=null;
        response= MyHttpUtil.sendGet(url);
        list=JSON.parseArray(response,clazz);
        return list;
    }
    public <T> List<T> getEntityListLocal(Class<T> clazz){
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

    public <T> boolean deleteSQLiteDataFromClass(Class<T> clazz){
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

    public <T> boolean saveListToSQLite(List<T> list){
        boolean isSuccess=true;
        DbUtils db=DbUtils.create(MyApplication.get());
        try {
            db.saveAll(list);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            if(db!=null){db.close();}
        }
        return isSuccess;
    }

    public  boolean saveEntityToSQLite(Object obj){
        boolean isSuccess=true;
        DbUtils db=DbUtils.create(MyApplication.get());
        try {
            db.save(obj);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            if(db!=null){db.close();}
        }
        return isSuccess;
    }


}
