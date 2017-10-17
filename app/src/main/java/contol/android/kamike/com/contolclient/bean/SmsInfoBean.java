package contol.android.kamike.com.contolclient.bean;

/**
 * Created by Administrator on 2017/9/23.
 */

public class SmsInfoBean {
    public String address;
    public String date;
    public String type;
    public String body;

    @Override
    public String toString() {
        return "SmsInfoBean{" +
                "address='" + address + '\'' +
                ", date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
