package com.pjm9355.hospitalmap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class DetailActivity extends Activity {
    private HospitalItem hospitalItem;
    private TextView tvName, tvClCdNm, tvAddr, tvTelNo, tvUrl, tvEstbDate, tvDrTotCnt, tvSDrCnt, tvGDrCnt, tvResidentCnt, tvInternCnt;
    private View telNoView, hospUrlView, estbDateView, detailInfoView, allView, loadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_detail);

        Intent intent = getIntent();
        hospitalItem = (HospitalItem) intent.getSerializableExtra("hospitalItem");

        tvName = findViewById(R.id.hosp_name_textview);
        tvClCdNm = findViewById(R.id.class_code_name_textview);
        tvAddr = findViewById(R.id.address_textview);
        tvTelNo = findViewById(R.id.tel_no_textview);
        tvUrl = findViewById(R.id.hosp_url_textview);
        tvEstbDate = findViewById(R.id.estb_date_textview);
        tvDrTotCnt = findViewById(R.id.doctor_tot_cnt_textview);
        tvSDrCnt = findViewById(R.id.specialist_dr_cnt_textview);
        tvGDrCnt = findViewById(R.id.general_dr_cnt_textview);
        tvResidentCnt = findViewById(R.id.resident_cnt_textview);
        tvInternCnt = findViewById(R.id.intern_cnt_textview);
        telNoView = findViewById(R.id.tel_no_view);
        hospUrlView = findViewById(R.id.hosp_url_view);
        estbDateView = findViewById(R.id.estb_date_view);
        detailInfoView = findViewById(R.id.detail_info_view);
        allView = findViewById(R.id.ll_all);
        loadingView = findViewById(R.id.loading_view);

        TextView titleTextView = findViewById(R.id.detail_title_textview);
        titleTextView.setText(hospitalItem.getHospName());

        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadingView.setVisibility(View.VISIBLE);
        detailInfoView.setVisibility(View.GONE);
        getHospDetailInfo(hospitalItem.getYkiho());

    }

    public void getHospDetailInfo(String ykiho) {
        RequestQueue requestQueue;
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        Integer numOfRows = 100;

        String url = "http://apis.data.go.kr/B551182/medicInsttDetailInfoService/getDetailInfo?pageNo=1&numOfRows=50"
                + "&ykiho=" + ykiho
                + "&ServiceKey=Q%2BbQw%2FUNPpDxP9hAGr3SQzR71t%2BCRCoDcFtPYmxVpEdlObYNjUINxMD3hurNngT3r19ae%2FDHw7t%2B5YhzIm2EuA%3D%3D&_type=json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // 응답
                        System.out.println("응답 : " + response);     // for Debug
                        HospDetailInfoItem item = JSONParse(response);

                        // UI 적용
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (item != null) {
                                    detailInfoView.setVisibility(View.VISIBLE);

                                    TextView tvPlace, tvRcvWeek, tvLunchWeek, tvRcvSat, tvLunchSat, tvSun, tvHoli, tvEmyDay, tvEmyNgt, tvParkQty, tvParkEtc;
                                    View rcvView, emyView, parkView;

                                    tvPlace = findViewById(R.id.place_textview);
                                    tvRcvWeek = findViewById(R.id.rcv_week_textview);
                                    tvLunchWeek = findViewById(R.id.lunch_week_textview);
                                    tvRcvSat = findViewById(R.id.rcv_sat_textview);
                                    tvLunchSat = findViewById(R.id.lunch_sat_textview);
                                    tvSun = findViewById(R.id.sun_textview);
                                    tvHoli = findViewById(R.id.holi_textview);
                                    tvEmyDay = findViewById(R.id.emy_day_textview);
                                    tvEmyNgt = findViewById(R.id.emy_ngt_textview);
                                    tvParkQty = findViewById(R.id.park_qty_textview);
                                    tvParkEtc = findViewById(R.id.park_etc_textview);
                                    rcvView = findViewById(R.id.rcv_view);
                                    emyView = findViewById(R.id.emy_view);
                                    parkView = findViewById(R.id.park_view);

                                    if (item.getPlace() != null) {
                                        tvPlace.setText(item.getPlace());
                                        tvPlace.setVisibility(View.VISIBLE);
                                    }

                                    if (item.getRcvWeek() == null && item.getRcvSat() == null
                                            && item.getNoTrmtSun() == null && item.getNoTrmtHoli() == null) {
                                        rcvView.setVisibility(View.GONE);
                                    }
                                    if (item.getRcvWeek() != null)
                                        tvRcvWeek.setText(getResources().getString(R.string.text_rcv_week) + item.getRcvWeek());
                                    else tvRcvWeek.setVisibility(View.GONE);
                                    if (item.getLunchWeek() != null)
                                        tvLunchWeek.setText(getResources().getString(R.string.text_lunch_week) + item.getLunchWeek());
                                    else tvLunchWeek.setVisibility(View.GONE);
                                    if (item.getRcvSat() != null)
                                        tvRcvSat.setText(getResources().getString(R.string.text_rcv_sat) + item.getRcvSat());
                                    else tvRcvSat.setVisibility(View.GONE);
                                    if (item.getLunchSat() != null)
                                        tvLunchSat.setText(getResources().getString(R.string.text_lunch_sat) + item.getLunchSat());
                                    else tvLunchSat.setVisibility(View.GONE);
                                    if (item.getNoTrmtSun() != null)
                                        tvSun.setText(getResources().getString(R.string.text_sun) + item.getNoTrmtSun());
                                    else tvSun.setVisibility(View.GONE);
                                    if (item.getNoTrmtHoli() != null)
                                        tvHoli.setText(getResources().getString(R.string.text_holi) + item.getNoTrmtHoli());
                                    else tvHoli.setVisibility(View.GONE);

                                    if(item.getEmyDayYn() == null && item.getEmyNgtYn() == null) {
                                        emyView.setVisibility(View.GONE);
                                    }
                                    if (item.getEmyDayYn() != null)
                                        tvEmyDay.setText(getResources().getString(R.string.text_emy_day) + item.getEmyDayYn());
                                    else tvEmyDay.setVisibility(View.GONE);
                                    if (item.getEmyNgtYn() != null)
                                        tvEmyNgt.setText(getResources().getString(R.string.text_emy_ngt) + item.getEmyNgtYn());
                                    else tvEmyNgt.setVisibility(View.GONE);

                                    if (item.getParkXpnsYn() != null && item.getParkXpnsYn().equals("Y")) {
                                        if (item.getParkQty() != null)
                                            tvParkQty.setText(getResources().getString(R.string.text_park_qty) + " " + item.getParkQty().toString() + "대");
                                        else tvParkQty.setVisibility(View.GONE);
                                        if (item.getParkEtc() != null)
                                            tvParkEtc.setText(item.getParkEtc());
                                        else tvParkEtc.setVisibility(View.GONE);
                                    } else parkView.setVisibility(View.GONE);
                                }

                                // 텍스트 채우기
                                tvName.setText(hospitalItem.getHospName());
                                tvAddr.setText(hospitalItem.getAddress());

                                String telno = hospitalItem.getTelNo();
                                if (telno==null || telno.isEmpty()) {
                                    telNoView.setVisibility(View.GONE);
                                } else {
                                    tvTelNo.setText(telno);
                                    telNoView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Uri uri = Uri.parse("tel:" + telno);
                                            Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                                            startActivity(intent);
                                        }
                                    });
                                }

                                String url = hospitalItem.getHospUrl();
                                if (url==null || url.isEmpty()) {
                                    hospUrlView.setVisibility(View.GONE);
                                } else {
                                    tvUrl.setPaintFlags(tvUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                                    tvUrl.setText(url);
                                    hospUrlView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Uri uri = Uri.parse(url);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            startActivity(intent);
                                        }
                                    });
                                }

                                String date = hospitalItem.getEstbDate();
                                if (date==null || date.isEmpty()) {
                                    estbDateView.setVisibility(View.GONE);
                                } else {
                                    tvEstbDate.setText(date);
                                }

                                tvDrTotCnt.setText(hospitalItem.getDoctorTotalCnt().toString());
                                tvSDrCnt.setText(hospitalItem.getSpecialistDoctorCnt().toString());
                                tvGDrCnt.setText(hospitalItem.getGeneralDoctorCnt().toString());
                                tvResidentCnt.setText(hospitalItem.getResidentCnt().toString());
                                tvInternCnt.setText(hospitalItem.getInternCnt().toString());

                                // DB에서 진료과 검색
                                DBHelper helper = new DBHelper(DetailActivity.this, "hdb.db", null, 1);
                                SQLiteDatabase db = helper.getWritableDatabase();

                                String mQuery = "SELECT * FROM tb_dgsbjt WHERE ykiho = ?";
                                Cursor c = db.rawQuery(mQuery, new String[]{hospitalItem.getYkiho()});
                                String dgsbjtStr = "";

                                while (c.moveToNext()) {
                                    dgsbjtStr += c.getString(c.getColumnIndex("dgsbjtCdNm")) + ", ";
                                }
                                if(!dgsbjtStr.isEmpty()) {
                                    dgsbjtStr = dgsbjtStr.substring(0, dgsbjtStr.length() - 2);
                                }
                                System.out.println(dgsbjtStr);

                                tvClCdNm.setText(hospitalItem.getClassCodeName() + " | " + dgsbjtStr);

                                allView.setVisibility(View.VISIBLE);
                                loadingView.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 에러 처리
                        error.printStackTrace();
                        detailInfoView.setVisibility(View.GONE);
                        loadingView.setVisibility(View.INVISIBLE);
                    }
                }){
            @Override //response를 UTF8로 변경해주는 소스코드
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    // log error
                    return Response.error(new ParseError(e));
                } catch (Exception e) {
                    // log error
                    return Response.error(new ParseError(e));
                }
            }
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
        };

        int socketTimeout = 10000;  //10 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    private HospDetailInfoItem JSONParse(String jsonStr) {
        try {
            JSONObject bodyObject = new JSONObject(jsonStr).getJSONObject("response").getJSONObject("body");
            Object response = bodyObject.get("items");
            if (response instanceof String) {
                return null;
            }
            JSONObject resObject = (JSONObject) response;
            JSONArray jsonArray;
            if (resObject.get("item") instanceof JSONArray) {
                jsonArray = resObject.getJSONArray("item");
            } else {
                jsonArray = new JSONArray();
                jsonArray.put(resObject.getJSONObject("item"));
            }

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String place = "", rcvWeek = null, lunchWeek = null, rcvSat = null, lunchSat = null, noTrmtHoli = null, noTrmtSun = null;
            String parkXpnsYn = null, parkEtc = null, emyDayYn = null, emyNgtYn = null;
            Integer parkQty = 0;

            if (jsonObject.has("plcDir"))
                place += jsonObject.getString("plcDir");
            if (jsonObject.has("plcDist"))
                place += " " + jsonObject.getString("plcDist");
            if (jsonObject.has("plcNm"))
                place += " " + jsonObject.getString("plcNm");

            if (jsonObject.has("rcvWeek"))
                rcvWeek = " " + jsonObject.getString("rcvWeek");
            if (jsonObject.has("lunchWeek"))
                lunchWeek = " " + jsonObject.getString("lunchWeek");
            if (jsonObject.has("rcvSat"))
                rcvSat = " " + jsonObject.getString("rcvSat");
            if (jsonObject.has("lunchSat"))
                lunchSat = " " + jsonObject.getString("lunchSat");
            if (jsonObject.has("noTrmtHoli"))
                noTrmtHoli = " " + jsonObject.getString("noTrmtHoli");
            if (jsonObject.has("noTrmtSun"))
                noTrmtSun = " " + jsonObject.getString("noTrmtSun");

            if (jsonObject.has("emyDayYn"))
                emyDayYn = " " + jsonObject.getString("emyDayYn");
            if (jsonObject.has("emyNgtYn"))
                emyNgtYn = " " + jsonObject.getString("emyNgtYn");

            if (jsonObject.has("parkXpnsYn")) {
                parkXpnsYn = jsonObject.getString("parkXpnsYn");
                if(parkXpnsYn.equals("Y")) {
                    if (jsonObject.has("parkQty"))
                        parkQty = jsonObject.getInt("parkQty");
                    if (jsonObject.has("parkEtc"))
                        parkEtc = " " + jsonObject.getString("parkEtc");
                }
            }

            // 새로운 HospDetailInfoItem 객체 생성
            HospDetailInfoItem item = new HospDetailInfoItem();
            if(!place.isEmpty())
                item.setPlace(place);
            if(rcvWeek != null)
                item.setRcvWeek(rcvWeek);
            if(lunchWeek != null)
                item.setLunchWeek(lunchWeek);
            if(rcvSat != null)
                item.setRcvSat(rcvSat);
            if(lunchSat != null)
                item.setLunchSat(lunchSat);
            if(noTrmtSun != null)
                item.setNoTrmtSun(noTrmtSun);
            if(noTrmtHoli != null)
                item.setNoTrmtHoli(noTrmtHoli);
            if(emyDayYn != null)
                item.setEmyDayYn(emyDayYn);
            if(emyNgtYn != null)
                item.setEmyNgtYn(emyNgtYn);
            if(parkXpnsYn != null)
                item.setParkXpnsYn(parkXpnsYn);
            if(parkEtc != null)
                item.setParkEtc(parkEtc);
            item.setParkQty(parkQty);

            return item;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
