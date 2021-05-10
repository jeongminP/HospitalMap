package com.example.hospitalmap;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class DataActivity extends Activity {
    SQLiteDatabase db;

    Integer pageNo;
    Integer idIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        DBHelper helper = new DBHelper(DataActivity.this, "hdb.db", null, 1);
        db = helper.getWritableDatabase();
        helper.onCreate(db);

        pageNo = 1;
        idIndex = 1;

//        getHospitalList();
        Button button = findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                findDong("대치동");
                getHospitalList2("01", "%대치동%");
            }
        });
    }

    public void findDong(String dong) {
        Cursor c = db.query(true, "tb_hospbasislist", null, "addr LIKE ?", new String[] {"%" + dong + "%"}, null, null, null, null, null);
        while (c.moveToNext()) {
            System.out.println("결과 : " + c.getString(c.getColumnIndex("addr")));
        }
    }

    public void insertSubject() {
        Cursor c = db.rawQuery("SELECT * FROM tb_hospbasislist WHERE _id = ?", new String[] {idIndex.toString()});

        while(c.moveToNext()) {
            String ykiho = c.getString(c.getColumnIndex("ykiho"));
            getMdlrtSbjectInfoList(ykiho);
        }
    }

    private void getMdlrtSbjectInfoList(String ykiho) {
        RequestQueue requestQueue;
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        Integer numOfRows = 100;

        String url = "\thttp://apis.data.go.kr/B551182/medicInsttDetailInfoService/getMdlrtSbjectInfoList?pageNo=1&numOfRows=50"
                + "&ykiho=" + ykiho
                + "&ServiceKey=Q%2BbQw%2FUNPpDxP9hAGr3SQzR71t%2BCRCoDcFtPYmxVpEdlObYNjUINxMD3hurNngT3r19ae%2FDHw7t%2B5YhzIm2EuA%3D%3D&_type=json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // 응답
                        System.out.println("진료과 응답 : " + response);     // for Debug
                        JSONParse2(response, ykiho);
                        System.out.println("완료완료 " + idIndex);

                        idIndex++;

                        if(idIndex == 71319) {
                            return;
                        }
                        insertSubject();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 에러 처리
                        error.printStackTrace();
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

    private void JSONParse2(String jsonStr, String ykiho) {
        try {
            JSONObject bodyObject = new JSONObject(jsonStr).getJSONObject("response").getJSONObject("body");
            Object response = bodyObject.get("items");
            if (response instanceof String) {
                Toast.makeText(getApplicationContext(), "일치하는 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject resObject = (JSONObject) response;
            JSONArray jsonArray;
            if (resObject.get("item") instanceof JSONArray) {
                jsonArray = resObject.getJSONArray("item");
            } else {
                jsonArray = new JSONArray();
                jsonArray.put(resObject.getJSONObject("item"));
            }

            for (int i=0; i < jsonArray.length(); i++) {
                // 각 아이템에서 데이터 추출
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Integer cdiagDrCnt = jsonObject.getInt("cdiagDrCnt");
                String dgsbjtCd = jsonObject.getString("dgsbjtCd");
                String dgsbjtCdNm = jsonObject.getString("dgsbjtCdNm");
                Integer dgsbjtPrSdrCnt = jsonObject.getInt("dgsbjtPrSdrCnt");

                ContentValues values = new ContentValues();
                values.put("ykiho", ykiho);
                values.put("cdiagDrCnt", cdiagDrCnt);
                values.put("dgsbjtCd", dgsbjtCd);
                values.put("dgsbjtCdNm", dgsbjtCdNm);
                values.put("dgsbjtPrSdrCnt", dgsbjtPrSdrCnt);
                db.insert("tb_dgsbjt", null, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getHospitalList() {
        RequestQueue requestQueue;
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        Integer numOfRows = 100;

        String url = "http://apis.data.go.kr/B551182/hospInfoService/getHospBasisList?numOfRows=500"
                + "&pageNo=" + pageNo
                + "&ServiceKey=Q%2BbQw%2FUNPpDxP9hAGr3SQzR71t%2BCRCoDcFtPYmxVpEdlObYNjUINxMD3hurNngT3r19ae%2FDHw7t%2B5YhzIm2EuA%3D%3D&_type=json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // 응답
                        System.out.println("병원 " + pageNo + "응답 : " + response);     // for Debug
                        JSONParse(response);
                        System.out.println("완료완료 " + pageNo);

                        pageNo++;

                        if(pageNo == 150) {
                            insertSubject();
                            return;
                        }
                        getHospitalList();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 에러 처리
                        error.printStackTrace();
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

    public void getHospitalList2(String dgsbjtCd, String dongNm) {
        String mQuery = "SELECT * FROM tb_hospbasislist a INNER JOIN tb_dgsbjt b ON a.ykiho = b.ykiho WHERE b.dgsbjtCd = ? AND a.addr LIKE ?";
        Cursor c = db.rawQuery(mQuery, new String[]{dgsbjtCd, dongNm});
        while (c.moveToNext()) {
            System.out.println("병원 이름 : " + c.getString(c.getColumnIndex("yadmNm")));
            System.out.println("병원 이름 : " + c.getDouble(c.getColumnIndex("XPos")));
        }
    }

    private void JSONParse(String jsonStr) {
        try {
            JSONObject bodyObject = new JSONObject(jsonStr).getJSONObject("response").getJSONObject("body");
            Object response = bodyObject.get("items");
            if (response instanceof String) {
                Toast.makeText(getApplicationContext(), "일치하는 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject resObject = (JSONObject) response;
            JSONArray jsonArray;
            if (resObject.get("item") instanceof JSONArray) {
                jsonArray = resObject.getJSONArray("item");
            } else {
                jsonArray = new JSONArray();
                jsonArray.put(resObject.getJSONObject("item"));
            }

            for (int i=0; i < jsonArray.length(); i++) {
                // 각 아이템에서 데이터 추출
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String hospName = jsonObject.getString("yadmNm");
                String classCodeName = jsonObject.getString("clCdNm");
                String address = jsonObject.getString("addr");
                String telNo = "";
                if (jsonObject.has("telno")) {
                    telNo = jsonObject.getString("telno");
                }
                String hospUrl = "";
                if (jsonObject.has("hospUrl")) {
                    hospUrl = jsonObject.getString("hospUrl");
                }

                Integer estbDate = jsonObject.getInt("estbDd");
                Integer doctorTotalCnt = jsonObject.getInt("drTotCnt");
                Integer specialistDoctorCnt = jsonObject.getInt("sdrCnt");
                Integer generalDoctorCnt = jsonObject.getInt("gdrCnt");
                Integer residentCnt = jsonObject.getInt("resdntCnt");
                Integer internCnt = jsonObject.getInt("intnCnt");

                if (!jsonObject.has("XPos")) {
                    continue;
                }
                Double xPos = jsonObject.getDouble("XPos");
                Double yPos = jsonObject.getDouble("YPos");

                // 새로운 HospitalItem 객체 생성
                HospitalItem item = new HospitalItem();
                item.setHospName(hospName);
                item.setClassCodeName(classCodeName);
                item.setAddress(address);
                if (!telNo.isEmpty()) {
                    item.setTelNo(telNo);
                }
                if (!hospUrl.isEmpty()) {
                    item.setHospUrl(hospUrl);
                }
                item.setEstbDate(convertDate(estbDate.toString()));

                item.setDoctorTotalCnt(doctorTotalCnt);
                item.setSpecialistDoctorCnt(specialistDoctorCnt);
                item.setGeneralDoctorCnt(generalDoctorCnt);
                item.setResidentCnt(residentCnt);
                item.setInternCnt(internCnt);

                item.setXPos(xPos);
                item.setYPos(yPos);

                ContentValues values = new ContentValues();
                values.put("addr", address);
                values.put("clCdNm", classCodeName);
                values.put("drTotCnt", doctorTotalCnt);
                values.put("estbDd", estbDate);
                values.put("gdrCnt", generalDoctorCnt);
                values.put("hospUrl", hospUrl);
                values.put("intnCnt", internCnt);
                values.put("resdntCnt", residentCnt);
                values.put("sdrCnt", specialistDoctorCnt);
                values.put("telno", telNo);
                values.put("XPos", xPos);
                values.put("YPos", yPos);
                values.put("yadmNm", hospName);
                db.insert("hospitaldb", null, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String convertDate(String dateStr) {
        DateFormat oldFormat = new SimpleDateFormat("yyyyMMdd");
        DateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = oldFormat.parse(dateStr);
            String newDateStr = newFormat.format(date);
            return newDateStr;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
