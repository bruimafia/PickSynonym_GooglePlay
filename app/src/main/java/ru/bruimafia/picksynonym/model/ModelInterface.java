package ru.bruimafia.picksynonym.model;

import java.util.List;

import ru.bruimafia.picksynonym.object.Word;

public interface ModelInterface {
    List<Word> getAllWordsFromDataBase(); // получение всех слов (уровней)

    String getWordFromDatabase(int level); // получение слова из базы данных

    List<Word> getSynonymsFromDatabase(int level); // получение всех возможных синонимов из базы данных

    String getRandomSynonymFromDatabase(int level, List<Word> userSynonyms); // полчение случайного синонима из базы данных
}
