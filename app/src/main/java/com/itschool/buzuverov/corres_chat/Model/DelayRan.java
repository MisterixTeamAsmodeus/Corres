package com.itschool.buzuverov.corres_chat.Model;

import android.os.Handler;

public class DelayRan{

    private int delay;
    private Runnable doIt;
    private Thread thread = null;
    private long lastTime = 0;
    private boolean stop = false;
    private Handler handler;

    public DelayRan(int delay, Runnable doIt, Handler handler) {
        this.delay = delay;
        this.doIt = doIt;
        this.handler = handler;
    }

    private void start() throws Exception {
        if (thread != null)
            throw new Exception("нельзя запустить второй раз");
        stop = false;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop){
                    long delta = lastTime + delay - System.currentTimeMillis();
                    if(delta <= 0){
                        handler.post(doIt);
                        break;
                    }
                    try {
                        Thread.sleep(delta);
                    } catch (InterruptedException e) {}
                }
                thread = null;
            }
        });
        reset();
        thread.start();
    }

    public void stop(){
        stop = true;
        while (thread != null){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {}
        }
    }

    public void reset(){
        if (thread != null)
            lastTime = System.currentTimeMillis();
        else {
            try {
                start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
