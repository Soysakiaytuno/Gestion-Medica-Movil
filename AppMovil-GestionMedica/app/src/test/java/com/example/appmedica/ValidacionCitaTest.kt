package com.example.appmedica

import com.example.appmedica.model.FormularioCita
import com.example.appmedica.model.validarCita
import java.time.LocalDateTime
import org.junit.Assert.*
import org.junit.Test

class ValidacionCitaTest {
    private val ahora = LocalDateTime.of(2026, 11, 13, 12, 0)
    private val valida = FormularioCita(pacienteId = "demo", fecha = "2026-11-14", hora = "09:30", motivo = "Control demo")

    @Test fun aceptaFechaHoraFuturasYRecortaEspacios() {
        assertNull(validarCita(valida, ahora))
        assertNull(validarCita(valida.copy(fecha = " 2026-11-14 ", hora = " 09:30 "), ahora))
    }

    @Test fun rechazaCamposObligatoriosVacios() {
        listOf(valida.copy(pacienteId = ""), valida.copy(fecha = ""),
            valida.copy(hora = ""), valida.copy(motivo = "  ")).forEach {
            assertEquals("Selecciona un paciente y completa todos los campos.", validarCita(it, ahora))
        }
    }

    @Test fun rechazaFechasInexistentesYFormatoIncorrecto() {
        listOf("2026-02-29", "2026-04-31", "2026-13-01", "14/11/2026", "2026-1-14").forEach {
            assertNotNull(it, validarCita(valida.copy(fecha = it), ahora))
        }
        assertNull(validarCita(valida.copy(fecha = "2028-02-29"), ahora))
    }

    @Test fun rechazaHorasInexistentesYSegundos() {
        listOf("24:00", "12:60", "9:30", "09:30:01", "texto").forEach {
            assertNotNull(it, validarCita(valida.copy(hora = it), ahora))
        }
    }

    @Test fun rechazaPasadoYElInstanteActual() {
        listOf(valida.copy(fecha = "2026-11-12"),
            valida.copy(fecha = "2026-11-13", hora = "11:59"),
            valida.copy(fecha = "2026-11-13", hora = "12:00")).forEach {
            assertEquals("La cita debe ser en una fecha y hora futuras.", validarCita(it, ahora))
        }
        assertNull(validarCita(valida.copy(fecha = "2026-11-13", hora = "12:01"), ahora))
    }
}
