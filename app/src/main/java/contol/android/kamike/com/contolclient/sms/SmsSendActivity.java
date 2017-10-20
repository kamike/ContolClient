package contol.android.kamike.com.contolclient.sms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;

import contol.android.kamike.com.contolclient.R;

public class SmsSendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_send);
        LogUtils.i("SmsSendActivity===");
    }
}
