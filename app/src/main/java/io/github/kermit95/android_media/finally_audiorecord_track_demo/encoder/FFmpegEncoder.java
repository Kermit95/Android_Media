package io.github.kermit95.android_media.finally_audiorecord_track_demo.encoder;

import android.content.Context;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import io.github.kermit95.android_media.finally_audiorecord_track_demo.OhMyEncoder;

/**
 * Created by kermit on 16/7/14.
 */

public class FFmpegEncoder implements OhMyEncoder {

    private static final String TAG = "FFmpegEncoder";

    // FFmpeg
    private FFmpeg mFFmpeg;

    private String inputPath;
    private String outputPath;

    private EncoderCallback mCallback;

    private Context mContext;

    public FFmpegEncoder(Context context){
        this.mContext = context;
    }

    @Override
    public void prepare(String inputPath, String outputPath, EncoderCallback callback) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.mCallback = callback;
        mFFmpeg = FFmpeg.getInstance(mContext);

        try {
            mFFmpeg.loadBinary(new LoadBinaryResponseHandler());
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void encode() {
        pcm2acc();
    }

    @Override
    public void relase() {

    }

    // ffmpeg -f s16le -ar 44100 -ac 2 -i test.pcm -acodec aac -strict experimental test.aac
    private void pcm2acc(){
        String s = FFmpegParam.FF_PREFIX + inputPath + FFmpegParam.FF_SUFFIX + outputPath;
        final String[] command = s.split(" ");
        try {
            mFFmpeg.execute(command, new ExecuteBinaryResponseHandler(){
                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    mCallback.onStart();
                }

                @Override
                public void onProgress(String message) {
                    mCallback.onProgress(message);
                }

                @Override
                public void onFinish() {
                    mCallback.onFinish();
                }

                @Override
                public void onFailure(String message) {
                    Log.e(TAG, "onFailure: " + message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.e(TAG, "onSuccess " + message);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

}
