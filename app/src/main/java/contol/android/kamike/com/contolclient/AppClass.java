package contol.android.kamike.com.contolclient;

import android.app.Application;
import android.content.IntentFilter;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import contol.android.kamike.com.contolclient.sms.SmsInterruptReceiver;

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
        intentFilter.setPriority(1000);
        registerReceiver(new SmsInterruptReceiver(),intentFilter);
        LogUtils.i("registerReceiver===");

    }



}
