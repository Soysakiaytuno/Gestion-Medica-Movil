package com.example.appmedica.model

import java.time.LocalDate

import java.time.Period

data class Paciente(
    val id: String = java.util.UUID.randomUUID().toString(),
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val ci: String,
    val email: String,
    val celular: String,
    val diagnosticos: List<Diagnostico>,
    val fechaNacimiento: LocalDate
)

fun calcularEdad(fechaNacimiento: LocalDate): Int {
    return Period.between(fechaNacimiento, LocalDate.now()).years
}