package ru.bruimafia.picksynonym.levels_view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import ru.bruimafia.picksynonym.R;
import ru.bruimafia.picksynonym.adapter.LevelAdapter;
import ru.bruimafia.picksynonym.databinding.ActivityLevelsBinding;
import ru.bruimafia.picksynonym.dialog.AllSynonymsDialog;
import ru.bruimafia.picksynonym.game_view.GameActivity;
import ru.bruimafia.picksynonym.model.Model;
import ru.bruimafia.picksynonym.model.ModelInterface;
import ru.bruimafia.picksynonym.object.Word;
import ru.bruimafia.picksynonym.util.SharedPreferencesManager;

public class LevelsActivity extends AppCompatActivity {

    private ActivityLevelsBinding binding;
    private ModelInterface model;
    private SharedPreferencesManager sPrefManager;
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        binding = ActivityLevelsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.training_mode));
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        model = new Model(this);
        sPrefManager = SharedPreferencesManager.getInstance(this);
        currentLevel = sPrefManager.getUserLevel();

        showLevels();
    }

    // отображение уровней
    private void showLevels() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.rvLevels.setLayoutManager(layoutManager);
        binding.rvLevels.addItemDecoration(new DividerItemDecoration(binding.rvLevels.getContext(), DividerItemDecoration.VERTICAL));
//        Collections.sort(aList, (w1, w2) -> Integer.compare(w1.getId(), w2.getId()));
        LevelAdapter adapter = new LevelAdapter(this, hideFailedLevels());
        binding.rvLevels.setAdapter(adapter); // устанавливаем адаптер

        adapter.setOnWordClickListener(position -> {
            Word word = adapter.getWords().get(position);
            if (word.getId() < currentLevel) {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("level", word.getId());
                intent.putExtra("mode", "training");
                startActivity(intent);
            } else if (word.getId() == currentLevel)
                Toasty.error(this, getString(R.string.complete_this_level_first), Toast.LENGTH_SHORT, false).show();
            else
                Toasty.error(this, getString(R.string.access_closed), Toast.LENGTH_SHORT, false).show();
        });

        adapter.setOnAllSynonymsClickListener(position -> {
            Word word = adapter.getWords().get(position);
            if (word.getId() < currentLevel) {
                AllSynonymsDialog dialog = new AllSynonymsDialog();
                Bundle bundle = new Bundle();
                bundle.putInt("level", word.getId());
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "BottomSheetDialogAllSynonyms");

            } else if (word.getId() == currentLevel)
                Toasty.error(this, getString(R.string.complete_this_level_first), Toast.LENGTH_SHORT, false).show();
            else
                Toasty.error(this, getString(R.string.access_closed), Toast.LENGTH_SHORT, false).show();
        });
    }

    // скрытие непройденных уровней
    private List<Word> hideFailedLevels() {
        List<Word> list = new ArrayList<>(model.getAllWordsFromDataBase());
        for (int i = currentLevel; i < list.size(); i++)
            list.get(i).setWord("???");
        return list;
    }

}
