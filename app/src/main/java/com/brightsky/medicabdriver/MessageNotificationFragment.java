package com.brightsky.medicabdriver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brightsky.medicabdriver.firebasemessaging.DriverBookingMessagingRequestModel;
import com.brightsky.medicabdriver.firebasemessaging.DriverBookingMessagingResponseModel;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MessageNotificationFragment extends Fragment {

    public static final String TAG = "MessageNotificationFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_message_notification, container, false);

        String transactionId = getArguments().getString("transactionId");
        String driverKey = getArguments().getString("driverKey");
        String clientFCMToken = getArguments().getString("clientFCMToken");

        Button acceptRequest = v.findViewById(R.id.accept_request);
        Button rejectRequest = v.findViewById(R.id.reject_request);

        acceptRequest.setOnClickListener(v1 -> {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://us-central1-acquired-jet-298514.cloudfunctions.net")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RetrofitCallbacks callbacks = retrofit.create(RetrofitCallbacks.class);

            DriverBookingMessagingRequestModel clientConfirmation = new DriverBookingMessagingRequestModel();
            clientConfirmation.setDriverKey(driverKey);
            clientConfirmation.setToken(clientFCMToken);
            clientConfirmation.setTxId(transactionId);

            Log.e("NotificationFragment", "driverkey: " + driverKey + "   txId: " + transactionId + "   clientToken: " + clientFCMToken);

            Call<DriverBookingMessagingResponseModel> sendClientConfirmation = callbacks.sendClientConfirmation(clientConfirmation);
            sendClientConfirmation.enqueue(new Callback<DriverBookingMessagingResponseModel>() {
                @Override
                public void onResponse(@NotNull Call<DriverBookingMessagingResponseModel> call, @NotNull Response<DriverBookingMessagingResponseModel> response) {
                    getFragmentManager().beginTransaction()
                            .remove(MessageNotificationFragment.this)
                            .commit();

                    if(response.body() != null) {
                        Toast.makeText(getContext(), "Confirmation Sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("MessageNotification", "Request: " + call.request().body());
                        Log.e("MessageNotification", "Response: " + response);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<DriverBookingMessagingResponseModel> call, @NotNull Throwable t) {
                    t.printStackTrace();
                }
            });
        });

        rejectRequest.setOnClickListener(v12 -> {
            getFragmentManager().beginTransaction()
                    .remove(MessageNotificationFragment.this)
                    .commit();
        });

        return v;
    }
}