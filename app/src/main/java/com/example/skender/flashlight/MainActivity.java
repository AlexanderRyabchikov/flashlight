package com.example.skender.flashlight;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean state;
    private boolean hasFlash;
    private Camera camera;
    private Parameters parameters;
    private ImageButton imageButton;
    private SensorManager sensorManager;
    private Sensor sensorLight;
    private SensorEventListener listenerLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (sensorLight == null) {
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Ошибка");
            alert.setMessage("Ваше устройство не поддерживает работу с датчиком света!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //Закрываем приложение:
                    finish();
                }
            });
            alert.show();
            return;
        }
        listenerLight = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (isState()) {
                    turnOffFlash();
                } else if (!isState()) {

                    turnOnFlash();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            //Устройство не поддерживает функцию вспышки
            //Показываем печальное сообщение и закрываем приложение:
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Ошибка");
            alert.setMessage("Ваше устройство не поддерживает работу с вспышкой!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //Закрываем приложение:
                    finish();
                }
            });
            alert.show();
            return;
        }
        else {
            System.out.println("hello");
            getCamera();
            setState(false);
            flash();
        }
    }
    public void flash(){
        imageButton = (ImageButton)findViewById(R.id.imageButton2);
        imageButton.setOnClickListener(this);
    }


    public void turnOffFlash(){
        if ((camera == null) || (parameters == null)){
            return;
        }

        parameters = camera.getParameters();
        parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
        camera.stopPreview();
        setState(false);
    }
    public void turnOnFlash(){
        if ((camera == null) || (parameters == null)){

            return;
        }
        parameters = camera.getParameters();
        parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
        camera.startPreview();
        setState(true);
    }
    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
    public Camera getCamera() {
        if(camera == null){
            try{
                camera = Camera.open();
                parameters = camera.getParameters();
            }catch (RuntimeException e){
                Log.e("Ошибка, невозможно запустить:" , e.getMessage());
            }
        }
        return camera;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listenerLight, sensorLight);
        //Временно выключаем фонарик:
        turnOffFlash();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listenerLight, sensorLight, SensorManager.SENSOR_DELAY_NORMAL);
        //Продолжаем работу фонарика:
        if(hasFlash)
            turnOnFlash();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(listenerLight, sensorLight, SensorManager.SENSOR_DELAY_NORMAL);
        //Получаем для приложения параметры камеры:
        getCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Закрываем работу камеры:
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageButton2:
                if (isState()) {
                    turnOffFlash();
                } else if (!isState()) {

                    turnOnFlash();
                }
                break;
        }
    }
}
