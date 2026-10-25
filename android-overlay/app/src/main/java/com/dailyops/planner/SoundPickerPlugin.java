package com.dailyops.planner;
import android.app.Activity; import android.app.NotificationChannel; import android.app.NotificationManager;
import android.content.ContentValues; import android.content.Context; import android.content.Intent;
import android.database.Cursor; import android.media.AudioAttributes; import android.media.RingtoneManager;
import android.net.Uri; import android.os.Build; import android.os.Environment; import android.provider.MediaStore; import android.provider.OpenableColumns;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSObject; import com.getcapacitor.Plugin; import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.ActivityCallback; import com.getcapacitor.annotation.CapacitorPlugin; import com.getcapacitor.PluginMethod;
import java.io.InputStream; import java.io.OutputStream;

@CapacitorPlugin(name = "SoundPicker")
public class SoundPickerPlugin extends Plugin {
  @PluginMethod
  public void pick(PluginCall call) {
    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("audio/*");
    startActivityForResult(call, i, "picked");
  }
  @ActivityCallback
  private void picked(PluginCall call, ActivityResult r) {
    if (call == null) return;
    if (r.getResultCode() != Activity.RESULT_OK || r.getData() == null || r.getData().getData() == null) { call.reject("cancel"); return; }
    try {
      Uri src = r.getData().getData(); Context c = getContext(); String name = "sound.mp3";
      Cursor q = c.getContentResolver().query(src, null, null, null, null);
      if (q != null) { if (q.moveToFirst()) { int ix = q.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (ix >= 0) name = q.getString(ix); } q.close(); }
      Uri out = src;
      if (Build.VERSION.SDK_INT >= 29) {
        ContentValues v = new ContentValues();
        v.put(MediaStore.Audio.Media.DISPLAY_NAME, "dailyops_" + System.currentTimeMillis() + "_" + name);
        v.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        v.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_ALARMS);
        v.put(MediaStore.Audio.Media.IS_ALARM, 1);
        Uri dst = c.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, v);
        try (InputStream in = c.getContentResolver().openInputStream(src); OutputStream os = c.getContentResolver().openOutputStream(dst)) {
          byte[] b = new byte[8192]; int n; while ((n = in.read(b)) > 0) os.write(b, 0, n);
        }
        out = dst;
      } else { try { c.getContentResolver().takePersistableUriPermission(src, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception e) {} }
      JSObject o = new JSObject(); o.put("uri", out.toString()); o.put("name", name); call.resolve(o);
    } catch (Exception e) { call.reject(String.valueOf(e.getMessage())); }
  }
  @PluginMethod
  public void makeChannel(PluginCall call) {
    if (Build.VERSION.SDK_INT < 26) { call.resolve(); return; }
    String id = call.getString("id"), uri = call.getString("uri", "");
    boolean alarm = call.getBoolean("alarm", true);
    NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationChannel ch = new NotificationChannel(id, call.getString("name", "Daily Ops"), call.getInt("importance", 5));
    Uri u = uri.startsWith("raw:") ? Uri.parse("android.resource://" + getContext().getPackageName() + "/raw/" + uri.substring(4)) : uri.equals("default_alarm") ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
          : uri.equals("default_notif") ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(uri);
    ch.setSound(u, new AudioAttributes.Builder().setUsage(alarm ? AudioAttributes.USAGE_ALARM : AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
    ch.enableVibration(true); ch.enableLights(true);
    nm.createNotificationChannel(ch); call.resolve();
  }
}
