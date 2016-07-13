package io.github.kermit95.android_media.refractor_and;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.todoroo.aacenc.AACEncoder;
import com.todoroo.aacenc.AACToM4A;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Created by kermit on 16/7/13.
 */

public class AccCodec implements OhMyEncoder {

    private AACEncoder aacEncoder;

    private Context mContext;

    private File inputFile;
    private File outputFile;


    public AccCodec(Context context){
        this.mContext = context;
    }

    @Override
    public void prepare(File inputFile, File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        aacEncoder = new AACEncoder();
    }

    @Override
    public void encode() {
        new EncodeToM4ATask().execute(inputFile);
    }

    @Override
    public void relase() {
    }

    private class EncodeToM4ATask extends AsyncTask<File, Integer, Void> {

        @Override
        protected Void doInBackground(File... params) {

            try {

                String filePath = params[0].getAbsolutePath();
                String outputAAC = filePath.replace(".pcm", ".aac");

                /** 以下使用android-aac-encoder **/
                DataInputStream mDataInputStream = new DataInputStream(new FileInputStream(params[0]));

                byte[] buffer = new byte[(int) params[0].length()];

                mDataInputStream.read(buffer);

                // PCM -> AAC
                aacEncoder.init(32000, 2, AudioConfig.SAMPLE_RATE, 16, outputAAC);
                aacEncoder.encode(buffer);
                aacEncoder.uninit();

                // AAC -> M4A
                new AACToM4A().convert(mContext, outputAAC, outputFile.getAbsolutePath());

                mDataInputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(mContext, "Finish!", Toast.LENGTH_SHORT).show();
        }
    }
}
