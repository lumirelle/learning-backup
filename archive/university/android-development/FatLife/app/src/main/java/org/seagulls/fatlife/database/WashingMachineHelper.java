package org.seagulls.fatlife.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;
import java.util.List;
import org.seagulls.fatlife.entity.WashingMachine;
import org.seagulls.fatlife.util.TestDataUtil;

@SuppressLint("Range")
public class WashingMachineHelper extends BaseHelper {

  public WashingMachineHelper(@Nullable Context context) {
    super(context);
  }

  public WashingMachine selectWashingMachineById(int id) {
    SQLiteDatabase database = getReadableDatabase();
    WashingMachine washingMachine = null;
    try (Cursor cursor = database.rawQuery(
        "SELECT * FROM washing_machine WHERE id = ?",
        new String[]{String.valueOf(id)})) {
      if (cursor.moveToNext()) {
        String name = cursor.getString(cursor.getColumnIndex("name"));
        String shop = cursor.getString(cursor.getColumnIndex("shop"));
        washingMachine = new WashingMachine(id, name, shop);
      }
    }
    return washingMachine;
  }

  public WashingMachine selectDefaultWaitingWashingMachine() {
    SQLiteDatabase database = getReadableDatabase();
    WashingMachine washingMachine = null;
    try (Cursor cursor = database.rawQuery(
        "SELECT * FROM washing_machine LIMIT 1", null)) {
      if (cursor.moveToNext()) {
        int id = cursor.getInt(cursor.getColumnIndex("id"));
        String name = cursor.getString(cursor.getColumnIndex("name"));
        String shop = cursor.getString(cursor.getColumnIndex("shop"));
        washingMachine = new WashingMachine(id, name, shop);
      } else {
        // 如果不存在默认的洗衣机，插入默认数据，并重新获取
        insertTestDataOfWashingMachines();
        washingMachine = selectDefaultWaitingWashingMachine();
      }
    }
    return washingMachine;
  }

  public void insertTestDataOfWashingMachines() {
    List<WashingMachine> washingMachines = TestDataUtil.getWashingMachines();
    SQLiteDatabase database = getWritableDatabase();
    for (WashingMachine washingMachine : washingMachines) {
      ContentValues values = new ContentValues();
      values.put("name", washingMachine.getName());
      values.put("shop", washingMachine.getShop());
      database.insert("washing_machine", null, values);
    }
    database.close();
  }

}
