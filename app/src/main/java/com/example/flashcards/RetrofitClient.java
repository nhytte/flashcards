package com.example.flashcards;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // !!! REPLACE WITH YOUR SUPABASE PROJECT URL !!!
    private static final String BASE_URL = "https://wfhdnbgfuqchmunpmhez.supabase.co/"; // Make sure it ends with '/'

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}