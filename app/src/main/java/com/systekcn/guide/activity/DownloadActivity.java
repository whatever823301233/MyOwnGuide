package com.systekcn.guide.activity;

import android.os.Bundle;
import android.widget.ListView;

import com.systekcn.guide.R;

public class DownloadActivity extends BaseActivity {

    private ListView listViewDownload;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        setContentView(R.layout.activity_download);
        initView();
    }

    private void initView() {
        listViewDownload=(ListView)findViewById(R.id.listViewDownload);
    }

}
