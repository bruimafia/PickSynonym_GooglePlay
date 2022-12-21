package ru.bruimafia.picksynonym.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ru.bruimafia.picksynonym.R;
import ru.bruimafia.picksynonym.databinding.DialogHelpByEggBinding;
import ru.bruimafia.picksynonym.model.Model;
import ru.bruimafia.picksynonym.model.ModelInterface;
import ru.bruimafia.picksynonym.util.SharedPreferencesManager;

public class HelpByEggDialog extends BottomSheetDialogFragment {

    private DialogHelpByEggBinding binding;
    private Context context;
    private ModelInterface model;
    private SharedPreferencesManager sPrefManager;
    private int hints, level;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogHelpByEggBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();

        model = new Model(context);
        sPrefManager = SharedPreferencesManager.getInstance(context);
        if (getArguments() != null)
            level = getArguments().getInt("level", 1);
        hints = sPrefManager.getUserHints();

        if (hints == 0) {
            binding.tvEmptyHints.setVisibility(View.VISIBLE);
            binding.tvHelpWord.setVisibility(View.GONE);
            binding.btnThanks.setText(R.string.oh_sasha);
        }
        binding.tvKeepCountHints.setText(String.format(getString(R.string.keep_count_hints), Math.max(hints - 1, 0)));
        binding.tvHelpWord.setText(String.format(getString(R.string.quotation_marks), model.getRandomSynonymFromDatabase(level, sPrefManager.getUserRhymes(level))));

        if (hints > 0)
            downUserHints(hints);

        if (sPrefManager.getIsFullVersion())
            binding.tvAdvantageFullApp.setVisibility(View.GONE);

        binding.btnThanks.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }

    private void downUserHints(int hints) {
        sPrefManager.setUserHints(hints - 1);
    }

}
