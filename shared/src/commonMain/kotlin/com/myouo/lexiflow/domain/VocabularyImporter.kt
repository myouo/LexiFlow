package com.myouo.lexiflow.domain

import com.myouo.lexiflow.network.NetworkClient
import com.myouo.lexiflow.network.models.CatalogDto
import com.myouo.lexiflow.network.models.WordDto
import com.myouo.lexiflow.crypto.sha256
import com.myouo.lexiflow.database.DatabaseHelper
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class VocabularyImporter(private val databaseHelper: DatabaseHelper) {
    private val client = NetworkClient.client
    
    suspend fun fetchCatalog(url: String): CatalogDto {
        return client.get(url).body()
    }
    
    suspend fun importDataset(url: String, expectedSha256: String) {
        val jsonText = client.get(url).bodyAsText()
        
        val actualSha256 = sha256(jsonText)
        if (actualSha256 != expectedSha256) {
            throw Exception("SHA-256 mismatch. Expected: \$expectedSha256, Actual: \$actualSha256")
        }
        
        val words: List<WordDto> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonText)
        
        databaseHelper.queries().transaction {
            words.forEach { word ->
                databaseHelper.queries().insertWord(word.id, word.lemma, word.lang)
                word.senses.forEachIndexed { index, sense ->
                    databaseHelper.queries().insertSense(sense.id, word.id, sense.pos, sense.gloss, index)
                }
            }
        }
    }
}
