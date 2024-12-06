package com.example.dtmfsender;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.mobile_radio_jammer.MainActivity;

public class sendDTMFer {
    static boolean If_start = false;
    public boolean is_start(){
        return If_start;
    }

    public void set_start(boolean t){
        If_start = t;
    }


    public int time = 32;
    public boolean send(double[] number) {
        Log.d(TAG, "Тест 19/09/24 " + If_start);

        if (!If_start){
            Log.d(TAG, "Тест2 19/09/24 " + If_start);
            new SendDTMFTask().execute(number);
            return true;
        }
        return false;
    }

    private class SendDTMFTask extends AsyncTask<double[], Void, Void> {
        @Override
        protected Void doInBackground(double[]... params) {
            double[] number = params[0];
            //sendDTMF(number);
            return null;
        }
    }

    public void sendDTMF(double number) {
        Log.d(TAG, "Тест3 19/09/24 " + If_start);

        int bufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

        if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            Log.d(TAG, "Ошибка инициализации audioTrack");
            return;
        }
        Log.d(TAG, "Тест4 19/09/24 " + If_start);

        audioTrack.play();
        if (number != 0.0 && number != -1.0){
            byte[] dtmfBytes1 = generateDTMF('*');
            audioTrack.write(dtmfBytes1, 0, dtmfBytes1.length);
            Log.d(TAG, "Тест4.2 19/09/24 " + If_start);
            try {
                Thread.sleep(time); // 32 миллисекунды
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            audioTrack.write(dtmfBytes1, 0, dtmfBytes1.length);
            Log.d(TAG, "Тест4.2 19/09/24 " + If_start);
            try {
                Thread.sleep(time); // 32 миллисекунды
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String numberStr = String.valueOf((int)number);
            for (char c : numberStr.toCharArray()) {
                byte[] dtmfBytes = generateDTMF(c);
                audioTrack.write(dtmfBytes, 0, dtmfBytes.length);
                Log.d(TAG, "Тест4.1 19/09/24 " + If_start);
                try {
                    Thread.sleep(time); // 32 миллисекунды
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                audioTrack.write(dtmfBytes1, 0, dtmfBytes1.length);
                Log.d(TAG, "Тест4.2 19/09/24 " + If_start);
                try {
                    Thread.sleep(time); // 32 миллисекунды
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            audioTrack.write(dtmfBytes1, 0, dtmfBytes1.length);
            Log.d(TAG, "Тест4.2 19/09/24 " + If_start);
            try {
                Thread.sleep(time); // 32 миллисекунды
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            byte[] dtmfBytes = generateDTMF('*');
            audioTrack.write(dtmfBytes, 0, dtmfBytes.length);
            Log.d(TAG, "Тест4.2 19/09/24 " + If_start);
            try {
                Thread.sleep(time); // 32 миллисекунды
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            byte[] dtmfBytes2 = generateDTMF('#');
            audioTrack.write(dtmfBytes2, 0, dtmfBytes2.length);
            Log.d(TAG, "Тест4.3 19/09/24 " + If_start);
            try {
                Thread.sleep(time); // 32 миллисекунды
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Тест5 19/09/24 " + If_start);

        audioTrack.stop();
        audioTrack.release();
        Log.d(TAG, "Тест6 19/09/24 " + If_start);

    }

    private byte[] generateDTMF(char digit) {
        int sampleRate = 8000;
        int duration = 32; // 32 миллисекунды
        int numSamples = (int) (sampleRate * duration / 1000);
        short[] output = new short[numSamples];

        double[] frequencies = getDTMFFrequencies(digit);
        double increment1 = 2.0 * Math.PI * frequencies[0] / sampleRate;
        double increment2 = 2.0 * Math.PI * frequencies[1] / sampleRate;
        double angle1 = 0;
        double angle2 = 0;

        for (int i = 0; i < numSamples; i++) {
            output[i] = (short) ((Math.sin(angle1) + Math.sin(angle2)) * Short.MAX_VALUE / 2);
            angle1 += increment1;
            angle2 += increment2;
        }

        byte[] byteOutput = new byte[numSamples * 2];
        for (int i = 0; i < numSamples; i++) {
            byteOutput[i * 2] = (byte) (output[i] & 0x00ff);
            byteOutput[i * 2 + 1] = (byte) ((output[i] & 0xff00) >>> 8);
        }

        return byteOutput;
    }

    private double[] getDTMFFrequencies(char digit) {
        switch (digit) {
            case '1': return new double[]{697, 1209};
            case '2': return new double[]{697, 1336};
            case '3': return new double[]{697, 1477};
            case '4': return new double[]{770, 1209};
            case '5': return new double[]{770, 1336};
            case '6': return new double[]{770, 1477};
            case '7': return new double[]{852, 1209};
            case '8': return new double[]{852, 1336};
            case '9': return new double[]{852, 1477};
            case '0': return new double[]{941, 1336};
            case '*': return new double[]{941, 1209};
            case '#': return new double[]{941, 1477};
            default: return new double[]{0, 0};
        }
    }

}
