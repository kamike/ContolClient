package contol.android.kamike.com.contolclient.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;

public class SmsInterruptReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        super.abortBroadcast();
        LogUtils.i("SmsInterruptReceiver=====");
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();

            Object messages[] = (Object[]) bundle.get("pdus");
            SmsMessage smsMessage[] = new SmsMessage[messages.length];
            for (int n = 0; n < messages.length; n++) {
                smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
            }
            Toast.makeText(context, "短信内容: " + smsMessage[0].getMessageBody(), Toast.LENGTH_LONG);
        }
    }
}
