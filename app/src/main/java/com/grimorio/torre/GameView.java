package com.grimorio.torre;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/** Motor del juego: bucle principal, escalado a coordenadas virtuales 1080x1920 y escenas. */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    public static final float VW = 1080f;
    public static final float VH = 1920f;

    public interface Scene {
        void update(float dt);
        void draw(Canvas c, float t);
        void touch(int action, float x, float y);
        boolean back();
    }

    public final Activity act;
    public final Save save;
    public final Billing billing;

    private Thread thread;
    private volatile boolean running;
    private long lastNs;
    private float time;

    private Scene scene;
    private float sx, sy, ox, oy;

    public GameView(Activity act) {
        super(act);
        this.act = act;
        getHolder().addCallback(this);
        setFocusable(true);
        save = new Save();
        save.load(act);
        if (save.install == 0) save.install = System.currentTimeMillis();
        save.lastSeen = System.currentTimeMillis();
        Art.init(act);
        billing = new Billing(act, this);
        billing.start();
        setScene(new MenuScene(this));
    }

    public void setScene(Scene s) {
        scene = s;
    }

    public void toast(String msg) {
        act.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void persist() {
        save.lastSeen = System.currentTimeMillis();
        save.save(act);
    }

    /** Lo llama MainActivity al pulsar Atrás. Devuelve true si la escena lo consumió. */
    public boolean backRequested() {
        if (scene != null) return scene.back();
        return false;
    }

    public void onPause_() {
        running = false;
        persist();
        if (thread != null) {
            try { thread.join(500); } catch (InterruptedException ignored) { }
            thread = null;
        }
    }

    public void onResume_() {
        if (thread != null) return;
        running = true;
        lastNs = System.nanoTime();
        thread = new Thread(this, "game");
        thread.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) { onResume_(); }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        computeScale(w, h);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { onPause_(); }

    private void computeScale(int w, int h) {
        float s = Math.min(w / VW, h / VH);
        sx = s; sy = s;
        ox = (w - VW * s) / 2f;
        oy = (h - VH * s) / 2f;
    }

    @Override
    public void run() {
        while (running) {
            long now = System.nanoTime();
            float dt = (now - lastNs) / 1000000000f;
            lastNs = now;
            if (dt > 0.05f) dt = 0.05f;
            time += dt;
            Canvas c = null;
            try {
                c = getHolder().lockCanvas();
                if (c != null) {
                    synchronized (getHolder()) {
                        if (c.getWidth() > 0 && sx == 0) computeScale(c.getWidth(), c.getHeight());
                        if (scene != null) scene.update(dt);
                        c.drawColor(Color.BLACK);
                        c.save();
                        c.translate(ox, oy);
                        c.scale(sx, sy);
                        c.clipRect(0, 0, VW, VH);
                        if (scene != null) scene.draw(c, time);
                        c.restore();
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (c != null) {
                    try { getHolder().unlockCanvasAndPost(c); } catch (Exception ignored) { }
                }
            }
            try {
                Thread.sleep(14);
            } catch (InterruptedException ignored) { }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float vx = (e.getX() - ox) / sx;
        float vy = (e.getY() - oy) / sy;
        if (scene != null) scene.touch(e.getActionMasked(), vx, vy);
        return true;
    }

    public Context ctx() { return act; }
}
