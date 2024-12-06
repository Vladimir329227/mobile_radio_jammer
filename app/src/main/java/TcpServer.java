package com.example.rtlscanner;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.mobile_radio_jammer.R;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TcpServer {
    /*private static final String TAG = "TcpServer";
    private static final int SAMPLE_RATE = 2048000;
    private static final int FFT_SIZE = 1024;
    private static final double THRESHOLD_MULTIPLIER = 3.0; // Коэффициент для определения порога

    private ServerSocket serverSocket;
    private List<Double> rez = new ArrayList<>();
    public byte[] cur_byte_buffer = new byte[1024];
    private static final String RTL_TCP_SERVER_IP = "127.0.0.1"; // IP-адрес сервера rtl_tcp_andro
    private static final int RTL_TCP_SERVER_PORT = 1234; // Порт сервера rtl_tcp_andro

    @SuppressLint("StaticFieldLeak")
    public void start() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Socket rtlTcpSocket = new Socket(RTL_TCP_SERVER_IP, RTL_TCP_SERVER_PORT);
                    Log.d(TAG, "Connected to RTL-TCP server");

                    DataInputStream rtlTcpInputStream = new DataInputStream(rtlTcpSocket.getInputStream());

                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = rtlTcpInputStream.read(buffer)) != -1) {
                        //processSignal(buffer);
                        for(int i = 0; i < 1023; i++) {
                            cur_byte_buffer[i] = buffer[i];
                        }
                    }

                    rtlTcpInputStream.close();
                    rtlTcpSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error in TCP client: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    private void processSignal(byte[] buffer) {
        Complex[] complexSignal = new Complex[FFT_SIZE];
        for (int i = 0; i < FFT_SIZE; i++) {
            int real = (buffer[2 * i] & 0xFF) - 128;
            int imag = (buffer[2 * i + 1] & 0xFF) - 128;
            complexSignal[i] = new Complex(real, imag);
        }

        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] transformedSignal = transformer.transform(complexSignal, TransformType.FORWARD);

        // Вычисление среднего значения и стандартного отклонения магнитуд
        double sum = 0;
        double sumSquared = 0;
        for (Complex c : transformedSignal) {
            double magnitude = c.abs();
            sum += magnitude;
            sumSquared += magnitude * magnitude;
        }
        double mean = sum / transformedSignal.length;
        double variance = (sumSquared / transformedSignal.length) - (mean * mean);
        double standardDeviation = Math.sqrt(variance);

        // Определение порога
        double threshold = mean + THRESHOLD_MULTIPLIER * standardDeviation;


        rez.clear();
        for (int i = 0; i < transformedSignal.length; i++) {
            double magnitude = transformedSignal[i].abs();
            if (magnitude > threshold) {
                rez.add((double) (i * (SAMPLE_RATE / FFT_SIZE)));
            }
        }

        Log.d(TAG, "Active frequencies: " + rez);
    }

    public List<Double> get_rez() {return rez;}
    public byte[] get_cur_byte_buffer() {
        if (cur_byte_buffer == null){
            byte[] t = new byte[1];
            t[0] = 0;
            return t;
        }
        return cur_byte_buffer;
    }

    public void stop() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error stopping server: " + e.getMessage());
            }
        }
    }
*/

    public static double[] cur_byte_buffer= new double[100];
    public static int cur_porog = 5;
    public static int cur_rez = 0;
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 1234;
    private static final int BUFFER_SIZE = 2; // Размер буфера для одного образца IQ-данных
    private static final int SAMPLE_COUNT = 1024; // Количество образцов для анализа
    private static final int FFT_SIZE = 1024;
    private static final double Fs = 2048000;
    com.example.dtmfsender.sendDTMFer sender = new com.example.dtmfsender.sendDTMFer();
    private Queue<Double> magnitudeQueue = new LinkedList<>();
    private double sum = 0;
    private double sumSquares = 0;
    private static final int WINDOW_SIZE = 10;

    public void start1() {
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        executorService.execute(new TCPClientTask());
    }

    public Handler mainHandler;
    private ExecutorService executorService;
    public boolean id_in_use_data = false, is_play_dtmf = false;
    private long lastUpdateTime = 0;
    private byte[] buffer = new byte[1024];
    private int bytesRead;

    private class TCPClientTask implements Runnable {

        @Override
        public void run() {
            try {
                Socket socket = new Socket(HOST, PORT);
                InputStream in = socket.getInputStream();

                while ((bytesRead = in.read(buffer)) != -1) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime >= 500) {
                        updateUI();
                        lastUpdateTime = currentTime;
                    }
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void updateUI() {
            mainHandler.post(() -> {
                if(!id_in_use_data){
                    processIQData(buffer, bytesRead);
                }
            });
        }
    }


    private class DataReceiverTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try (Socket socket = new Socket(HOST, PORT);
                 DataInputStream input = new DataInputStream(socket.getInputStream())) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                Log.d("29.09.2024", "Test");


                while ((bytesRead = input.read(buffer)) != -1) {
                    processIQData(buffer, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


    }

    @SuppressLint("StaticFieldLeak")
    public void start() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {

                    /*
                    Socket rtlTcpSocket = new Socket(HOST, PORT);
                    //Log.d(TAG, "Connected to RTL-TCP server");

                    DataInputStream rtlTcpInputStream = new DataInputStream(rtlTcpSocket.getInputStream());
*/
                    /*
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;

                    while ((bytesRead = rtlTcpInputStream.read(buffer)) != -1) {
                        processIQData(buffer, bytesRead);
                    }*/




                    SocketChannel socketChannel = SocketChannel.open();
                    Log.d("29.09.2024", "Test2");
                    socketChannel.connect(new java.net.InetSocketAddress(HOST, PORT));
                    socketChannel.configureBlocking(false);

                    DataInputStream input = new DataInputStream(socketChannel.socket().getInputStream());
                    Log.d("29.09.2024", "Test2.2");


                    Log.d("29.09.2024", "Test3");

                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead;
                    Log.d("29.09.2024", "Test4");

                    while (true) {
                        bytesRead = socketChannel.read(buffer);
                        if (bytesRead > 0) {
                            buffer.flip();
                            byte[] data = new byte[bytesRead];
                            buffer.get(data);
                            processIQData(data, bytesRead);
                            buffer.clear();
                        }
                    }

                    /*rtlTcpInputStream.close();
                    rtlTcpSocket.close();*/


                } catch (IOException e) {
                    //Log.e(TAG, "Error in TCP client: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    private void processIQData(byte[] buffer, int length) {
        id_in_use_data = true;
        Complex[] iqData = new Complex[FFT_SIZE]; // Массив для комплексных чисел (I и Q)
        int index = 0;

        for (int i = 0; i < length; i += 2) {
            if (i + 1 < length) {
                int iPart = Byte.toUnsignedInt(buffer[i]);
                int qPart = Byte.toUnsignedInt(buffer[i + 1]);

                iqData[index++] = new Complex(iPart, qPart);            //// можно обеденить с другими циклами
            }
        }

        if (index < FFT_SIZE) {
            for (int i = index; i < FFT_SIZE; i++) {
                iqData[i] = Complex.ZERO;
            }
        }

        // Выполняем FFT
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] transformedData = transformer.transform(iqData, TransformType.FORWARD);

        // Анализируем результаты FFT для поиска активных частот
        analyzeFrequencies_stat(transformedData);
    }

    private void analyzeFrequencies_stat(Complex[] transformedData) {
        // Вычисляем среднее значение и стандартное отклонение мощности
        double sum = 0;
        double sumSquares = 0;
        int count = 0;

        for (int i = 0; i < FFT_SIZE; i++) {
            double magnitude = transformedData[i].abs();
            sum += magnitude;
            sumSquares += magnitude * magnitude;
            count++;
        }

        double mean = sum / count;
        double variance = (sumSquares / count) - (mean * mean);
        double stdDev = Math.sqrt(variance);

        // Устанавливаем пороговое значение как среднее значение плюс несколько стандартных отклонений
        double threshold = mean + cur_porog * stdDev;
        for (int i = 0; i < cur_rez+1; i++)
            cur_byte_buffer[i] = 0;
        cur_rez = 0;
        double frequency, magnitude, max_magnitude = 0, max_frequency = 0;
        for (int i = 0; i < FFT_SIZE; i++) {
            magnitude = transformedData[i].abs();
            frequency = (i * Fs) / FFT_SIZE;
            if (magnitude > threshold && frequency != 0.0 && frequency != 2000.0 && frequency != 2046000.0) {   //FS
                max_magnitude = Double.max(max_magnitude, magnitude);
                cur_byte_buffer[cur_rez] = frequency;
                cur_rez++;
                cur_byte_buffer[cur_rez] = magnitude;
                cur_rez++;
                //publishProgress(frequency, magnitude);
            }
        }
        for (int i = 0; i < FFT_SIZE; i++) {
            magnitude = transformedData[i].abs();
            frequency = (i * Fs) / FFT_SIZE;
            if (max_magnitude == magnitude && frequency != 0.0) {
                cur_byte_buffer[cur_rez] = frequency;
                cur_rez++;
                max_frequency = frequency;
                //publishProgress(frequency, magnitude);
            }
        }
        cur_byte_buffer[cur_rez] = -1;
        if (!is_play_dtmf && max_frequency != 0){
            is_play_dtmf = true;
            sender.sendDTMF(max_frequency);
            is_play_dtmf = false;
        }
        id_in_use_data = false;
    }
    private void analyzeFrequencies_analiz(Complex[] transformedData) {
        for (int i = 0; i < cur_rez+1; i++)
            cur_byte_buffer[i] = 0;
        cur_rez=0;
        for (int i = 0; i < FFT_SIZE; i++) {
            double magnitude = transformedData[i].abs();

            // Обновляем скользящее среднее и стандартное отклонение
            updateMovingAverage(magnitude);

            // Устанавливаем пороговое значение как среднее значение плюс несколько стандартных отклонений
            double threshold = getThreshold();
            double frequency = (i * Fs) / FFT_SIZE;

            if (magnitude > threshold && frequency != 0.0 && frequency != 2046000.0) {
                cur_byte_buffer[cur_rez] = frequency;
                cur_rez++;
            }
            cur_byte_buffer[cur_rez] = -1;
        }
    }
    private void updateMovingAverage(double magnitude) {
        if (magnitudeQueue.size() >= WINDOW_SIZE) {
            double removedValue = magnitudeQueue.poll();
            sum -= removedValue;
            sumSquares -= removedValue * removedValue;
        }
        magnitudeQueue.add(magnitude);
        sum += magnitude;
        sumSquares += magnitude * magnitude;
    }

    private double getThreshold() {
        int count = magnitudeQueue.size();
        double mean = sum / count;
        double variance = (sumSquares / count) - (mean * mean);
        double stdDev = Math.sqrt(variance);
        return mean + 3 * stdDev;
    }

    public static double[] get_cur_byte_buffer() {
        return cur_byte_buffer.clone();
    }
    public static int get_cur_porog() {
        return cur_porog;
    }
    public static void set_cur_porog(int porog) {
        cur_porog = porog;
    }
}
