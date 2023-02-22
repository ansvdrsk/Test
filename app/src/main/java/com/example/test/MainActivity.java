package com.example.test;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

        private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE;
        private static final String FILE_NAME = "my_file.txt";
    static {
        REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;
    }

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

        Button usbButton = findViewById(R.id.usbButton);
            usbButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showUsbDialog();
                }
            });
        }

        private void showUsbDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Подключите флэш-накопитель");

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if ((isUsbConnected())) {
                        if (hasWriteExternalStoragePermission()) {
                            showSaveFileDialog();
                        } else {
                            requestWriteExternalStoragePermission();
                        }
                    } else {
                        showUsbDialog();
                    }
                }
            });

            builder.show();
        }

        private boolean isUsbConnected() {
            File[] roots = getExternalCacheDirs();
            if (roots != null && roots.length > 1) {
                return roots[1] != null;
            } else {
                return false;
            }
        }

        private void showSaveFileDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Сохранить файл");

            final EditText fileNameEditText = new EditText(this);
            fileNameEditText.setHint("Введите имя файла");
            builder.setView(fileNameEditText);

            builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String fileName = fileNameEditText.getText().toString();
                    String fileContents = "Произвольный текст";

                    File root = getExternalCacheDirs()[0];
                    File file = new File(root,FILE_NAME);

                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fileContents.getBytes());
                        fos.close();

                        showSuccessDialog();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            builder.setNegativeButton("Отмена", null);

            builder.show();
        }

        private void showSuccessDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Действие выполнено");
            builder.setMessage("Файл успешно сохранен на флэш-накопитель");

            builder.setPositiveButton("Ok", null);

            builder.show();
            // Отмонтируем накопитель
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            try {
                Runtime.getRuntime().exec("umount " + path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private boolean hasWriteExternalStoragePermission() {
            return ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        private void requestWriteExternalStoragePermission() {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }

        @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSaveFileDialog();
                } else {
                    Toast.makeText(this, "Разрешение на запись на внешний накопитель отклонено", Toast.LENGTH_SHORT).show();
                }
            }
            }

        }


