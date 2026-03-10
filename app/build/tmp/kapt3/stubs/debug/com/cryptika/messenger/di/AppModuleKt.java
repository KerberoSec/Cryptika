package com.cryptika.messenger.di;

import android.content.Context;
import com.cryptika.messenger.BuildConfig;
import com.cryptika.messenger.data.local.db.*;
import com.cryptika.messenger.data.local.keystore.KeystoreManager;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.api.RelayApi;
import com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient;
import com.cryptika.messenger.data.repository.*;
import com.cryptika.messenger.domain.crypto.*;
import com.cryptika.messenger.domain.repository.*;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\f\n\u0000\n\u0002\u0010\u0012\n\u0002\u0010\u000e\n\u0000\u001a\f\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u0002\u00a8\u0006\u0003"}, d2 = {"hexToByteArray", "", "", "Cryptika_debug"})
public final class AppModuleKt {
    
    private static final byte[] hexToByteArray(java.lang.String $this$hexToByteArray) {
        return null;
    }
}