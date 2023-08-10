package ru.bruimafia.picksynonym.main_view;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;

import es.dmoral.toasty.Toasty;
import ru.bruimafia.picksynonym.R;
import ru.bruimafia.picksynonym.databinding.ActivityMainBinding;
import ru.bruimafia.picksynonym.dialog.AboutDialog;
import ru.bruimafia.picksynonym.dialog.BuyFullDialog;
import ru.bruimafia.picksynonym.dialog.SuggestSynonymDialog;
import ru.bruimafia.picksynonym.game_view.GameActivity;
import ru.bruimafia.picksynonym.levels_view.LevelsActivity;
import ru.bruimafia.picksynonym.model.Model;
import ru.bruimafia.picksynonym.util.FirebaseManager;
import ru.bruimafia.picksynonym.util.GoogleAccountManager;
import ru.bruimafia.picksynonym.util.SharedPreferencesManager;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private final String TAG = "ADS";

    private static final int RC_LEADERBOARD_UI = 9004; // код запроса для вызова таблицы лидеров
    private static final int RC_SIGN_IN = 9001; // код запроса для вызова взаимодействия с пользователем

    private ActivityMainBinding binding;
    private MainContract.Presenter presenter;
    private SharedPreferencesManager sPrefManager;
    private GoogleAccountManager googleAccountManager;
    private FirebaseManager firebaseManager;
    private RewardedAd yandexRewardedAd; // реклама с вознаграждением

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        presenter = new MainPresenter(this, this, new Model(this));
        sPrefManager = SharedPreferencesManager.getInstance(this);
        googleAccountManager = new GoogleAccountManager(this, this);
        firebaseManager = new FirebaseManager(this);

        presenter.checkPurchased(); // обычная версия приложения, с рекламой

        checkUpdateAvailability();

        binding.btnSignIn.setOnClickListener(v -> presenter.onSignInClicked());
        binding.btnPlay.setOnClickListener(v -> presenter.onPlayGameClicked());
        binding.btnTraining.setOnClickListener(v -> presenter.onTrainingGameClicked());
        binding.btnLeaderboards.setOnClickListener(v -> presenter.openLeaderboardsClicked());
        binding.btnGetHintForViewAds.setOnClickListener(v -> presenter.onViewAdsForHint());
        binding.btnSuggestSynonym.setOnClickListener(v -> presenter.openSuggestSynonymClicked());
        binding.btnReset.setOnClickListener(v -> presenter.onResetGameClicked());
        binding.btnSignOut.setOnClickListener(v -> presenter.onSignOutClicked());
        binding.clPickRhymeLink.setOnClickListener(v -> presenter.onPickRhymeLinkClicked());
        binding.tvBuyFullApp.setOnClickListener(v -> presenter.onBuyFullAppClicked());
        binding.tvAbout.setOnClickListener(v -> presenter.openAboutAppClicked());

        isPickRhymeInstalled();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            googleAccountManager.saveSignIn(data);
            firebaseManager.checkDataCloudFirestore();
            presenter.loadSavedData();
        }
    }

    @Override
    public void showLevelValue(String value) {
        binding.tvLevelValue.setText(value);
    }

    @Override
    public void showPointsValue(String value) {
        binding.tvPointsValue.setText(value);
    }

    @Override
    public void showPlayerName(String playerName) {
        binding.tvPlayerName.setText(String.format(getString(R.string.welcome_player), playerName));
    }

    @Override
    public void showMessageBuyFullApp(boolean isPurchased) {
        binding.tvBuyFullApp.setVisibility(isPurchased ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onSignIn() {
        googleAccountManager.startSignInIntent();
    }

    @Override
    public void onPlayGame() {
        startActivity(new Intent(this, GameActivity.class));
    }

    @Override
    public void onTrainingGame() {
        startActivity(new Intent(this, LevelsActivity.class));
    }

    @Override
    public void openLeaderboards() {
        googleAccountManager.getAllLeaderboardsIntent(presenter.isNetworkOnline(this));
        if (!googleAccountManager.getAllLeaderboardsIntent(presenter.isNetworkOnline(this)))
            showSnackbar("Кажется, отсутствует интернет-соединение");
    }

    @Override
    public void viewAdsForHint() {
        initAdsRewarded();
    }

    @Override
    public void openSuggestSynonym() {
        SuggestSynonymDialog dialog = new SuggestSynonymDialog();
        dialog.show(getSupportFragmentManager(), "BottomSheetDialogSuggestSynonym");
    }

    @Override
    public void onResetGame() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reset, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        view.findViewById(R.id.btn_allSynonyms).setOnClickListener(view12 -> {
            firebaseManager.clearData();
            sPrefManager.clearAll();
            presenter.loadSavedData();
            bottomSheetDialog.dismiss();
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> bottomSheetDialog.cancel());

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    @Override
    public void onSignOut() {
        if (googleAccountManager.isSignedIn())
            googleAccountManager.signOut();
        presenter.loadSavedData();
    }

    @Override
    public void onPickRhymeLink() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.gukov.pickrhyme")));
    }

    @Override
    public void onBuyFullApp() {
        BuyFullDialog dialog = new BuyFullDialog();
        dialog.show(getSupportFragmentManager(), "BottomSheetDialogBuyFull");
    }

    @Override
    public void openAboutApp() {
        AboutDialog dialog = new AboutDialog();
        dialog.show(getSupportFragmentManager(), "DialogAbout");
    }

    @Override
    public void showHandleExceptionDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void showSomeIntent(Intent intent, int code) {
        startActivityForResult(intent, code);
    }

    @Override
    public void showMainMenu() {
        if (!googleAccountManager.isSignedIn()) {
            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.tvPlayerName.setVisibility(View.GONE);
            binding.btnLeaderboards.setVisibility(View.GONE);
            binding.btnSignOut.setVisibility(View.GONE);
        } else {
            Log.d("GoogleAccount", "Пользователь уже авторизован");
            binding.btnSignIn.setVisibility(View.GONE);
            binding.tvPlayerName.setVisibility(View.VISIBLE);
            binding.btnLeaderboards.setVisibility(View.VISIBLE);
            binding.btnSignOut.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showExtraMenu() {
        if (sPrefManager.getUserLevel() <= 1) {
            binding.btnTraining.setVisibility(View.GONE);
            binding.btnReset.setVisibility(View.GONE);
        } else {
            binding.btnTraining.setVisibility(View.VISIBLE);
            binding.btnReset.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showSnackbar(String message) {
        Snackbar.make(binding.constraintLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showToastInfo(String message) {
        Toasty.info(this, message, Toast.LENGTH_LONG, false).show();
    }

    // инициализация рекламы с вознаграждением в приложении
    public void initAdsRewarded() {
        yandexRewardedAd = new RewardedAd(this);
        yandexRewardedAd.setAdUnitId(getString(R.string.ads_yandex_rewardedAd_unitId));
        final AdRequest adRequest = new AdRequest.Builder().build();
        yandexRewardedAd.setRewardedAdEventListener(new RewardedAdEventListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Yandex: onAdLoaded");
                yandexRewardedAd.show();
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                Log.d(TAG, "Yandex (onAdFailedToLoad): " + adRequestError.getDescription());
                showToastInfo("Ищем рекламу... Подождите несколько секунд и повторите попытку");
                yandexRewardedAd = null;
            }

            @Override
            public void onAdShown() {
                Log.d(TAG, "Yandex: onAdShown");
            }

            @Override
            public void onAdDismissed() {
                Log.d(TAG, "Yandex: onAdDismissed");
                yandexRewardedAd = null;
            }

            @Override
            public void onRewarded(@NonNull Reward reward) {
                Log.d(TAG, "Yandex: onRewarded");
                // здесь происходит возаграждение
                presenter.upUserHintsForViewAds();
                showToastInfo("Ура! Вы заработали одну подсказку, теперь у вас их: " + sPrefManager.getUserHints());
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Yandex: onAdClicked");
            }

            @Override
            public void onLeftApplication() {
                Log.d(TAG, "Yandex: onLeftApplication");
            }

            @Override
            public void onReturnedToApplication() {
                Log.d(TAG, "Yandex: onReturnedToApplication");
            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {
                Log.d(TAG, "Yandex: onImpression");
            }
        });
        yandexRewardedAd.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleAccountManager.signInSilently();
        presenter.loadSavedData();
        firebaseManager.checkDataCloudFirestore();
        presenter.loadSavedData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        googleAccountManager.updateLeaderboards(sPrefManager.getUserPoints(), sPrefManager.getUserLevel());
    }

    // проверка обновлений
    private void checkUpdateAvailability() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            8);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // проверка установки Подбери рифму
    public void isPickRhymeInstalled() {
        try {
            getApplicationContext().getPackageManager().getApplicationInfo("com.gukov.pickrhyme", 0);
            binding.clPickRhymeLink.setVisibility(View.GONE);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }
}
