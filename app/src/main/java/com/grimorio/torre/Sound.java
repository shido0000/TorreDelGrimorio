package com.grimorio.torre;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

/** Efectos de sonido chiptune con SoundPool (con limitador anti-spam). */
public final class Sound {

    public static final int SHOOT = 0, HIT = 1, DIE = 2, COIN = 3, LEVEL = 4,
            SUMMON = 5, WAVE = 6, STEAL = 7, CLICK = 8;

    private static SoundPool sp;
    private static int[] ids = new int[9];
    private static long[] last = new long[9];
    private static boolean on = true;

    public static void init(Context ctx) {
        if (sp != null) return;
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sp = new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attrs).build();
        int[] res = {
                R.raw.shoot, R.raw.hit, R.raw.die, R.raw.coin, R.raw.levelup,
                R.raw.summon, R.raw.wave, R.raw.steal, R.raw.click
        };
        for (int i = 0; i < res.length; i++) ids[i] = sp.load(ctx, res[i], 1);
    }

    public static void setOn(boolean b) { on = b; }

    public static boolean isOn() { return on; }

    public static void play(int i) {
        if (!on || sp == null) return;
        long now = System.currentTimeMillis();
        if (now - last[i] < 60) return;
        last[i] = now;
        try {
            sp.play(ids[i], 0.85f, 0.85f, 1, 0, 1f);
        } catch (Exception ignored) { }
    }
}
