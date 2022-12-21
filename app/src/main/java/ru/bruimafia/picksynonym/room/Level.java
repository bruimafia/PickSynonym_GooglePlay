package ru.bruimafia.picksynonym.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "levels")
public class Level {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String word;
    private String synonyms;

    public Level(int id, String word, String synonyms) {
        this.id = id;
        this.word = word;
        this.synonyms = synonyms;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }
}
