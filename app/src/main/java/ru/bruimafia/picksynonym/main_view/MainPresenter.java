package ru.bruimafia.picksynonym.main_view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

import ru.bruimafia.picksynonym.model.ModelInterface;
import ru.bruimafia.picksynonym.object.Word;
import ru.bruimafia.picksynonym.util.SharedPreferencesManager;

public class MainPresenter implements MainContract.Presenter {

    private int level = 1, points = 0, hints = 2;
    private List<Word> userWordsList = new ArrayList<>(); // массив введенных пользователем синонимов

    private Context context;
    private MainContract.View view;
    private ModelInterface model;
    private SharedPreferencesManager sPrefManager;

    public MainPresenter(Context context, MainContract.View view, ModelInterface model) {
        this.context = context;
        this.view = view;
        this.model = model;
        this.sPrefManager = SharedPreferencesManager.getInstance(context);
    }

    @Override
    public void loadSavedData() {
        level = sPrefManager.getUserLevel();
        points = sPrefManager.getUserPoints();
        hints = sPrefManager.getUserHints();
        userWordsList = sPrefManager.getUserRhymes(level);
        updateUI();
    }

    @Override
    public void onSignInClicked() {
        view.onSignIn();
    }

    @Override
    public void onPlayGameClicked() {
        view.onPlayGame();
    }

    @Override
    public void onTrainingGameClicked() {
        view.onTrainingGame();
    }

    @Override
    public void openLeaderboardsClicked() {
        view.openLeaderboards();
    }

    @Override
    public void onViewAdsForHint() {
        view.viewAdsForHint();
    }

    @Override
    public void openSuggestSynonymClicked() {
        view.openSuggestSynonym();
    }

    @Override
    public void onResetGameClicked() {
        view.onResetGame();
    }

    @Override
    public void onSignOutClicked() {
        view.onSignOut();
    }

    @Override
    public void onPickRhymeLinkClicked() {
        view.onPickRhymeLink();
    }

    @Override
    public void onBuyFullAppClicked() {
        view.onBuyFullApp();
    }

    @Override
    public void openAboutAppClicked() {
        view.openAboutApp();
    }

    @Override
    public void updateUI() {
        view.showLevelValue(String.valueOf(level));
        view.showPointsValue(String.valueOf(points));
        view.showMainMenu();
        view.showExtraMenu();
    }

    @Override
    public void checkPurchased() {
        view.showMessageBuyFullApp(sPrefManager.getIsFullVersion());
    }

    @Override
    public boolean isNetworkOnline(Context context) {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }

    @Override
    public void upUserHintsForViewAds() {
        hints += 1;
        sPrefManager.setUserHints(hints);
    }

}
