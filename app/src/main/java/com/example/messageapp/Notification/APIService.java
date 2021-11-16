package com.example.messageapp.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAZ6-EFu8:APA91bGw1V2kTHbYbX2VY0ZxWzui8lf9a1A-eU7r_ccvkySRQ6eeG88V-rKv1iuxH5oGakf1r9DdaleR-2AFMzdcm5OqMbacagmkKXw_BWAoSduvwcbOPuLqOjfxfFVQNW7ZOBCi2Q2d"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}