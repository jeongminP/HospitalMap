package com.pjm9355.hospitalmap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
        implements MapView.CurrentLocationEventListener,
        MapReverseGeoCoder.ReverseGeoCodingResultListener,
        MapView.MapViewEventListener,
        MapView.POIItemEventListener {
    private static final String LOG_TAG = "MainActivity";

    private MapView mMapView;
    private RelativeLayout choiceView;
    private TextView deptTextView, emdTextView;
    private ImageButton currentLocationBtn;
    private Button showListBtn;
    private View infoView;
    private View loadingView;

    private MapPoint currentLocation;
    private String centerEMDong = "";
    private DepartmentCode deptCode;
    private SharedPreferences sharedPreferences;
    private ArrayList<HospitalItem> hospitalItemList;
    private Disposable backgroundtask;
    private SQLiteDatabase db;

    private long backBtnTime = 0;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {android.Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Double latitude = intent.getDoubleExtra("latitude", 0);
        Double longitude = intent.getDoubleExtra("longitude", 0);

        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), false);
        mMapView.setCurrentLocationEventListener(this);
        mMapView.setMapViewEventListener(this);
        mMapView.setPOIItemEventListener(this);
        mMapView.setZoomLevel(3,true);

        choiceView = findViewById(R.id.choice_dept_view);
        deptTextView = findViewById(R.id.dept_textview);
        emdTextView = findViewById(R.id.emd_textview);
        currentLocationBtn = findViewById(R.id.current_location_btn);
        showListBtn = findViewById(R.id.show_list_btn);
        infoView = findViewById(R.id.info_view);
        loadingView = findViewById(R.id.loading_view);

        DBHelper helper = new DBHelper(MainActivity.this, "hdb.db", null, 1);
        db = helper.getWritableDatabase();
        helper.onCreate(db);

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_preferences_file_name), MODE_PRIVATE);
        deptCode = DepartmentCode.valueOf(sharedPreferences.getString(getResources().getString(R.string.sp_stored_department), "IM"));
        deptTextView.setText(deptCode.getDepartmentName());
        hideLoadingView();
        showListBtn.setClickable(false);
        infoView.setVisibility(View.INVISIBLE);

        showListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 목록 화면으로 이동
                Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                intent.putExtra("hospitalItemList", hospitalItemList);
                intent.putExtra("emdongName", centerEMDong);
                intent.putExtra("deptName", deptCode.getDepartmentName());
                startActivity(intent);
            }
        });

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        choiceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoView.setVisibility(View.INVISIBLE);

                final DepartmentCode[] deptCodeArray = DepartmentCode.values();
                ArrayList<String> deptNameArray = new ArrayList<String>();
                for (DepartmentCode deptCode: DepartmentCode.values()) {
                    deptNameArray.add(deptCode.getDepartmentName());
                }
                final String[] deptArr = deptNameArray.toArray(new String[deptNameArray.size()]);

                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("진료과");
                dlg.setItems(deptArr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deptCode = deptCodeArray[which];
                        deptTextView.setText(deptCode.getDepartmentName());
                        getHospitalListFromDB(deptCode, centerEMDong);
                        dialog.dismiss();
                    }
                });
                dlg.setPositiveButton("닫기", null);
                dlg.show();
            }
        });

        currentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null) {
                    mMapView.setMapCenterPoint(currentLocation, true);
                }
            }
        });

        loadingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do Nothing - disable user interaction
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mMapView.setShowCurrentLocationMarker(false);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getResources().getString(R.string.sp_stored_department), deptCode.toString());
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if(0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        }
        else {
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoadingView() {
        loadingView.setVisibility(View.VISIBLE);
    }

    private void hideLoadingView() {
        loadingView.setVisibility(View.INVISIBLE);
    }

    /*
        MapView.CurrentLocationEventListener 구현
     */

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        this.currentLocation = currentLocation;
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    /*
        MapView.MapViewEventListener 구현
     */
    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        infoView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        System.out.println("지도 움직이기 시작!!");
        infoView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        System.out.println("지도 움직이기 끝!");
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder(getResources().getString(R.string.rest_api_key),
                mapPoint,
                this,
                this);
        reverseGeoCoder.startFindingAddress();
    }

    /*
        MapView.POIItemEventListener 구현
     */

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        TextView tvName = findViewById(R.id.hosp_name_textview);
        TextView tvClCdNm = findViewById(R.id.class_code_name_textview);
        TextView tvAddr = findViewById(R.id.address_textview);
        TextView tvTelNo = findViewById(R.id.tel_no_textview);
        TextView tvUrl = findViewById(R.id.hosp_url_textview);

        HospitalItem selectedItem = (HospitalItem) mapPOIItem.getUserObject();

        tvName.setText(selectedItem.getHospName());
        tvAddr.setText(selectedItem.getAddress());

        String telno = selectedItem.getTelNo();
        if (telno==null || telno.isEmpty()) {
            tvTelNo.setVisibility(View.GONE);
        } else {
            tvTelNo.setVisibility(View.VISIBLE);
            tvTelNo.setText(telno);

            tvTelNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("tel:" + telno);
                    Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                    startActivity(intent);
                }
            });
        }

        String url = selectedItem.getHospUrl();
        if (url==null || url.isEmpty()) {
            tvUrl.setVisibility(View.GONE);
        } else {
            tvUrl.setVisibility(View.VISIBLE);
            tvUrl.setPaintFlags(tvUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvUrl.setText(url);

            tvUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }

        infoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("hospitalItem", selectedItem);
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            }
        });

        // DB에서 진료과 검색
        String mQuery = "SELECT * FROM tb_dgsbjt WHERE ykiho = ?";
        Cursor c = db.rawQuery(mQuery, new String[]{selectedItem.getYkiho()});
        String dgsbjtStr = "";

        while (c.moveToNext()) {
            dgsbjtStr += c.getString(c.getColumnIndex("dgsbjtCdNm")) + ", ";
        }
        if(!dgsbjtStr.isEmpty()) {
            dgsbjtStr = dgsbjtStr.substring(0, dgsbjtStr.length() - 2);
        }
        System.out.println(dgsbjtStr);

        tvClCdNm.setText(selectedItem.getClassCodeName() + " | " + dgsbjtStr);
        infoView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        // Deprecated
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        HospitalItem selectedItem = (HospitalItem) mapPOIItem.getUserObject();
        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
        intent.putExtra("hospitalItem", selectedItem);
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    /*
        MapReverseGeoCoder.ReverseGeoCodingResultListener 구현
     */

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        // 동일한 읍면동이면 API 요청을 보내지 않도록 개선
        String newEMDong = parseEMDongNm(s);
        if (centerEMDong.equals(newEMDong)) {
            hideLoadingView();
            onFinishReverseGeoCoding(s);
            return;
        }

        centerEMDong = newEMDong;
        emdTextView.setText(centerEMDong);
        getHospitalListFromDB(deptCode, centerEMDong);
        onFinishReverseGeoCoding(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        onFinishReverseGeoCoding("Fail");
    }

    private String parseEMDongNm(String addrStr) {
        String[] splitList = addrStr.split(" ");
        for (String s : splitList) {
            String last = s.substring(s.length() - 1);
            if (last.equals("읍") || last.equals("면") || last.equals("동")) {
                return s;
            } else if (last.equals("로") || last.equals("길") || last.equals("가")) {
                return s;
            }
        }
        return "";
    }

    private void onFinishReverseGeoCoding(String result) {
        System.out.println("Reverse Geo-coding : " + result);
    }

    public void getHospitalListFromDB(DepartmentCode deptCode, String emdongNm) {
        showLoadingView();
        //onPreExecute
        hospitalItemList = new ArrayList<HospitalItem>();
        
        backgroundtask = Observable.fromCallable(() -> {
            //doInBackground
            String mQuery = "SELECT * FROM tb_hospbasislist a INNER JOIN tb_dgsbjt b ON a.ykiho = b.ykiho WHERE b.dgsbjtCd = ? AND a.addr LIKE ?";
            Cursor c = db.rawQuery(mQuery, new String[]{deptCode.getCode(), "%" + emdongNm + "%"});

            while (c.moveToNext()) {
                String hospName = c.getString(c.getColumnIndex("yadmNm"));
                String classCodeName = c.getString(c.getColumnIndex("clCdNm"));
                String address = c.getString(c.getColumnIndex("addr"));
                String telNo = c.getString(c.getColumnIndex("telno"));
                String hospUrl = c.getString(c.getColumnIndex("hospUrl"));
                String ykiho = c.getString(c.getColumnIndex("ykiho"));

                Integer estbDate = c.getInt(c.getColumnIndex("estbDd"));
                Integer doctorTotalCnt = c.getInt(c.getColumnIndex("drTotCnt"));
                Integer specialistDoctorCnt = c.getInt(c.getColumnIndex("sdrCnt"));
                Integer generalDoctorCnt = c.getInt(c.getColumnIndex("gdrCnt"));
                Integer residentCnt = c.getInt(c.getColumnIndex("resdntCnt"));
                Integer internCnt = c.getInt(c.getColumnIndex("intnCnt"));

                Double xPos = c.getDouble(c.getColumnIndex("XPos"));
                Double yPos = c.getDouble(c.getColumnIndex("YPos"));

                String dgsbjtCdNm = c.getString(c.getColumnIndex("dgsbjtCdNm"));

                // 새로운 HospitalItem 객체 생성
                HospitalItem item = new HospitalItem();
                item.setHospName(hospName);
                item.setClassCodeName(classCodeName);
                item.setDeptCodeName(dgsbjtCdNm);
                item.setAddress(address);
                if (!telNo.isEmpty()) {
                    item.setTelNo(telNo);
                }
                if (!hospUrl.isEmpty()) {
                    item.setHospUrl(hospUrl);
                }
                item.setEstbDate(convertDate(estbDate.toString()));
                item.setYkiho(ykiho);

                item.setDoctorTotalCnt(doctorTotalCnt);
                item.setSpecialistDoctorCnt(specialistDoctorCnt);
                item.setGeneralDoctorCnt(generalDoctorCnt);
                item.setResidentCnt(residentCnt);
                item.setInternCnt(internCnt);

                item.setXPos(xPos);
                item.setYPos(yPos);
                hospitalItemList.add(item);
            }
            return false;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {

                    //onPostExecute
                    hideLoadingView();
                    showListBtn.setText(getResources().getString(R.string.button_text_show_hosp_list) + " (" + hospitalItemList.size() + ")");
                    showListBtn.setClickable(true);

                    if (hospitalItemList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "일치하는 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    // 지도에 마킹
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMapView.removeAllPOIItems();
                            for (int i=0; i < hospitalItemList.size(); i++) {
                                HospitalItem hospItem = hospitalItemList.get(i);
                                MapPOIItem marker = new MapPOIItem();
                                marker.setTag(i);
                                marker.setItemName(hospItem.getHospName());
                                marker.setUserObject(hospItem);
                                MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(hospItem.getYPos(), hospItem.getXPos());
                                marker.setMapPoint(mapPoint);
                                marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
                                marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                                mMapView.addPOIItem(marker);
                            }
                        }
                    });

                    backgroundtask.dispose();
                });
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

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {
                Log.d("@@@", "start");
                //위치 값을 가져올 수 있음
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음
            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}