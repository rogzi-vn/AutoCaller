package vn.vistark.autocaller;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import vn.vistark.autocaller.services.BackgroundServiceCompanion;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
    Activity activity;

    public DefaultExceptionHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        BackgroundServiceCompanion.Companion.StopBackgroundService(activity);
        Log.d("ERROR_CEPTION", "===============================================================");
        Log.e("ERROR_CEPTION", "uncaughtException: ", ex);
        Log.d("ERROR_CEPTION", "===============================================================");
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);

        //finishing the activity.
        activity.finish();
        //Stopping application
        System.exit(0);

    }
}

