package com.example.tp_g_12_l3_inf_25_26.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Gestion_Objet";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createAdminTable =
                "CREATE TABLE ADMIN (" +
                        "id_admin INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "hashed_password TEXT NOT NULL" +
                        ")";
        db.execSQL(createAdminTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ADMIN");
        onCreate(db);
    }

    // CREATE
    public boolean insertAdmin(String name, String hashedPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("hashed_password", hashedPassword);
        return db.insert("ADMIN", null, values) != -1;
    }

    // READ ALL
    public Cursor getAllAdmins() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM ADMIN", null);
    }

    // READ ONE
    public Cursor getAdminByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM ADMIN WHERE name = ?",
                new String[]{name}
        );
    }

    // UPDATE
    public boolean updateAdminPassword(int idAdmin, String newHashedPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("hashed_password", newHashedPassword);
        return db.update(
                "ADMIN",
                values,
                "id_admin = ?",
                new String[]{String.valueOf(idAdmin)}
        ) > 0;
    }

    // DELETE
    public boolean deleteAdmin(int idAdmin) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                "ADMIN",
                "id_admin = ?",
                new String[]{String.valueOf(idAdmin)}
        ) > 0;
    }
}
