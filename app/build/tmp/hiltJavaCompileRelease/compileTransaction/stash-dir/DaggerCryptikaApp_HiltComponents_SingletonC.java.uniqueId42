package com.cryptika.messenger;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.cryptika.messenger.data.local.AuthStore;
import com.cryptika.messenger.data.local.db.AppDatabase;
import com.cryptika.messenger.data.local.db.ContactDao;
import com.cryptika.messenger.data.local.db.ConversationDao;
import com.cryptika.messenger.data.local.db.LocalIdentityDao;
import com.cryptika.messenger.data.local.db.MessageDao;
import com.cryptika.messenger.data.local.keystore.KeystoreManager;
import com.cryptika.messenger.data.remote.BackgroundConnectionManager;
import com.cryptika.messenger.data.remote.CallManager;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.api.AuthApi;
import com.cryptika.messenger.data.remote.api.RelayApi;
import com.cryptika.messenger.di.AppModule_ProvideAuthApiFactory;
import com.cryptika.messenger.di.AppModule_ProvideAuthRepositoryFactory;
import com.cryptika.messenger.di.AppModule_ProvideContactDaoFactory;
import com.cryptika.messenger.di.AppModule_ProvideContactRepositoryFactory;
import com.cryptika.messenger.di.AppModule_ProvideConversationDaoFactory;
import com.cryptika.messenger.di.AppModule_ProvideDatabaseFactory;
import com.cryptika.messenger.di.AppModule_ProvideIdentityKeyManagerFactory;
import com.cryptika.messenger.di.AppModule_ProvideIdentityRepositoryFactory;
import com.cryptika.messenger.di.AppModule_ProvideKeystoreManagerFactory;
import com.cryptika.messenger.di.AppModule_ProvideLocalIdentityDaoFactory;
import com.cryptika.messenger.di.AppModule_ProvideMessageDaoFactory;
import com.cryptika.messenger.di.AppModule_ProvideMessageRepositoryFactory;
import com.cryptika.messenger.di.AppModule_ProvideOkHttpClientFactory;
import com.cryptika.messenger.di.AppModule_ProvideRelayApiFactory;
import com.cryptika.messenger.di.AppModule_ProvideSessionKeyManagerFactory;
import com.cryptika.messenger.di.AppModule_ProvideTicketManagerFactory;
import com.cryptika.messenger.domain.crypto.HandshakeManager;
import com.cryptika.messenger.domain.crypto.IdentityKeyManager;
import com.cryptika.messenger.domain.crypto.SessionKeyManager;
import com.cryptika.messenger.domain.crypto.TicketManager;
import com.cryptika.messenger.domain.repository.AuthRepository;
import com.cryptika.messenger.domain.repository.ContactRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import com.cryptika.messenger.domain.repository.MessageRepository;
import com.cryptika.messenger.presentation.viewmodel.AuthViewModel;
import com.cryptika.messenger.presentation.viewmodel.AuthViewModel_HiltModules;
import com.cryptika.messenger.presentation.viewmodel.CallViewModel;
import com.cryptika.messenger.presentation.viewmodel.CallViewModel_HiltModules;
import com.cryptika.messenger.presentation.viewmodel.ChatViewModel;
import com.cryptika.messenger.presentation.viewmodel.ChatViewModel_HiltModules;
import com.cryptika.messenger.presentation.viewmodel.ContactConfirmViewModel;
import com.cryptika.messenger.presentation.viewmodel.ContactConfirmViewModel_HiltModules;
import com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryViewModel;
import com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryViewModel_HiltModules;
import com.cryptika.messenger.presentation.viewmodel.HomeViewModel;
import com.cryptika.messenger.presentation.viewmodel.HomeViewModel_HiltModules;
import com.cryptika.messenger.presentation.viewmodel.QrDisplayViewModel;
import com.cryptika.messenger.presentation.viewmodel.QrDisplayViewModel_HiltModules;
import com.cryptika.messenger.presentation.viewmodel.QrScanViewModel;
import com.cryptika.messenger.presentation.viewmodel.QrScanViewModel_HiltModules;
import com.cryptika.messenger.presentation.viewmodel.SettingsViewModel;
import com.cryptika.messenger.presentation.viewmodel.SettingsViewModel_HiltModules;
import com.cryptika.messenger.presentation.viewmodel.SplashViewModel;
import com.cryptika.messenger.presentation.viewmodel.SplashViewModel_HiltModules;
import com.cryptika.messenger.worker.MessageExpiryWorker;
import com.cryptika.messenger.worker.MessageExpiryWorker_AssistedFactory;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DelegateFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DaggerCryptikaApp_HiltComponents_SingletonC {
  private DaggerCryptikaApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public CryptikaApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements CryptikaApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public CryptikaApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements CryptikaApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public CryptikaApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements CryptikaApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public CryptikaApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements CryptikaApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CryptikaApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements CryptikaApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CryptikaApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements CryptikaApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public CryptikaApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements CryptikaApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public CryptikaApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends CryptikaApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends CryptikaApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends CryptikaApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends CryptikaApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(10).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_AuthViewModel, AuthViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_CallViewModel, CallViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_ChatViewModel, ChatViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_ContactConfirmViewModel, ContactConfirmViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_ContactDiscoveryViewModel, ContactDiscoveryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_QrDisplayViewModel, QrDisplayViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_QrScanViewModel, QrScanViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_SplashViewModel, SplashViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectEphemeralSessionManager(instance, singletonCImpl.ephemeralSessionManagerProvider.get());
      MainActivity_MembersInjector.injectBackgroundConnectionManager(instance, singletonCImpl.backgroundConnectionManagerProvider.get());
      MainActivity_MembersInjector.injectAuthRepository(instance, singletonCImpl.provideAuthRepositoryProvider.get());
      return instance;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_cryptika_messenger_presentation_viewmodel_AuthViewModel = "com.cryptika.messenger.presentation.viewmodel.AuthViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_ContactConfirmViewModel = "com.cryptika.messenger.presentation.viewmodel.ContactConfirmViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_ContactDiscoveryViewModel = "com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_CallViewModel = "com.cryptika.messenger.presentation.viewmodel.CallViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_QrDisplayViewModel = "com.cryptika.messenger.presentation.viewmodel.QrDisplayViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_SettingsViewModel = "com.cryptika.messenger.presentation.viewmodel.SettingsViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_QrScanViewModel = "com.cryptika.messenger.presentation.viewmodel.QrScanViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_HomeViewModel = "com.cryptika.messenger.presentation.viewmodel.HomeViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_ChatViewModel = "com.cryptika.messenger.presentation.viewmodel.ChatViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_SplashViewModel = "com.cryptika.messenger.presentation.viewmodel.SplashViewModel";

      @KeepFieldType
      AuthViewModel com_cryptika_messenger_presentation_viewmodel_AuthViewModel2;

      @KeepFieldType
      ContactConfirmViewModel com_cryptika_messenger_presentation_viewmodel_ContactConfirmViewModel2;

      @KeepFieldType
      ContactDiscoveryViewModel com_cryptika_messenger_presentation_viewmodel_ContactDiscoveryViewModel2;

      @KeepFieldType
      CallViewModel com_cryptika_messenger_presentation_viewmodel_CallViewModel2;

      @KeepFieldType
      QrDisplayViewModel com_cryptika_messenger_presentation_viewmodel_QrDisplayViewModel2;

      @KeepFieldType
      SettingsViewModel com_cryptika_messenger_presentation_viewmodel_SettingsViewModel2;

      @KeepFieldType
      QrScanViewModel com_cryptika_messenger_presentation_viewmodel_QrScanViewModel2;

      @KeepFieldType
      HomeViewModel com_cryptika_messenger_presentation_viewmodel_HomeViewModel2;

      @KeepFieldType
      ChatViewModel com_cryptika_messenger_presentation_viewmodel_ChatViewModel2;

      @KeepFieldType
      SplashViewModel com_cryptika_messenger_presentation_viewmodel_SplashViewModel2;
    }
  }

  private static final class ViewModelCImpl extends CryptikaApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<CallViewModel> callViewModelProvider;

    private Provider<ChatViewModel> chatViewModelProvider;

    private Provider<ContactConfirmViewModel> contactConfirmViewModelProvider;

    private Provider<ContactDiscoveryViewModel> contactDiscoveryViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<QrDisplayViewModel> qrDisplayViewModelProvider;

    private Provider<QrScanViewModel> qrScanViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<SplashViewModel> splashViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.callViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.chatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.contactConfirmViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.contactDiscoveryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.qrDisplayViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.qrScanViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.splashViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(10).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_AuthViewModel, ((Provider) authViewModelProvider)).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_CallViewModel, ((Provider) callViewModelProvider)).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_ChatViewModel, ((Provider) chatViewModelProvider)).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_ContactConfirmViewModel, ((Provider) contactConfirmViewModelProvider)).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_ContactDiscoveryViewModel, ((Provider) contactDiscoveryViewModelProvider)).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_QrDisplayViewModel, ((Provider) qrDisplayViewModelProvider)).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_QrScanViewModel, ((Provider) qrScanViewModelProvider)).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_cryptika_messenger_presentation_viewmodel_SplashViewModel, ((Provider) splashViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_cryptika_messenger_presentation_viewmodel_ContactDiscoveryViewModel = "com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_SplashViewModel = "com.cryptika.messenger.presentation.viewmodel.SplashViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_ChatViewModel = "com.cryptika.messenger.presentation.viewmodel.ChatViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_ContactConfirmViewModel = "com.cryptika.messenger.presentation.viewmodel.ContactConfirmViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_HomeViewModel = "com.cryptika.messenger.presentation.viewmodel.HomeViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_SettingsViewModel = "com.cryptika.messenger.presentation.viewmodel.SettingsViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_QrDisplayViewModel = "com.cryptika.messenger.presentation.viewmodel.QrDisplayViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_CallViewModel = "com.cryptika.messenger.presentation.viewmodel.CallViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_AuthViewModel = "com.cryptika.messenger.presentation.viewmodel.AuthViewModel";

      static String com_cryptika_messenger_presentation_viewmodel_QrScanViewModel = "com.cryptika.messenger.presentation.viewmodel.QrScanViewModel";

      @KeepFieldType
      ContactDiscoveryViewModel com_cryptika_messenger_presentation_viewmodel_ContactDiscoveryViewModel2;

      @KeepFieldType
      SplashViewModel com_cryptika_messenger_presentation_viewmodel_SplashViewModel2;

      @KeepFieldType
      ChatViewModel com_cryptika_messenger_presentation_viewmodel_ChatViewModel2;

      @KeepFieldType
      ContactConfirmViewModel com_cryptika_messenger_presentation_viewmodel_ContactConfirmViewModel2;

      @KeepFieldType
      HomeViewModel com_cryptika_messenger_presentation_viewmodel_HomeViewModel2;

      @KeepFieldType
      SettingsViewModel com_cryptika_messenger_presentation_viewmodel_SettingsViewModel2;

      @KeepFieldType
      QrDisplayViewModel com_cryptika_messenger_presentation_viewmodel_QrDisplayViewModel2;

      @KeepFieldType
      CallViewModel com_cryptika_messenger_presentation_viewmodel_CallViewModel2;

      @KeepFieldType
      AuthViewModel com_cryptika_messenger_presentation_viewmodel_AuthViewModel2;

      @KeepFieldType
      QrScanViewModel com_cryptika_messenger_presentation_viewmodel_QrScanViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.cryptika.messenger.presentation.viewmodel.AuthViewModel 
          return (T) new AuthViewModel(singletonCImpl.provideAuthRepositoryProvider.get());

          case 1: // com.cryptika.messenger.presentation.viewmodel.CallViewModel 
          return (T) new CallViewModel(singletonCImpl.callManagerProvider.get(), singletonCImpl.provideContactRepositoryProvider.get(), singletonCImpl.provideIdentityRepositoryProvider.get(), singletonCImpl.backgroundConnectionManagerProvider.get());

          case 2: // com.cryptika.messenger.presentation.viewmodel.ChatViewModel 
          return (T) new ChatViewModel(singletonCImpl.provideMessageRepositoryProvider.get(), singletonCImpl.provideContactRepositoryProvider.get(), singletonCImpl.provideIdentityRepositoryProvider.get(), singletonCImpl.provideIdentityKeyManagerProvider.get(), singletonCImpl.provideRelayApiProvider.get(), singletonCImpl.serverConfigProvider.get(), singletonCImpl.backgroundConnectionManagerProvider.get(), singletonCImpl.ephemeralSessionManagerProvider.get(), singletonCImpl.conversationDao(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.cryptika.messenger.presentation.viewmodel.ContactConfirmViewModel 
          return (T) new ContactConfirmViewModel(singletonCImpl.provideContactRepositoryProvider.get());

          case 4: // com.cryptika.messenger.presentation.viewmodel.ContactDiscoveryViewModel 
          return (T) new ContactDiscoveryViewModel(singletonCImpl.provideAuthRepositoryProvider.get(), singletonCImpl.ephemeralSessionManagerProvider.get());

          case 5: // com.cryptika.messenger.presentation.viewmodel.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.provideContactRepositoryProvider.get(), singletonCImpl.provideIdentityRepositoryProvider.get(), singletonCImpl.provideMessageRepositoryProvider.get(), singletonCImpl.conversationDao());

          case 6: // com.cryptika.messenger.presentation.viewmodel.QrDisplayViewModel 
          return (T) new QrDisplayViewModel(singletonCImpl.provideIdentityRepositoryProvider.get());

          case 7: // com.cryptika.messenger.presentation.viewmodel.QrScanViewModel 
          return (T) new QrScanViewModel();

          case 8: // com.cryptika.messenger.presentation.viewmodel.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideIdentityRepositoryProvider.get(), singletonCImpl.ephemeralSessionManagerProvider.get(), singletonCImpl.provideAuthRepositoryProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.serverConfigProvider.get());

          case 9: // com.cryptika.messenger.presentation.viewmodel.SplashViewModel 
          return (T) new SplashViewModel(singletonCImpl.provideIdentityRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends CryptikaApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends CryptikaApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends CryptikaApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<KeystoreManager> provideKeystoreManagerProvider;

    private Provider<AppDatabase> provideDatabaseProvider;

    private Provider<MessageRepository> provideMessageRepositoryProvider;

    private Provider<MessageExpiryWorker_AssistedFactory> messageExpiryWorker_AssistedFactoryProvider;

    private Provider<ContactRepository> provideContactRepositoryProvider;

    private Provider<IdentityKeyManager> provideIdentityKeyManagerProvider;

    private Provider<IdentityRepository> provideIdentityRepositoryProvider;

    private Provider<SessionKeyManager> provideSessionKeyManagerProvider;

    private Provider<HandshakeManager> handshakeManagerProvider;

    private Provider<TicketManager> provideTicketManagerProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<RelayApi> provideRelayApiProvider;

    private Provider<AuthStore> authStoreProvider;

    private Provider<ServerConfig> serverConfigProvider;

    private Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider;

    private Provider<CallManager> callManagerProvider;

    private Provider<AuthApi> provideAuthApiProvider;

    private Provider<AuthRepository> provideAuthRepositoryProvider;

    private Provider<EphemeralSessionManager> ephemeralSessionManagerProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private MessageDao messageDao() {
      return AppModule_ProvideMessageDaoFactory.provideMessageDao(provideDatabaseProvider.get());
    }

    private Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return Collections.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>singletonMap("com.cryptika.messenger.worker.MessageExpiryWorker", ((Provider) messageExpiryWorker_AssistedFactoryProvider));
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    private ContactDao contactDao() {
      return AppModule_ProvideContactDaoFactory.provideContactDao(provideDatabaseProvider.get());
    }

    private LocalIdentityDao localIdentityDao() {
      return AppModule_ProvideLocalIdentityDaoFactory.provideLocalIdentityDao(provideDatabaseProvider.get());
    }

    private ConversationDao conversationDao() {
      return AppModule_ProvideConversationDaoFactory.provideConversationDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideKeystoreManagerProvider = DoubleCheck.provider(new SwitchingProvider<KeystoreManager>(singletonCImpl, 3));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 2));
      this.provideMessageRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MessageRepository>(singletonCImpl, 1));
      this.messageExpiryWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<MessageExpiryWorker_AssistedFactory>(singletonCImpl, 0));
      this.provideContactRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ContactRepository>(singletonCImpl, 5));
      this.provideIdentityKeyManagerProvider = DoubleCheck.provider(new SwitchingProvider<IdentityKeyManager>(singletonCImpl, 7));
      this.provideIdentityRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<IdentityRepository>(singletonCImpl, 6));
      this.provideSessionKeyManagerProvider = DoubleCheck.provider(new SwitchingProvider<SessionKeyManager>(singletonCImpl, 9));
      this.handshakeManagerProvider = DoubleCheck.provider(new SwitchingProvider<HandshakeManager>(singletonCImpl, 8));
      this.provideTicketManagerProvider = DoubleCheck.provider(new SwitchingProvider<TicketManager>(singletonCImpl, 10));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 12));
      this.provideRelayApiProvider = DoubleCheck.provider(new SwitchingProvider<RelayApi>(singletonCImpl, 11));
      this.authStoreProvider = DoubleCheck.provider(new SwitchingProvider<AuthStore>(singletonCImpl, 13));
      this.serverConfigProvider = DoubleCheck.provider(new SwitchingProvider<ServerConfig>(singletonCImpl, 14));
      this.backgroundConnectionManagerProvider = new DelegateFactory<>();
      this.callManagerProvider = DoubleCheck.provider(new SwitchingProvider<CallManager>(singletonCImpl, 15));
      DelegateFactory.setDelegate(backgroundConnectionManagerProvider, DoubleCheck.provider(new SwitchingProvider<BackgroundConnectionManager>(singletonCImpl, 4)));
      this.provideAuthApiProvider = DoubleCheck.provider(new SwitchingProvider<AuthApi>(singletonCImpl, 18));
      this.provideAuthRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 17));
      this.ephemeralSessionManagerProvider = DoubleCheck.provider(new SwitchingProvider<EphemeralSessionManager>(singletonCImpl, 16));
    }

    @Override
    public void injectCryptikaApp(CryptikaApp cryptikaApp) {
      injectCryptikaApp2(cryptikaApp);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private CryptikaApp injectCryptikaApp2(CryptikaApp instance) {
      CryptikaApp_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      CryptikaApp_MembersInjector.injectBackgroundConnectionManager(instance, backgroundConnectionManagerProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.cryptika.messenger.worker.MessageExpiryWorker_AssistedFactory 
          return (T) new MessageExpiryWorker_AssistedFactory() {
            @Override
            public MessageExpiryWorker create(Context context, WorkerParameters params) {
              return new MessageExpiryWorker(context, params, singletonCImpl.provideMessageRepositoryProvider.get());
            }
          };

          case 1: // com.cryptika.messenger.domain.repository.MessageRepository 
          return (T) AppModule_ProvideMessageRepositoryFactory.provideMessageRepository(singletonCImpl.messageDao(), singletonCImpl.provideKeystoreManagerProvider.get());

          case 2: // com.cryptika.messenger.data.local.db.AppDatabase 
          return (T) AppModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideKeystoreManagerProvider.get());

          case 3: // com.cryptika.messenger.data.local.keystore.KeystoreManager 
          return (T) AppModule_ProvideKeystoreManagerFactory.provideKeystoreManager();

          case 4: // com.cryptika.messenger.data.remote.BackgroundConnectionManager 
          return (T) new BackgroundConnectionManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideContactRepositoryProvider.get(), singletonCImpl.provideIdentityRepositoryProvider.get(), singletonCImpl.provideMessageRepositoryProvider.get(), singletonCImpl.conversationDao(), singletonCImpl.handshakeManagerProvider.get(), singletonCImpl.provideIdentityKeyManagerProvider.get(), singletonCImpl.provideTicketManagerProvider.get(), singletonCImpl.provideRelayApiProvider.get(), singletonCImpl.authStoreProvider.get(), singletonCImpl.serverConfigProvider.get(), singletonCImpl.provideOkHttpClientProvider.get(), DoubleCheck.lazy(singletonCImpl.callManagerProvider));

          case 5: // com.cryptika.messenger.domain.repository.ContactRepository 
          return (T) AppModule_ProvideContactRepositoryFactory.provideContactRepository(singletonCImpl.contactDao());

          case 6: // com.cryptika.messenger.domain.repository.IdentityRepository 
          return (T) AppModule_ProvideIdentityRepositoryFactory.provideIdentityRepository(singletonCImpl.localIdentityDao(), singletonCImpl.provideIdentityKeyManagerProvider.get());

          case 7: // com.cryptika.messenger.domain.crypto.IdentityKeyManager 
          return (T) AppModule_ProvideIdentityKeyManagerFactory.provideIdentityKeyManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // com.cryptika.messenger.domain.crypto.HandshakeManager 
          return (T) new HandshakeManager(singletonCImpl.provideIdentityKeyManagerProvider.get(), singletonCImpl.provideSessionKeyManagerProvider.get());

          case 9: // com.cryptika.messenger.domain.crypto.SessionKeyManager 
          return (T) AppModule_ProvideSessionKeyManagerFactory.provideSessionKeyManager();

          case 10: // com.cryptika.messenger.domain.crypto.TicketManager 
          return (T) AppModule_ProvideTicketManagerFactory.provideTicketManager();

          case 11: // com.cryptika.messenger.data.remote.api.RelayApi 
          return (T) AppModule_ProvideRelayApiFactory.provideRelayApi(singletonCImpl.provideOkHttpClientProvider.get());

          case 12: // okhttp3.OkHttpClient 
          return (T) AppModule_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 13: // com.cryptika.messenger.data.local.AuthStore 
          return (T) new AuthStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 14: // com.cryptika.messenger.data.remote.ServerConfig 
          return (T) new ServerConfig(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 15: // com.cryptika.messenger.data.remote.CallManager 
          return (T) new CallManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.backgroundConnectionManagerProvider.get(), singletonCImpl.provideIdentityKeyManagerProvider.get(), singletonCImpl.provideSessionKeyManagerProvider.get());

          case 16: // com.cryptika.messenger.data.remote.EphemeralSessionManager 
          return (T) new EphemeralSessionManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.authStoreProvider.get(), singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.serverConfigProvider.get(), singletonCImpl.provideContactRepositoryProvider.get(), singletonCImpl.provideIdentityRepositoryProvider.get(), singletonCImpl.provideMessageRepositoryProvider.get(), singletonCImpl.handshakeManagerProvider.get(), singletonCImpl.provideIdentityKeyManagerProvider.get(), singletonCImpl.provideAuthRepositoryProvider.get());

          case 17: // com.cryptika.messenger.domain.repository.AuthRepository 
          return (T) AppModule_ProvideAuthRepositoryFactory.provideAuthRepository(singletonCImpl.provideAuthApiProvider.get(), singletonCImpl.authStoreProvider.get(), singletonCImpl.serverConfigProvider.get(), singletonCImpl.provideIdentityRepositoryProvider.get());

          case 18: // com.cryptika.messenger.data.remote.api.AuthApi 
          return (T) AppModule_ProvideAuthApiFactory.provideAuthApi(singletonCImpl.provideOkHttpClientProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
