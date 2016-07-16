package io.github.kermit95.android_media.finally_audiorecord_track_demo.recorder;

import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import io.github.kermit95.android_media.finally_audiorecord_track_demo.AudioConfig;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.OhMyRecorder;

/**
 * Created by kermit on 16/7/13.
 */

public class AudioRecordRecorder implements OhMyRecorder {

    private static final String TAG = "AudioRecordRecorder";

    /**
     * 最大录音长度 50 min
     */
    private static final int MAX_LENGTH = 300 * 1000;

    // audiorecord
    private AudioRecord audioRecord;
    private int inBufferSize;

    private String targetPath;

    private RecorderCallback mCallback;

    private RecordState mState;

    private enum RecordState{
        Prepared,
        Recording,
        Paused,
        Stoped
    }


    public AudioRecordRecorder(){
        // bufferSize = samplerate x bit-width x 采样时间 x channel_count
        inBufferSize = AudioRecord.getMinBufferSize(
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_IN,
                AudioConfig.AUDIO_ENCODING);

        audioRecord = new AudioRecord(
                AudioConfig.AUDIO_SOURCE,
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_IN,
                AudioConfig.AUDIO_ENCODING,
                inBufferSize);
    }


    @Override
    public void prepare(String targetPath, RecorderCallback callback) {
        this.mCallback = callback;
        this.targetPath = targetPath;
        mState = RecordState.Prepared;
    }

    @Override
    public void record() {
        switch (mState){
            case Prepared:
                new RecordTask().execute(targetPath);
                break;
            case Paused:
                mState = RecordState.Recording;
                break;

        }
    }

    @Override
    public void pause() {
        mState = RecordState.Paused;
    }

    @Override
    public void stop() {
        mState = RecordState.Stoped;
    }


    @Override
    public void release() {
        if (audioRecord != null){
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED){
                audioRecord.stop();
            }
            audioRecord.release();
        }
    }

    private class RecordTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {

            RandomAccessFile randomAccessFile = null;

            try {
                randomAccessFile = new RandomAccessFile(params[0], "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (randomAccessFile == null){
                return null;
            }

            try {
                byte[] buffer = new byte[inBufferSize/4];
                audioRecord.startRecording();
                mState = RecordState.Recording;

                while(mState != RecordState.Stoped) {

                    if (mState == RecordState.Paused){
                        continue;
                    }

                    audioRecord.read(buffer, 0, buffer.length);

                    //向原文件中追加内容
                    randomAccessFile.seek(randomAccessFile.length());
                    randomAccessFile.write(buffer, 0, buffer.length);
                }

                audioRecord.stop();
                randomAccessFile.close();

            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mCallback.onFinish();
        }
    }

    // 定时器设置，实现计时, 单位是秒
    private int timeLength = 0;
    private Handler handler = new Handler();

//    private Runnable recordTimeTask = new Runnable() {
//        public void run() {
//            if(mState == PlayerState.Playing){
//                handler.postDelayed(this, 1000);
//                timeLength++;
//            }
//        }
//    };

}
