package com.dhis2.cloud;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.prd.elindependiente.R;
import com.prd.elindependiente.presentation.usecases.ActivityGlobalAbstract;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static String currentBaseUrl = "";

    private RetrofitClient() {
        // unused
    }

    public static void resetClient() {
        retrofit = null;
    }

    static Retrofit getClient(Context context, String baseURL) {
        if (!currentBaseUrl.equals(baseURL))
            resetClient();
        currentBaseUrl = baseURL;
        if (retrofit == null) {
            HttpLoggingInterceptor interceptorBasic = new HttpLoggingInterceptor();
            interceptorBasic.setLevel(HttpLoggingInterceptor.Level.BASIC);

            HttpLoggingInterceptor interceptorHeaders = new HttpLoggingInterceptor();
            interceptorHeaders.setLevel(HttpLoggingInterceptor.Level.HEADERS);

            HttpLoggingInterceptor interceptorBody = new HttpLoggingInterceptor();
            interceptorBody.setLevel(HttpLoggingInterceptor.Level.BODY);


            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptorBasic)
                    .addInterceptor(interceptorHeaders)
                    .addInterceptor(interceptorBody)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .client(client)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}