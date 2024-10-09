package com.example.polloengorde;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import com.example.polloengorde.event.UiToastEvent;
import com.example.polloengorde.helper.EnhancedSharedPreferences;
import com.example.polloengorde.helper.NotificationHelper;
import com.example.polloengorde.service.MyBluetoothSerialService;
import com.example.polloengorde.util.Config;
import com.example.polloengorde.util.Constants;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class PrincipalActivity extends AppCompatActivity {

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private static String address = null;
    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;
    private static final int REQUEST_ENABLE_BLUETOOTH = 3;
    private EnhancedSharedPreferences sharedPref;
    private Switch switchAutomatico, switchAlimentacion, switchAgua, switchVentilacion, switchFocos;
    private ImageView imageView;
    final static String A = "A";
    final static String B = "B";
    final static String C = "C";
    final static String D = "D";
    final static String E = "E";
    final static String F = "F";
    final static String G = "G";
    final static String H = "H";
    final static String I = "I";
    final static String J = "J";
    Button btnCerrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        btnCerrar = findViewById(R.id.btnCerrar);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        inicializarControles();

        verificarBluetooth();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 31) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
            }

            return;
        }
        Set<BluetoothDevice> pairedDevicesList = btAdapter.getBondedDevices();

        for (
                BluetoothDevice pairedDevice : pairedDevicesList) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= 31) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                }
                return;
            }
            if (pairedDevice.getName().equals("HC-06")) {
                // Toast.makeText(getBaseContext(), "HC-06 CONECTADO", Toast.LENGTH_SHORT).show();
                address = pairedDevice.getAddress();
            }
        }


        sharedPref = EnhancedSharedPreferences.getInstance(getApplicationContext(), getString(R.string.shared_preference_key));

        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
                preferences.edit().clear().commit();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void verificarBluetooth() {
        if (btAdapter.isEnabled()) {

        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= 31) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                }
                return;
            }
            startActivityForResult(intent, 1);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.createChannels();
        getNotification();

        // Verificar si se tiene el permiso necesario
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            // Permiso ya concedido
            if (!btAdapter.isEnabled()) {
                // Bluetooth deshabilitado, abrir la configuración de Bluetooth
                openBluetoothSettings();
            }
        } else {
            // Permiso no concedido, solicitarlo al usuario
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH_PERMISSION);
        }
    }

    private void getNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NotificationHelper.CHANNEL_SERVICE_ID)
                .setContentTitle(getString(R.string.text_bluetooth_service))
                .setContentText(getString(R.string.text_bluetooth_service_foreground_message))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_200))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManagerCompat.notify(0, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                if (!btAdapter.isEnabled()) {
                    // Bluetooth deshabilitado, abrir la configuración de Bluetooth
                    openBluetoothSettings();
                }
            } else {
                // Permiso denegado, mostrar un mensaje de error o realizar acciones alternativas
                Toast.makeText(this, "Permiso de Bluetooth denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openBluetoothSettings() {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }

    private void inicializarControles() {

        switchAutomatico = findViewById(R.id.btnAutomatico);
        switchAlimentacion = findViewById(R.id.btnAlimentacion);
        switchAgua = findViewById(R.id.btnAgua);
        switchVentilacion = findViewById(R.id.btnVentilacion);
        switchFocos = findViewById(R.id.btnFocos);
        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(PrincipalActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= 31) {
                        ActivityCompat.requestPermissions(PrincipalActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                    }
                }
                if (btAdapter == null) {
                    // El dispositivo no es compatible con Bluetooth
                    Toast.makeText(PrincipalActivity.this, "Bluetooth no compatible", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!btAdapter.isEnabled()) {
                    // El Bluetooth está desactivado, solicitar activación
                    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
                } else {
                    // El Bluetooth está activado, mostrar la lista de dispositivos
                    Intent serverIntent = new Intent(PrincipalActivity.this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                }
            }
        });


        switchAlimentacion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // El Switch está encendido
                    EventBus.getDefault().post(new UiToastEvent("ALIMENTO: ON", true, false));
                    Estado_ON_OFF(A);
                } else {
                    // El Switch está apagado
                    EventBus.getDefault().post(new UiToastEvent("ALIMENTO: OFF", true, false));
                    Estado_ON_OFF(B);
                }
            }
        });

        switchAgua.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // El Switch está encendido
                    EventBus.getDefault().post(new UiToastEvent("AGUA: ON", true, false));
                    Estado_ON_OFF(C);
                } else {
                    // El Switch está apagado
                    EventBus.getDefault().post(new UiToastEvent("AGUA: OFF", true, false));
                    Estado_ON_OFF(D);
                }
            }
        });

        switchVentilacion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // El Switch está encendido
                    EventBus.getDefault().post(new UiToastEvent("VENTILACION: ON", true, false));
                    Estado_ON_OFF(E);
                } else {
                    // El Switch está apagado
                    EventBus.getDefault().post(new UiToastEvent("VENTILACION: OFF", true, false));
                    Estado_ON_OFF(F);
                }
            }
        });

        switchFocos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // El Switch está encendido
                    EventBus.getDefault().post(new UiToastEvent("FOCOS: ON", true, false));
                    Estado_ON_OFF(G);
                } else {
                    // El Switch está apagado
                    EventBus.getDefault().post(new UiToastEvent("FOCOS: OFF", true, false));
                    Estado_ON_OFF(H);
                }
            }
        });

        switchAutomatico.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // El Switch está encendido
                    EventBus.getDefault().post(new UiToastEvent("AUTOMATICO: ON", true, false));
                    Estado_ON_OFF(I);
                } else {
                    // El Switch está apagado
                    EventBus.getDefault().post(new UiToastEvent("AUTOMATICO: OFF", true, false));
                    Estado_ON_OFF(J);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ActivityCompat.checkSelfPermission(PrincipalActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 31) {
                ActivityCompat.requestPermissions(PrincipalActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
            }
        }

        if (requestCode == REQUEST_CONNECT_DEVICE && resultCode == RESULT_OK) {
            String deviceAddress = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

            if (deviceAddress != null && !deviceAddress.isEmpty()) {
                if (deviceAddress.equals(address)) {
                    BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

                    try {
                        btSocket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
                        btSocket.connect();
                        Toast.makeText(this, "Bluetooth conectado", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, "Bluetooth desconectado", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "No se puede establecer la conexión con el dispositivo", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void Estado_ON_OFF(String i){
        try {
            if(btSocket != null){
                btSocket.getOutputStream().write(i.getBytes());
                SystemClock.sleep(100);
            }
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUiToastEvent(UiToastEvent event) {
        Config.Mensaje(PrincipalActivity.this, event.getMessage(), event.getLongToast(), event.getIsWarning());
    };
}