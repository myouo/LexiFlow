package com.myouo.lexiflow.database

import app.cash.sqldelight.ColumnAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class DatabaseHelper(databaseDriverFactory: DatabaseDriverFactory) {
    private val intAdapter = object : ColumnAdapter<Int, Long> {
        override fun decode(databaseValue: Long): Int = databaseValue.toInt()
        override fun encode(value: Int): Long = value.toLong()
    }

    private val doubleAdapter = object : ColumnAdapter<Double, Double> {
        override fun decode(databaseValue: Double): Double = databaseValue
        override fun encode(value: Double): Double = value
    }

    private val database = AppDatabase(
        driver = databaseDriverFactory.createDriver(),
        daily_statsAdapter = Daily_stats.Adapter(
            focus_secondsAdapter = intAdapter,
            new_learned_wordsAdapter = intAdapter,
            reviewsAdapter = intAdapter
        ),
        review_logAdapter = Review_log.Adapter(
            ratingAdapter = intAdapter
        ),
        sensesAdapter = Senses.Adapter(
            sense_orderAdapter = intAdapter
        ),
        srs_stateAdapter = Srs_state.Adapter(
            stabilityAdapter = doubleAdapter,
            difficultyAdapter = doubleAdapter,
            lapsesAdapter = intAdapter
        )
    )
    private val dbQuery = database.appDatabaseQueries

    suspend fun insertWord(id: String, lemma: String, lang: String) {
        withContext(Dispatchers.IO) {
            dbQuery.insertWord(id = id, lemma = lemma, lang = lang)
        }
    }

    suspend fun insertSense(id: String, wordId: String, pos: String, gloss: String, order: Int) {
        withContext(Dispatchers.IO) {
            dbQuery.insertSense(id = id, word_id = wordId, pos = pos, gloss = gloss, sense_order = order)
        }
    }

    suspend fun getWord(id: String): Words? {
        return withContext(Dispatchers.IO) {
            dbQuery.getWord(id).executeAsOneOrNull()
        }
    }

    suspend fun getSensesForWord(wordId: String): List<Senses> {
        return withContext(Dispatchers.IO) {
            dbQuery.getSensesForWord(wordId).executeAsList()
        }
    }
    
    // Additional methods matching database access logic can live here
    fun queries() = dbQuery
}
