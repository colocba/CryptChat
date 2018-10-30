package com.example.amirbaum.cryptchat.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by amirbaum on 30/10/2018.
 */

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAHQvsIPc:APA91bF9XHIx6iUGZBn4uxrohqP5bn_HWzOcdfiezwkUzIyWqWE61dluSL_Ba13JE0Ow-2dSYNR6aT4Z6h4FwYxI9CezjgqJ1ojGxfOF-_sK49jW4PebEDYRj3Sru2_Z47sGsHSypjOt"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
