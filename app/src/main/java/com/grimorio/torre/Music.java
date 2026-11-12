package com.grimorio.torre;

import android.content.Context;
import android.media.MediaPlayer;

/** Música de fondo en bucle (tema de menú y de batalla). */
public final class Music {

    private static MediaPlayer mp;
    private static int current = 0;

    public static void play(Context ctx, int res) {
        if (current == res) return;
        stop();
        try {
            mp = MediaPlayer.create(ctx, res);
            if (mp == null) return;
            mp.setLooping(true);
            mp.setVolume(0.55f, 0.55f);
            mp.start();
            current = res;
        } catch (Exception e) {
            mp = null;
            current = 0;
        }
    }

    public static void stop() {
        if (mp != null) {
            try {
                mp.stop();
                mp.release();
            } catch (Exception ignored) { }
            mp = null;
        }
        current = 0;
    }

    public static void pause() {
        if (mp != null) {
            try { mp.pause(); } catch (Exception ignored) { }
        }
    }

    public static void resume() {
        if (mp != null) {
            try { mp.start(); } catch (Exception ignored) { }
        }
    }
}
