package com.example.hospitalmap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DetailActivity extends Activity {
    private HospitalItem hospitalItem;
    private TextView tvName, tvClCdNm, tvAddr, tvTelNo, tvUrl, tvEstbDate, tvDrTotCnt, tvSDrCnt, tvGDrCnt, tvResidentCnt, tvInternCnt;
    private View telNoView, hospUrlView, estbDateView;

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

        TextView titleTextView = findViewById(R.id.detail_title_textview);
        titleTextView.setText(hospitalItem.getHospName());

        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 텍스트 채우기
        tvName.setText(hospitalItem.getHospName());
        tvClCdNm.setText(hospitalItem.getClassCodeName());
        tvAddr.setText(hospitalItem.getAddress());

        String telno = hospitalItem.getTelNo();
        if (telno==null || telno.isEmpty()) {
            telNoView.setVisibility(View.GONE);
        } else {
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

        String url = hospitalItem.getHospUrl();
        if (url==null || url.isEmpty()) {
            hospUrlView.setVisibility(View.GONE);
        } else {
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
    }
}
