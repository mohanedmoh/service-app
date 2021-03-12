package com.savvy.service;

import android.animation.Animator;
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
import com.savvy.service.Network.Iokihttp;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.hbb20.CountryCodePicker;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Profile_information extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, MultiSelectionSpinnerDialog.OnMultiSpinnerSelectionListener {
    public DatePickerDialog dpd;
    public Calendar now;
    View free_package_include, profile_info_include, packages_include;
    String date = "";
    List<Object> list;
    LIFOqueue lifo;
    View before = null;
    View after = null;
    private boolean main = true;
    Iokihttp iokihttp;
    SharedPreferences shared;
    ArrayList<String> areas;
    ArrayList<Integer> areasId, selecetedIds;
    int req_code;
    ArrayList<String> profile_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_information);
        //setTheme(R.style.AppTheme);
        init();
    }
    private void init() {
        free_package_include = findViewById(R.id.free_package_include);
        profile_info_include = findViewById(R.id.profile_info_include);
        packages_include = findViewById(R.id.packages_include);
        now = Calendar.getInstance();
        iokihttp = new Iokihttp();
        shared = this.getSharedPreferences("com.example.service", Context.MODE_PRIVATE);

        dpd = DatePickerDialog.newInstance(
                Profile_information.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.showYearPickerFirst(true);

        profile_info_include.findViewById(R.id.birthdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        profile_info_include.findViewById(R.id.birthdate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        findViewById(R.id.next_form).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openNextForm();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.back_form).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    onBackPressed();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        profile_info_include.findViewById(R.id.profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCam(101);
            }
        });
        //setList(4);
        list = new ArrayList<Object>();
        list.add(R.id.profile_info_include);
        list.add(R.id.free_package_include);


        lifo = new LIFOqueue(list);
        lifo.pop();
        getCountries();
        getMainServices();
        getAreas(0);
        getCities(0);
        getSub_Services(0);
        fill_gender();
    }

    private void openCam(int code) {
        Pix.start(Profile_information.this, Options.init().setRequestCode(code));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Profile_information.RESULT_OK && requestCode == 101) {
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

        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(Profile_information.this, Options.init().setRequestCode(101));
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
    private void openNextForm() {
        System.out.println(lifo.scndsize() + "=SIZE=" + lifo.size());
        if (lifo.size() == 0) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            shared.edit().putBoolean("login", true).apply();
            finish();
            startActivity(i);

        } else if (lifo.size() != 2) {
            if (verify()) {
                sendProfileInfo();
            }
        } else {
            before = findViewById((Integer) lifo.scndpeek());
            after = null;
            after = findViewById((Integer) lifo.pop());
            animateLayout(before, after);
        }
    }

    public void animateLayout(final View before, final View after) {
        Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                System.out.println("SHOW " + after.getId());
                before.setVisibility(View.GONE);
                after.setVisibility(View.VISIBLE);
                //  finalAfter.animate().alpha(1f).setDuration(700);

                main = false;


            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
        before.animate().alpha(0f).setDuration(0).setListener(animatorListener);
        //hideLoading();
    }

    public void onBackPressed() {
        if (main) {
            finish();
        } else {
            findViewById(R.id.next_linear).setVisibility(View.VISIBLE);
            View after = findViewById((Integer) lifo.scndpop());
            View before = findViewById((Integer) lifo.scndpeek());
            before.setVisibility(View.VISIBLE);
            after.setVisibility(View.GONE);


            before.setAlpha(1f);
        }
        if (lifo.scndsize() == 1) {
            main = true;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
        ((TextView) (profile_info_include.findViewById(R.id.birthdate))).setText(date);
    }

    private void fill_gender() {
        final Spinner gender = profile_info_include.findViewById(R.id.gender);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, getResources().getStringArray(R.array.gender_list));
        gender.setAdapter(adapter);
    }

    private Boolean verify() {
        String name = ((EditText) profile_info_include.findViewById(R.id.full_name)).getText().toString();
        String gender = String.valueOf(((Spinner) profile_info_include.findViewById(R.id.gender)).getSelectedItemId());
        String country_id = ((StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.country)).getSelectedItem())).key;
        String city_id = ((StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.city)).getSelectedItem())).key;
        String main_service = ((StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.service)).getSelectedItem())).key;
        String sub_service = ((StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.sub_service)).getSelectedItem())).key;
        String birth_date = date;
        System.out.println("!!!!!!!" + name + "F" + gender + "F" + name + "F" + country_id + "F" + city_id + "F" + main_service + "F" + sub_service + "F" + birth_date);
        //String career = ((EditText) profile_info_include.findViewById(R.id.certificates)).getText().toString();

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
        try {
            System.out.println(selecetedIds + "gender=" + (((Spinner) findViewById(R.id.gender)).getSelectedItemId() - 1));
            subJSON.put("id", shared.getString("user_id", ""));
            subJSON.put("name", ((EditText) profile_info_include.findViewById(R.id.full_name)).getText().toString());
            subJSON.put("gender", String.valueOf(((Spinner) profile_info_include.findViewById(R.id.gender)).getSelectedItemId()));
            subJSON.put("country_code", ((CountryCodePicker) profile_info_include.findViewById(R.id.countryPicker)).getSelectedCountryCode());
            subJSON.put("phone", ((EditText) profile_info_include.findViewById(R.id.whatsapp_phone)).getText().toString());
            subJSON.put("country_id", ((StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.country)).getSelectedItem())).key);
            subJSON.put("city_id", ((StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.city)).getSelectedItem())).key);
            subJSON.putOpt("areas", selecetedIds);
            subJSON.put("main_services_id", ((StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.service)).getSelectedItem())).key);
            subJSON.put("sub_services_id", ((StringWithTag) (((Spinner) profile_info_include.findViewById(R.id.sub_service)).getSelectedItem())).key);
            subJSON.put("birth_date", date);
            subJSON.put("certificate", ((EditText) profile_info_include.findViewById(R.id.certificates)).getText());
            subJSON.put("experience", ((EditText) profile_info_include.findViewById(R.id.ex_desc)).getText());
            subJSON.put("work_years", ((EditText) profile_info_include.findViewById(R.id.year_ex)).getText());
            System.out.println("ARRAY=" + subJSON.toString());
            json.put("data", subJSON);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            iokihttp.post(getString(R.string.url) + "signup", json.toString(), new Callback() {
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

                                before = findViewById((Integer) lifo.scndpeek());
                                after = null;
                                after = findViewById((Integer) lifo.pop());
                                animateLayout(before, after);                                //openPassword();
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
        ArrayList<StringWithTag> countries = new ArrayList<>();
        countries.add(new StringWithTag(getResources().getString(R.string.country), "0"));
        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(Countries.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(Countries.FeedEntry.name));
            countries.add(new StringWithTag(name, String.valueOf(id)));
        }
        cursor.close();
        fillCountries(countries);
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
        ArrayList<StringWithTag> cities = new ArrayList<>();
        cities.add(new StringWithTag(getResources().getString(R.string.city), "0"));
        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(Cities.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(Cities.FeedEntry.name));
            cities.add(new StringWithTag(name, String.valueOf(id)));
        }
        cursor.close();
        fillCities(cities);
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
        ArrayList<StringWithTag> main_services = new ArrayList<>();
        main_services.add(new StringWithTag(getResources().getString(R.string.main_service), "0"));
        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(Main_services.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(Main_services.FeedEntry.name));
            main_services.add(new StringWithTag(name, String.valueOf(id)));
        }
        cursor.close();
        fillMainServices(main_services);
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
        ArrayList<StringWithTag> sub_services = new ArrayList<>();
        sub_services.add(new StringWithTag(getResources().getString(R.string.sub_service), "0"));
        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(Sub_services.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(Sub_services.FeedEntry.name));
            sub_services.add(new StringWithTag(name, String.valueOf(id)));
        }
        cursor.close();
        fillSub_services(sub_services);
    }

    public void fillMainServices(final ArrayList main_services) {

        final Spinner service = profile_info_include.findViewById(R.id.service);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, main_services);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        service.setAdapter(adapter);
        service.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("@@" + Integer.valueOf(((StringWithTag) (main_services.get(i))).key) + "ff");
                if (Integer.valueOf(((StringWithTag) (main_services.get(i))).key) != 0) {
                    getSub_Services(Integer.valueOf(((StringWithTag) (main_services.get(i))).key));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void fillCountries(final ArrayList countries) {

        final Spinner country = profile_info_include.findViewById(R.id.country);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, countries);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country.setAdapter(adapter);
        country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("@@" + Integer.valueOf(((StringWithTag) (countries.get(i))).key) + "ff");
                if (Integer.valueOf(((StringWithTag) (countries.get(i))).key) != 0) {
                    getCities(Integer.valueOf(((StringWithTag) (countries.get(i))).key));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void fillCities(final ArrayList cities) {

        final Spinner city = profile_info_include.findViewById(R.id.city);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, cities);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        city.setAdapter(adapter);
        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("@@" + Integer.valueOf(((StringWithTag) (cities.get(i))).key) + "ff");
                if (Integer.valueOf(((StringWithTag) (cities.get(i))).key) != 0) {
                    getAreas(Integer.valueOf(((StringWithTag) (cities.get(i))).key));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void fillSub_services(final ArrayList sub_services) {

        final Spinner sub_service = profile_info_include.findViewById(R.id.sub_service);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, sub_services);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sub_service.setAdapter(adapter);
        sub_service.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("@@" + Integer.valueOf(((StringWithTag) (sub_services.get(i))).key) + "ff");
                if (Integer.valueOf(((StringWithTag) (sub_services.get(i))).key) != 0) {
                    //getSub_Services(Integer.valueOf(((StringWithTag) (sub_services.get(i))).key));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void fillAreas(final ArrayList areas) {

        final MultiSpinner area = profile_info_include.findViewById(R.id.areas);

        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        area.setAdapterWithOutImage(this, areas, this);
        area.initMultiSpinner(this, area);
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
    private static class StringWithTag {
        public String key;
        public String value;

        public StringWithTag(String value, String key) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
