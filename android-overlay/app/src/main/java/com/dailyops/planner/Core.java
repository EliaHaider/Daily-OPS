package com.dailyops.planner;
import android.app.AlarmManager; import android.app.NotificationManager; import android.app.PendingIntent;
import android.content.Context; import android.content.Intent; import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONArray; import org.json.JSONObject;

public class Core {
  static SharedPreferences sp(Context c) { return c.getSharedPreferences("dops", 0); }
  static Intent launch(Context c) { Intent i = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName()); return i == null ? new Intent() : i; }
  static JSONArray items(Context c) { try { return new JSONArray(sp(c).getString("items", "[]")); } catch (Exception e) { return new JSONArray(); } }
  static PendingIntent pi(Context c, int slot, String extra) {
    Intent i = new Intent(c, AlarmReceiver.class).setAction("com.dailyops.FIRE").putExtra("slot", slot);
    if (extra != null) i.putExtra("item", extra);
    return PendingIntent.getBroadcast(c, slot, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
  }
  // slot 0 = ringing alarms (alarm-clock, most reliable), slot 1 = normal reminders. Each slot always holds only the NEXT item.
  static void scheduleNext(Context c) {
    AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    JSONArray a = items(c); long now = System.currentTimeMillis();
    for (int slot = 0; slot < 2; slot++) {
      long last = sp(c).getLong("last" + slot, 0), best = 0;
      for (int i = 0; i < a.length(); i++) {
        JSONObject o = a.optJSONObject(i); if (o == null) continue;
        if ((o.optInt("ring") > 0) != (slot == 0)) continue;
        long at = o.optLong("at");
        if (at > last && at > now - 120000 && (best == 0 || at < best)) best = at;
      }
      PendingIntent p = pi(c, slot, null);
      am.cancel(p);
      if (best == 0) continue;
      try {
        if (slot == 0) am.setAlarmClock(new AlarmManager.AlarmClockInfo(best, PendingIntent.getActivity(c, 0, launch(c), PendingIntent.FLAG_IMMUTABLE)), p);
        else if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, best, p);
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, best, p);
      } catch (Exception e) {}
    }
  }
  static void fire(Context c, int slot) {
    long now = System.currentTimeMillis(), last = sp(c).getLong("last" + slot, 0), mx = last; JSONArray a = items(c);
    for (int i = 0; i < a.length(); i++) {
      JSONObject o = a.optJSONObject(i); if (o == null) continue;
      if ((o.optInt("ring") > 0) != (slot == 0)) continue;
      long at = o.optLong("at");
      if (at > last && at <= now + 3000) { if (at > now - 300000) show(c, o); if (at > mx) mx = at; }
    }
    sp(c).edit().putLong("last" + slot, mx).apply();
    scheduleNext(c);
  }
  static void show(Context c, JSONObject o) {
    if (o.optInt("ring") > 0) {
      try { ContextCompat.startForegroundService(c, new Intent(c, RingService.class).putExtra("item", o.toString())); return; } catch (Exception e) {}
    }
    post(c, o);
  }
  static void post(Context c, JSONObject o) {
    NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationCompat.Builder b = new NotificationCompat.Builder(c, o.optString("cid", "c_task_0"))
      .setSmallIcon(R.drawable.ic_stat_icon).setContentTitle(o.optString("title")).setContentText(o.optString("body"))
      .setStyle(new NotificationCompat.BigTextStyle().bigText(o.optString("body")))
      .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).setColor(0xFF12F7A4)
      .setContentIntent(PendingIntent.getActivity(c, 0, launch(c), PendingIntent.FLAG_IMMUTABLE));
    if (Build.VERSION.SDK_INT < 26) b.setDefaults(NotificationCompat.DEFAULT_ALL);
    nm.notify(o.optInt("id") + 1000, b.build());
  }
}
