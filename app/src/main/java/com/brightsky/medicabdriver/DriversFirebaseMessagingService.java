package com.brightsky.medicabdriver;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class DriversFirebaseMessagingService extends FirebaseMessagingService {

    public static final String INTENT_FILTER = "SARTHAKISTHEMVP";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> payloadData = remoteMessage.getData();

            String clientKey = payloadData.get("clientKey");
            String transactionId = payloadData.get("transactionId");
            String clientToken = payloadData.get("clientToken");

            Intent intent = new Intent(INTENT_FILTER);

            intent.putExtra("ClientKey", clientKey);
            intent.putExtra("TransactionId", transactionId);
            intent.putExtra("ClientToken", clientToken);

            Log.e("FirebaseMessaging", "clientKey: " + clientKey + "   txId: " + transactionId + "   clientToken: " + clientToken);

            sendBroadcast(intent);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}