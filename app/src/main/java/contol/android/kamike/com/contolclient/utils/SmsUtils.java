package contol.android.kamike.com.contolclient.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.blankj.utilcode.util.Utils;

import java.util.ArrayList;

import contol.android.kamike.com.contolclient.bean.SmsInfoBean;

/**
 * Created by wangtao on 2017/9/28.
 */

public class SmsUtils {
    public static ArrayList<SmsInfoBean> getAllSMS() {
        // 1.获取短信
        // 1.1获取内容解析者
        ContentResolver resolver = Utils.getApp().getContentResolver();
        // 1.2获取内容提供者地址   sms,sms表的地址:null  不写
        // 1.3获取查询路径
        Uri uri = Uri.parse("content://sms/");
        // 1.4.查询操作
        // projection : 查询的字段
        // selection : 查询的条件
        // selectionArgs : 查询条件的参数
        // sortOrder : 排序
        Cursor cursor = resolver.query(uri, new String[]{"address", "date", "type", "body"}, null, null, "date desc");
        // 设置最大进度
        int count = cursor.getCount();//获取短信的个数
        // 2.备份短信
        // 2.1获取xml序列器
        ArrayList<SmsInfoBean> listAll = new ArrayList<>(count);
        try {
            // 1.5.解析cursor
            while (cursor.moveToNext()) {
                SmsInfoBean sms = new SmsInfoBean();
                sms.address = cursor.getString(0);
                sms.date = cursor.getString(1);
                sms.type = cursor.getString(2);
                sms.body = cursor.getString(3);
                listAll.add(sms);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            cursor.close();
        }
        return listAll;
    }
}
