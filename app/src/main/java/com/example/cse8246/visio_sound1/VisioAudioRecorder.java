package com.example.cse8246.visio_sound1;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by cse8246 on 7/24/2016.
 */

public class VisioAudioRecorder {
    private AudioRecord recorder = null;
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath();
    File outputFiledir = new File(path);
    static boolean mStartRecording;

    private int bufferSize = 0;
    private static final int RECORDER_SAMPLERATE = 22050;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private Thread recordingThread = null;
    public static final String AUDIO_RECORDER_FOLDER = "VisioSound";
    public static final String AUDIO_Learner_FOLDER = "Learn";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final int RECORDER_BPP = 16;
    private static boolean IsLearn = false;
    public VisioAudioRecorder()
    {
        bufferSize = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    public List<String> GetFiles()
    {
        File folder = new File(path,AUDIO_RECORDER_FOLDER);
        if (!folder.exists()) {
            return Collections.emptyList();
        }
        File[] listOfFiles = folder.listFiles();
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                list.add(listOfFiles[i].getName());
            }
        }
        Collections.sort(list,Collections.reverseOrder());
        return list;
    }

    public void Record(boolean isLearn) {
        IsLearn = isLearn;
        if(isLearn)
        {
            deleteLearnFiles();
        }
        if (!outputFiledir.exists()) {
            outputFiledir.mkdir();
        }
        if (recorder == null) {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
            mStartRecording = false;
        }//if
        if (!mStartRecording) {

            int i = recorder.getState();
            if (i == 1) {
                Thread waitThread = new Thread(new Runnable() {

                    @Override
                    public void run() {

                    }

                }, "Wait Thread");
                try {
                    waitThread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                try {
                    waitThread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                recorder.startRecording();
                recordingThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        writeAudioDataToFile();
                    }
                }, "AudioRecorder Thread");
                recordingThread.start();
                mStartRecording = true;
            }
        }
    }

    private void deleteLearnFiles() {
        File folder = new File(path+'/'+AUDIO_RECORDER_FOLDER+'/'+AUDIO_Learner_FOLDER);
        if (!folder.exists()) {
            return;
        }
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                listOfFiles[i].delete();
            }
        }
    }

    public void Stop()
    {
        int i = recorder.getState();
        if(i==1) {
            recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
            mStartRecording = false;
        }
        if(!IsLearn) {
            copyWaveFile(getTempFilename(), getFilename());
        }
        else copyWaveFile(getTempFilename(),getLearnFileName());
        deleteTempFile();
    }

    private String getLearnFileName() {
        File file = new File(path+"/"+AUDIO_RECORDER_FOLDER,AUDIO_Learner_FOLDER);
        if(!file.exists()){
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
    }

    public String getLearnFileNameForModify() {
        File folder = new File(path+"/"+AUDIO_RECORDER_FOLDER,AUDIO_Learner_FOLDER);
        if (!folder.exists()) {
            return new String();
        }
        File[] listOfFiles = folder.listFiles();
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                list.add(listOfFiles[i].getName());
            }
        }
        return list.get(0);
    }

    public void Play(String file)
    {
        MediaPlayer m = new MediaPlayer();
        try {
            m.setDataSource(path+'/'+AUDIO_RECORDER_FOLDER+'/'+file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            m.prepare();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        m.start();
    }

    public void PlayLearn() {
        MediaPlayer m = new MediaPlayer();
        try {
            m.setDataSource(path + '/' + AUDIO_RECORDER_FOLDER + '/'+'/'+AUDIO_Learner_FOLDER + '/'+getLearnFileNameForModify());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            m.prepare();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        m.start();
    }

    private void writeAudioDataToFile() {
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        int read = 0;

        if (null != os) {
            while (mStartRecording) {
                read = recorder.read(data, 0, bufferSize);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String getTempFilename() {
        File file = new File(path, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }
        File tempFile = new File(path,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    private String getFilename(){
        File file = new File(path,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
    }

    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());

        file.delete();
    }





}
