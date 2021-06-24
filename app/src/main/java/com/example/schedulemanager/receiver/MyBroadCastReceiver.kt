package com.example.schedulemanager.receiver

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.schedulemanager.R
import com.example.schedulemanager.activity.InfoActivity

/**
 * 알림 브로드캐스트 리시버
 */
class MyBroadCastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getStringExtra("alarmCode")
        val title = intent?.getStringExtra("title")
        val dataID=intent?.getStringExtra("dataID")
        val content = intent?.getStringExtra("date")+" "+intent?.getStringExtra("time")
        val clickIntent = Intent(context, InfoActivity::class.java)
        clickIntent.putExtra("dataID", dataID)
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(context, id!!.toInt(), clickIntent, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = NotificationCompat.Builder(context!!, "id")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            NotificationManagerCompat.from(context).let {
                id.let { it1 ->
                    if (it1 != null) {
                        it.notify(it1.toInt(), builder.build())
                    }
                }
            }
        }else{
            context?.let {
                val notificationManager=it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = Notification.Builder(it)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build()
                notificationManager.notify(id.toInt(),notification)
            }
        }


    }
}