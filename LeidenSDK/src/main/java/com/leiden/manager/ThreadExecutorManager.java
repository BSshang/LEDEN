package com.leiden.manager;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadExecutorManager {

    //定义一个 Handle 用于线程的切换
    private Handler handler = new Handler(Looper.getMainLooper());


    //维护一个线程池
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private static class ThreadExecutorManagerHolder {
        private static ThreadExecutorManager instance = new ThreadExecutorManager();
    }

    public static ThreadExecutorManager getInstance() {
        return ThreadExecutorManager.ThreadExecutorManagerHolder.instance;
    }

    public void execute(Runnable runnable){
        cachedThreadPool.execute(runnable);
    }

    public void post(Runnable runnable){
        post(runnable,0);
    }

    public void post(Runnable runnable,long time){
        handler.postDelayed(runnable,time);
    }


}
