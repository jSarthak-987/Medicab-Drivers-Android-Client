package com.brightsky.medicabdriver;

import com.brightsky.medicabdriver.firebasemessaging.DriverBookingMessagingRequestModel;
import com.brightsky.medicabdriver.firebasemessaging.DriverBookingMessagingResponseModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitCallbacks {

    @POST("/notification/send/client")
    Call<DriverBookingMessagingResponseModel> sendClientConfirmation(@Body DriverBookingMessagingRequestModel demoRequest);

}