package org.seagulls.fatlife.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.seagulls.fatlife.entity.Order;
import org.seagulls.fatlife.entity.WashingMachine;
import org.seagulls.fatlife.type.WashingType;

@SuppressLint("Range")
public class OrderHelper extends BaseHelper {

  private final WashingMachineHelper washingMachineHelper;

  public OrderHelper(@Nullable Context context) {
    super(context);
    washingMachineHelper = new WashingMachineHelper(context);
  }

  public List<Order> selectOrders() {
    SQLiteDatabase database = getReadableDatabase();
    List<Order> orders;
    try (Cursor cursor = database.rawQuery("SELECT * FROM orders", null)) {
      orders = new ArrayList<>();
      while (cursor.moveToNext()) {
        int id = cursor.getInt(cursor.getColumnIndex("id"));
        String no = cursor.getString(cursor.getColumnIndex("order_no"));
        String makeTime = cursor.getString(cursor.getColumnIndex("make_time"));
        String finishTime = cursor.getString(
            cursor.getColumnIndex("finish_time"));
        String payTime = cursor.getString(cursor.getColumnIndex("pay_time"));
        int washingMachineId = cursor.getInt(
            cursor.getColumnIndex("washing_machine_id"));
        WashingMachine washingMachine = washingMachineHelper.selectWashingMachineById(
            washingMachineId);
        String washingTypeStr = cursor.getString(
            cursor.getColumnIndex("washing_type"));
        WashingType washingType = WashingType.valueOf(washingTypeStr);
        orders.add(
            new Order(id, no, makeTime, finishTime, payTime, washingMachine,
                washingType));
      }
    }
    return orders;
  }

  public Order selectOrderById(int id) {
    SQLiteDatabase database = getReadableDatabase();
    Order order = null;
    try (Cursor cursor = database.rawQuery("SELECT * FROM orders WHERE id = ?",
        new String[]{String.valueOf(id)})) {
      if (cursor.moveToNext()) {
        String no = cursor.getString(cursor.getColumnIndex("order_no"));
        String makeTime = cursor.getString(cursor.getColumnIndex("make_time"));
        String finishTime = cursor.getString(
            cursor.getColumnIndex("finish_time"));
        String payTime = cursor.getString(cursor.getColumnIndex("pay_time"));
        int washingMachineId = cursor.getInt(
            cursor.getColumnIndex("washing_machine_id"));
        WashingMachine washingMachine = washingMachineHelper.selectWashingMachineById(
            washingMachineId);
        String washingTypeStr = cursor.getString(
            cursor.getColumnIndex("washing_type"));
        WashingType washingType = WashingType.valueOf(washingTypeStr);
        order = new Order(id, no, makeTime, finishTime, payTime, washingMachine,
            washingType);
      }
    }
    return order;
  }

  public void insert(Order order) {
    SQLiteDatabase database = getWritableDatabase();
    database.execSQL(
        "INSERT INTO orders (order_no, make_time, finish_time, pay_time, washing_machine_id, washing_type) VALUES (?, ?, ?, ?, ?, ?)",
        new Object[]{order.getNo(), order.getMakeTime(), order.getFinishTime(),
            order.getPayTime(), order.getWashingMachine().getId(),
            order.getWashingType().name()});
    database.close();
  }

  public void update(Order order) {
    SQLiteDatabase database = getWritableDatabase();
    database.execSQL(
        "UPDATE orders SET order_no = ?, make_time = ?, finish_time = ?, pay_time = ?, washing_machine_id = ?, washing_type = ? WHERE id = ?",
        new Object[]{order.getNo(), order.getMakeTime(), order.getFinishTime(),
            order.getPayTime(), order.getWashingMachine().getId(),
            order.getWashingType().name(), order.getId()});
    database.close();
  }

  public void delete(Order order) {
    SQLiteDatabase database = getWritableDatabase();
    database.execSQL("DELETE FROM orders WHERE id = ?",
        new Object[]{order.getId()});
    database.close();
  }

}
