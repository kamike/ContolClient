package contol.android.kamike.com.contolclient.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by hasee on 2017/1/9
 */
public class PermissionUtil {


    public static boolean checkPermission(final Activity activity,String permission,int requestCode) {
        if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                return false;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
//                activity.showToast(R.string.error_sd_permission);
                return false;
            }
        } else {
            return true;
        }
    }



    public static String[] PERMISSION_ARRAY = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.MODIFY_AUDIO_SETTINGS};
    /**
     * 读写存储卡权限请求码
     */
    public static int REQUECT_PERMISSION_CODE_STORAGE = 0;

    /**
     * 手机状态及打电话等权限请求码
     */
    public static int REQUECT_PERMISSION_CODE_PHONE = 1;

    /**
     * 使用相机的权限请求码
     */
    public static int REQUECT_PERMISSION_CODE_CAMERA = 2;

    /**
     * 短信相关权限的请求码
     */
    public static int REQUECT_PERMISSION_CODE_SMS = 3;

    /**
     * 联系人相关的权限请求码
     */
    public static int REQUECT_PERMISSION_CODE_CONTACTS = 4;

    /**
     * 定位权限请求码
     */
    public static int REQUECT_PERMISSION_CODE_LOCATION = 5;

    /**
     * 传感器权限请求码
     */
    public static int REQUECT_PERMISSION_CODE_SENSORS = 6;

    /**
     * 日历权限请求码
     */
    public static int REQUECT_PERMISSION_CODE_CALENDAR = 7;

    /**
     * 麦克风权限请求码
     */
    public static int REQUECT_PERMISSION_CODE_MICROPHONE = 8;
}
