package io.github.kermit95.android_media.finally_audiorecord_track_demo.player;

import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.github.kermit95.android_media.finally_audiorecord_track_demo.AudioConfig;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.OhMyPlayer;

/**
 * Created by kermit on 16/7/13.
 */

public class AudioTrackPlayer implements OhMyPlayer {

    // audio track
    private AudioTrack audioTrack;
    private int outBufferSize;

    // targetfile
    private String targetPath;

    private byte[] audioData;

    private int mPlaySize;
    private int offSet;

    private enum PlayerState{
        Prepared,
        Playing,
        Pause,
        Stop,
    }

    private PlayerState mState;

    @Override
    public void prepare(String targetPath) {
        outBufferSize = AudioTrack.getMinBufferSize(
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_OUT,
                AudioConfig.AUDIO_ENCODING);

        mPlaySize = outBufferSize * 2;

        audioTrack = new AudioTrack(
                AudioConfig.AUDIO_SOURCE,
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_OUT,
                AudioConfig.AUDIO_ENCODING,
                outBufferSize,
                AudioTrack.MODE_STREAM);

        this.targetPath = targetPath;

        File file = new File(targetPath);

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        long fileSize = file.length();

        audioData = new byte[(int) fileSize];
        try {
            inputStream.read(audioData, 0, audioData.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mState = PlayerState.Prepared;
    }

    @Override
    public void play() {
        switch (mState){
            case Stop:
            case Prepared:
                offSet = 0;
                new PlayTask().execute(targetPath);
                break;
            case Pause:
                new PlayTask().execute(targetPath);
                break;
        }
    }

    @Override
    public void pause() {
        if (mState == PlayerState.Playing){
            mState = PlayerState.Pause;
        }
    }

    @Override
    public void stop() {
        if (mState == PlayerState.Playing || mState == PlayerState.Pause){
            mState = PlayerState.Stop;
        }
    }

    @Override
    public void release() {
        if (audioTrack != null){
            if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED){
                audioTrack.stop();
            }
            audioTrack.release();
        }
    }

    @Override
    public void seekTo(int msec) {

    }


    private class PlayTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {

            audioTrack.play();

            mState = PlayerState.Playing;

            while(true){

                if (mState == PlayerState.Pause || mState == PlayerState.Stop){
                    break;
                }

                int size = audioTrack.write(audioData, offSet, mPlaySize);
                offSet += size;

                if (offSet >= audioData.length){
                    mState = PlayerState.Stop;
                }
            }

            audioTrack.stop();

            return null;
        }
    }

}
