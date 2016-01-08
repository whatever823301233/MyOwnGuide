package com.systekcn.guide.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.systekcn.guide.IConstants;
import com.systekcn.guide.R;
import com.systekcn.guide.custom.RoundImageView;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.utils.ExceptionUtil;
import com.systekcn.guide.utils.ImageLoaderUtil;
import com.systekcn.guide.utils.LogUtil;

import java.io.File;
import java.util.List;

/**
 * Created by Qiang on 2015/10/27.
 */
public class ExhibitAdapter extends BaseAdapter implements IConstants {

    private Context context;
    private List<ExhibitBean> list;
    private LayoutInflater inflater;
    private  boolean scrollState=false;

    public void setScrollState(boolean scrollState) {
        this.scrollState = scrollState;
    }
    public ExhibitAdapter(Context context, List<ExhibitBean> list) {
        super();
        this.context = context;
        this.list = list;
        inflater=LayoutInflater.from(context);
    }

    public void updateData(List<ExhibitBean> list){
        this.list=list;
        notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ExhibitBean getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null||convertView.getTag() == null) {
            convertView = inflater.inflate( R.layout.item_list_exhibit, null);
            viewHolder = new ViewHolder();
            viewHolder.tvExhibitName = (TextView) convertView.findViewById(R.id.tvExhibitName);
            viewHolder.tvExhibitYears = (TextView) convertView.findViewById(R.id.tvExhibitYears);
            viewHolder.tvExhibitPosition = (TextView) convertView.findViewById(R.id.tvExhibitPosition);
            viewHolder.ivExhibitIcon = (RoundImageView) convertView.findViewById(R.id.ivExhibitIcon);
            viewHolder.tvExhibitDistance = (TextView) convertView.findViewById(R.id.tvExhibitDistance);
            viewHolder.llCollectionBtn = (LinearLayout) convertView.findViewById(R.id.llCollectionBtn);
            viewHolder.ivCollection = (ImageView) convertView.findViewById(R.id.ivCollection);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // 取数据
        final ExhibitBean exhibitBean = list.get(position);
        viewHolder.tvExhibitName.setText(exhibitBean.getName());
        viewHolder.tvExhibitYears.setText(exhibitBean.getLabels());
        String beaconId=exhibitBean.getBeaconId();
        String disId=beaconId.substring(beaconId.length()-6,beaconId.length());
        viewHolder.tvExhibitPosition.setText(disId);// TODO: 2015/12/30 改为展厅
        String distance=String.valueOf(exhibitBean.getDistance());
        if(distance.length()>6){
            distance=distance.substring(0,6);
        }
        viewHolder.tvExhibitDistance.setText(distance);
        if(exhibitBean.isSaveForPerson()){
            viewHolder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_full));
        }else{
            viewHolder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_empty));
        }
        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.llCollectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(exhibitBean.isSaveForPerson()){
                    exhibitBean.setSaveForPerson(false);
                    finalViewHolder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_empty));
                    Toast.makeText(context, "取消收藏", Toast.LENGTH_LONG).show();
                }else{
                    exhibitBean.setSaveForPerson(true);
                    finalViewHolder.ivCollection.setImageDrawable(context.getResources().getDrawable(R.drawable.iv_heart_full));
                    Toast.makeText(context, "已收藏", Toast.LENGTH_LONG).show();
                }
                DbUtils db=DbUtils.create(context);
                try {
                    db.saveOrUpdate(exhibitBean);
                    LogUtil.i("ZHANG", "收藏数据已更新至数据库");
                } catch (DbException e) {
                    ExceptionUtil.handleException(e);
                }finally {
                    if(db!=null){
                        db.close();
                    }
                }
            }
        });
        if (!scrollState){
            // 显示图片
            String iconUrl = exhibitBean.getIconurl();

            //每个博物馆的资源以ID为目录
            String museumId = exhibitBean.getMuseumId();

            String imageName = iconUrl.replaceAll("/", "_");
            String imgLocalUrl = LOCAL_ASSETS_PATH+museumId + "/" + LOCAL_FILE_TYPE_IMAGE+"/"+imageName;
            File file = new File(imgLocalUrl);
            // 判断sdcard上有没有图片
            if (file.exists()) {
                // 显示sdcard
                ImageLoaderUtil.displaySdcardImage(context, imgLocalUrl, viewHolder.ivExhibitIcon);
            } else {
                iconUrl = BASE_URL + iconUrl;
                ImageLoaderUtil.displayNetworkImage(context, iconUrl, viewHolder.ivExhibitIcon);
            }
        }
        return convertView;
    }

    class ViewHolder{
        TextView tvExhibitName, tvExhibitYears, tvExhibitPosition,tvExhibitDistance;
        RoundImageView ivExhibitIcon;
        LinearLayout llCollectionBtn;
        ImageView ivCollection;
    }
}
