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

package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.background.Constants;

import java.io.FileNotFoundException;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BlurWorker extends Worker {

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public BlurWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private static final String TAG = BlurWorker.class.getSimpleName();

    @NonNull
    @Override
    public Worker.Result doWork() {

        Context applicationContext = getApplicationContext();

        Log.e(TAG, "doWork: " );
        // Makes a notification when the work starts and slows down the work so that it's easier to
        // see each WorkRequest start, even on emulated devices
        WorkerUtils.makeStatusNotification("Sending message", applicationContext);
        WorkerUtils.sleep();

        String message = getInputData().getString(Constants.KEY_MESSAGE);
        String time = getInputData().getString(Constants.KEY_TIME);
        String mobile = getInputData().getString(Constants.KEY_MOBILE);
        try {
            if (TextUtils.isEmpty(message)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }

            ContentResolver resolver = applicationContext.getContentResolver();


            sendSMS(mobile, message +time);
            // If there were no errors, return SUCCESS
            return Worker.Result.SUCCESS;
        } catch (Throwable throwable) {

            // If there were errors, return FAILURE
            Log.e(TAG, "Error applying blur", throwable);
            return Worker.Result.FAILURE;
        }
    }
    private void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
             Toast.makeText(getApplicationContext(), "Message Sent",
                     Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
             Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                     Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
}