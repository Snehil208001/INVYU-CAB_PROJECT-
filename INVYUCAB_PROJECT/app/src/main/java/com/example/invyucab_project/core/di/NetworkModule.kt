package com.example.invyucab_project.core.di

import com.example.invyucab_project.BuildConfig
import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // --- Base URLs ---
    private const val GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/"
    private const val CUSTOM_API_BASE_URL = "https://ovlo8ek40d.execute-api.us-east-1.amazonaws.com/"

    // --- Moshi (Shared) ---
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // --- Logger (Shared) ---
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // --- Google Maps API Components ---

    @Provides
    @Singleton
    @Named("GoogleMapsApiKeyInterceptor")
    fun provideApiKeyInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url
            val urlWithKey = originalUrl.newBuilder()
                .addQueryParameter("key", BuildConfig.MAPS_API_KEY)
                .build()
            val requestBuilder = originalRequest.newBuilder().url(urlWithKey)
            val request = requestBuilder.build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    @Named("GoogleMapsOkHttp")
    fun provideGoogleMapsOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("GoogleMapsApiKeyInterceptor") apiKeyInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("GoogleMapsRetrofit")
    fun provideGoogleMapsRetrofit(
        @Named("GoogleMapsOkHttp") okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GOOGLE_MAPS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideGoogleMapsApiService(@Named("GoogleMapsRetrofit") retrofit: Retrofit): GoogleMapsApiService {
        return retrofit.create(GoogleMapsApiService::class.java)
    }

    // --- ✅ NEW: Custom API Components ---

    @Provides
    @Singleton
    @Named("CustomApiOkHttp")
    fun provideCustomApiOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("CustomApiRetrofit")
    fun provideCustomApiRetrofit(
        @Named("CustomApiOkHttp") okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(CUSTOM_API_BASE_URL)
            .client(okHttpClient)
            // ✅✅✅ START OF FIX ✅✅✅
            // The correct method is .withNullSerialization()
            .addConverterFactory(MoshiConverterFactory.create(moshi).withNullSerialization())
            // ✅✅✅ END OF FIX ✅✅✅
            .build()
    }

    @Provides
    @Singleton
    fun provideCustomApiService(@Named("CustomApiRetrofit") retrofit: Retrofit): CustomApiService {
        return retrofit.create(CustomApiService::class.java)
    }
}