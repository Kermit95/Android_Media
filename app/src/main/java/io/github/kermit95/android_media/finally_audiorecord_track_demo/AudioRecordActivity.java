package io.github.kermit95.android_media.finally_audiorecord_track_demo;

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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.github.kermit95.android_media.R;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.encoder.EncoderCallback;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.encoder.MediaCodecEncoder;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.player.AudioTrackPlayer;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.recorder.AudioRecordRecorder;
import io.github.kermit95.android_media.finally_audiorecord_track_demo.recorder.RecorderCallback;


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
    private Button mBtnDeleteAll;
    private AlertDialog mDialog;
    private ProgressDialog mProgressDialog;

    // Audio Worker
    private OhMyPlayer mPlayer;
    private OhMyEncoder mEncoder;
    private OhMyRecorder mRecorder;

    // data
    private String[] recordFilesName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        mListView = (ListView) findViewById(R.id.list_record);

        mBtnRecord = (Button) findViewById(R.id.btn_record);
        mBtnPause = (Button) findViewById(R.id.btn_pause);
        mBtnSave = (Button) findViewById(R.id.btn_save);
        mBtnResume = (Button) findViewById(R.id.btn_resume);
        mBtnDeleteAll = (Button) findViewById(R.id.btn_delete_all);

        mBtnRecord.setOnClickListener(this);
        mBtnPause.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mBtnResume.setOnClickListener(this);
        mBtnDeleteAll.setOnClickListener(this);

        initButtonState();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(null);

        handlePermission();

        mRecorder = new AudioRecordRecorder();
        mPlayer = new AudioTrackPlayer();
        mEncoder = new MediaCodecEncoder();

        if(Environment.getExternalStorageState().
                equals( Environment.MEDIA_MOUNTED)){
            fileDirPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/TestRecord";
        }

        initLisetView();
    }

    private void initButtonState(){
        toggleRecordButton(true);
        toggleSaveButton(false);
        togglePauseButton(false);
        toggleResumeButton(false);
        toggleDeleteAll(true);
    }

    /**
     * 更新文件目录, 更新listview的显示
     */
    private void updateDir(){
        // read updated file
        File files = new File(fileDirPath);
        if (!files.exists()){
            if (files.mkdir()){
                recordFilesName = files.list();
            }
        }else {
            recordFilesName = files.list();
        }
        // update listview
        mRecordAdapter.notifyDataSetChanged();
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
                            requestPermissions(new String[]{ Manifest.permission.RECORD_AUDIO },
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

                new AlertDialog.Builder(this)
                        .setTitle("输入文件名(可不输入):")
                        .setView(ed_filename)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // get input
                                String text = ed_filename.getText().toString();

                                // set save file name
                                filePath = fileDirPath + File.separator + text +
                                        new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
                                                .format(System .currentTimeMillis()) + ".pcm";

                                savedFile = new File(filePath);

                                // start record
                                mRecorder.prepare(filePath, new RecorderCallback() {
                                    @Override
                                    public void onStart() {

                                    }

                                    @Override
                                    public void onFinish() {
                                        updateDir();
                                    }
                                });

                                mRecorder.record();

                                // set button
                                toggleRecordButton(false);
                                togglePauseButton(true);
                                toggleResumeButton(false);
                                toggleSaveButton(true);
                                toggleDeleteAll(false);
                            }
                        }).setCancelable(true).show();
                break;
            case R.id.btn_pause:
                mRecorder.pause();

                toggleRecordButton(false);
                togglePauseButton(false);
                toggleResumeButton(true);
                toggleSaveButton(true);
                break;
            case R.id.btn_resume:
                mRecorder.resume();
                toggleRecordButton(false);
                togglePauseButton(true);
                toggleResumeButton(false);
                toggleSaveButton(true);
                break;
            case R.id.btn_save:
                if (savedFile != null && savedFile.exists()){
                    mRecorder.stop();
                    new AlertDialog.Builder(this)
                            .setTitle("Save?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    toggleRecordButton(true);
                                    togglePauseButton(true);
                                    toggleResumeButton(true);
                                    toggleSaveButton(true);
                                    toggleDeleteAll(true);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (savedFile.delete()){
                                        updateDir();
                                    }
                                    initButtonState();
                                }
                            }).show();
                }

                break;
            case R.id.btn_delete_all:
                fileDelete(new File(fileDirPath));
                initButtonState();
                updateDir();
                break;
        }
    }

    private void toggleSaveButton(boolean active){
        if (active){
            mBtnSave.setText("停止/保存");
        }else{
            mBtnSave.setText("停止/保存");
        }
        mBtnSave.setEnabled(active);
    }

    private void togglePauseButton(boolean active){
        if (active){
            mBtnPause.setText("暂停");
        }else {
            mBtnPause.setText("暂停中");
        }
        mBtnPause.setEnabled(active);
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
            mBtnRecord.setText("录音中");
            mBtnRecord.setEnabled(false);
        }
    }

    private void toggleDeleteAll(boolean active){
        mBtnDeleteAll.setEnabled(active);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecorder.release();
        mPlayer.release();
    }


    private class RecordAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        RecordAdapter(){
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
                    mPlayer.prepare(targetPath);
                    mPlayer.play();
                }
            });
            viewHolder.stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String targetPath = fileDirPath + File.separator + recordFilesName[position];
                    mPlayer.prepare(targetPath);
                    mPlayer.stop();
                }
            });

            viewHolder.encode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String targetPath = fileDirPath + File.separator + recordFilesName[position];

                    mEncoder.prepare(targetPath, targetPath.replace(".pcm", ".m4a"), new EncoderCallback() {
                        @Override
                        public void onStart() {
                            mProgressDialog.show();
                        }

                        @Override
                        public void onProgress(String msg) {
                            mProgressDialog.setMessage(msg);
                        }

                        @Override
                        public void onFinish() {
                            mProgressDialog.dismiss();
                            Toast.makeText(AudioRecordActivity.this, "Finish!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mEncoder.encode();

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

    /**
     * 删除文件，可删除文件夹
     * @param file
     */
    private void fileDelete(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                fileDelete(f);
            }
            file.delete();
        }
    }
}
