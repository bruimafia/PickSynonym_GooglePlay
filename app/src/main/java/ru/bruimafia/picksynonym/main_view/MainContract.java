package ru.bruimafia.picksynonym.main_view;

import android.content.Context;
import android.content.Intent;

public interface MainContract {

    interface View {
        void showLevelValue(String value); // показ текущего уровня игры

        void showPointsValue(String value); // показ текущего количества баллов

        void showPlayerName(String playerName); // показ имени авторизованного игрока

        void showMessageBuyFullApp(boolean isPurchased); // показ ссылки о покупке приложения

        void onSignIn(); // войти в аккаунт

        void onPlayGame(); // начать игру

        void onTrainingGame(); // начать тренировку

        void openLeaderboards(); // открыть таблицу лидеров

        void viewAdsForHint(); // заработать подсказку за просмотр рекламы

        void openSuggestSynonym(); // предложить свой синоним

        void onResetGame(); // сбросить результаты

        void onSignOut(); // выйти из аккаунта

        void onPickRhymeLink(); // перейти на игру Подбери рифму

        void onBuyFullApp(); // купить полную версию

        void openAboutApp(); // открыть окно о приложении

        void showHandleExceptionDialog(String message); // показ диалогового окна с описанием ошибки

        void showSomeIntent(Intent intent, int code); // открытие intent (startActivityForResult)

        void showMainMenu(); // показ основного меню

        void showExtraMenu(); // показ расширенного меню

        void showSnackbar(String message); // всплывающее сообщение Обычное

        void showToastInfo(String message); // всплывающее сообщение Информация
    }

    interface Presenter {
        void loadSavedData(); // загрузка созраненных данных игры

        void onSignInClicked(); // кнопка войти в аккаунт

        void onPlayGameClicked(); // кнопка начать игру

        void onTrainingGameClicked(); // кнопка начать тренировку

        void openLeaderboardsClicked(); // кнопка открыть таблицу лидеров

        void onViewAdsForHint(); // заработать подсказку за просмотр рекламы

        void openSuggestSynonymClicked(); // кнопка предложить свой синоним

        void onResetGameClicked(); // кнопка сбросить результаты

        void onSignOutClicked(); // кнопка выйти из аккаунта

        void onPickRhymeLinkClicked(); // кнопка перехода на игру Подбери рифму

        void onBuyFullAppClicked(); // кнопка купить полную версию

        void openAboutAppClicked(); // открыть окно о приложении

        void updateUI(); // обновить интерфейс

        void checkPurchased(boolean isPurchased); // проверяем на полную купленную версию

        boolean isNetworkOnline(Context context); // проверка интернет-соединения

        void upUserHintsForViewAds(); // увеличение количества подсказок за просмотр рекламы
    }

}
