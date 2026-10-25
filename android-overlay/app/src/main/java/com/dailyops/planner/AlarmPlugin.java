package com.dailyops.planner;
import android.app.AlarmManager; import android.app.PendingIntent;
import android.content.Context; import android.content.Intent;
import android.net.Uri; import android.os.Build; import android.os.PowerManager; import android.provider.Settings;
import com.getcapacitor.JSArray; import com.getcapacitor.JSObject; import com.getcapacitor.Plugin; import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod; import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "DailyAlarm")
public class AlarmPlugin extends Plugin {
  @PluginMethod
  public void setAll(PluginCall call) {
    try {
      JSArray a = call.getArray("items"); Context c = getContext();
      Core.sp(c).edit().putString("items", a.toString()).apply();
      Core.scheduleNext(c);
      JSObject o = new JSObject(); o.put("n", a.length()); call.resolve(o);
    } catch (Exception e) { call.reject(String.valueOf(e.getMessage())); }
  }
  @PluginMethod
  public void test(PluginCall call) {
    try {
      Context c = getContext(); JSObject it = call.getObject("item");
      long at = System.currentTimeMillis() + call.getInt("delay", 4) * 1000L;
      AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
      am.setAlarmClock(new AlarmManager.AlarmClockInfo(at, PendingIntent.getActivity(c, 0, Core.launch(c), PendingIntent.FLAG_IMMUTABLE)), Core.pi(c, 9, it.toString()));
      call.resolve();
    } catch (Exception e) { call.reject(String.valueOf(e.getMessage())); }
  }
  @PluginMethod
  public void status(PluginCall call) {
    Context c = getContext(); JSObject o = new JSObject();
    PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
    AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    o.put("battery", Build.VERSION.SDK_INT < 23 || pm.isIgnoringBatteryOptimizations(c.getPackageName()));
    o.put("exact", Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms());
    call.resolve(o);
  }
  @PluginMethod
  public void battery(PluginCall call) {
    try {
      Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getContext().getPackageName()));
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); getContext().startActivity(i);
    } catch (Exception e) { appSettings(call); return; }
    call.resolve();
  }
  @PluginMethod
  public void appSettings(PluginCall call) {
    try {
      Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); getContext().startActivity(i);
    } catch (Exception e) {}
    call.resolve();
  }
}
