package io.github.kermit95.android_media.ffmpeg;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import io.github.kermit95.android_media.R;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button btn;
    private EditText mEditText;
    private LinearLayout mOutputLayout;
    private ProgressDialog mProgressDialog;


    private FFmpeg mFFmpeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init view
        btn = (Button) findViewById(R.id.btn_run);
        mEditText = (EditText) findViewById(R.id.ed_input);
        mOutputLayout = (LinearLayout) findViewById(R.id.ll_output);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(null);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(input)){
                    String[] command = input.split(" ");
                    execFFmpegBinary(command);
                }else{
                    Toast.makeText(MainActivity.this, "input error!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // init ffmpeg
        mFFmpeg = FFmpeg.getInstance(this);
        try {
            mFFmpeg.loadBinary(new LoadBinaryResponseHandler());
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void addTextViewtoLayout(String text){
        TextView textView = new TextView(this);
        textView.setText(text);
        mOutputLayout.addView(textView);
    }

    // ffmpeg -f s16le -ar 44100 -ac 2 -i test.pcm -acodec aac -strict experimental test.aac
    private void execFFmpegBinary(final String[] command){
        try {
            mFFmpeg.execute(command, new ExecuteBinaryResponseHandler(){
                @Override
                public void onStart() {
                    mOutputLayout.removeAllViews();
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    mProgressDialog.setMessage("Processing...");
                    mProgressDialog.show();
                }

                @Override
                public void onProgress(String message) {
                    addTextViewtoLayout(message);
                    mProgressDialog.setMessage("Processing\n"+ message);
                }

                @Override
                public void onFinish() {
                    Toast.makeText(MainActivity.this, "Finish!", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
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
