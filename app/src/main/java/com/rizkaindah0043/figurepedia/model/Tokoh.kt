package com.rizkaindah0043.figurepedia.model

data class ApiResponse(
    val status: String,
    val message: String,
    val people: List<Tokoh>
)

data class Tokoh(
    val name: String,
    val country: String,
    val field: String,
    val imageUrl: String
)
