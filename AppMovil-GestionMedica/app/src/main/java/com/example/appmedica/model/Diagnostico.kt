package com.example.appmedica.model

data class Diagnostico(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fecha: String,
    val titulo: Stirng,
    val descripcion: String
)