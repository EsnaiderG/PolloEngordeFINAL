package com.example.polloengorde;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DeviceListActivity extends Activity {
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    private static final int REQUEST_PERMISSION_LOCATION = 1;

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mDevicesArrayAdapter;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_dispositivos);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Verificar si se tiene el permiso BLUETOOTH_CONNECT
        if (!checkBluetoothConnectPermission()) {
            requestBluetoothConnectPermission();
            return;
        }

        ListView pairedListView = findViewById(R.id.paired_devices);
        mDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        pairedListView.setAdapter(mDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            mDevicesArrayAdapter.add("No paired devices found");
        }
    }

    private boolean checkBluetoothConnectPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothConnectPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                REQUEST_BLUETOOTH_CONNECT_PERMISSION);
    }


    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                recreate(); // Reiniciar la actividad para obtener los dispositivos Bluetooth emparejados
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private final AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (checkLocationPermission()) {
                mBtAdapter.cancelDiscovery();

                String info = mDevicesArrayAdapter.getItem(position);
                if (info != null && info.length() > 17) {
                    String address = info.substring(info.length() - 17);

                    if (address.equals("00:00:00:00:00:00")) {
                        // Dirección Bluetooth no válida (cambiar por la dirección correcta)
                        Toast.makeText(DeviceListActivity.this, "Error de emparejamiento", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
            } else {
                requestLocationPermission();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (checkLocationPermission()) {
            if (mBtAdapter != null) {
                try {
                    mBtAdapter.cancelDiscovery();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
