package com.example.mobile_radio_jammer;

import android.app.appsearch.GetByDocumentIdRequest;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.rtlscanner.TcpServer;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mobile_radio_jammer.databinding.ActivityMainBinding;
import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1234;
    private ActivityMainBinding binding;
    private com.example.rtlscanner.TcpServer tcpServer;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        int port = 1234; // Порт rtl_tcp
        int samplerate = 2048000; // Частота дискретизации      //2048000

        // Создаем Intent для запуска драйвера
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("iqsrc://-a 127.0.0.1 -p " + port + " -s " + samplerate + " -f 138000000"));

        // Запускаем активность и ожидаем результат
        startActivityForResult(intent, REQUEST_CODE);

        //if (intent.resolveActivity(getPackageManager()) != null) {
            //startActivity(intent);
        //}
        Button button = (Button)findViewById(R.id.button);
        TextView textView = (TextView)findViewById(R.id.textView2);
        //textView.setText(Arrays.toString(tcpServer.get_cur_byte_buffer()));
        EditText editText =  (EditText)findViewById(R.id.editTextNumberSigned);


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tcpServer.set_cur_porog(Integer.parseInt(editText.getText().toString()));
            }
        });

        Button button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Intent intent = new Intent(Intent.ACTION_VIEW)
                //        .setData(Uri.parse("iqsrc://-a 127.0.0.1 -p " + port + " -s " + samplerate + " -f 200000000"));

                Intent intent2 = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("iqsrc://-x"));

                startActivityForResult(intent2, REQUEST_CODE);
            }
        });

        Button button3 = (Button)findViewById(R.id.button2);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent2 = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("iqsrc://-x"));
                startActivityForResult(intent2, REQUEST_CODE);
            }
        });


        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                // Ваш код, который будет выполняться каждую секунду
                //Log.d("MainActivity", "Task executed" + Arrays.toString(tcpServer.get_cur_byte_buffer()));
                textView.setText(Arrays.toString(tcpServer.get_cur_byte_buffer()));
                // Запланировать следующее выполнение через 1 секунду
                handler.postDelayed(this, 2000);
            }
        };

        // Запустить первое выполнение
        handler.post(runnable);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("01.09.2024", "onActivityResult запущен");

        super.onActivityResult(requestCode, resultCode, data);
        TextView textView = (TextView)findViewById(R.id.textView);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                textView.setText("Драйвер запущен успешно");
                Log.d("01.09.2024", "Драйвер запущен успешно");
                // Здесь можно добавить код для обработки данных
                tcpServer = new TcpServer();
                tcpServer.start1();
                Log.d("08.09.2024", "TCP сервер запущен успешно");
            } else {
                Log.e("01.09.2024", "Ошибка запуска драйвера, код: " + resultCode);
                textView.setText(String.format("Ошибка запуска драйвера, код: %d", resultCode));

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //tcpServer.stop();
    }

}