package io.github.kermit95.android_media.audiorecord_track_demo.encoder;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.todoroo.aacenc.AACEncoder;
import com.todoroo.aacenc.AACToM4A;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.github.kermit95.android_media.audiorecord_track_demo.AudioConfig;
import io.github.kermit95.android_media.audiorecord_track_demo.OhMyEncoder;


/**
 * Created by kermit on 16/7/13.
 */

public class AccCodecEncoder implements OhMyEncoder {

    private AACEncoder aacEncoder;

    private Context mContext;

    private String inputPath;
    private String outputPath;

    private EncoderCallback mCallback;

    public AccCodecEncoder(Context context){
        this.mContext = context;
    }

    @Override
    public void prepare(String inputPath, String outputPath, EncoderCallback callback) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        aacEncoder = new AACEncoder();
        this.mCallback = callback;
    }

    @Override
    public void encode() {
        new EncodeToM4ATask().execute(inputPath);
    }

    @Override
    public void relase() {
    }

    private class EncodeToM4ATask extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {

                String filePath = params[0];
                String outputAAC = filePath.replace(".pcm", ".aac");

                /** 以下使用android-aac-encoder **/
                DataInputStream mDataInputStream = new DataInputStream(new FileInputStream(params[0]));

                byte[] buffer = new byte[(int) new File(params[0]).length()];

                mDataInputStream.read(buffer);

                // PCM -> AAC
                aacEncoder.init(AudioConfig.BITRATE, AudioConfig.CHANNEL_COUNT, AudioConfig.SAMPLE_RATE, 16, outputAAC);
                aacEncoder.encode(buffer);
                aacEncoder.uninit();

                // AAC -> M4A
                new AACToM4A().convert(mContext, outputAAC, outputPath);

                mDataInputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            mCallback.onFinish();
            Toast.makeText(mContext, "Finish!", Toast.LENGTH_SHORT).show();
        }
    }
}
