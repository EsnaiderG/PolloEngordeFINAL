package com.example.polloengorde.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.polloengorde.R;

public class Config {

    private static Toast toastMessage;

    @SuppressLint("ShowToast")
    public static void Mensaje(Context context, String message, boolean longToast , boolean isWarning) {
        if (toastMessage != null) {
            toastMessage.cancel();
        }

        toastMessage = new Toast(context.getApplicationContext());
        toastMessage.setGravity(Gravity.TOP, 0, 140);

        LayoutInflater inflater = LayoutInflater.from(context);
        View toastView = inflater.inflate(R.layout.custom_toast_layout, null);
        TextView toastMessageText = toastView.findViewById(R.id.toast_message);
        toastMessageText.setTextColor(Color.parseColor("#ffffff"));
        toastMessageText.setText(message);

        if (isWarning) {
            toastView.setBackgroundColor(Color.parseColor("#d50000"));
        } else {
            toastView.setBackgroundColor(Color.parseColor("#646464"));
        }

        toastMessage.setDuration(longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toastMessage.setView(toastView);
        toastMessage.show();
    }
}

