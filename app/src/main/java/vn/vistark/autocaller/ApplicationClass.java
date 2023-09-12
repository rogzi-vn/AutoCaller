package vn.vistark.autocaller;

import android.app.Application;
import android.content.Context;

public class ApplicationClass extends Application {
    public static ApplicationClass instace;

    @Override
    public void onCreate() {
        super.onCreate();
        instace = this;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public static ApplicationClass getInstance() {
        return instace;
    }
}
