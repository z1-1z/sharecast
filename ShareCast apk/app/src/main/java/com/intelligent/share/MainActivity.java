package com.intelligent.share;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.intelligent.share.databinding.ActivityHomeBinding;
import com.tosmart.dlna.application.BaseApplication;
import com.tosmart.dlna.base.BaseActivity;
import com.tosmart.dlna.data.repository.DeviceRepository;
import com.tosmart.dlna.data.repository.LibraryRepository;
import com.tosmart.dlna.util.StatusBarUtil;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;

public class MainActivity extends BaseActivity<ActivityHomeBinding> {
    private static final String TAG = "MainActivity";

    private AppBarConfiguration appBarConfiguration;
    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_home;
    }

    @Override
    protected void init() {
        StatusBarUtil.fullScreen(this);
        StatusBarUtil.setStatusTextColor(true, this);

        setSupportActionBar(mViewDataBinding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        requestPermission();
        bindService(new Intent(BaseApplication.getContext(), AndroidUpnpServiceImpl.class),
                DeviceRepository.obtain().getServiceConnection(), Context.BIND_AUTO_CREATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setBackgroundDrawable(null);
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = ContextCompat.checkSelfPermission(this, PERMISSIONS[0]);
            if (i != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);
            } else {
                // 如果有权限，直接init
                BaseApplication.setHasPermission(true);
                if (BaseApplication.isServiceInit()) {
                    LibraryRepository.getInstance().init();
                }
            }
        } else {
            BaseApplication.setHasPermission(true);
            if (BaseApplication.isServiceInit()) {
                LibraryRepository.getInstance().init();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 申请完权限之后，直接init
                BaseApplication.setHasPermission(true);
                if (BaseApplication.isServiceInit()) {
                    LibraryRepository.getInstance().init();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(DeviceRepository.obtain().getServiceConnection());
        DeviceRepository.obtain().onDestroy();
        LibraryRepository.getInstance().onDestroy();
    }
}