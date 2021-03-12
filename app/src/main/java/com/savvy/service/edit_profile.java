package com.savvy.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.anurag.multiselectionspinner.MultiSelectionSpinnerDialog;
import com.anurag.multiselectionspinner.MultiSpinner;
import com.savvy.service.DB.Areas;
import com.savvy.service.DB.Cities;
import com.savvy.service.DB.Countries;
import com.savvy.service.DB.Main_services;
import com.savvy.service.DB.Sub_services;
import com.savvy.service.Models.Provider;
import com.savvy.service.Network.Iokihttp;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.hbb20.CountryCodePicker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class edit_profile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, MultiSelectionSpinnerDialog.OnMultiSpinnerSelectionListener {
    public DatePickerDialog dpd;
    public Calendar now;
    View profile_info_include;
    SharedPreferences shared;
    ArrayList<String> areas;
    ArrayList<Integer> areasId, selecetedIds;
    Provider provider;
    ArrayAdapter<String> countryA, cityA, mainA, subA, genderA;
    String date;
    boolean in = true;
    Iokihttp iokihttp;
    int req_code;
    ArrayList<String> profile_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        init();
    }

    private void init() {
        now = Calendar.getInstance();
        iokihttp = new Iokihttp();
        shared = this.getSharedPreferences("com.example.service", Context.MODE_PRIVATE);
        dpd = DatePickerDialog.newInstance(
                edit_profile.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        //dpd.showYearPickerFirst(true);

        provider = (Provider) getIntent().getExtras().getBundle("provider").getSerializable("provider");
        profile_info_include = findViewById(R.id.profile_info_include);
        profile_info_include.findViewById(R.id.profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCam(101);
            }
        });
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendProfileInfo();
            }
        });
        getCountries();
        getMainServices();
        fill_gender();
        setInfo();
    }

    private Boolean verify() {
        String name = ((EditText) profile_info_include.findViewById(R.id.full_name)).getText().toString();
        String gender = String.valueOf(((Spinner) profile_info_include.findViewById(R.id.gender)).getSelectedItemId());
        String country_id = ((edit_profile.StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.country)).getSelectedItem())).key;
        String city_id = ((edit_profile.StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.city)).getSelectedItem())).key;
        String main_service = ((edit_profile.StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.service)).getSelectedItem())).key;
        String sub_service = ((edit_profile.StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.sub_service)).getSelectedItem())).key;
        String birth_date = date;
        if (name.isEmpty() || birth_date.isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill_error) + ".", Toast.LENGTH_LONG).show();
            return false;
        } else if (gender.equals("0") || gender.isEmpty() || country_id.equals("0") || country_id.isEmpty() || city_id.equals("0") || city_id.isEmpty() || main_service.equals("0") || main_service.isEmpty() || sub_service.equals("0") || sub_service.isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill_error), Toast.LENGTH_LONG).show();
            return false;
        } else {

            return true;
        }
    }

    private void sendProfileInfo() {
        showLoading();
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        String sub, main, country, city, gender;
        main = ((edit_profile.StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.service)).getSelectedItem())).key;
        sub = ((edit_profile.StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.sub_service)).getSelectedItem())).key;
        country = ((edit_profile.StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.country)).getSelectedItem())).key;
        city = ((edit_profile.StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.city)).getSelectedItem())).key;
        gender = String.valueOf(((Spinner) profile_info_include.findViewById(R.id.gender)).getSelectedItemId());
        try {
            System.out.println(selecetedIds + "gender=" + (((Spinner) findViewById(R.id.gender)).getSelectedItemId() - 1));
            subJSON.put("id", shared.getString("user_id", ""));
            subJSON.put("name", ((EditText) profile_info_include.findViewById(R.id.full_name)).getText().toString());
            subJSON.put("gender", gender);
            subJSON.put("country_code", ((CountryCodePicker) profile_info_include.findViewById(R.id.countryPicker)).getSelectedCountryCode());
            subJSON.put("phone", ((EditText) profile_info_include.findViewById(R.id.whatsapp_phone)).getText().toString());
            subJSON.put("country_id", Integer.parseInt(country) == 0 ? provider.getCountry_id() : country);
            subJSON.put("city_id", Integer.parseInt(city) == 0 ? provider.getCity_id() : city);
            subJSON.put("areas", selecetedIds);
            subJSON.put("main_services_id", Integer.parseInt(main) == 0 ? provider.getMain_service_id() : main);
            subJSON.put("sub_services_id", Integer.parseInt(sub) == 0 ? provider.getSub_service_id() : sub);
            subJSON.put("birth_date", date);
            subJSON.put("certificate", ((EditText) profile_info_include.findViewById(R.id.certificates)).getText());
            subJSON.put("experience", ((EditText) profile_info_include.findViewById(R.id.ex_desc)).getText());
            subJSON.put("work_years", ((EditText) profile_info_include.findViewById(R.id.year_ex)).getText());
            System.out.println("====ARRAY=" + subJSON.toString());
            json.put("data", subJSON);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            iokihttp.post(getString(R.string.url) + "setProfile", json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("FAIL");
                    hideLoading();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    hideLoading();
                    if (response.isSuccessful()) {

                        String responseStr = response.body().string();

                        try {
                            JSONObject resJSON = new JSONObject(responseStr);

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                setResult(1);
                                finish();
                            } else {
                                runOnUiThread(new Runnable() {
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

    private void openCam(int code) {
        Pix.start(edit_profile.this, Options.init().setRequestCode(code));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            profile_image = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            try {
                changeImage(profile_image.get(0), R.id.profile_image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            hideLoading();
        }
    }

    private void changeImage(String imageString, int id) throws IOException {
        File f = new File(imageString);
        Bitmap d = new BitmapDrawable(getApplicationContext().getResources(), f.getAbsolutePath()).getBitmap();
        //Bitmap scaled = com.fxn.utility.Utility.getScaledBitmap(512, com.fxn.utility.Utility.getExifCorrectedBitmap(f));
        Bitmap scaled = com.fxn.utility.Utility.getScaledBitmap(512, d);
        ((ImageView) profile_info_include.findViewById(id)).setImageBitmap(scaled);
        uploadImage(101, imageString, "profile_image");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Pix.start(edit_profile.this, Options.init().setRequestCode(101));
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Approve permissions to open ImagePicker", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return;
        }
    }

    private void uploadImage(int code, String image1, String name1) throws IOException {
        File f1 = new File(getApplicationContext().getCacheDir(), name1);
        f1.createNewFile();
        OutputStream os1 = new BufferedOutputStream(new FileOutputStream(f1));

        File imageString1 = new File(image1);
        Bitmap bitmap1 = new BitmapDrawable(getApplicationContext().getResources(), imageString1.getAbsolutePath()).getBitmap();
        bitmap1.compress(Bitmap.CompressFormat.JPEG, 70, os1);
        os1.close();


        iokihttp.uploadImage(shared.getString("user_id", "0"), f1, name1, getResources().getString(R.string.url) + "set_profile_picture", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (iokihttp.retry <= 3) {
                    call.clone();
                    iokihttp.retry++;
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.try_later), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                System.out.println("FAIL");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                hideLoading();
                iokihttp.retry = 0;
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    try {
                        JSONObject resJSON = new JSONObject(responseStr);
                        // JSONObject subresJSON = new JSONObject(resJSON.getString("data"));
                        if (Integer.parseInt(resJSON.get("error").toString()) == 1) {


                        } else {
                            runOnUiThread(new Runnable() {
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

    private void setInfo() {
        shared.getString("user_id", "");
        ((EditText) profile_info_include.findViewById(R.id.full_name)).setText(provider.getName());
        ((Spinner) profile_info_include.findViewById(R.id.gender)).setSelection(genderA.getPosition(provider.getGender_title()));
        ((EditText) profile_info_include.findViewById(R.id.whatsapp_phone)).setText(provider.getPhone());
//       System.out.println(countryA.getPosition(provider.getCountry())+"dDDDDD"+provider.getCountry());

        ((TextView) profile_info_include.findViewById(R.id.birthdate)).setText(provider.getBirth_date());
        date = provider.getBirth_date();
        ((EditText) profile_info_include.findViewById(R.id.certificates)).setText(provider.getCertificates());
        ((EditText) profile_info_include.findViewById(R.id.ex_desc)).setText(provider.getExp());
        ((EditText) profile_info_include.findViewById(R.id.year_ex)).setText(provider.getExp_years());
        if (!(provider.getImage_url().isEmpty())) {
            ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                    .writeDebugLogs()
                    .build();
            imageLoader.init(config);
            imageLoader.displayImage(getResources().getString(R.string.image_url) + provider.getImage_url(), (CircleImageView) profile_info_include.findViewById(R.id.profile_image));

        }
    }

    public void getCountries() {
        Countries.FeedReaderDbHelper dbHelper = new Countries.FeedReaderDbHelper(getApplicationContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                Countries.FeedEntry.GId,
                Countries.FeedEntry.name
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                Countries.FeedEntry.name + " DESC";

        Cursor cursor = db.query(
                Countries.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<edit_profile.StringWithTag> countries = new ArrayList<>();
        countries.add(new edit_profile.StringWithTag(getResources().getString(R.string.country), "0"));
        int counter = 0, index = -1;
        boolean enter = false;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(Countries.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(Countries.FeedEntry.name));
            if (name.equals(provider.getCountry())) {
                index = counter;
                enter = true;
            }
            countries.add(new edit_profile.StringWithTag(name, String.valueOf(id)));
            counter++;
        }
        cursor.close();
        fillCountries(countries, index, enter);
    }

    public void getCities(int country_id) {

        Cities.FeedReaderDbHelper dbHelper = new Cities.FeedReaderDbHelper(getApplicationContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                Cities.FeedEntry.GId,
                Cities.FeedEntry.name,
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                Cities.FeedEntry.name + " DESC";
        String selection = Cities.FeedEntry.country_id + " = ? ";
        String[] selectionArgs = {String.valueOf(country_id)};

        Cursor cursor = db.query(
                Cities.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<edit_profile.StringWithTag> cities = new ArrayList<>();
        cities.add(new edit_profile.StringWithTag(getResources().getString(R.string.city), "0"));
        int counter = 0, index = -1;
        boolean enter = false;
        System.out.println("___" + provider.getCity());
        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(Cities.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(Cities.FeedEntry.name));
            if (name.equals(provider.getCity())) {
                index = counter;
                enter = true;
            }
            cities.add(new edit_profile.StringWithTag(name, String.valueOf(id)));
            counter++;
        }
        cursor.close();

        fillCities(cities, index, enter);

    }

    public void getAreas(int city_id) {
        Areas.FeedReaderDbHelper dbHelper = new Areas.FeedReaderDbHelper(getApplicationContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                Areas.FeedEntry.GId,
                Areas.FeedEntry.name,
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                Areas.FeedEntry.name + " DESC";
        String selection = Areas.FeedEntry.city_id + " = ? ";
        String[] selectionArgs = {String.valueOf(city_id)};

        Cursor cursor = db.query(
                Areas.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        areas = new ArrayList<>();
        areasId = new ArrayList<>();
        //areas.add(getResources().getString(R.string.area));
        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(Areas.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(Areas.FeedEntry.name));
            areas.add(name);
            areasId.add(id);
        }
        cursor.close();
        fillAreas(areas);
    }

    public void getMainServices() {
        Main_services.FeedReaderDbHelper dbHelper = new Main_services.FeedReaderDbHelper(getApplicationContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                Main_services.FeedEntry.GId,
                Main_services.FeedEntry.name
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                Main_services.FeedEntry.name + " DESC";

        Cursor cursor = db.query(
                Main_services.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<edit_profile.StringWithTag> main_services = new ArrayList<>();
        main_services.add(new edit_profile.StringWithTag(getResources().getString(R.string.main_service), "0"));
        int counter = 0, index = -1;
        boolean enter = false;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(Main_services.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(Main_services.FeedEntry.name));
            if (name.equals(provider.getMain_service())) {
                index = counter;
                enter = true;
            }
            main_services.add(new edit_profile.StringWithTag(name, String.valueOf(id)));
            counter++;
        }
        cursor.close();
        fillMainServices(main_services, index, enter);
    }

    public void getSub_Services(int main_id) {
        Sub_services.FeedReaderDbHelper dbHelper = new Sub_services.FeedReaderDbHelper(getApplicationContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                Sub_services.FeedEntry.GId,
                Sub_services.FeedEntry.name,
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                Sub_services.FeedEntry.name + " DESC";
        String selection = Sub_services.FeedEntry.main_id + " = ? ";
        String[] selectionArgs = {String.valueOf(main_id)};

        Cursor cursor = db.query(
                Sub_services.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<edit_profile.StringWithTag> sub_services = new ArrayList<>();
        sub_services.add(new edit_profile.StringWithTag(getResources().getString(R.string.sub_service), "0"));
        int counter = 0, index = -1;
        boolean enter = false;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(Sub_services.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(Sub_services.FeedEntry.name));
            if (name.equals(provider.getSub_service())) {
                index = counter;
                enter = true;
            }

            sub_services.add(new edit_profile.StringWithTag(name, String.valueOf(id)));
            counter++;
        }
        cursor.close();
        fillSub_services(sub_services, index, enter);
    }

    private void fill_gender() {
        final Spinner gender = profile_info_include.findViewById(R.id.gender);
        genderA = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, getResources().getStringArray(R.array.gender_list));
        gender.setAdapter(genderA);
    }

    public void fillMainServices(final ArrayList main_services, final int index, final boolean enter) {

        final Spinner service = profile_info_include.findViewById(R.id.service);
        mainA = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, main_services);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        service.setAdapter(mainA);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enter)
                    service.setSelection(index + 1, true);
                getSub_Services(Integer.parseInt(((edit_profile.StringWithTag) (main_services.get(index + 1))).key));

            }
        });
        service.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("@@" + Integer.valueOf(((edit_profile.StringWithTag) (main_services.get(i))).key) + "ff");
                if (Integer.parseInt(((edit_profile.StringWithTag) (main_services.get(i))).key) != 0) {
                    getSub_Services(Integer.parseInt(((edit_profile.StringWithTag) (main_services.get(i))).key));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void fillCountries(final ArrayList countries, final int index, final boolean enter) {

        final Spinner country = profile_info_include.findViewById(R.id.country);
        countryA = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, countries);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country.setAdapter(countryA);
        System.out.println("(((((((((((" + index);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enter)
                    country.setSelection(index + 1, true);

                //System.out.println(Integer.parseInt(((edit_profile.StringWithTag) (countries.get(index))).key)+"++++++"+((StringWithTag) (countries.get(index))).value);

            }
        });
        getCities(Integer.parseInt(((edit_profile.StringWithTag) (countries.get(index + 1))).key));


        country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("@@" + Integer.valueOf(((edit_profile.StringWithTag) (countries.get(i))).key) + "ff");
                if (Integer.parseInt(((edit_profile.StringWithTag) (countries.get(i))).key) != 0) {
                    getCities(Integer.parseInt(((edit_profile.StringWithTag) (countries.get(i))).key));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void fillCities(final ArrayList cities, final int index, final boolean enter) {
        final Spinner city = profile_info_include.findViewById(R.id.city);
        cityA = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, cities);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        city.setAdapter(cityA);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (enter)
                        city.setSelection(index + 1, true);
                } catch (Exception e) {
                }

                //  getCities(Integer.parseInt(((edit_profile.StringWithTag) (cities.get(index))).key));

            }
        });
        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("@@" + Integer.valueOf(((edit_profile.StringWithTag) (cities.get(i))).key) + "ff");
                if (Integer.parseInt(((edit_profile.StringWithTag) (cities.get(i))).key) != 0) {
                    getAreas(Integer.parseInt(((edit_profile.StringWithTag) (cities.get(i))).key));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void fillSub_services(final ArrayList sub_services, final int index, final boolean enter) {

        final Spinner sub_service = profile_info_include.findViewById(R.id.sub_service);
        subA = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, sub_services);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sub_service.setAdapter(subA);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (enter)
                        sub_service.setSelection(index + 1, true);

                } catch (Exception e) {

                }
            }
        });
        sub_service.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("@@" + Integer.valueOf(((edit_profile.StringWithTag) (sub_services.get(i))).key) + "ff");
                if (Integer.parseInt(((edit_profile.StringWithTag) (sub_services.get(i))).key) != 0) {
                    //getSub_Services(Integer.valueOf(((StringWithTag) (sub_services.get(i))).key));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void fillAreas(final ArrayList<String> areas) {
        final MultiSpinner area = profile_info_include.findViewById(R.id.areas);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        area.setAdapterWithOutImage(this, areas, this);
        area.initMultiSpinner(this, area);
    }

    @Override
    public void OnMultiSpinnerItemSelected(List<String> chosenItems) {
        stringToIds(chosenItems);
    }

    private void stringToIds(List<String> items) {
        selecetedIds = new ArrayList<>();
        for (int x = 0; x < items.size(); x++) {
            for (int i = 0; i < areas.size(); i++) {
                if (areas.get(i).equals(items.get(x))) {
                    selecetedIds.add(areasId.get(i));
                }
            }
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
        ((TextView) (profile_info_include.findViewById(R.id.birthdate))).setText(date);
    }

    public void showLoading() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("inside run");
                findViewById(R.id.linear_main).setVisibility(View.GONE);
                findViewById(R.id.main_loading).setVisibility(View.VISIBLE);
                System.out.println("after inside run");

            }

        });
        System.out.println("after run");


    }

    public void hideLoading() {
        System.out.println("inside hide");

        final View main = findViewById(R.id.linear_main);
        final ProgressBar loading = findViewById(R.id.main_loading);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(true);
                main.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    main.setForeground(getDrawable(android.R.color.transparent));
                }
                loading.setVisibility(View.GONE);
            }
        });
    }

    private static class StringWithTag {
        String key;
        String value;

        StringWithTag(String value, String key) {
            this.key = key;
            this.value = value;
        }

        @NotNull
        @Override
        public String toString() {
            return value;
        }
    }

}
