package com.example.appmedica.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.appmedica.data.local.CitaEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Recordatorio {

    fun programarAlarmaCita(context: Context, cita: CitaEntity) {
        try {
            val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val fechaDate = formato.parse("${cita.fecha} ${cita.hora}") ?: return

            val horaPosterior = Calendar.getInstance().apply {
                time = fechaDate
                add(Calendar.HOUR_OF_DAY, -5)
            }

            val tiempoAlarma = horaPosterior.timeInMillis

            if (tiempoAlarma <= System.currentTimeMillis()) return

            val controlador = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intento = Intent(context, ReceptorCita::class.java).apply {
                putExtra("EXTRA_CITA_ID", cita.id)
                putExtra("EXTRA_MOTIVO", cita.motivo)
                putExtra("EXTRA_HORA_CITA", cita.hora)
            }

            val intentoPendiente = PendingIntent.getBroadcast(
                context,
                cita.id.hashCode(),
                intento,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (controlador.canScheduleExactAlarms()) {
                    controlador.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        tiempoAlarma,
                        intentoPendiente
                    )
                }
            } else {
                controlador.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    tiempoAlarma,
                    intentoPendiente
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}