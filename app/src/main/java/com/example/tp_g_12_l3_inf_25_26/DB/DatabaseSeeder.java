package com.example.tp_g_12_l3_inf_25_26.DB;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Classe utilitaire pour initialiser la base de données avec des données de test
 */
public class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";
    private final DatabaseHelper dbHelper;
    private final Context context;

    public DatabaseSeeder(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Initialise la base de données avec des données de démonstration
     * @return true si l'initialisation a réussi
     */
    public boolean seedDatabase() {
        try {
            // Vérifier si la base est déjà initialisée
            if (isDatabaseSeeded()) {
                Log.d(TAG, "Base de données déjà initialisée");
                return true;
            }

            Log.d(TAG, "Début de l'initialisation de la base de données...");

            // 1. Créer des administrateurs
            seedAdmins();

            // 2. Créer des utilisateurs
            seedUsers();

            // 3. Créer des déclarations
            seedDeclarations();

            // 4. Créer des objets trouvés
            seedObjets();

            // 5. Créer des correspondances (matching)
            seedMatchings();

            Log.d(TAG, "Base de données initialisée avec succès !");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation de la base de données", e);
            return false;
        }
    }

    /**
     * Vérifie si la base de données contient déjà des données
     */
    private boolean isDatabaseSeeded() {
        Cursor cursor = dbHelper.getAllAdmins();
        boolean hasData = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return hasData;
    }

    /**
     * Crée des comptes administrateurs de test
     */
    private void seedAdmins() {
        Log.d(TAG, "Création des administrateurs...");

        // Admin principal avec mot de passe haché simple (en production, utiliser BCrypt)
        dbHelper.insertAdmin("admin", hashPassword("admin123"));
        dbHelper.insertAdmin("othman", hashPassword("othman"));
        dbHelper.insertAdmin("gestionnaire", hashPassword("gest123"));

        Log.d(TAG, "3 administrateurs créés");
    }

    /**
     * Crée des utilisateurs de test
     */
    private void seedUsers() {
        Log.d(TAG, "Création des utilisateurs...");

        dbHelper.insertUser("ousmanou nana", "656629344", "22B707FS");
        dbHelper.insertUser("Marie Kouam", "237690112233", "L3INF002");
        dbHelper.insertUser("Paul Tchuente", "237690223344", "L3INF003");
        dbHelper.insertUser("Sophie Mbarga", "237690334455", "L3INF004");
        dbHelper.insertUser("David Ngono", "237690445566", "L3INF005");
        dbHelper.insertUser("Clarisse Ateba", "237690556677", "L3INF006");
        dbHelper.insertUser("Eric Fouda", "237690667788", "L3INF007");
        dbHelper.insertUser("Rachel Ngo Bilong", "237690778899", "L3INF008");

        Log.d(TAG, "8 utilisateurs créés");
    }

    /**
     * Crée des déclarations d'objets perdus
     */
    private void seedDeclarations() {
        Log.d(TAG, "Création des déclarations...");

        String today = getCurrentDate();
        String yesterday = getDateDaysAgo(1);
        String twoDaysAgo = getDateDaysAgo(2);
        String threeDaysAgo = getDateDaysAgo(3);
        String weekAgo = getDateDaysAgo(7);


        long decl1 = dbHelper.insertDeclaration(
                1,
                "Portefeuille noir en cuir avec cartes bancaires et pièce d'identité",
                getTypeId("Portefeuille"),
                yesterday
        );


        long decl2 = dbHelper.insertDeclaration(
                1,
                "iPhone 13 Pro avec coque bleue, écran fissuré en haut à droite",
                getTypeId("Téléphone"),
                twoDaysAgo
        );

        // Déclarations de Paul Tchuente (id_user = 3)
        long decl3 = dbHelper.insertDeclaration(
                3,
                "Trousseau de 5 clés avec porte-clés FC Barcelone",
                getTypeId("Clé"),
                threeDaysAgo
        );

        // Déclarations de Sophie Mbarga (id_user = 4)
        long decl4 = dbHelper.insertDeclaration(
                4,
                "Sac à dos noir Nike avec livres de cours d'informatique",
                getTypeId("Sac"),
                weekAgo
        );

        // Déclarations de David Ngono (id_user = 5)
        long decl5 = dbHelper.insertDeclaration(
                5,
                "Carte nationale d'identité au nom de David Ngono",
                getTypeId("Carte d'identité"),
                today
        );

        // Déclarations de Clarisse Ateba (id_user = 6)
        long decl6 = dbHelper.insertDeclaration(
                6,
                "Montre bracelet argenté marque Casio",
                getTypeId("Bijoux"),
                yesterday
        );

        // Déclarations de Eric Fouda (id_user = 7)
        long decl7 = dbHelper.insertDeclaration(
                1,
                "Veste en jean bleue taille M avec logo Adidas",
                getTypeId("Vêtements"),
                twoDaysAgo
        );

        // Déclarations de Rachel Ngo Bilong (id_user = 8)
        long decl8 = dbHelper.insertDeclaration(
                8,
                "Lunettes de vue monture rectangulaire noire",
                getTypeId("Lunettes"),
                threeDaysAgo
        );

        // Ajouter quelques images fictives aux déclarations
        addDeclarationImages(decl1, 2);
        addDeclarationImages(decl2, 3);
        addDeclarationImages(decl3, 1);
        addDeclarationImages(decl4, 2);

        Log.d(TAG, "8 déclarations créées");
    }

    /**
     * Crée des objets trouvés
     */
    private void seedObjets() {
        Log.d(TAG, "Création des objets trouvés...");

        String today = getCurrentDate();
        String yesterday = getDateDaysAgo(1);
        String twoDaysAgo = getDateDaysAgo(2);
        String threeDaysAgo = getDateDaysAgo(3);
        String fourDaysAgo = getDateDaysAgo(4);

        // Objets trouvés par l'administration
        long obj1 = dbHelper.insertObjet(
                "Agent de sécurité Kamdem",
                "237699887766",
                "Portefeuille marron trouvé dans l'amphithéâtre 500",
                getTypeId("Portefeuille"),
                yesterday,
                1
        );

        long obj2 = dbHelper.insertObjet(
                "Technicienne Ngo Bassong",
                "237699776655",
                "Téléphone Samsung Galaxy trouvé à la bibliothèque",
                getTypeId("Téléphone"),
                twoDaysAgo,
                1
        );

        long obj3 = dbHelper.insertObjet(
                "Agent de nettoyage Mballa",
                "237699665544",
                "Trousseau de clés avec badge d'accès trouvé au parking",
                getTypeId("Clé"),
                threeDaysAgo,
                2
        );

        long obj4 = dbHelper.insertObjet(
                "Secrétaire Ondoa",
                "237699554433",
                "Sac à dos rouge avec ordinateur portable trouvé salle C204",
                getTypeId("Sac"),
                fourDaysAgo,
                2
        );

        long obj5 = dbHelper.insertObjet(
                "Gardien Messi",
                "237699443322",
                "Carte d'étudiant trouvée devant le portail principal",
                getTypeId("Carte d'identité"),
                today,
                1
        );

        long obj6 = dbHelper.insertObjet(
                "Agent Bella",
                "237699332211",
                "Montre homme trouvée aux toilettes du bâtiment A",
                getTypeId("Bijoux"),
                yesterday,
                3
        );

        long obj7 = dbHelper.insertObjet(
                "Surveillant Ekotto",
                "237699221100",
                "Veste noire North Face trouvée au terrain de sport",
                getTypeId("Vêtements"),
                twoDaysAgo,
                3
        );

        long obj8 = dbHelper.insertObjet(
                "Bibliothécaire Eto'o",
                "237699110099",
                "Lunettes de soleil Ray-Ban trouvées en salle de lecture",
                getTypeId("Lunettes"),
                threeDaysAgo,
                2
        );

        // Ajouter quelques images fictives aux objets
        addObjetImages(obj1, 2);
        addObjetImages(obj2, 3);
        addObjetImages(obj3, 1);
        addObjetImages(obj4, 2);
        addObjetImages(obj5, 1);

        Log.d(TAG, "8 objets trouvés créés");
    }

    /**
     * Crée des correspondances entre déclarations et objets
     */
    private void seedMatchings() {
        Log.d(TAG, "Création des correspondances...");

        String today = getCurrentDate();

        // Correspondance 1: Déclaration de portefeuille avec objet portefeuille
        dbHelper.createMatching(1, 1, 1, today);
        dbHelper.updateDeclarationStatut(1, "En cours de vérification");
        dbHelper.updateObjetStatut(1, "En cours de vérification");

        // Correspondance 2: Déclaration de clés avec objet clés
        dbHelper.createMatching(3, 3, 2, today);
        dbHelper.updateDeclarationStatut(3, "En cours de vérification");
        dbHelper.updateObjetStatut(3, "En cours de vérification");

        // Marquer quelques objets comme récupérés
        dbHelper.updateDeclarationStatut(8, "Récupéré");
        dbHelper.updateObjetStatut(8, "Récupéré");

        Log.d(TAG, "2 correspondances créées");
    }

    /**
     * Ajoute des images fictives à une déclaration
     */
    private void addDeclarationImages(long declarationId, int count) {
        for (int i = 1; i <= count; i++) {
            String imagePath = "content://media/declaration_" + declarationId + "_image_" + i + ".jpg";
            dbHelper.insertImageForDeclaration((int) declarationId, imagePath);
        }
    }

    /**
     * Ajoute des images fictives à un objet
     */
    private void addObjetImages(long objetId, int count) {
        for (int i = 1; i <= count; i++) {
            String imagePath = "content://media/objet_" + objetId + "_image_" + i + ".jpg";
            dbHelper.insertImage((int) objetId, imagePath);
        }
    }

    /**
     * Récupère l'ID d'un type d'objet par son nom
     */
    private int getTypeId(String typeName) {
        return dbHelper.getTypeIdByName(typeName);
    }

    /**
     * Retourne la date actuelle au format yyyy-MM-dd
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Retourne une date X jours dans le passé
     */
    private String getDateDaysAgo(int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        long timeInMillis = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
        return sdf.format(new Date(timeInMillis));
    }

    /**
     * Hachage simple de mot de passe (en production, utiliser BCrypt ou similaire)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback (non sécurisé, uniquement pour éviter un crash)
        }
    }

    /**
     * Efface toutes les données de la base (utile pour les tests)
     */
    public void clearDatabase() {
        Log.d(TAG, "Suppression de toutes les données...");
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1);
        Log.d(TAG, "Base de données effacée");
    }

    /**
     * Réinitialise complètement la base de données
     */
    public boolean resetAndSeed() {
        clearDatabase();
        return seedDatabase();
    }

    /**
     * Affiche les statistiques de la base de données
     */
    public void printDatabaseStats() {
        Log.d(TAG, "=== Statistiques de la base de données ===");

        Cursor admins = dbHelper.getAllAdmins();
        Log.d(TAG, "Nombre d'administrateurs: " + (admins != null ? admins.getCount() : 0));
        if (admins != null) admins.close();

        Cursor users = dbHelper.getAllUsers();
        Log.d(TAG, "Nombre d'utilisateurs: " + (users != null ? users.getCount() : 0));
        if (users != null) users.close();

        Cursor declarations = dbHelper.getAllDeclarations();
        Log.d(TAG, "Nombre de déclarations: " + (declarations != null ? declarations.getCount() : 0));
        if (declarations != null) declarations.close();

        Cursor objets = dbHelper.getAllObjets();
        Log.d(TAG, "Nombre d'objets trouvés: " + (objets != null ? objets.getCount() : 0));
        if (objets != null) objets.close();

        Cursor types = dbHelper.getAllTypes();
        Log.d(TAG, "Nombre de types d'objets: " + (types != null ? types.getCount() : 0));
        if (types != null) types.close();

        Log.d(TAG, "==========================================");
    }


}