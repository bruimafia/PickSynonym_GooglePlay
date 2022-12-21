package ru.bruimafia.picksynonym.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import ru.bruimafia.picksynonym.R;
import ru.bruimafia.picksynonym.databinding.DialogSuggestSynonymBinding;
import ru.bruimafia.picksynonym.model.Model;
import ru.bruimafia.picksynonym.model.ModelInterface;
import ru.bruimafia.picksynonym.object.Word;
import ru.bruimafia.picksynonym.util.FirebaseManager;
import ru.bruimafia.picksynonym.util.SharedPreferencesManager;

public class SuggestSynonymDialog extends BottomSheetDialogFragment {

    private DialogSuggestSynonymBinding binding;
    private Context context;
    private ModelInterface model;
    private SharedPreferencesManager sPrefManager;
    private FirebaseManager firebaseManager;
    private String wordLevel;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSuggestSynonymBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();

        model = new Model(context);
        sPrefManager = SharedPreferencesManager.getInstance(context);
        firebaseManager = new FirebaseManager(context);

        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(context, R.layout.word_in_spinner, getOpenLevelsListString(model.getAllWordsFromDataBase()));
        binding.spinner.setAdapter(userAdapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wordLevel = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.btnSend.setOnClickListener(v -> {
            if (!binding.etSuggestSynonym.getText().toString().equals("")) {
                firebaseManager.sendSuggestRhymeToCloudFirestore(wordLevel, binding.etSuggestSynonym.getText().toString());
                Toasty.info(context, getString(R.string.suggest_success), Toast.LENGTH_SHORT, false).show();
                dismiss();
            }
        });

        // исправление наезда клавиатуры на bottom sheet dialog
        Objects.requireNonNull(getDialog()).setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal == null) return;
            BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        return binding.getRoot();
    }

    private List<String> getOpenLevelsListString(List<Word> listAllWords) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < sPrefManager.getUserLevel(); i++)
            list.add(listAllWords.get(i).getWord());
        return list;
    }

}
