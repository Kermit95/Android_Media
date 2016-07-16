package io.github.kermit95.android_media.finally_audiorecord_track_demo;

/**
 * Created by kermit on 16/7/13.
 */

public interface OhMyPlayer {

    void play(String tagetPath);

    void pause();

    void stop();

    void release();

    PlayerState getState();

    void seekTo(int msec);

}
