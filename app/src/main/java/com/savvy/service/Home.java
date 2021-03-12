package com.savvy.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.savvy.service.Models.Provider;
import com.savvy.service.Network.Iokihttp;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Home.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View provider_info_include, packages_include, subscription_include;
    Iokihttp iokihttp;
    SharedPreferences shared;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    View view;
    int packageType;
    Provider provider;
    private double pickedPrice;
    private OnFragmentInteractionListener mListener;
    public Home() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        init();
        return view;
    }

    private void init() {
        shared = getActivity().getSharedPreferences("com.example.service", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();
        packages_include = view.findViewById(R.id.packages_include);
        subscription_include = view.findViewById(R.id.subscription_include);
        provider_info_include = view.findViewById(R.id.provider_info_include);

        packages_include.findViewById(R.id.classic_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePackageLinearBg(view);
            }
        });
        packages_include.findViewById(R.id.vip_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePackageLinearBg(view);
            }
        });
        packages_include.findViewById(R.id.subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    subscribe();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        provider_info_include.findViewById(R.id.edit_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openProfileEditor();
            }
        });
        try {
            getData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openProfileEditor() {
        Intent i = new Intent(getContext(), edit_profile.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("provider", provider);
        i.putExtra("provider", bundle);
        startActivityForResult(i, 111);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111) {
            if (resultCode == 1) {
                try {
                    getData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void changePackageLinearBg(View view) {
        View p1 = packages_include.findViewById(R.id.package1);
        View p2 = packages_include.findViewById(R.id.package2);
        CheckBox vipC = packages_include.findViewById(R.id.vip_check);
        CheckBox classicC = packages_include.findViewById(R.id.classic_check);
        TextView selected = packages_include.findViewById(R.id.package_selected);

        if (view.getId() == classicC.getId()) {
            selected.setText(getResources().getString(R.string.short_month));
            vipC.setChecked(false);
            classicC.setChecked(true);
            packageType = 1;
            //pickedPrice = price;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                p1.setElevation(3f);
                p2.setElevation(0f);
            }

        } else if (view.getId() == vipC.getId()) {
            selected.setText(getResources().getString(R.string.long_month));
            vipC.setChecked(true);
            classicC.setChecked(false);
            packageType = 2;
            // pickedPrice = price[1];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                p1.setElevation(0f);
                p2.setElevation(3f);
            }

        }
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    private void getData() throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        subJSON.put("id", shared.getString("user_id", ""));
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getActivity().getApplicationContext())) {
            showLoading();
            iokihttp.post(getString(R.string.url) + "getProfile", json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (iokihttp.retry <= 3) {
                        call.clone();
                        iokihttp.retry++;
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoading();
                                Toast.makeText(getContext(), getResources().getString(R.string.try_later), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    hideLoading();
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {


                            JSONObject resJSON = new JSONObject(responseStr);

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                final JSONObject subresJSON = resJSON.getJSONObject("data");
                                createModel(subresJSON);
                                //   checkVersion(Double.valueOf(shared.getString("version", "0")), subresJSON.getJSONObject(0).getDouble("version"));
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
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

    private void getPackagePrices() throws JSONException {
        JSONObject json = new JSONObject();
        final JSONObject subJSON = new JSONObject();
        //subJSON.put("id", shared.getString("user_id", ""));
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getActivity().getApplicationContext())) {
            showLoading();
            iokihttp.post(getString(R.string.url) + "getPackages", json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    hideLoading();
                    System.out.println("FAIL");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    hideLoading();
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {


                            JSONObject resJSON = new JSONObject(responseStr);

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                final JSONArray subresJSON = resJSON.getJSONArray("data");
                                fillPackages(subresJSON);
                                // createModel(subresJSON);
                                //   checkVersion(Double.valueOf(shared.getString("version", "0")), subresJSON.getJSONObject(0).getDouble("version"));
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
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
    @SuppressLint("SetTextI18n")
    private void fillPackages(final JSONArray jsonArray) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ((TextView) packages_include.findViewById(R.id.classic_price)).setText(getActivity().getResources().getString(R.string.priceString) + " " + jsonArray.getJSONObject(0).getString("price"));
                    ((TextView) packages_include.findViewById(R.id.vip_price)).setText(getActivity().getResources().getString(R.string.priceString) + " " + jsonArray.getJSONObject(1).getString("price"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                packages_include.setVisibility(View.VISIBLE);
            }
        });

    }
    public void showLoading() {
        final View main = view.findViewById(R.id.layout);
        final ProgressBar loading = view.findViewById(R.id.login_loading);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(false);

                main.setVisibility(View.GONE);

                loading.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideLoading() {
        final View main = view.findViewById(R.id.layout);
        final ProgressBar loading = view.findViewById(R.id.login_loading);
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    main.setClickable(true);

                    main.setVisibility(View.VISIBLE);

                    loading.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createModel(JSONObject jsonObject) throws JSONException {
        System.out.println("INSIDE MODEL" + jsonObject.toString());
        provider = new Provider();
        provider.setId(jsonObject.getString("id"));
        if (jsonObject.getString("name").isEmpty() || jsonObject.getString("name") == null) {
            shared.edit().putBoolean("login", false).apply();
            Intent i = new Intent(getContext(), Signup.class);
            startActivity(i);
            Objects.requireNonNull(getActivity()).finish();
            return;
        }
        provider.setName(jsonObject.getString("name"));
        shared.edit().putString("username", provider.getName()).apply();
        provider.setGender_title(jsonObject.getString("gender_title"));
        provider.setAge(jsonObject.getString("birth_date"));
        provider.setBirth_date(jsonObject.getString("date"));
        provider.setPhone(jsonObject.getString("country_code") + jsonObject.getString("phone"));
        provider.setCountry((jsonObject.getJSONObject("country")).getString("name"));
        provider.setCity((jsonObject.getJSONObject("city")).getString("name"));
        provider.setSubscription(jsonObject.getJSONObject("subscription").getString("status_id"));
        JSONArray areas = jsonObject.getJSONArray("areas");
        String[] areasS = new String[areas.length()];
        String[] areasid = new String[areas.length()];

        for (int i = 0; i < areas.length(); i++) {
            JSONObject jsonObject1 = areas.getJSONObject(i);
            areasid[i] = jsonObject1.getJSONObject("pivot").getString("area_id");
            areasS[i] = jsonObject1.getString("name");
        }
        provider.setVerified(jsonObject.getString("verified"));
        provider.setAreasId(areasid);
        provider.setAreasS(areasS);
        provider.setCertificates(jsonObject.getString("certificate").equals("null") ? "" : jsonObject.getString("certificate"));
        provider.setMain_service(jsonObject.getJSONObject("main_service").getString("name"));
        provider.setSub_service(jsonObject.getJSONObject("sub_service").getString("name"));
        shared.edit().putString("job", provider.getSub_service()).apply();
        provider.setQualityRate(jsonObject.getJSONObject("ratings").getString("quality"));
        provider.setPriceRate(jsonObject.getJSONObject("ratings").getString("price"));
        provider.setTimeRate(jsonObject.getJSONObject("ratings").getString("time"));
        provider.setBehaviorRate(jsonObject.getJSONObject("ratings").getString("way"));
        provider.setPeopleRate(jsonObject.getJSONObject("ratings").getString("people_no"));
        provider.setImage_url(jsonObject.getString("img_url"));
        provider.setExp(jsonObject.getString("experience").equals("null") ? "" : jsonObject.getString("experience"));
        provider.setExp_years(jsonObject.getString("work_years").equals("null") ? "" : jsonObject.getString("work_years"));
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (Integer.parseInt(provider.getSubscription())) {
                        case 0: {
                            try {
                                getPackagePrices();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                        case 1: {
                            provider_info_include.setVisibility(View.VISIBLE);
                            setData(provider);
                        }
                        break;
                        default: {
                            subscription_include.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    private void subscribe() throws JSONException {
        if (packageType == 0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), getResources().getString(R.string.please_choose_package), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            sendSubscribeInfo(packageType);
        }
    }

    private void sendSubscribeInfo(int packageType) throws JSONException {
        JSONObject json = new JSONObject();
        final JSONObject subJSON = new JSONObject();
        subJSON.put("id", shared.getString("user_id", ""));
        subJSON.put("package_id", packageType);
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getActivity().getApplicationContext())) {
            showLoading();
            iokihttp.post(getString(R.string.url) + "subscribe", json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    hideLoading();
                    System.out.println("FAIL");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    hideLoading();
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {


                            JSONObject resJSON = new JSONObject(responseStr);

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                //final JSONObject subresJSON = resJSON.getJSONObject("data");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        packages_include.setVisibility(View.GONE);
                                        subscription_include.setVisibility(View.VISIBLE);
                                    }
                                });

                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
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

    private void setData(Provider provider) {
        ((TextView) provider_info_include.findViewById(R.id.provider_name)).setText(provider.getName());
        ((TextView) provider_info_include.findViewById(R.id.provider_age)).setText(provider.getAge());
        ((TextView) provider_info_include.findViewById(R.id.provider_gender)).setText(provider.getGender_title());
        ((TextView) provider_info_include.findViewById(R.id.provider_phone)).setText(provider.getPhone());
        ((TextView) provider_info_include.findViewById(R.id.provider_job)).setText(provider.getSub_service());
        ((TextView) provider_info_include.findViewById(R.id.provider_main)).setText(provider.getMain_service());
        ((TextView) provider_info_include.findViewById(R.id.provider_certificate)).setText(provider.getCertificates());
        ((TextView) provider_info_include.findViewById(R.id.exp)).setText(provider.getExp());
        ((TextView) provider_info_include.findViewById(R.id.exp_year)).setText(provider.getExp_years());
        ((TextView) provider_info_include.findViewById(R.id.verified)).setText(provider.getVerified().equals("1") ? getActivity().getResources().getString(R.string.verified) : "");
        if (!(provider.getVerified().equals("1"))) {
            hideVerified();
        }
        ((TextView) provider_info_include.findViewById(R.id.num_of_rate)).setText(provider.getPeopleRate());
        ((TextView) provider_info_include.findViewById(R.id.exp_year)).setText(provider.getExp_years());
        ((ImageView) provider_info_include.findViewById(R.id.priceRate)).setImageDrawable(getActivity().getResources().getDrawable(getStar(provider.getPriceRate())));
        ((ImageView) provider_info_include.findViewById(R.id.qualityRate)).setImageDrawable(getActivity().getResources().getDrawable(getStar(provider.getQualityRate())));
        ((ImageView) provider_info_include.findViewById(R.id.timeRate)).setImageDrawable(getActivity().getResources().getDrawable(getStar(provider.getTimeRate())));
        ((ImageView) provider_info_include.findViewById(R.id.behaviorRate)).setImageDrawable(getActivity().getResources().getDrawable(getStar(provider.getBehaviorRate())));
        if (!(provider.getImage_url().isEmpty())) {
            ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
                    .writeDebugLogs()
                    .build();
            imageLoader.init(config);
            imageLoader.displayImage(getResources().getString(R.string.image_url) + provider.getImage_url(), (ImageView) provider_info_include.findViewById(R.id.provider_image));

        }

    }
    private void hideVerified() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.findViewById(R.id.ver1).setVisibility(View.GONE);
                view.findViewById(R.id.ver2).setVisibility(View.GONE);
            }
        });

    }
    private int getStar(String num) {
        int number = Integer.parseInt(num);
        switch (number) {
            case 2:
                return R.drawable.twostar;
            case 3:
                return R.drawable.threestar;
            case 4:
                return R.drawable.fourstar;
            case 5:
                return R.drawable.fivestar;
            default:
                return R.drawable.onestar;
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
