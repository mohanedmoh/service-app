package com.savvy.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.savvy.service.Network.Iokihttp;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        Home.OnFragmentInteractionListener, Aboutus.OnFragmentInteractionListener, Favorite.OnFragmentInteractionListener {
    DrawerLayout drawer;
    boolean home = true;
    SharedPreferences shared;
    boolean doubleBackToExitPressedOnce = false;
    Fragment fragment = null;
    Class fragmentClass = null;
    private Iokihttp okhttp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        okhttp = new Iokihttp();

        drawer = findViewById(R.id.drawer_layout);
        // actionBar = getActionBar();
        //actionBar.setHomeButtonEnabled(false);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(R.drawable.side_nav_bar);

        shared = getApplicationContext().getSharedPreferences("com.example.service", Context.MODE_PRIVATE);
        setSupportActionBar(toolbar);
        //change the name and the phone
        Fragment fragment = null;
        Class fragmentClass = null;
        Bundle b = new Bundle();
        Home h = new Home();


        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (savedInstanceState == null) {

        }
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setHomeAsUpIndicator(R.drawable.drawer);

        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, h).commit();
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();

        changeNavLabels();

    }

    private void onLangChangeClick() {
        System.out.println("in change lang");
        if (shared.getString("defaultLang", Locale.getDefault().getLanguage()).toLowerCase().equals("ar")) {
            changeLang("en");

        } else {
            changeLang("ar");
        }
    }

    private void changeLang(String langCode) {
        System.out.println(langCode + "ddddd" + Locale.getDefault().getLanguage());
        shared.edit().putString("defaultLang", langCode).apply();
        System.out.println(shared.getString("defaultLang", "none") + "ddddd");
        Resources res = getResources();
// Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            System.out.println("inside fun");
            conf.setLocale(new Locale(langCode)); // API 17+ only.
            conf.setLayoutDirection(new Locale(langCode));

        } else {
            conf.locale = new Locale(langCode);
        }


// Use conf.locale = new Locale(...) if targeting lower versions
        res.updateConfiguration(conf, dm);
        shared.edit().putString("defaultLang", langCode).apply();
        restartApp(langCode);
    }

    private void restartApp(String langCode) {
        Intent mStartActivity = new Intent(getApplicationContext(), Splash.class);
        mStartActivity.putExtra("defaultLang", langCode);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (home) {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    finish();
                    return;
                }
            } else {
                MenuItem item = ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0);
                onNavigationItemSelected(item);
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.press_back), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 3000);
        }
    }

    private void changeNavLabels() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.side_name)).setText(shared.getString("username", "No name"));
        ((TextView) headerView.findViewById(R.id.side_phone)).setText(shared.getString("job", ""));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {   // Handle navigation view item clicks here.
        int id = menuItem.getItemId();
        fragment = null;
        fragmentClass = null;

        if (id == R.id.nav_home) {
            fragmentClass = Home.class;
            home = true;

        } else if (id == R.id.nav_package) {

            fragmentClass = Packages_info.class;
            home = false;
        } else if (id == R.id.nav_aboutus) {

            fragmentClass = Aboutus.class;
            home = false;
        } else if (id == R.id.nav_sign_out) {
            shared.edit().putBoolean("login", false).apply();
            Intent i = new Intent(getApplicationContext(), Signup.class);
            startActivity(i);
            finish();
        } else if (id == R.id.changeLang) {
            onLangChangeClick();
        }

        if (fragmentClass != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
