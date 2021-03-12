package com.savvy.service;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.savvy.service.Network.Iokihttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Packages_info#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Packages_info extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View view;
    Iokihttp iokihttp;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Packages_info() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Packages_info.
     */
    // TODO: Rename and change types and number of parameters
    public static Packages_info newInstance(String param1, String param2) {
        Packages_info fragment = new Packages_info();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void init() throws JSONException {
        iokihttp = new Iokihttp();
        getPackagePrices();
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
                    ((TextView) view.findViewById(R.id.classic_price)).setText(getActivity().getResources().getString(R.string.priceString) + " " + jsonArray.getJSONObject(0).getString("price"));
                    ((TextView) view.findViewById(R.id.vip_price)).setText(getActivity().getResources().getString(R.string.priceString) + " " + jsonArray.getJSONObject(1).getString("price"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                view.setVisibility(View.VISIBLE);
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
        view = inflater.inflate(R.layout.fragment_packages_info, container, false);
        try {
            init();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}
