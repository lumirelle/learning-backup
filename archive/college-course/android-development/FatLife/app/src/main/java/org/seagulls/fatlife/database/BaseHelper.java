package org.seagulls.fatlife.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public abstract class BaseHelper extends SQLiteOpenHelper {

  protected BaseHelper(@Nullable Context context) {
    super(context, "db.db", null, 2);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    // 创建洗衣机表
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS washing_machine (id INTEGER PRIMARY KEY AUTOINCREMENT, name varchar(255) NOT NULL, shop varchar(255) NOT NULL)");
    // 创建订单表
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY AUTOINCREMENT, order_no varchar(255) NOT NULL, make_time varchar(255) NOT NULL, finish_time varchar(255) NOT NULL, pay_time varchar(255) NOT NULL, washing_machine_id INTEGER NOT NULL, washing_type varchar(255) NOT NULL)");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // 创建洗衣机表
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS washing_machine (id INTEGER PRIMARY KEY AUTOINCREMENT, name varchar(255) NOT NULL, shop varchar(255) NOT NULL)");
    // 创建订单表
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY AUTOINCREMENT, order_no varchar(255) NOT NULL, make_time varchar(255) NOT NULL, finish_time varchar(255) NOT NULL, pay_time varchar(255) NOT NULL, washing_machine_id INTEGER NOT NULL, washing_type varchar(255) NOT NULL)");

  }

}
