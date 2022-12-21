package ru.bruimafia.picksynonym.game_view;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.bruimafia.picksynonym.R;
import ru.bruimafia.picksynonym.model.ModelInterface;
import ru.bruimafia.picksynonym.object.Word;
import ru.bruimafia.picksynonym.util.FirebaseManager;
import ru.bruimafia.picksynonym.util.SharedPreferencesManager;

public class GamePresenter implements GameContract.Presenter {

    public static final int WORDS_IN_LEVEL = 5; // количество слов на один уровень
    private int level = 1, points = 0, hints = 2, numberWordsForNextLevel = 5; // значения по умолчанию
    private List<Word> userWordsList = new ArrayList<>(); // массив введенных пользователем синонимов
    private List<Word> allWordsList = new ArrayList<>(); // массив возможных синониов из базы данных

    private Context context;
    private GameContract.View view;
    private ModelInterface model;
    private SharedPreferencesManager sPrefManager;
    private FirebaseManager firebaseManager;

    public GamePresenter(Context context, GameContract.View view, ModelInterface model) {
        this.context = context;
        this.view = view;
        this.model = model;
        this.sPrefManager = SharedPreferencesManager.getInstance(context);
        firebaseManager = new FirebaseManager(context);
    }

    @Override
    public void loadSavedData(int level) {
        this.level = level;
        points = sPrefManager.getUserPoints();
        hints = sPrefManager.getUserHints();
        userWordsList = sPrefManager.getUserRhymes(level);
        numberWordsForNextLevel = WORDS_IN_LEVEL - userWordsList.size();
        allWordsList = model.getSynonymsFromDatabase(level);
        updateUI();
        for (Word w : userWordsList)
            Log.d("KKK", w.getWord());
    }

    @Override
    public void onEnterWord(String inputText, String mode) {
        if (inputText.equals(""))
            view.showToastError(context.getString((R.string.enter_word)));
        else {
            if (!inputTextIsSynonym(inputText, mode))
                view.showToastError(context.getString(R.string.do_not_synonym));
            updateUI();
        }
    }

    // проверяем, является ли введенный текст синонимом
    private boolean inputTextIsSynonym(String word, String mode) {
        for (Word w : allWordsList) {
            if (w.getWord().equals(word)) {
                if (!isAdded(word)) {
                    userWordsList.add(w);
                    points += 1;
                    view.showToastSuccess(context.getString(R.string.good_synonym));
                    if (mode.equals("game")) {
                        numberWordsForNextLevel -= 1;
                        goToNextLevelIfNeed();
                    }
                    if (!sPrefManager.getIsFullVersion() && mode.equals("training") && userWordsList.size() % 6 == 0)
                        view.showAdsInterstitial();
                } else view.showToastWarning(context.getString(R.string.repeat_synonym));
                return true;
            }
        }
        return false;
    }

    private boolean isAdded(String word) {
        for (Word w : userWordsList) {
            if (w.getWord().equals(word))
                return true;
        }
        return false;
    }

    // переходим на следующий уровень, если набрали необходимое количество слов
    private void goToNextLevelIfNeed() {
        if (numberWordsForNextLevel == 0) {
            sPrefManager.updateUserLevelData(level, points, hints, userWordsList);
            firebaseManager.setDataCloudFirestore(level);
            level += 1;
            numberWordsForNextLevel = WORDS_IN_LEVEL;
            if (sPrefManager.getIsFullVersion())
                hints += 2;
            userWordsList.clear();
            allWordsList = model.getSynonymsFromDatabase(level);
            if (!sPrefManager.getIsFullVersion())
                view.showAdsInterstitial();
        }
    }

    @Override
    public void onHelpByEgg() {
        if (hints > 0)
            hints--;
        view.showHelpByEggDialog(level);
    }

    @Override
    public void updateUI() {
        view.showLevelValue(String.valueOf(level));
        view.showPointsValue(String.valueOf(points));
        view.showWord(model.getWordFromDatabase(level));
        view.showNumberWordsForNextLevel(String.valueOf(numberWordsForNextLevel));
        view.showCurrentEnteredSynonyms(userWordsList);
    }

    @Override
    public void saveResult() {
        sPrefManager.setUserData(level, points, hints, userWordsList);
    }

    @Override
    public void updateResult() {
        sPrefManager.updateUserLevelData(level, points, hints, userWordsList);
    }

}
