/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background;

import android.Manifest;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;

import androidx.work.Data;
import androidx.work.WorkStatus;


public class SmsActivity extends AppCompatActivity {

    private SmsViewModel mViewModel;
    private EditText mEditMessage;
    private EditText mEditMobile;
    private TextView mEditTime;
    private Button mGoButton, mOutputButton, mCancelButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        // Get the ViewModel
        mViewModel = ViewModelProviders.of(this).get(SmsViewModel.class);

        // Get all of the Views
        mEditMessage = findViewById(R.id.et_message);
        mEditTime = findViewById(R.id.et_time);
        mEditMobile = findViewById(R.id.et_mobile);
        mGoButton = findViewById(R.id.go_button);
        mOutputButton = findViewById(R.id.see_file_button);
        mCancelButton = findViewById(R.id.cancel_button);

        // Image uri should be stored in the ViewModel; put it there then display
        Intent intent = getIntent();
        String imageUriExtra = intent.getStringExtra(Constants.KEY_IMAGE_URI);
        mViewModel.setImageUri(imageUriExtra);

        // Setup blur image file button
        mGoButton.setOnClickListener(view -> mViewModel.applyBlur(mEditMessage.getText().toString(), mEditTime.getText().toString(),
                mEditMobile.getText().toString().trim(), this));


        String[] PERMISSIONS = {Manifest.permission.SEND_SMS};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }


        // Hookup the Cancel button
        //  mCancelButton.setOnClickListener(view -> mViewModel.cancelWork());

        mEditTime.setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);

            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(SmsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    mEditTime.setText(selectedHour + ":" + selectedMinute);
                }
            }, hour, minute, false);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();

        });
        Log.e("","");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();

            Log.e("Test", "Current list = " + subsInfoList);

            for (SubscriptionInfo subscriptionInfo : subsInfoList) {

                String number = subscriptionInfo.getCarrierName().toString();

                Log.e("Test", " Number is  " + number);
            }
        }
//
        //// Show work status
        //mViewModel.getOutputStatus().observe(this, listOfWorkStatuses -> {
        //
        //// Note that these next few lines grab a single WorkStatus if it exists
        //// This code could be in a Transformation in the ViewModel; they are included here
        //// so that the entire process of displaying a WorkStatus is in one location.
        //
        //// If there are no matching work statuses, do nothing
        //if (listOfWorkStatuses == null || listOfWorkStatuses.isEmpty()) {
        //return;
        //}
        //
        //// We only care about the one output status.
        //// Every continuation has only one worker tagged TAG_OUTPUT
        //WorkStatus workStatus = listOfWorkStatuses.get(0);
        //
        //boolean finished = workStatus.getState().isFinished();
        //if (!finished) {
        //showWorkInProgress();
        //} else {
        //showWorkFinished();
        //
        //// Normally this processing, which is not directly related to drawing views on
        //// screen would be in the ViewModel. For simplicity we are keeping it here.
        //Data outputData = workStatus.getOutputData();
        //
        //String outputImageUri =
        //outputData.getString(Constants.KEY_IMAGE_URI);
        //
        //// If there is an output file show "See File" button
        //if (!TextUtils.isEmpty(outputImageUri)) {
        //mViewModel.setOutputUri(outputImageUri);
        //mOutputButton.setVisibility(View.VISIBLE);
        //}
        //}
        //});
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
/*

    */
/**
     * Shows and hides views for when the Activity is processing an image
     *//*

    private void showWorkInProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mCancelButton.setVisibility(View.VISIBLE);
        mGoButton.setVisibility(View.GONE);
        mOutputButton.setVisibility(View.GONE);
    }

    */
/**
     * Shows and hides views for when the Activity is done processing an image
     *//*

    private void showWorkFinished() {
        mProgressBar.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
        mGoButton.setVisibility(View.VISIBLE);
    }
*/

    /**
     * Get the blur level from the radio button as an integer
     * @return Integer representing the amount of times to blur the image
     */
  /*  private int getBlurLevel() {
        RadioGroup radioGroup = findViewById(R.id.radio_blur_group);

        switch(radioGroup.getCheckedRadioButtonId()) {
            case R.id.radio_blur_lv_1:
                return 1;
            case R.id.radio_blur_lv_2:
                return 2;
            case R.id.radio_blur_lv_3:
                return 3;
        }

        return 1;
    }*/
}
