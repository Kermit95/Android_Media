package io.github.kermit95.android_media.mediaplay_record_demo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import io.github.kermit95.android_media.R;


/**
 * Created by kermit on 16/7/9.
 */
public class MediaRecorderActivity extends AppCompatActivity
        implements View.OnClickListener{

    private static final String TAG = "MediaRecorderActivity";

    public static final int PERMISSION_REQUEST_RECORD_AUDIO = 566;

    private String fileDirPath;
    private String filePath;
    private File savedFile;

    // view
    private ListView mListView;
    private RecordAdapter mRecordAdapter;
    private Button mBtnRecord;
    private Button mBtnSave;
    private AlertDialog mDialog;


    // functional record
    private MediaPlayer mMediaPlayer;
    private MediaRecorder mMediaRecorder;

    // data
    private String[] recordFilesName;

    // flag
    private Boolean checkedPermission = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        mListView = (ListView) findViewById(R.id.list_activity_record);
        mBtnRecord = (Button) findViewById(R.id.btn_activity_record_record);
        mBtnSave = (Button) findViewById(R.id.btn_activity_record_save);
        mMediaPlayer = new MediaPlayer();
        mMediaRecorder = new MediaRecorder();

        mBtnRecord.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);

        handlePermission();
        initLisetView();
        initFile();
    }

    private void initMediaRecorder() {
        // 录音源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        // 输出格式
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 编码格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    }

    private void handlePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
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
            }else{
                checkedPermission = true;
            }
        }else{
            checkedPermission = true;
        }
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
                            checkedPermission = true;
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
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        if (mMediaRecorder != null){
            mMediaRecorder.release();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_activity_record_record:
                final EditText ed_filename = new EditText(this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Please input the filename to save:")
                        .setView(ed_filename)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // get input
                                String text = ed_filename.getText().toString();

                                // set save file name
                                filePath = fileDirPath + File.separator + text +
                                        new SimpleDateFormat("yyyyMMddHHmmss")
                                                .format(System .currentTimeMillis()) + ".m4a";
                                savedFile = new File(filePath);

                                // set output path
                                mMediaRecorder.setOutputFile(savedFile.getAbsolutePath());

                                try {
                                    savedFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (checkedPermission){
                                    initMediaRecorder();
                                }

                                try {
                                    mMediaRecorder.prepare();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // start record
                                mMediaRecorder.start();

                                // set button
                                mBtnRecord.setText("recording...");
                                mBtnRecord.setEnabled(false);
                                mDialog.dismiss();

                                // read updated file
                                File files = new File(fileDirPath);
                                recordFilesName = files.list();

                                // update listview
                                mRecordAdapter.notifyDataSetChanged();
                            }
                        });

                mDialog = builder.create();
                mDialog.setCancelable(false);
                mDialog.show();
                break;
            case R.id.btn_activity_record_save:
                if (savedFile != null && savedFile.exists()){
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    new AlertDialog.Builder(this)
                            .setTitle("Save?")
                            .setPositiveButton("Yes", null)
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    savedFile.delete();

                                    // read the old file
                                    File files = new File(fileDirPath);
                                    recordFilesName = files.list();

                                    mRecordAdapter.notifyDataSetChanged();
                                }
                            }).show();
                }
                mBtnRecord.setText("Record");
                mBtnRecord.setEnabled(true);
                break;
        }
    }


    class RecordAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public RecordAdapter(){
            mInflater = LayoutInflater.from(MediaRecorderActivity.this);
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
            viewHolder.pause = (Button) convertView.findViewById(R.id.btn_record_stop);
            viewHolder.isPlayed = false;

            viewHolder.filename.setText(recordFilesName[position]);
            viewHolder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!viewHolder.isPlayed) {

                        mMediaPlayer.reset();

                        try {
                            mMediaPlayer.setDataSource(fileDirPath + File.separator + recordFilesName[position]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            mMediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!mMediaPlayer.isPlaying()){

                        mMediaPlayer.start();

                        viewHolder.isPlayed = true;
                    }
                }
            });
            viewHolder.pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMediaPlayer.isPlaying()){
                        mMediaPlayer.pause();
                    }
                }
            });

            return convertView;
        }

        class ViewHolder{
            TextView filename;
            Button play;
            Button pause;
            boolean isPlayed;
        }
    }
}
