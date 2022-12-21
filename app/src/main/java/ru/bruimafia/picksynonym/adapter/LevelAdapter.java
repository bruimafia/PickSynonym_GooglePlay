package ru.bruimafia.picksynonym.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.bruimafia.picksynonym.R;
import ru.bruimafia.picksynonym.object.Word;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

    private Context context;
    private OnWordClickListener onWordClickListener;
    private OnAllSynonymsClickListener onAllSynonymsClickListener;
    private List<Word> words;

    public interface OnWordClickListener {
        void onWordClick(int position);
    }

    public interface OnAllSynonymsClickListener {
        void onWordClick(int position);
    }

    public void setOnWordClickListener(OnWordClickListener onWordClickListener) {
        this.onWordClickListener = onWordClickListener;
    }

    public void setOnAllSynonymsClickListener(OnAllSynonymsClickListener onAllSynonymsClickListener) {
        this.onAllSynonymsClickListener = onAllSynonymsClickListener;
    }

    class LevelViewHolder extends RecyclerView.ViewHolder {
        private TextView text, level;
        private ImageView allSynonyms;

        LevelViewHolder(View itemView) {
            super(itemView);
            this.level = itemView.findViewById(R.id.tv_levelValue);
            this.text = itemView.findViewById(R.id.tv_word);
            this.allSynonyms = itemView.findViewById(R.id.btn_allSynonyms);
            itemView.setOnClickListener(v -> {
                if (onWordClickListener != null)
                    onWordClickListener.onWordClick(getAdapterPosition());
            });
            allSynonyms.setOnClickListener(v -> {
                if (onAllSynonymsClickListener != null)
                    onAllSynonymsClickListener.onWordClick(getAdapterPosition());
            });
        }
    }

    public LevelAdapter(Context context, List<Word> words) {
        this.words = words;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.level, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LevelViewHolder holder, final int position) {
        TextView tvLevel = holder.level;
        TextView tvName = holder.text;

        tvLevel.setText(String.format(context.getString(R.string.level_of), words.get(position).getId()));
        tvName.setText(words.get(position).getWord());

        if (words.get(position).getWord().equals("?")) {
            tvName.setTextColor(context.getResources().getColor(R.color.grey_300));
            tvLevel.setTextColor(context.getResources().getColor(R.color.grey_300));
        } else {
            tvName.setTextColor(context.getResources().getColor(R.color.grey_600));
            tvLevel.setTextColor(context.getResources().getColor(R.color.grey_600));
        }

    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public List<Word> getWords() {
        return words;
    }

}