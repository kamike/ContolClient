package contol.android.kamike.com.contolclient.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.LogUtils;

/**
 * Created by wangtao on 2017/10/20.
 */

public class SmsDeliverReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.i("SmsDeliverReceiver:"+intent.getAction());
    }
}
