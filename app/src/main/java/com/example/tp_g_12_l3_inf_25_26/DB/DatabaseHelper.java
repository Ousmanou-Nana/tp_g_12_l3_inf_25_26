package com.example.tp_g_12_l3_inf_25_26.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Gestion_Objet";
    private static final int DB_VERSION = 3; // Incremented version

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

        // Table USER (for registered users)
        String createUserTable =
                "CREATE TABLE USER (" +
                        "id_user INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "phone TEXT NOT NULL," +
                        "matricule TEXT NOT NULL UNIQUE" +
                        ")";
        db.execSQL(createUserTable);

        // Table DECLARATION (declarations made by users)
        String createDeclarationTable =
                "CREATE TABLE DECLARATION (" +
                        "id_declaration INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "id_user INTEGER NOT NULL," +
                        "description TEXT NOT NULL," +
                        "id_type INTEGER NOT NULL," +
                        "date_declaration TEXT NOT NULL," +
                        "statut TEXT DEFAULT 'En attente'," +
                        "id_admin INTEGER," +
                        "FOREIGN KEY (id_user) REFERENCES USER(id_user)," +
                        "FOREIGN KEY (id_type) REFERENCES TYPE_OBJET(id_type)," +
                        "FOREIGN KEY (id_admin) REFERENCES ADMIN(id_admin)" +
                        ")";
        db.execSQL(createDeclarationTable);

        // Table OBJET (kept for compatibility)
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

        // Table IMAGE (updated to support both OBJET and DECLARATION)
        String createImageTable =
                "CREATE TABLE IMAGE (" +
                        "id_image INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "id_objet INTEGER," +
                        "id_declaration INTEGER," +
                        "chemin_image TEXT NOT NULL," +
                        "FOREIGN KEY (id_objet) REFERENCES OBJET(id_objet) ON DELETE CASCADE," +
                        "FOREIGN KEY (id_declaration) REFERENCES DECLARATION(id_declaration) ON DELETE CASCADE" +
                        ")";
        db.execSQL(createImageTable);

        String createMatchingTable =
                "CREATE TABLE MATCHING (" +
                        "id_matching INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "id_declaration INTEGER NOT NULL," +
                        "id_objet INTEGER NOT NULL," +
                        "date_matching TEXT NOT NULL," +
                        "id_admin INTEGER NOT NULL," +
                        "FOREIGN KEY (id_declaration) REFERENCES DECLARATION(id_declaration) ON DELETE CASCADE," +
                        "FOREIGN KEY (id_objet) REFERENCES OBJET(id_objet) ON DELETE CASCADE," +
                        "FOREIGN KEY (id_admin) REFERENCES ADMIN(id_admin)" +
                        ")";
        db.execSQL(createMatchingTable);

        // Insert default types
        insertDefaultTypes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS IMAGE");
        db.execSQL("DROP TABLE IF EXISTS DECLARATION");
        db.execSQL("DROP TABLE IF EXISTS OBJET");
        db.execSQL("DROP TABLE IF EXISTS USER");
        db.execSQL("DROP TABLE IF EXISTS TYPE_OBJET");
        db.execSQL("DROP TABLE IF EXISTS ADMIN");
        db.execSQL("DROP TABLE IF EXISTS MATCHING");
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

    // ==================== USER OPERATIONS ====================

    public long insertUser(String name, String phone, String matricule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        values.put("matricule", matricule);
        return db.insert("USER", null, values);
    }

    public Cursor getUserByMatricule(String matricule) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM USER WHERE matricule = ?",
                new String[]{matricule}
        );
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM USER", null);
    }

    public boolean updateUser(int idUser, String name, String phone, String matricule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        values.put("matricule", matricule);
        return db.update(
                "USER",
                values,
                "id_user = ?",
                new String[]{String.valueOf(idUser)}
        ) > 0;
    }

    // ==================== DECLARATION OPERATIONS ====================

    public long insertDeclaration(int idUser, String description, int idType, String dateDeclaration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_user", idUser);
        values.put("description", description);
        values.put("id_type", idType);
        values.put("date_declaration", dateDeclaration);
        values.put("statut", "En attente");
        return db.insert("DECLARATION", null, values);
    }

    public Cursor getAllDeclarations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT d.*, u.name, u.phone, u.matricule, t.nom_type " +
                        "FROM DECLARATION d " +
                        "INNER JOIN USER u ON d.id_user = u.id_user " +
                        "INNER JOIN TYPE_OBJET t ON d.id_type = t.id_type " +
                        "ORDER BY d.date_declaration DESC",
                null
        );
    }

    public Cursor getDeclarationById(int idDeclaration) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT d.*, u.name, u.phone, u.matricule, t.nom_type " +
                        "FROM DECLARATION d " +
                        "INNER JOIN USER u ON d.id_user = u.id_user " +
                        "INNER JOIN TYPE_OBJET t ON d.id_type = t.id_type " +
                        "WHERE d.id_declaration = ?",
                new String[]{String.valueOf(idDeclaration)}
        );
    }

    public Cursor getDeclarationsByStatut(String statut) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT d.*, u.name, u.phone, u.matricule, t.nom_type " +
                        "FROM DECLARATION d " +
                        "INNER JOIN USER u ON d.id_user = u.id_user " +
                        "INNER JOIN TYPE_OBJET t ON d.id_type = t.id_type " +
                        "WHERE d.statut = ? " +
                        "ORDER BY d.date_declaration DESC",
                new String[]{statut}
        );
    }


    public Cursor getDeclarationsByUser(int idUser) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT d.*, u.name, u.phone, u.matricule, t.nom_type " +
                        "FROM DECLARATION d " +
                        "INNER JOIN USER u ON d.id_user = u.id_user " +
                        "INNER JOIN TYPE_OBJET t ON d.id_type = t.id_type " +
                        "WHERE d.id_user = ? " +
                        "ORDER BY d.date_declaration DESC",
                new String[]{String.valueOf(idUser)}
        );
    }

    public boolean updateDeclarationStatut(int idDeclaration, String newStatut) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("statut", newStatut);
        return db.update(
                "DECLARATION",
                values,
                "id_declaration = ?",
                new String[]{String.valueOf(idDeclaration)}
        ) > 0;
    }

    public boolean deleteDeclaration(int idDeclaration) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                "DECLARATION",
                "id_declaration = ?",
                new String[]{String.valueOf(idDeclaration)}
        ) > 0;
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

    // ==================== OBJET OPERATIONS (kept for compatibility) ====================

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

    public boolean insertImageForDeclaration(int idDeclaration, String cheminImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_declaration", idDeclaration);
        values.put("chemin_image", cheminImage);
        return db.insert("IMAGE", null, values) != -1;
    }

    public boolean insertImage(int idObjet, String cheminImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_objet", idObjet);
        values.put("chemin_image", cheminImage);
        return db.insert("IMAGE", null, values) != -1;
    }

    public Cursor getImagesByDeclaration(int idDeclaration) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM IMAGE WHERE id_declaration = ?",
                new String[]{String.valueOf(idDeclaration)}
        );
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

    public boolean deleteImagesByDeclaration(int idDeclaration) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                "IMAGE",
                "id_declaration = ?",
                new String[]{String.valueOf(idDeclaration)}
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


    public long createMatching(int idDeclaration, int idObjet, int idAdmin, String dateMatching) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_declaration", idDeclaration);
        values.put("id_objet", idObjet);
        values.put("id_admin", idAdmin);
        values.put("date_matching", dateMatching);
        return db.insert("MATCHING", null, values);
    }

    /**
     * Get potential matching objects for a declaration based on type
     */
    public Cursor getPotentialMatchingObjets(int idType, int excludeDeclarationId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT o.*, t.nom_type FROM OBJET o " +
                        "INNER JOIN TYPE_OBJET t ON o.id_type = t.id_type " +
                        "WHERE o.id_type = ? " +
                        "AND o.statut IN ('En attente', 'En cours de vérification') " +
                        "AND o.id_objet NOT IN (" +
                        "  SELECT id_objet FROM MATCHING WHERE id_declaration = ?" +
                        ") " +
                        "ORDER BY o.date_declaration DESC",
                new String[]{String.valueOf(idType), String.valueOf(excludeDeclarationId)}
        );
    }

    /**
     * Get potential matching declarations for an object based on type
     */
    public Cursor getPotentialMatchingDeclarations(int idType, int excludeObjetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT d.*, u.name, u.phone, u.matricule, t.nom_type " +
                        "FROM DECLARATION d " +
                        "INNER JOIN USER u ON d.id_user = u.id_user " +
                        "INNER JOIN TYPE_OBJET t ON d.id_type = t.id_type " +
                        "WHERE d.id_type = ? " +
                        "AND d.statut IN ('En attente', 'En cours de vérification') " +
                        "AND d.id_declaration NOT IN (" +
                        "  SELECT id_declaration FROM MATCHING WHERE id_objet = ?" +
                        ") " +
                        "ORDER BY d.date_declaration DESC",
                new String[]{String.valueOf(idType), String.valueOf(excludeObjetId)}
        );
    }

    /**
     * Get matching for a declaration
     */
    public Cursor getMatchingForDeclaration(int idDeclaration) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT m.*, o.*, t.nom_type " +
                        "FROM MATCHING m " +
                        "INNER JOIN OBJET o ON m.id_objet = o.id_objet " +
                        "INNER JOIN TYPE_OBJET t ON o.id_type = t.id_type " +
                        "WHERE m.id_declaration = ?",
                new String[]{String.valueOf(idDeclaration)}
        );
    }

    /**
     * Get matching for an object
     */
    public Cursor getMatchingForObjet(int idObjet) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT m.*, d.*, u.name, u.phone, u.matricule, t.nom_type " +
                        "FROM MATCHING m " +
                        "INNER JOIN DECLARATION d ON m.id_declaration = d.id_declaration " +
                        "INNER JOIN USER u ON d.id_user = u.id_user " +
                        "INNER JOIN TYPE_OBJET t ON d.id_type = t.id_type " +
                        "WHERE m.id_objet = ?",
                new String[]{String.valueOf(idObjet)}
        );
    }

    /**
     * Check if a matching already exists
     */
    public boolean matchingExists(int idDeclaration, int idObjet) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id_matching FROM MATCHING WHERE id_declaration = ? AND id_objet = ?",
                new String[]{String.valueOf(idDeclaration), String.valueOf(idObjet)}
        );
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    /**
     * Delete a matching
     */
    public boolean deleteMatching(int idMatching) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                "MATCHING",
                "id_matching = ?",
                new String[]{String.valueOf(idMatching)}
        ) > 0;
    }


}