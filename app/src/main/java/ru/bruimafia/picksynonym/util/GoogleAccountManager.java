package ru.bruimafia.picksynonym.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayersClient;
import ru.bruimafia.picksynonym.R;
import ru.bruimafia.picksynonym.main_view.MainActivity;
import ru.bruimafia.picksynonym.main_view.MainContract;

public class GoogleAccountManager {

    private static final int RC_SIGN_IN = 9001; // код запроса для вызова взаимодействия с пользователем
    private static final int RC_LEADERBOARD_UI = 9004; // код запроса для вызова таблицы лидеров

    public String playerId = null, playerName = null;

    private Context context;
    private MainContract.View view;
    private SharedPreferencesManager sPrefManager;
    private GoogleSignInClient googleSignInClient = null; // клиент для входа в Google API
    private LeaderboardsClient leaderboardsClient = null; // google game – таблица лидеров
    private PlayersClient playersClient = null; // google game – клиент игрока

    public GoogleAccountManager(Context context, MainContract.View view) {
        this.context = context;
        this.view = view;
        googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN); // создание клиента для входа в систему
        sPrefManager = SharedPreferencesManager.getInstance(context);
    }

    // проверяем, авторизован ли пользователь
    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(context) != null;
    }

    // стартуем окно авторизации
    public void startSignInIntent() {
        view.showSomeIntent(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    // сохранение учетной записи, вошедшей в систему
    public void saveSignIn(Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess())
            onConnected(result.getSignInAccount());
        else {
            String message = result.getStatus().getStatusMessage();
            if (message == null || message.isEmpty())
                message = context.getString(R.string.error_sign_in);
            view.showHandleExceptionDialog(message);
        }
    }

    // соединение
    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        leaderboardsClient = Games.getLeaderboardsClient(context, googleSignInAccount);
        playersClient = Games.getPlayersClient(context, googleSignInAccount);
    }

    // разъединение
    public void onDisconnected() {
        leaderboardsClient = null;
        playersClient = null;
    }

    // выходим из системы
    public void signOut() {
        googleSignInClient.signOut().addOnCompleteListener((Activity) context, task -> onDisconnected());
        signInSilently();
    }

    // молчаливый вход в аккаунт
    public void signInSilently() {
        googleSignInClient.silentSignIn().addOnCompleteListener((MainActivity) context, task -> {
            if (task.isSuccessful())
                onConnected(task.getResult());
            else
                onDisconnected();
        });
        view.showMainMenu();
        getUserInfo();
    }

    // получения данных об авторизованном пользователе
    public void getUserInfo() {
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
        if (googleSignInAccount != null) {
            PlayersClient playersClient = Games.getPlayersClient(context, googleSignInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(p -> {
                        view.showPlayerName(p.getDisplayName());
                        sPrefManager.setUserId(p.getPlayerId());
                        sPrefManager.setUserName(p.getDisplayName());
                    })
                    .addOnFailureListener(e -> handleException(e, context.getString(R.string.error_player_id)));
        }
    }

    // открываем таблицу лидеров
    public boolean getAllLeaderboardsIntent(boolean isNetworkOnline) {
        if (isNetworkOnline && leaderboardsClient != null) {
            leaderboardsClient.getAllLeaderboardsIntent()
                    .addOnSuccessListener(intent -> view.showSomeIntent(intent, RC_LEADERBOARD_UI))
                    .addOnFailureListener(e -> handleException(e, context.getString(R.string.error_leaderboards)));
            return true;
        } else
            return false;

    }

    // занести данные игрока в таблицу лидеров
    public void updateLeaderboards(int points, int level) {
        if (leaderboardsClient != null) {
            leaderboardsClient.submitScore(context.getString(R.string.games_google_leaderboard_points), points);
            leaderboardsClient.submitScore(context.getString(R.string.games_google_leaderboard_level), level);
        }
    }

    // обработка ошибок
    private void handleException(Exception e, String details) {
        int status = (e instanceof ApiException) ? ((ApiException) e).getStatusCode() : 0;
        view.showHandleExceptionDialog(String.format("%1$s (status %2$d). %3$s.", details, status, e));
    }

}
