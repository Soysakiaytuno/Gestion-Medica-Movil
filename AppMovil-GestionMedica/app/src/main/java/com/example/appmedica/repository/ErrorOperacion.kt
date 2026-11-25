package com.example.appmedica.repository

/** Mensaje apto para la interfaz. El diagnóstico técnico se conserva como causa. */
class ErrorOperacion(mensaje: String, causa: Throwable? = null) : IllegalStateException(mensaje, causa)
