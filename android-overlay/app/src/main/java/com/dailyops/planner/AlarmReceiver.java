package com.dailyops.planner;
import android.content.BroadcastReceiver; import android.content.Context; import android.content.Intent;
import org.json.JSONObject;

public class AlarmReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context c, Intent i) {
    if ("com.dailyops.FIRE".equals(i.getAction())) {
      String extra = i.getStringExtra("item");
      if (extra != null) { try { Core.show(c, new JSONObject(extra)); } catch (Exception e) {} return; }
      Core.fire(c, i.getIntExtra("slot", 1));
    } else Core.scheduleNext(c);   // boot / app update / time change
  }
}
