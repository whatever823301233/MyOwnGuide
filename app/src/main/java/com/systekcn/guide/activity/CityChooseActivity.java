package com.systekcn.guide.activity;

import android.os.Bundle;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.systekcn.guide.R;
import com.systekcn.guide.utils.ViewUtils;

public class CityChooseActivity extends BaseActivity {

    private Drawer result;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        ViewUtils.setStateBarToAlpha(this);
        View view=getLayoutInflater().inflate(R.layout.activity_city_choose,null);
        setContentView(view);

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


}
