package contol.android.kamike.com.contolclient;

import android.app.Application;
import android.content.IntentFilter;

import com.blankj.utilcode.util.Utils;

/**
 * Created by Administrator on 2017/9/24.
 */

public class AppClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.addAction("android.provider.Telephony.SMS_DELIVER");
        registerReceiver(new SmsInterruptReceiver(),intentFilter);
    }



}
