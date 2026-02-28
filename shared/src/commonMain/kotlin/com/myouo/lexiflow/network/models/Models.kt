package com.myouo.lexiflow.network.models

import kotlinx.serialization.Serializable

@Serializable
data class CatalogDto(
    val datasets: List<DatasetDto>
)

@Serializable
data class DatasetDto(
    val id: String,
    val name: String,
    val description: String,
    val sha256: String,
    val url: String
)

@Serializable
data class WordDto(
    val id: String,
    val lemma: String,
    val lang: String,
    val senses: List<SenseDto>
)

@Serializable
data class SenseDto(
    val id: String,
    val pos: String,
    val gloss: String
)
