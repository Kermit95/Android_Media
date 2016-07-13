package io.github.kermit95.android_media.audiodemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import io.github.kermit95.android_media.R;
import io.github.kermit95.android_media.ffmpeg.FFmpegParam;
import io.github.kermit95.android_media.refractor_and.AccCodec;
import io.github.kermit95.android_media.refractor_and.AudioRecordRecorder;
import io.github.kermit95.android_media.refractor_and.AudioTrackPlayer;
import io.github.kermit95.android_media.refractor_and.OhMyEncoder;
import io.github.kermit95.android_media.refractor_and.OhMyPlayer;
import io.github.kermit95.android_media.refractor_and.OhMyRecorder;


/**
 * Created by kermit on 16/7/9.
 */
public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "AudioRecordActivity";

    public static final int PERMISSION_REQUEST_RECORD_AUDIO = 566;

    private String fileDirPath;
    private String filePath;
    private File savedFile;

    // view
    private ListView mListView;
    private RecordAdapter mRecordAdapter;
    private Button mBtnRecord;
    private Button mBtnPause;
    private Button mBtnSave;
    private Button mBtnResume;
    private AlertDialog mDialog;

    // Audio Worker
//    private AudioWorker mAudioWorker;
    private OhMyPlayer mPlayer;
    private OhMyEncoder mEncoder;
    private OhMyRecorder mRecorder;

    // data
    private String[] recordFilesName;

    // FFmpeg
    private FFmpeg mFmpeg;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        mListView = (ListView) findViewById(R.id.list_record);

        mBtnRecord = (Button) findViewById(R.id.btn_record);
        mBtnPause = (Button) findViewById(R.id.btn_pause);
        mBtnSave = (Button) findViewById(R.id.btn_save);
        mBtnResume = (Button) findViewById(R.id.btn_resume);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(null);

        mBtnRecord.setOnClickListener(this);
        mBtnPause.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mBtnResume.setOnClickListener(this);

        handlePermission();

//        mAudioWorker = new AudioWorker(this);
        mRecorder = new AudioRecordRecorder();
        mPlayer = new AudioTrackPlayer();
        mEncoder = new AccCodec(this);

        initFile();
        initLisetView();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mAudioWorker != null){
//            mAudioWorker.releasePlayer();
//            mAudioWorker.releaseRecorder();
//        }
        mRecorder.release();
        mPlayer.release();
    }


    private void initFile() {
        if(Environment.getExternalStorageState().
                equals( Environment.MEDIA_MOUNTED)){
            fileDirPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/TestRecord";

            File files = new File(fileDirPath);

            if (!files.exists()){
                files.mkdir();
            }

            recordFilesName = files.list();
        }
    }

    private void initLisetView() {
        mRecordAdapter = new RecordAdapter();
        mListView.setAdapter(mRecordAdapter);
    }


    private void handlePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("需要录制音频权限");
                builder.setMessage("确认授予音频权限吗?");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                    PERMISSION_REQUEST_RECORD_AUDIO);
                        }
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORD_AUDIO: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "granted RECORD_AUDIO permission");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("功能受限");
                    builder.setMessage("由于未能得到权限, 将无法录制音频");
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            checkedPermission = true;
                        }
                    });
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_record:
                final EditText ed_filename = new EditText(this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("输入文件名(可不输入):")
                        .setView(ed_filename)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // get input
                                String text = ed_filename.getText().toString();

                                // set save file name
                                filePath = fileDirPath + File.separator + text +
                                        new SimpleDateFormat("yyyyMMddHHmmss")
                                                .format(System .currentTimeMillis()) + ".pcm";
                                savedFile = new File(filePath);

                                try {
                                    savedFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                                // start record
                                mRecorder.prepare(savedFile);
                                mRecorder.record();

                                // set button
                                toggleRecordButton(false);
                                toggleResumeButton(false);
                                mDialog.dismiss();

                                updateDir();
                            }
                        });

                mDialog = builder.create();
                mDialog.setCancelable(false);
                mDialog.show();
                break;
            case R.id.btn_pause:
//                mAudioWorker.pauseRecord();
                mRecorder.pause();
                toggleRecordButton(true);
                toggleResumeButton(true);
                break;
            case R.id.btn_resume:
                mRecorder.resume();
//                mAudioWorker.resumeRecord();
                break;
            case R.id.btn_save:
                if (savedFile != null && savedFile.exists()){

//                    mAudioWorker.stopRecord();
                    mRecorder.stop();
                    new AlertDialog.Builder(this)
                            .setTitle("Save?")
                            .setPositiveButton("Yes", null)
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    savedFile.delete();
                                    updateDir();
                                }
                            }).show();
                }
                toggleRecordButton(true);
                break;
        }
    }

    private void toggleSaveButton(boolean active){
        if (active){
            mBtnResume.setText("停止/保存");
            mBtnResume.setEnabled(true);
        }else{
            mBtnResume.setText("停止/保存");
            mBtnResume.setEnabled(false);
        }
    }


    private void toggleResumeButton(boolean active){
        if (active){
            mBtnResume.setText("恢复");
            mBtnResume.setEnabled(true);
        }else{
            mBtnResume.setText("恢复");
            mBtnResume.setEnabled(false);
        }
    }

    private void toggleRecordButton(boolean active){
        if (active){
            mBtnRecord.setText("录音");
            mBtnRecord.setEnabled(true);
        }else{
            mBtnRecord.setText("录音...");
            mBtnRecord.setEnabled(false);
        }
    }

    /**
     * 更新文件目录, 更新listview的显示
     */
    private void updateDir(){
        // read updated file
        File files = new File(fileDirPath);
        recordFilesName = files.list();

        // update listview
        mRecordAdapter.notifyDataSetChanged();
    }


    class RecordAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public RecordAdapter(){
            mInflater = LayoutInflater.from(AudioRecordActivity.this);
        }

        @Override
        public int getCount() {
            return recordFilesName == null ? 0 : recordFilesName.length;
        }

        @Override
        public Object getItem(int position) {
            return recordFilesName[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_record, parent, false);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.filename = (TextView) convertView.findViewById(R.id.tv_record_filename);
            viewHolder.play = (Button) convertView.findViewById(R.id.btn_record_play);
            viewHolder.stop = (Button) convertView.findViewById(R.id.btn_record_stop);
            viewHolder.encode = (Button) convertView.findViewById(R.id.btn_record_encode);


            viewHolder.filename.setText(recordFilesName[position]);
            viewHolder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String targetPath = fileDirPath + File.separator + recordFilesName[position];
                    File targetFile = new File(targetPath);

//                    mAudioWorker.play(targetFile);
                    mPlayer.prepare(targetFile);
                    mPlayer.play();

                }
            });
            viewHolder.stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    mAudioWorker.stopPlay();
                    mPlayer.stop();
                }
            });
            viewHolder.encode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String targetPath = fileDirPath + File.separator + recordFilesName[position];
//                    mAudioWorker.convertToM4A(targetPath);
                    mEncoder.prepare(new File(targetPath), new File(targetPath.replace(".pcm", ".m4a")));
                    mEncoder.encode();
//                    pcm2acc(targetPath, fileDirPath + File.separator + "encode.m4a");

                }
            });

            return convertView;
        }

        class ViewHolder{
            TextView filename;
            Button play;
            Button stop;
            Button encode;
        }
    }





    /** 以下是尝试使用 FFmpeg , 没有成功 **/
    // encode pcm to acc
    private void pcm2acc(String in, String out){
        String s = FFmpegParam.FF_PREFIX + in + FFmpegParam.FF_SUFFIX + out;
        String[] command = s.split(" ");
        execFFmpegBinary(command);
    }


    // ffmpeg -f s16le -ar 44100 -ac 2 -i test.pcm -acodec aac -strict experimental test.aac
    private void execFFmpegBinary(final String[] command){
        try {
            mFmpeg.execute(command, new ExecuteBinaryResponseHandler(){
                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    mProgressDialog.setMessage("Processing...");
                    mProgressDialog.show();
                }

                @Override
                public void onProgress(String message) {
                    mProgressDialog.setMessage("Processing\n"+ message);
                }

                @Override
                public void onFinish() {
                    Toast.makeText(AudioRecordActivity.this, "Finish!", Toast.LENGTH_SHORT).show();
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
