/**
 * @className: HandlerUtil
 * @author: shang
 * @date: 2025/3/24 13:34
 **/
package com.lanzhen.projecttest;

/**
 * @description
 * @author：shang
 * @data： 2025/3/24 13
 */

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

public class HandlerUtil {

    private Handler handler;
    private Handler nonUIHandler;

    private HandlerThread nonUIHandlerThread;

    private HandlerUtil() {
    }

    private static class Holder {
        private static final HandlerUtil INSTANCE = new HandlerUtil();
    }

    public static final HandlerUtil getInstance() {
        return Holder.INSTANCE;
    }

    public void postDelay(Runnable runnable, long delayTimes) {
        if (handler == null) {
            Log.v("HandlerUtil", "new Handler");
            handler = new Handler(Looper.getMainLooper());
        }

        handler.postDelayed(runnable, delayTimes);
    }


    public void post(Runnable runnable) {
        if (handler == null) {
            Log.v("HandlerUtil", "new Handler");
            handler = new Handler(Looper.getMainLooper());
        }

        handler.post(runnable);
    }

    public void postNonUIDelay(Runnable runnable, long delayTimes) {
        initNonUIHandler();

        nonUIHandler.postDelayed(runnable, delayTimes);
    }

    public void postNonUI(Runnable runnable) {
        initNonUIHandler();

        nonUIHandler.post(runnable);
    }

    private void initNonUIHandlerThread() {
        if (nonUIHandlerThread == null) {
            nonUIHandlerThread = new HandlerThread("idle-work-non-UIHandler");
            nonUIHandlerThread.start();
        }
    }

    private void initNonUIHandler() {
        if (nonUIHandler == null) {
            initNonUIHandlerThread();

            nonUIHandler = new Handler(nonUIHandlerThread.getLooper());
        }
    }

    public void removeAllMessage() {
        if (handler != null) {
            handler.removeMessages(0);
        }
    }
}
