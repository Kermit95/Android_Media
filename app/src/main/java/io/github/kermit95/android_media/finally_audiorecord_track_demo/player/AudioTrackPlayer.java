package io.github.kermit95.android_media.finally_audiorecord_track_demo.player;

import android.media.AudioTrack;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.github.kermit95.android_media.finally_audiorecord_track_demo.AudioConfig;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.OhMyPlayer;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.PlayerState;

/**
 * Created by kermit on 16/7/13.
 */

public class AudioTrackPlayer implements OhMyPlayer {

    // audio track
    private AudioTrack audioTrack;

    private int mPlaySize;
    private int offSet;

    private PlayerState mState;

    public AudioTrackPlayer() {

        int outBufferSize = AudioTrack.getMinBufferSize(
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
        mState = PlayerState.Stop;
    }

    @Override
    public void play(String targetPath) {
        switch (mState){
            case Stop:
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
    public PlayerState getState() {
        return mState;
    }

    @Override
    public void seekTo(int msec) {

    }


    private class PlayTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {

            File file = new File(params[0]);

            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            long fileSize = file.length();
            byte[] audioData = new byte[(int) fileSize];

            try {
                inputStream.read(audioData, 0, audioData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

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
