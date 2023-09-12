package vn.vistark.autocaller;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ApplicationClass.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        //Restart your app after 2 seconds
        AlarmManager mgr = (AlarmManager) ApplicationClass.getInstance().getBaseContext()
                .getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mgr.setExact(AlarmManager.RTC, System.currentTimeMillis() + 2000, pendingIntent);
        } else {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, pendingIntent);
        }
        //finishing the activity.
        activity.finish();
        //Stopping application
        System.exit(0);

    }
}

