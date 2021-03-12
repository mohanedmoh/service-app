package com.savvy.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.savvy.service.DB.Areas;
import com.savvy.service.DB.Cities;
import com.savvy.service.DB.Countries;
import com.savvy.service.DB.Main_services;
import com.savvy.service.DB.Sub_services;
import com.savvy.service.Network.Iokihttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
public class Splash extends AppCompatActivity {
    private static int splash_time_out = 1000;
    SharedPreferences shared;
    Iokihttp iokihttp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        shared = getApplicationContext().getSharedPreferences("com.example.service", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();
        if (getIntent().hasExtra("defaultLang")) {
            changeLang(getIntent().getExtras().getString("defaultLang"));
        } else {
            changeLang(shared.getString("defaultLang", Locale.getDefault().getLanguage()));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (checkCounter()) getVersion();
                else openApp();
            }
        }, splash_time_out);
    }

    private boolean checkCounter() {
        int counter = shared.getInt("counter", 0);
        if (counter == 0) {
            return true;
        } else if (counter == 3) {
            shared.edit().putInt("counter", 0).apply();
            return false;
        } else {
            shared.edit().putInt("counter", ++counter).apply();
            return false;
        }
    }
    private void changeLang(String langCode) {
        System.out.println(langCode + "ddddd" + Locale.getDefault().getLanguage() + "ddddd" + shared.getString("defaultLang", "none"));
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

    }
    public void getVersion() {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            iokihttp.post(getString(R.string.url) + "version", json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("FAIL");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {


                            JSONObject resJSON = new JSONObject(responseStr);

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                final JSONObject subresJSON = resJSON.getJSONObject("data");
                                checkVersion(Double.valueOf(shared.getString("version", "0")), subresJSON.getDouble("version"));
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response=" + responseStr);

                    } else {
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
                    }
                }

            });
        }
    }

    public void checkVersion(double localVersion, double serverVersion) {
        if (serverVersion > localVersion) {
            getSystemData(serverVersion);
        } else {
            openApp();
        }
    }

    private void getSystemData(final double serverVersion) {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            iokihttp.post(getString(R.string.url) + "startup", json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("FAIL");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {


                            JSONObject resJSON = new JSONObject(responseStr);

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                final JSONObject subresJSON = resJSON.getJSONObject("data");
                                JSONArray countries = subresJSON.getJSONArray("countries");
                                JSONArray cities = subresJSON.getJSONArray("cities");
                                JSONArray areas = subresJSON.getJSONArray("areas");

                                JSONArray main_services = subresJSON.getJSONArray("main");
                                JSONArray sub_services = subresJSON.getJSONArray("sub");
                                fillAreas(areas);
                                fillCities(cities);
                                fillCountries(countries);
                                fillMainServices(main_services);
                                fillSubServices(sub_services);
                                shared.edit().putString("version", String.valueOf(serverVersion)).apply();
                                shared.edit().putInt("counter", 1).apply();
                                openApp();

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response=" + responseStr);

                    } else {
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
                    }
                }

            });
        }
    }

    public void fillCountries(JSONArray countries) throws JSONException {

        Countries.FeedReaderDbHelper dbHelper = new Countries.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(Countries.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < countries.length(); x++) {
            JSONObject country = countries.getJSONObject(x);
            ContentValues values = new ContentValues();
            values.put(Countries.FeedEntry.GId, country.getString("id"));
            values.put(Countries.FeedEntry.name, country.getString("name"));

// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(Countries.FeedEntry.TABLE_NAME, null, values);
        }
    }

    public void fillCities(JSONArray cities) throws JSONException {

        Cities.FeedReaderDbHelper dbHelper = new Cities.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(Cities.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < cities.length(); x++) {
            JSONObject city = cities.getJSONObject(x);
            ContentValues values = new ContentValues();
            values.put(Cities.FeedEntry.GId, city.getString("id"));
            values.put(Cities.FeedEntry.name, city.getString("name"));
            values.put(Cities.FeedEntry.country_id, city.getString("country_id"));


// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(Cities.FeedEntry.TABLE_NAME, null, values);
        }
    }

    public void fillAreas(JSONArray areas) throws JSONException {

        Areas.FeedReaderDbHelper dbHelper = new Areas.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(Areas.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < areas.length(); x++) {
            JSONObject area = areas.getJSONObject(x);
            ContentValues values = new ContentValues();
            values.put(Areas.FeedEntry.GId, area.getString("id"));
            values.put(Areas.FeedEntry.name, area.getString("name"));
            values.put(Areas.FeedEntry.city_id, area.getString("city_id"));


// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(Areas.FeedEntry.TABLE_NAME, null, values);
        }
    }

    public void fillMainServices(JSONArray mainServices) throws JSONException {

        Main_services.FeedReaderDbHelper dbHelper = new Main_services.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(Main_services.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < mainServices.length(); x++) {
            JSONObject mainService = mainServices.getJSONObject(x);
            ContentValues values = new ContentValues();
            values.put(Main_services.FeedEntry.GId, mainService.getString("id"));
            values.put(Main_services.FeedEntry.name, mainService.getString("name"));

// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(Main_services.FeedEntry.TABLE_NAME, null, values);
        }
    }

    public void fillSubServices(JSONArray subServices) throws JSONException {

        Sub_services.FeedReaderDbHelper dbHelper = new Sub_services.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(Sub_services.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < subServices.length(); x++) {
            JSONObject subService = subServices.getJSONObject(x);
            ContentValues values = new ContentValues();
            values.put(Sub_services.FeedEntry.GId, subService.getString("id"));
            values.put(Sub_services.FeedEntry.name, subService.getString("name"));
            values.put(Sub_services.FeedEntry.main_id, subService.getString("main_services_id"));


// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(Sub_services.FeedEntry.TABLE_NAME, null, values);
        }
    }

    private void openApp() {
        if (shared.getBoolean("login", false)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            Intent intent = new Intent(getApplicationContext(), Signup.class);
            startActivity(intent);
            finish();

        }

    }
}
