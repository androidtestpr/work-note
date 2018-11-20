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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.background.workers.BlurWorker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;

import static com.example.background.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.example.background.Constants.KEY_MESSAGE;
import static com.example.background.Constants.KEY_MOBILE;
import static com.example.background.Constants.KEY_TIME;
import static com.example.background.Constants.TAG_OUTPUT;

public class SmsViewModel extends ViewModel {

    private WorkManager mWorkManager;
    private Uri mImageUri;
    private Uri mOutputUri;
    private LiveData<List<WorkStatus>> mSavedWorkStatus;

    public SmsViewModel() {

        mWorkManager = WorkManager.getInstance();

        // This transformation makes sure that whenever the current work Id changes the WorkStatus
        // the UI is listening to changes
        mSavedWorkStatus = mWorkManager.getStatusesByTag(TAG_OUTPUT);
    }


    void applyBlur(String message, String time,String mobile, Context context) {

        // Add WorkRequest to Cleanup temporary images
       /* WorkContinuation continuation = mWorkManager
                .beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequest.from(CleanupWorker.class));*/

       /* // Add WorkRequests to blur the image the number of times requested
        for (int i = 0; i < blurLevel; i++) {
            OneTimeWorkRequest.Builder blurBuilder =
                    new OneTimeWorkRequest.Builder(BlurWorker.class);

            // Input the Uri if this is the first blur operation
            // After the first blur operation the input will be the output of previous
            // blur operations.
            if ( i == 0 ) {
                blurBuilder.setInputData(createInputDataForUri());
            }

            continuation = continuation.then(blurBuilder.build());
        }*/

        // Create charging constraint
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(false)
                .build();

        // Add WorkRequest to save the image to the filesystem
        OneTimeWorkRequest save = new OneTimeWorkRequest.Builder(BlurWorker.class)
                .setInputData(createInputDataForUri(message, time,mobile))
                .setInitialDelay(getDelay(time), TimeUnit.MILLISECONDS)
                .addTag(TAG_OUTPUT)
                .build();

        mWorkManager.enqueue(save);

        Toast.makeText(context, "message sent", Toast.LENGTH_SHORT).show();
       /* continuation = continuation.then(save);

        // Actually start the work
        continuation.enqueue();*/

    }

    private long getDelay(String time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String[] separated = time.split(":");
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(separated[0]));
        calendar1.set(Calendar.MINUTE, Integer.parseInt(separated[1]));
        calendar1.set(Calendar.SECOND,0);
        //Date date2 = new Date();

        Log.e("remaining time", calendar1.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() + "");
        return calendar1.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

    }

    /**
     * Cancel work using the work's unique name
     */
    void cancelWork() {
        mWorkManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME);
    }

    /**
     * Creates the input data bundle which includes the Uri to operate on
     *
     * @return Data which contains the Image Uri as a String
     */
    private Data createInputDataForUri(String message, String time, String mobile) {
        Data.Builder builder = new Data.Builder();

            builder.putString(KEY_MESSAGE, message);
            builder.putString(KEY_TIME, time);
            builder.putString(KEY_MOBILE, mobile);

        return builder.build();
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    /**
     * Setters
     */
    void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }

    void setOutputUri(String outputImageUri) {
        mOutputUri = uriOrNull(outputImageUri);
    }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }

    Uri getOutputUri() {
        return mOutputUri;
    }

    LiveData<List<WorkStatus>> getOutputStatus() {
        return mSavedWorkStatus;
    }
}