package me.hetian.flutter_qr_reader.custom;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.PointF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;
import me.hetian.flutter_qr_reader.R;
import me.hetian.flutter_qr_reader.readerView.QRCodeReaderView;

import static android.content.Context.VIBRATOR_SERVICE;

public class QrReaderNewView implements PlatformView, QRCodeReaderView.OnQRCodeReadListener, MethodChannel.MethodCallHandler, QRCodeView.Delegate {
    private static final String TAG = QrReaderNewView.class.getSimpleName();
    private MethodChannel mMethodChannel;
    private Context mContext;
    private Map<String, Object> mParams;
    ZXingView _view;
    BinaryMessenger binaryMessenger;

    public static String EXTRA_FOCUS_INTERVAL = "extra_focus_interval";
    public static String EXTRA_TORCH_ENABLED = "extra_torch_enabled";
    boolean flashlight = false;

    public QrReaderNewView(Context context, BinaryMessenger binaryMessenger, int id, Map<String, Object> params) {
        this.mContext = context;
        this.mParams = params;
        this.binaryMessenger = binaryMessenger;
        // 创建视图
        int width = (int) mParams.get("width");
        int height = (int) mParams.get("height");
        int rectWidth = (int) mParams.get("rectWidth");
        int isOnlyDecodeScanBoxArea = (int) mParams.get("isOnlyDecodeScanBoxArea");
        _view
                = (ZXingView) LayoutInflater.from(context).inflate(R.layout.zxing_view, null);
        _view.setDelegate(this);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(width, height);
        _view.setLayoutParams(layoutParams);
        _view.setMinimumWidth(width);
        if (rectWidth > 0) {
            _view.getScanBoxView().setRectWidth(rectWidth);
            _view.getScanBoxView().setRectHeight(rectWidth);
        }
        _view.getScanBoxView().setOnlyDecodeScanBoxArea(isOnlyDecodeScanBoxArea == 0 ? false : true);
        // 操作监听
        mMethodChannel = new MethodChannel(binaryMessenger, "me.hetian.plugins/flutter_qr_reader/reader_view_" + id);
        mMethodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        try {
            switch (methodCall.method) {
                case "flashlight":
                    flashlight = !flashlight;
                    if (flashlight) {
                        _view.openFlashlight();
                    } else {
                        _view.closeFlashlight();
                    }
                    result.success(flashlight);
                    break;
                case "startCamera":
                    _view.startCamera();
                    _view.startSpotAndShowRect();
                    result.success(true);
                    break;
                case "stopCamera":
                    _view.stopCamera();
                    result.success(true);
                    break;
            }
        } catch (Exception e) {

        }

    }

    @Override
    public View getView() {
        return _view;
    }

    @Override
    public void dispose() {
        try {
            _view.stopCamera();
            _view.onDestroy();
            _view = null;
            mParams = null;
        } catch (Exception e) {

        }
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        HashMap<String, Object> rest = new HashMap<String, Object>();
        rest.put("text", text);
        ArrayList<String> poi = new ArrayList<String>();
        for (PointF point : points) {
            poi.add(point.x + "," + point.y);
        }
        rest.put("points", poi);
        mMethodChannel.invokeMethod("onQRCodeRead", rest);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.e("aaa", "扫码成功");
        Log.i(TAG, "result:" + result);
        Log.e(TAG, "扫描结果为：" + result);
        vibrate();
        _view.startSpot(); // 开始识别

        HashMap<String, Object> rest = new HashMap<String, Object>();
        rest.put("text", result);
        ArrayList<String> poi = new ArrayList<String>();
//        for (PointF point : points) {
//            poi.add(point.x + "," + point.y);
//        }
        rest.put("points", poi);
        mMethodChannel.invokeMethod("onQRCodeRead", rest);
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
//        String tipText = _view.getScanBoxView().getTipText();
//        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
//        if (isDark) {
//            if (!tipText.contains(ambientBrightnessTip)) {
//                _view.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
//            }
//        } else {
//            if (tipText.contains(ambientBrightnessTip)) {
//                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
//                _view.getScanBoxView().setTipText(tipText);
//            }
//        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }
}
