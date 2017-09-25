package contol.android.kamike.com.contolclient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
        setContentView(R.layout.activity_main);
        etIp = (EditText) findViewById(R.id.main_ip_et);
        tvContent = (TextView) findViewById(R.id.main_content_tv);
        PermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PermissionUtil.checkPermission(this, Manifest.permission.READ_PHONE_STATE);

    }

    public void onclickConnect(View view) {
        final String SERVER_ADDRESS = etIp.getText().toString();
        if (TextUtils.isEmpty(SERVER_ADDRESS)) {
            Toast.makeText(this, "ip地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread() {
            @Override
            public void run() {
                Socket socket = null;
                Bitmap bmp = null;
                try {
                    socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

                    OutputStream out = socket.getOutputStream();
                    out.write("str".getBytes());
                    String info = AllUtils.getClientInfo(MainActivity.this);
                    System.out.println(info);
                    out.write(info.getBytes(CHAR_SET));
                    out.flush();
                    out.close();

                } catch (IOException e) {
                    if (!(e instanceof EOFException)) {
                        e.printStackTrace();
                    }
                    System.out.println("IOException=======" + e.getMessage());
                } finally {
                    closedSocket(socket);
                    recyBitmap(bmp);
                }
            }
        }.start();
    }

    public void onclickConnectImg(View view) {
        final String SERVER_ADDRESS = etIp.getText().toString();
        if (TextUtils.isEmpty(SERVER_ADDRESS)) {
            Toast.makeText(this, "ip地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread() {
            @Override
            public void run() {
                Socket socket = null;
                Bitmap bmp = null;
                try {
                    bmp = BitmapFactory.decodeStream(getAssets().open("test.jpg"));
                    socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.write("img".getBytes());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    //dos.writeInt(baos.size());
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
                    recyBitmap(bmp);

                }
            }
        }.start();
    }

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
        Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
    }


}
