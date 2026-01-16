package com.example.tp_g_12_l3_inf_25_26.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Gestion_Objet";
    private static final int DB_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table ADMIN
        String createAdminTable =
                "CREATE TABLE ADMIN (" +
                        "id_admin INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "hashed_password TEXT NOT NULL" +
                        ")";
        db.execSQL(createAdminTable);

        // Table TYPE_OBJET
        String createTypeObjetTable =
                "CREATE TABLE TYPE_OBJET (" +
                        "id_type INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nom_type TEXT NOT NULL UNIQUE" +
                        ")";
        db.execSQL(createTypeObjetTable);

        // Table OBJET
        String createObjetTable =
                "CREATE TABLE OBJET (" +
                        "id_objet INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nom_declarant TEXT NOT NULL," +
                        "telephone TEXT NOT NULL," +
                        "description TEXT NOT NULL," +
                        "id_type INTEGER NOT NULL," +
                        "date_declaration TEXT NOT NULL," +
                        "statut TEXT DEFAULT 'En attente'," +
                        "id_admin INTEGER," +
                        "FOREIGN KEY (id_type) REFERENCES TYPE_OBJET(id_type)," +
                        "FOREIGN KEY (id_admin) REFERENCES ADMIN(id_admin)" +
                        ")";
        db.execSQL(createObjetTable);

        // Table IMAGE
        String createImageTable =
                "CREATE TABLE IMAGE (" +
                        "id_image INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "id_objet INTEGER NOT NULL," +
                        "chemin_image TEXT NOT NULL," +
                        "FOREIGN KEY (id_objet) REFERENCES OBJET(id_objet) ON DELETE CASCADE" +
                        ")";
        db.execSQL(createImageTable);

        // Insérer les types d'objets par défaut
        insertDefaultTypes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS IMAGE");
        db.execSQL("DROP TABLE IF EXISTS OBJET");
        db.execSQL("DROP TABLE IF EXISTS TYPE_OBJET");
        db.execSQL("DROP TABLE IF EXISTS ADMIN");
        onCreate(db);
    }

    private void insertDefaultTypes(SQLiteDatabase db) {
        String[] types = {
                "Portefeuille",
                "Téléphone",
                "Clé",
                "Sac",
                "Carte d'identité",
                "Bijoux",
                "Vêtements",
                "Lunettes",
                "Autre"
        };

        for (String type : types) {
            ContentValues values = new ContentValues();
            values.put("nom_type", type);
            db.insert("TYPE_OBJET", null, values);
        }
    }

    // ==================== ADMIN OPERATIONS ====================

    public boolean insertAdmin(String name, String hashedPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("hashed_password", hashedPassword);
        return db.insert("ADMIN", null, values) != -1;
    }

    public Cursor getAllAdmins() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM ADMIN", null);
    }

    public Cursor getAdminByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM ADMIN WHERE name = ?",
                new String[]{name}
        );
    }

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

    public boolean deleteAdmin(int idAdmin) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                "ADMIN",
                "id_admin = ?",
                new String[]{String.valueOf(idAdmin)}
        ) > 0;
    }

    // ==================== TYPE_OBJET OPERATIONS ====================

    public Cursor getAllTypes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM TYPE_OBJET ORDER BY nom_type", null);
    }

    public boolean insertType(String nomType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nom_type", nomType);
        return db.insert("TYPE_OBJET", null, values) != -1;
    }

    public int getTypeIdByName(String nomType) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id_type FROM TYPE_OBJET WHERE nom_type = ?",
                new String[]{nomType}
        );

        int typeId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id_type");
            if (idIndex != -1) {
                typeId = cursor.getInt(idIndex);
            }
            cursor.close();
        }
        return typeId;
    }

    // ==================== OBJET OPERATIONS ====================

    public long insertObjet(String nomDeclarant, String telephone,
                            String description, int idType,
                            String dateDeclaration, int idAdmin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nom_declarant", nomDeclarant);
        values.put("telephone", telephone);
        values.put("description", description);
        values.put("id_type", idType);
        values.put("date_declaration", dateDeclaration);
        values.put("statut", "En attente");
        values.put("id_admin", idAdmin);
        return db.insert("OBJET", null, values);
    }

    public Cursor getAllObjets() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT o.*, t.nom_type FROM OBJET o " +
                        "INNER JOIN TYPE_OBJET t ON o.id_type = t.id_type " +
                        "ORDER BY o.date_declaration DESC",
                null
        );
    }

    public Cursor getObjetById(int idObjet) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT o.*, t.nom_type FROM OBJET o " +
                        "INNER JOIN TYPE_OBJET t ON o.id_type = t.id_type " +
                        "WHERE o.id_objet = ?",
                new String[]{String.valueOf(idObjet)}
        );
    }

    public Cursor getObjetsByStatut(String statut) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT o.*, t.nom_type FROM OBJET o " +
                        "INNER JOIN TYPE_OBJET t ON o.id_type = t.id_type " +
                        "WHERE o.statut = ? " +
                        "ORDER BY o.date_declaration DESC",
                new String[]{statut}
        );
    }

    public boolean updateObjetStatut(int idObjet, String newStatut) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("statut", newStatut);
        return db.update(
                "OBJET",
                values,
                "id_objet = ?",
                new String[]{String.valueOf(idObjet)}
        ) > 0;
    }

    public boolean deleteObjet(int idObjet) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                "OBJET",
                "id_objet = ?",
                new String[]{String.valueOf(idObjet)}
        ) > 0;
    }

    // ==================== IMAGE OPERATIONS ====================

    public boolean insertImage(int idObjet, String cheminImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_objet", idObjet);
        values.put("chemin_image", cheminImage);
        return db.insert("IMAGE", null, values) != -1;
    }

    public Cursor getImagesByObjet(int idObjet) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM IMAGE WHERE id_objet = ?",
                new String[]{String.valueOf(idObjet)}
        );
    }

    public boolean deleteImage(int idImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                "IMAGE",
                "id_image = ?",
                new String[]{String.valueOf(idImage)}
        ) > 0;
    }

    public boolean deleteImagesByObjet(int idObjet) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                "IMAGE",
                "id_objet = ?",
                new String[]{String.valueOf(idObjet)}
        ) > 0;
    }
}