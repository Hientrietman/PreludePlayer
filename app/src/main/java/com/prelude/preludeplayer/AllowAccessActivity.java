package com.prelude.preludeplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

public class AllowAccessActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_PERMISSION_SETTINGS = 12;
    Button allow_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allow_access);

        SharedPreferences sharedPreferences = getSharedPreferences("Allow", MODE_PRIVATE);
        String value = sharedPreferences.getString("Allow", "");
        if (value == "OK") {
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            finish();
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Allow", "OK");
            editor.apply();
        }

        allow_btn = findViewById(R.id.btn_allow);
        allow_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //sử dụng ContextCompat để check quyền của activity hiện tại nếu quyền đã cấp thì chuyển qua main
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    finish();
                } else {
                    //nếu chưa cấp thì yêu cầu quyền
                    ActivityCompat.requestPermissions(AllowAccessActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            for (int i = 0; i < permissions.length; i++) {
                String per = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(per);
                    if (!showRationale) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Permission Required!")
                                .setMessage("This app need to access storage for proper usage"
                                        + "\n\n" + "Open Setting and give storage permission")
                                .setPositiveButton("Open settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);

                                        startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
                                    }
                                }).create().show();
                    } else {
                        ActivityCompat.requestPermissions(AllowAccessActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                } else {
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    finish();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            finish();
        }
    }
}