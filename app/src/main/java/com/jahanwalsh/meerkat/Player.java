package com.jahanwalsh.meerkat;

/**
 * Created by jahanwalsh on 7/14/17.
 */
import android.support.annotation.Nullable;

public interface Player {

    void play(String url);

    void pause();

    void resume();

    boolean isPlaying();

    @Nullable
    String getCurrentTrack();

    void release();
}
