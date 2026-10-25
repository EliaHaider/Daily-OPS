package com.dailyops.planner;
import android.app.Notification; import android.app.NotificationChannel; import android.app.NotificationManager; import android.app.PendingIntent; import android.app.Service;
import android.content.BroadcastReceiver; import android.content.Context; import android.content.Intent; import android.content.IntentFilter; import android.content.pm.ServiceInfo;
import android.media.AudioAttributes; import android.media.MediaPlayer; import android.media.RingtoneManager;
import android.net.Uri; import android.os.Build; import android.os.Handler; import android.os.IBinder; import android.os.Looper; import android.os.PowerManager; import android.os.VibrationEffect; import android.os.Vibrator;
import androidx.core.app.NotificationCompat;
import org.json.JSONObject;

public class RingService extends Service {
  MediaPlayer mp; Vibrator vib; BroadcastReceiver rcv; boolean stopped;
  final Handler h = new Handler(Looper.getMainLooper());
  @Override public IBinder onBind(Intent i) { return null; }
  @Override public int onStartCommand(Intent in, int flags, int id) {
    if (in != null && "STOP".equals(in.getAction())) { finish(); return START_NOT_STICKY; }
    JSONObject o;
    try { o = new JSONObject(in.getStringExtra("item")); } catch (Exception e) { stopSelf(); return START_NOT_STICKY; }
    release(); stopped = false;
    PendingIntent stop = PendingIntent.getService(this, 7, new Intent(this, RingService.class).setAction("STOP"), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    PendingIntent open = PendingIntent.getActivity(this, 8, Core.launch(this), PendingIntent.FLAG_IMMUTABLE);
    if (Build.VERSION.SDK_INT >= 26) {
      NotificationChannel ch = new NotificationChannel("ring_live", "Chalta alarm", NotificationManager.IMPORTANCE_HIGH);
      ch.setSound(null, null); ch.enableVibration(false);
      ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(ch);
    }
    Notification n = new NotificationCompat.Builder(this, "ring_live").setSmallIcon(R.drawable.ic_stat_icon)
      .setContentTitle(o.optString("title")).setContentText(o.optString("body") + " (band karne ke liye volume ya power dabao)")
      .setPriority(NotificationCompat.PRIORITY_HIGH).setCategory(NotificationCompat.CATEGORY_ALARM).setOngoing(true).setColor(0xFF12F7A4)
      .setContentIntent(open).addAction(0, "Band karo", stop).build();
    try {
      if (Build.VERSION.SDK_INT >= 29) startForeground(4242, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK); else startForeground(4242, n);
    } catch (Exception e) { Core.post(this, o); stopSelf(); return START_NOT_STICKY; }
    play(o.optString("snd")); vibrate();
    h.postDelayed(new Runnable() { public void run() { finish(); } }, Math.max(5, o.optInt("ring", 30)) * 1000L);
    h.postDelayed(new Runnable() { public void run() { watch(); } }, 1500);
    return START_NOT_STICKY;
  }
  void play(String spec) {
    Uri u;
    if (spec.startsWith("raw:")) { int rid = getResources().getIdentifier(spec.substring(4), "raw", getPackageName()); u = rid != 0 ? Uri.parse("android.resource://" + getPackageName() + "/" + rid) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); }
    else if (spec.equals("default_notif")) u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    else if (spec.isEmpty() || spec.equals("default_alarm")) u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    else u = Uri.parse(spec);
    for (int t = 0; t < 2 && mp == null; t++) {
      try {
        MediaPlayer m = new MediaPlayer();
        m.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
        m.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        m.setDataSource(this, u); m.setLooping(true); m.prepare(); m.start(); mp = m;
      } catch (Exception e) { u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); }
    }
  }
  void vibrate() {
    try {
      vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); long[] p = {0, 600, 400};
      if (Build.VERSION.SDK_INT >= 26) vib.vibrate(VibrationEffect.createWaveform(p, 0)); else vib.vibrate(p, 0);
    } catch (Exception e) {}
  }
  void watch() {   // volume button or power button => stop
    if (stopped) return;
    rcv = new BroadcastReceiver() { public void onReceive(Context c, Intent i) { finish(); } };
    IntentFilter f = new IntentFilter(); f.addAction(Intent.ACTION_SCREEN_OFF); f.addAction(Intent.ACTION_SCREEN_ON); f.addAction("android.media.VOLUME_CHANGED_ACTION");
    try { if (Build.VERSION.SDK_INT >= 33) registerReceiver(rcv, f, Context.RECEIVER_EXPORTED); else registerReceiver(rcv, f); } catch (Exception e) {}
  }
  void release() {
    h.removeCallbacksAndMessages(null);
    try { if (mp != null) { mp.stop(); mp.release(); } } catch (Exception e) {}
    mp = null;
    try { if (vib != null) vib.cancel(); } catch (Exception e) {}
    if (rcv != null) { try { unregisterReceiver(rcv); } catch (Exception e) {} rcv = null; }
  }
  void finish() { if (stopped) return; stopped = true; release(); stopForeground(true); stopSelf(); }
  @Override public void onDestroy() { release(); super.onDestroy(); }
}
