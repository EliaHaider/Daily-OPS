package com.dailyops.planner;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
public class MainActivity extends BridgeActivity {
  @Override public void onCreate(Bundle s) { registerPlugin(SoundPickerPlugin.class); registerPlugin(AlarmPlugin.class); registerPlugin(BackupPlugin.class); super.onCreate(s); }
}
