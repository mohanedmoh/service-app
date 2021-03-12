package com.savvy.service;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.savvy.service.Network.Iokihttp;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Signup extends AppCompatActivity {
    Iokihttp iokihttp;
    int code = 0;
    SharedPreferences shared;
    View phone_layout_include, otp_layout_include;
    boolean doubleBackToExitPressedOnce = false;
    int exist = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        init();
    }
    private boolean main = true;
    private Button verify, verify_pin;
    private void init() {
        iokihttp = new Iokihttp();
        shared = this.getSharedPreferences("com.example.service", Context.MODE_PRIVATE);
        phone_layout_include = findViewById(R.id.phoneLayout);
      //  otp_layout_include = findViewById(R.id.otcLayout);
        verify = phone_layout_include.findViewById(R.id.btn_send);
     //   verify_pin = otp_layout_include.findViewById(R.id.verify_pin);
        /*verify_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verify_pin(view);
            }
        });*/
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    send();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @SuppressLint("SetTextI18n")
    public void openpinLayout(int code) {
        this.code = code;
        final View phoneLayout = findViewById(R.id.phoneLayout);
        final View otcLayout = findViewById(R.id.otcLayout);
        TextView enter_otp = findViewById(R.id.enter_otp);
        enter_otp.setText(getString(R.string.enter_verfication) + ": " + shared.getString("phone_key", "") + "" + shared.getString("phone", ""));
        animateLayout(phoneLayout, otcLayout);
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
        before.animate().alpha(0f).setDuration(700).setListener(animatorListener);
    }
    @Override
    public void onBackPressed() {
        if (main) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                finish();
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

        } else {
            final LinearLayout phoneLayout = findViewById(R.id.phoneLayout);
            final LinearLayout otcLayout = findViewById(R.id.otcLayout);


            phoneLayout.setVisibility(View.VISIBLE);
            otcLayout.setVisibility(View.GONE);

            main = true;
            phoneLayout.setAlpha(1f);
        }
    }
    /*private void verify_pin(View view) {
        Pinview pin = findViewById(R.id.pinView);
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        if (pin.getValue().isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill_error), Toast.LENGTH_LONG).show();
            return;
        }
        try {
            System.out.println("PIN=" + pin.getValue());
            subJSON.put("id", shared.getString("user_id", ""));
            //subJSON.put("token", token);
            subJSON.put("pin", pin.getValue());
            // subJSON.put("device", 1);
            json.put("data", subJSON);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        showLoading();
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            iokihttp.post(getString(R.string.url) + "verifyPin", json.toString(), new Callback() {
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
                                finish();
                                openProfileInfo();
                                //openPassword();
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
    }*/
    private String encrypt(String password) throws NoSuchAlgorithmException {
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(password.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void openProfileInfo(int ResponseCode) {
        System.out.println("code="+ResponseCode+"exist="+exist);
        if (ResponseCode == 1 && exist == 1) {
            shared.edit().putBoolean("login", true).apply();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }
        else if (ResponseCode == 1 && exist == 0) {
            Intent i = new Intent(getApplicationContext(), Profile_information.class);
            startActivity(i);
            finish();
        }
        else if (ResponseCode == 2){
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 Toast.makeText(getApplicationContext(),getResources().getString(R.string.wrong_phone_pass),Toast.LENGTH_LONG).show();
             }
         });
        }
    }
    public void showLoading() {
        final View main = findViewById(R.id.login_layout);
        final ProgressBar loading = findViewById(R.id.login_loading);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(false);

                main.setVisibility(View.GONE);

                loading.setVisibility(View.VISIBLE);
            }
        });
    }
    public void hideLoading() {
        final View main = findViewById(R.id.login_layout);
        final ProgressBar loading = findViewById(R.id.login_loading);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(true);

                main.setVisibility(View.VISIBLE);

                loading.setVisibility(View.GONE);
            }
        });
    }
    protected void send() throws NoSuchAlgorithmException {
        CountryCodePicker countryCodePicker = phone_layout_include.findViewById(R.id.countryPicker);
        EditText phonenumber = phone_layout_include.findViewById(R.id.phone);
        String[] phone = new String[2];
        phone[0] = countryCodePicker.getSelectedCountryCode();
        phone[1] = phonenumber.getText().toString();
        String password=encrypt(((EditText)phone_layout_include.findViewById(R.id.password)).getText().toString());
        String passValue=((EditText)phone_layout_include.findViewById(R.id.password)).getText().toString();

        exist=0;
        if (phone[1].isEmpty()|| passValue.isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill_error), Toast.LENGTH_LONG).show();
            return;
        }


        final String[] finalPhone = phone;

                        showLoading();
                        JSONObject json = new JSONObject();
                        JSONObject subJSON = new JSONObject();
                        try {
                            System.out.println("country code number before:" + finalPhone[0]);
                            finalPhone[0] = (finalPhone[0].replace("+", "")).trim();
                            subJSON.put("country_code", finalPhone[0]);
                            subJSON.put("phone", finalPhone[1]);
                            subJSON.put("password",password);
                            json.put("data", subJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (iokihttp.isNetworkConnected(getApplicationContext())) {
                            iokihttp.post(getString(R.string.url) + "newProvider", json.toString(), new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    System.out.println("FAIL");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                        }
                                    });
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


                                                shared.edit().putString("phone_key", finalPhone[0]).apply();
                                                shared.edit().putString("phone", finalPhone[1]).apply();
                                                if(Integer.parseInt(resJSON.getString("code"))==1) {
                                                    final JSONObject subresJSON = new JSONObject(resJSON.getString("data"));
                                                    exist = subresJSON.getInt("exist");
                                                    shared.edit().putString("user_id", subresJSON.getString("id")).apply();
                                                    openProfileInfo(Integer.parseInt(resJSON.get("code").toString()));
                                                }
                                                else{
                                                    hideLoading();
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getApplicationContext(), getString(R.string.wrong_phone_pass), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }

                                            } else {
                                                hideLoading();
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
    }

