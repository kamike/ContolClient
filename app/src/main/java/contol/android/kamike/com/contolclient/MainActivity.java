package contol.android.kamike.com.contolclient;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import contol.android.kamike.com.contolclient.bean.SmsInfoBean;
import contol.android.kamike.com.contolclient.utils.AllUtils;
import contol.android.kamike.com.contolclient.utils.PermissionUtil;
import contol.android.kamike.com.contolclient.utils.SmsUtils;

public class MainActivity extends AppCompatActivity {
    private EditText etIp;
    public static final int SERVER_PORT = 6666;
    private TextView tvContent;
    public static final String CHAR_SET = "utf-8";
    private int mDensity = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setFullScreen(this);
        setContentView(R.layout.activity_main);
        etIp = (EditText) findViewById(R.id.main_ip_et);
        tvContent = (TextView) findViewById(R.id.main_content_tv);
        PermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 0);


        imgWidth = ScreenUtils.getScreenWidth() / 2;
        imgHeight = ScreenUtils.getScreenHeight() / 2;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDensity = metrics.densityDpi;
//        readAllSmsTest();

    }

    private int failTimes;


    public void sendClientInfo() {
        new Thread() {
            @Override
            public void run() {
                Socket socket;
                try {
                    socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "链接失败：" + e, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;

                }
                try {
                    OutputStream socketOutput = socket.getOutputStream();
                    socketOutput.write(AllUtils.getUUIDCache().getBytes(CHAR_SET));
                    String info = AllUtils.getClientInfo(MainActivity.this);
                    LogUtils.i("======" + info);
                    socketOutput.write("str".getBytes());
                    socketOutput.write(info.getBytes(CHAR_SET));
                    socketOutput.flush();
                    socketOutput.close();

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
                    try {
                        sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if (failTimes < 5) {
//                        sendClientInfo();
                    }
                    failTimes++;

                } finally {
                    closedSocket(socket);
                    LogUtils.i("finally===closedSocket");
                }
            }
        }.start();

    }

    private String SERVER_ADDRESS;

    private MediaProjectionManager projectionManager;
    private int SCREEN_SHOT = 1;
    private MediaProjection mediaProject;
    private ImageReader imageReader;
    private int imgWidth = 1080;
    private int imgHeight = 1920;
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onclickConnectImg(View view) {
        SERVER_ADDRESS = etIp.getText().toString();
        if (TextUtils.isEmpty(SERVER_ADDRESS)) {
            Toast.makeText(this, "ip地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        failTimes = 0;
        sendClientInfo();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ToastUtils.showLong("系统版本太低");
            return;
        }
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
//        startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_SHOT);


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCREEN_SHOT) {
            mediaProject = projectionManager.getMediaProjection(resultCode, data);
            imageReader = ImageReader.newInstance(imgWidth, imgHeight, PixelFormat.RGBA_8888, 2);
            if (imageReader == null) {
                LogUtils.i("imageReader===fail");
                return;
            }
            mediaProject.createVirtualDisplay("screencap", imgWidth, imgHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, imageReader.getSurface(), null, mHandler);

            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = imageReader.acquireLatestImage();
                    if (image == null) {
                        return;
                    }
                    int width = image.getWidth();
                    int height = image.getHeight();
                    final Image.Plane[] planes = image.getPlanes();
                    final ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;
                    Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                    recyBitmap(bitmap);
//                        saveBitmap(bitmap);
                    sendBitmaSocket(bmp);
                    image.close();


                }
            }, mHandler);
        }
    }


    private void sendBitmaSocket(final Bitmap bitmap) {
        if (bitmap == null) {
            LogUtils.i("Bitmap null=====");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                Socket socket = null;
                try {
                    socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.write(AllUtils.getUUIDCache().getBytes(CHAR_SET));
                    dos.write("img".getBytes());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    dos.write(baos.toByteArray());
                    dos.flush();
                    dos.close();

                } catch (IOException e) {
                    if (!(e instanceof EOFException)) {
                        e.printStackTrace();
                    }
                    System.out.println("IOException=======" + e.getMessage());
                } finally {
                    closedSocket(socket);
                    recyBitmap(bitmap);
                }
            }
        }.start();

    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };


    private void closedSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void recyBitmap(Bitmap bitmap) {
        System.out.println("recyBitmap=======");
        if (bitmap == null) {
            return;
        }
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private void readAllSmsTest() {
        ArrayList<SmsInfoBean> list = SmsUtils.getAllSMS();
        LogUtils.i("读取到了多少条短信：" + list.size());
        for (SmsInfoBean sms : list) {
            LogUtils.i("" + sms.toString());
        }
    }


}
