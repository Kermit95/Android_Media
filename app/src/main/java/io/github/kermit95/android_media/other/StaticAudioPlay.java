package io.github.kermit95.android_media.other;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kermit on 16/7/12.
 */

public class StaticAudioPlay {

    private AudioTrack audioTrack;
    private int pos;

    // audio params
    private int frequency;
    private int channelConfigurationIn;
    private int channelConfigurationOut;
    private int audioEncoding;

    public StaticAudioPlay(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
    }

    private void playAudioTrack_Static(short[] data, int offset, int length) {
        if (data == null || data.length == 0 && audioTrack.getPlayState() == audioTrack.PLAYSTATE_STOPPED) {
            return;
        }
        audioTrack.reloadStaticData();
        audioTrack.write(data, offset, length);
        audioTrack.setNotificationMarkerPosition(length/2);
        audioTrack.play();
    }

    public void playpause(){
        try {
            if (audioTrack.getPlayState() == audioTrack.PLAYSTATE_PLAYING){
                pos = audioTrack.getPlaybackHeadPosition();
                audioTrack.pause();
            }else if (audioTrack.getPlayState() == audioTrack.PLAYSTATE_PAUSED){
                audioTrack.setPlaybackHeadPosition(pos);
                audioTrack.play();
            }
        } catch (Exception e) {
            Log.i("MyAudioTrack", "catch exception..."+e.getMessage());
        }
        Log.e("MyAudioTrack",String.valueOf(audioTrack.getPlayState()) );
    }

    private class AudioPlayTask_Static extends AsyncTask<File, Integer, Void> {

        @Override
        protected Void doInBackground(File... params) {

            File audioFile = params[0];
            int fileSize = (int)audioFile.length();
            byte fileByte[] = new byte[fileSize];

            try{
                InputStream inputStream = new BufferedInputStream(new FileInputStream(audioFile));

                audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        frequency,
                        channelConfigurationOut,
                        audioEncoding,
                        fileSize,
                        AudioTrack.MODE_STATIC);


                int byteCount = inputStream.read(fileByte, 0, fileSize);

//                 head position to zero
                audioTrack.reloadStaticData();

                audioTrack.write(fileByte, 0, byteCount);

                audioTrack.play();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public byte[] getFileInByte(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }
        fi.close();
        return buffer;
    }
}
