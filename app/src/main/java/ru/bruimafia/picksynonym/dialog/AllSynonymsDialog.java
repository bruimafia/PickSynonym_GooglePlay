package ru.bruimafia.picksynonym.dialog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.bruimafia.picksynonym.R;
import ru.bruimafia.picksynonym.adapter.WordAdapter;
import ru.bruimafia.picksynonym.databinding.DialogAllSynonymsBinding;
import ru.bruimafia.picksynonym.model.DatabaseHelper;
import ru.bruimafia.picksynonym.object.Word;

public class AllSynonymsDialog extends BottomSheetDialogFragment {

    private DialogAllSynonymsBinding binding;
    private Context context;

    private String wordOfLevel = "..."; // слово текущего уровня
    private List<Word> aList = new ArrayList<>(); // массив для адаптера
    private List<String> words = new ArrayList<>(); // массив слов
    private SQLiteDatabase mDb;
    private int level;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAllSynonymsBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();

        if (getArguments() != null)
            level = getArguments().getInt("level", 1);

        initDB();
        loadingLevel();

        for (String s : words) {
            if (!s.equals(""))
                aList.add(new Word(s));
        }

        showEnteredWords();
        setInfo();
        binding.btnExcellent.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }

    // загрузка информации на экран
    private void setInfo() {
        binding.tvFoundCountSynonyms.setText(String.format(getString(R.string.found_count_synonyms), String.valueOf(words.size())));
        binding.tvThereAreSuchSynonyms.setText(String.format(getString(R.string.other_synonyms), wordOfLevel));
        binding.btnExcellent.setText(R.string.close);
    }

    // инициализация базы данных
    private void initDB() {
        DatabaseHelper mDBHelper = new DatabaseHelper(getContext());
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        mDb = mDBHelper.getWritableDatabase();
    }

    // загрузка текущего уровня из БД
    private void loadingLevel() {
        Cursor c = mDb.rawQuery("SELECT * FROM levels WHERE id = " + level, null);
        c.moveToFirst();
        if (c.getCount() != 0) {
            wordOfLevel = c.getString(1);
            words = Arrays.asList((c.getString(2)).split(","));
        }
        c.close();
    }

    // отображение введенных слов
    private void showEnteredWords() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        binding.rvSynonyms.setLayoutManager(layoutManager);
        Collections.shuffle(aList); // перемешать слова
        WordAdapter adapter = new WordAdapter(aList);
        binding.rvSynonyms.setAdapter(adapter); // устанавливаем адаптер
    }

}
