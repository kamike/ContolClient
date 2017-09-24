package contol.android.kamike.com.contolclient;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

/**
 * Created by Administrator on 2017/9/24.
 */

public class AppClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
