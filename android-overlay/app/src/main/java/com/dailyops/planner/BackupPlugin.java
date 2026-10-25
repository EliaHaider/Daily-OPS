package com.dailyops.planner;
import android.app.Activity; import android.content.ContentUris; import android.content.ContentValues; import android.content.Context; import android.content.Intent;
import android.database.Cursor; import android.net.Uri; import android.os.Build; import android.provider.MediaStore;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSObject; import com.getcapacitor.Plugin; import com.getcapacitor.PluginCall; import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback; import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.ByteArrayOutputStream; import java.io.InputStream; import java.io.OutputStream;

@CapacitorPlugin(name = "Backup")
public class BackupPlugin extends Plugin {
  static final String NAME = "dailyops-backup.json", DIR = "Documents/DailyOps/";
  Uri find(Context c) {
    Uri base = MediaStore.Files.getContentUri("external");
    try (Cursor q = c.getContentResolver().query(base, new String[]{MediaStore.MediaColumns._ID},
        MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + MediaStore.MediaColumns.RELATIVE_PATH + " LIKE ?", new String[]{NAME, DIR + "%"}, null)) {
      if (q != null && q.moveToFirst()) return ContentUris.withAppendedId(base, q.getLong(0));
    } catch (Exception e) {}
    return null;
  }
  String read(Context c, Uri u) throws Exception {
    try (InputStream in = c.getContentResolver().openInputStream(u)) {
      ByteArrayOutputStream bo = new ByteArrayOutputStream(); byte[] b = new byte[8192]; int n;
      while ((n = in.read(b)) > 0) bo.write(b, 0, n);
      return bo.toString("UTF-8");
    }
  }
  @PluginMethod
  public void saveBackup(PluginCall call) {
    if (Build.VERSION.SDK_INT < 29) { call.resolve(); return; }
    try {
      Context c = getContext(); Uri u = find(c);
      if (u == null) {
        ContentValues v = new ContentValues();
        v.put(MediaStore.MediaColumns.DISPLAY_NAME, NAME); v.put(MediaStore.MediaColumns.MIME_TYPE, "application/json"); v.put(MediaStore.MediaColumns.RELATIVE_PATH, DIR);
        u = c.getContentResolver().insert(MediaStore.Files.getContentUri("external"), v);
      }
      try (OutputStream os = c.getContentResolver().openOutputStream(u, "wt")) { os.write(call.getString("data", "").getBytes("UTF-8")); }
      call.resolve();
    } catch (Exception e) { call.reject(String.valueOf(e.getMessage())); }
  }
  @PluginMethod
  public void loadBackup(PluginCall call) {
    try {
      Context c = getContext(); Uri u = Build.VERSION.SDK_INT >= 29 ? find(c) : null;
      JSObject o = new JSObject(); o.put("data", u == null ? "" : read(c, u)); call.resolve(o);
    } catch (Exception e) { JSObject o = new JSObject(); o.put("data", ""); call.resolve(o); }
  }
  @PluginMethod
  public void importFile(PluginCall call) {
    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
    startActivityForResult(call, i, "imported");
  }
  @ActivityCallback
  private void imported(PluginCall call, ActivityResult r) {
    if (call == null) return;
    if (r.getResultCode() != Activity.RESULT_OK || r.getData() == null || r.getData().getData() == null) { call.reject("cancel"); return; }
    try { JSObject o = new JSObject(); o.put("data", read(getContext(), r.getData().getData())); call.resolve(o); }
    catch (Exception e) { call.reject(String.valueOf(e.getMessage())); }
  }
}
