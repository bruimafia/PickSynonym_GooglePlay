package ru.bruimafia.picksynonym.room;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LevelDao {

    @Query("SELECT * FROM levels")
    List<Level> getAllWordsFromDataBase();

    @Query("SELECT * FROM levels WHERE id = :id")
    Level getWordFromDatabase(int id);

}
