package com.example.appmedica.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.appmedica.MainActivity
import com.example.appmedica.R
import com.example.appmedica.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceptorCita : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val citaId = intent.getStringExtra("EXTRA_CITA_ID") ?: return
        val motivo = intent.getStringExtra("EXTRA_MOTIVO") ?: "Consulta médica"
        val horaCita = intent.getStringExtra("EXTRA_HORA_CITA") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "citas_recordatorio_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Citas Médicas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de citas programadas para 5 horas antes"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val abrirApp = PendingIntent.getActivity(
            context,
            citaId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_agenda)
            .setContentTitle("¡Recordatorio de Cita Médica!")
            .setContentText("Tienes una cita de: '$motivo' hoy a las $horaCita.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(citaId.hashCode(), notification)

        // Marcar como notificado en la base de datos Room
        val pendiente = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.obtener(context)
                database.citaDao().actualizarEstadoNotificacion(citaId, true)
            } finally {
                pendiente.finish()
            }
        }
    }
}