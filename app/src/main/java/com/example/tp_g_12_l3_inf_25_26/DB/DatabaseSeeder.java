package com.example.tp_g_12_l3_inf_25_26.DB;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.tp_g_12_l3_inf_25_26.R;

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

        // Administrateurs avec vrais noms
        dbHelper.insertAdmin("ADAMOU YAYA", hashPassword("ADAMOU"));
        dbHelper.insertAdmin("APISSIDI ABAKALAOU JOSUE", hashPassword("APISSIDI"));
        dbHelper.insertAdmin("MAMADOU NOURIDINE", hashPassword("MAMADOU"));
        dbHelper.insertAdmin("KOUNHAWA WABO ANGE AURELIE", hashPassword("KOUNHAWA"));
        dbHelper.insertAdmin("MOHAMADOU DANDI MOHAMADOU", hashPassword("MOHAMADOU"));
        dbHelper.insertAdmin("MOUHAMMADOU MANSALIOU", hashPassword("MOUHAMMADOU"));
        dbHelper.insertAdmin("OUSMANOU NANA", hashPassword("OUSMANOU"));

        Log.d(TAG, "7 administrateurs créés");
    }

    /**
     * Crée des utilisateurs de test
     */
    private void seedUsers() {
        Log.d(TAG, "Création des utilisateurs...");

        // Utilisateurs avec matricules et téléphones réels
        dbHelper.insertUser("ADAMOU YAYA", "699395946", "23A008FS");
        dbHelper.insertUser("APISSIDI ABAKALAOU JOSUE", "692877603", "23A654FS");
        dbHelper.insertUser("KOUNHAWA WABO ANGE AURELIE", "697248179", "23B290FS");
        dbHelper.insertUser("MAMADOU NOURIDINE", "696196757", "22A786FS");
        dbHelper.insertUser("MOHAMADOU DANDI MOHAMADOU", "693292178", "23B078FS");
        dbHelper.insertUser("MOUHAMMADOU MANSALIOU", "671499634", "23A843FS");
        dbHelper.insertUser("OUSMANOU NANA", "656629344", "22B707FS");

        Log.d(TAG, "7 utilisateurs créés");
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
        String fourDaysAgo = getDateDaysAgo(4);
        String fiveDaysAgo = getDateDaysAgo(5);
        String weekAgo = getDateDaysAgo(7);
        String tenDaysAgo = getDateDaysAgo(10);

        // Déclarations de ADAMOU YAYA (id_user = 1)
        long decl1 = dbHelper.insertDeclaration(
                1,
                "Portefeuille noir en cuir avec cartes bancaires BICEC et pièce d'identité. Perdu près de l'amphithéâtre 500",
                getTypeId("Portefeuille"),
                yesterday
        );
        addDeclarationImagesFromRes(decl1, new int[]{R.drawable.portefeuille1, R.drawable.portefeuille2});

        long decl2 = dbHelper.insertDeclaration(
                1,
                "Clés de voiture Toyota avec porte-clés rouge. Perdues au parking principal",
                getTypeId("Clé"),
                threeDaysAgo
        );
        addDeclarationImagesFromRes(decl2, new int[]{R.drawable.cle1});

        // Déclarations de APISSIDI ABAKALAOU JOSUE (id_user = 2)
        long decl3 = dbHelper.insertDeclaration(
                2,
                "iPhone 14 Pro Max avec coque transparente. Écran fissuré en bas à gauche. Perdu à la bibliothèque",
                getTypeId("Téléphone"),
                twoDaysAgo
        );
        addDeclarationImagesFromRes(decl3, new int[]{R.drawable.telephone1, R.drawable.telephone2, R.drawable.telephone2});

        long decl4 = dbHelper.insertDeclaration(
                2,
                "Lunettes de vue monture noire rectangulaire marque Ray-Ban. Perdues en salle C204",
                getTypeId("Lunettes"),
                fiveDaysAgo
        );
        addDeclarationImagesFromRes(decl4, new int[]{R.drawable.lunettes1, R.drawable.lunettes2});

        // Déclarations de KOUNHAWA WABO ANGE AURELIE (id_user = 3)
        long decl5 = dbHelper.insertDeclaration(
                3,
                "Sac à dos rose Nike contenant cahiers de TP et calculatrice scientifique Casio",
                getTypeId("Sac"),
                weekAgo
        );
        addDeclarationImagesFromRes(decl5, new int[]{R.drawable.sac1, R.drawable.sac2});

        long decl6 = dbHelper.insertDeclaration(
                3,
                "Montre bracelet doré Michael Kors. Perdue aux toilettes du bâtiment B",
                getTypeId("Bijoux"),
                yesterday
        );
        addDeclarationImagesFromRes(decl6, new int[]{R.drawable.bijoux1});

        // Déclarations de MAMADOU NOURIDINE (id_user = 4)
        long decl7 = dbHelper.insertDeclaration(
                4,
                "Carte nationale d'identité camerounaise au nom de MAMADOU NOURIDINE. Perdue devant le restaurant universitaire",
                getTypeId("Carte d'identité"),
                today
        );
        addDeclarationImagesFromRes(decl7, new int[]{R.drawable.carte1});

        long decl8 = dbHelper.insertDeclaration(
                4,
                "Ordinateur portable HP Pavilion 15 pouces avec sac de transport noir",
                getTypeId("Ordinateur"),
                fourDaysAgo
        );
        addDeclarationImagesFromRes(decl8, new int[]{R.drawable.ordinateur1, R.drawable.ordinateur2});

        // Déclarations de MOHAMADOU DANDI MOHAMADOU (id_user = 5)
        long decl9 = dbHelper.insertDeclaration(
                5,
                "Veste en jean bleue Levi's taille L avec badge étudiant dans la poche",
                getTypeId("Vêtements"),
                twoDaysAgo
        );
        addDeclarationImagesFromRes(decl9, new int[]{R.drawable.vetement1});

        long decl10 = dbHelper.insertDeclaration(
                5,
                "Trousseau de 8 clés avec porte-clés FC Barcelone et badge d'accès UY1",
                getTypeId("Clé"),
                threeDaysAgo
        );
        addDeclarationImagesFromRes(decl10, new int[]{R.drawable.cle2, R.drawable.cle3});

        // Déclarations de MOUHAMMADOU MANSALIOU (id_user = 6)
        long decl11 = dbHelper.insertDeclaration(
                6,
                "Casque audio Bluetooth Sony WH-1000XM4 noir dans son étui",
                getTypeId("Électronique"),
                yesterday
        );
        addDeclarationImagesFromRes(decl11, new int[]{R.drawable.electronique1, R.drawable.electronique2});

        long decl12 = dbHelper.insertDeclaration(
                6,
                "Parapluie automatique noir avec manche argenté. Perdu à l'entrée de la faculté",
                getTypeId("Autre"),
                tenDaysAgo
        );
        addDeclarationImagesFromRes(decl12, new int[]{R.drawable.autre1});

        // Déclarations de OUSMANOU NANA (id_user = 7)
        long decl13 = dbHelper.insertDeclaration(
                7,
                "Samsung Galaxy S23 Ultra avec coque défense noire. Perdu au terrain de sport",
                getTypeId("Téléphone"),
                fiveDaysAgo
        );
        addDeclarationImagesFromRes(decl13, new int[]{R.drawable.telephone4, R.drawable.telephone5});

        long decl14 = dbHelper.insertDeclaration(
                7,
                "Sac banane Adidas gris contenant carte étudiante et argent",
                getTypeId("Sac"),
                fourDaysAgo
        );
        addDeclarationImagesFromRes(decl14, new int[]{R.drawable.sac3});

        long decl15 = dbHelper.insertDeclaration(
                7,
                "Livre 'Algorithmes et structures de données' édition Dunod avec annotations",
                getTypeId("Livre"),
                weekAgo
        );
        addDeclarationImagesFromRes(decl15, new int[]{R.drawable.livre1, R.drawable.livre2});

        Log.d(TAG, "15 déclarations créées");
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
        String fiveDaysAgo = getDateDaysAgo(5);
        String weekAgo = getDateDaysAgo(7);
        String tenDaysAgo = getDateDaysAgo(10);

        // Objets trouvés par différents agents
        long obj1 = dbHelper.insertObjet(
                "Agent de sécurité KAMDEM Paul",
                "237699887766",
                "Portefeuille marron en cuir trouvé dans l'amphithéâtre 500, contient plusieurs cartes",
                getTypeId("Portefeuille"),
                yesterday,
                1
        );
        addObjetImagesFromRes(obj1, new int[]{R.drawable.portefeuille3, R.drawable.portefeuille4});

        long obj2 = dbHelper.insertObjet(
                "Technicienne NGO BASSONG Marie",
                "237699776655",
                "Téléphone Samsung Galaxy A54 trouvé à la bibliothèque universitaire, batterie déchargée",
                getTypeId("Téléphone"),
                twoDaysAgo,
                1
        );
        addObjetImagesFromRes(obj2, new int[]{R.drawable.telephone6, R.drawable.telephone7});

        long obj3 = dbHelper.insertObjet(
                "Agent de nettoyage MBALLA Jean",
                "237699665544",
                "Trousseau de 6 clés avec badge d'accès magnétique trouvé au parking visiteurs",
                getTypeId("Clé"),
                threeDaysAgo,
                2
        );
        addObjetImagesFromRes(obj3, new int[]{R.drawable.cle4});

        long obj4 = dbHelper.insertObjet(
                "Secrétaire ONDOA Sophie",
                "237699554433",
                "Sac à dos rouge Puma avec ordinateur portable Dell à l'intérieur, trouvé salle C204",
                getTypeId("Sac"),
                fourDaysAgo,
                2
        );
        addObjetImagesFromRes(obj4, new int[]{R.drawable.sac4, R.drawable.sac5});

        long obj5 = dbHelper.insertObjet(
                "Gardien MESSI André",
                "237699443322",
                "Carte d'étudiant au nom de MAMADOU NOURIDINE trouvée devant le portail principal",
                getTypeId("Carte d'identité"),
                today,
                1
        );
        addObjetImagesFromRes(obj5, new int[]{R.drawable.carte2});

        long obj6 = dbHelper.insertObjet(
                "Agent BELLA Christine",
                "237699332211",
                "Montre homme Casio G-Shock noire trouvée aux toilettes du bâtiment A",
                getTypeId("Bijoux"),
                yesterday,
                3
        );
        addObjetImagesFromRes(obj6, new int[]{R.drawable.bijoux2, R.drawable.bijoux3});

        long obj7 = dbHelper.insertObjet(
                "Surveillant EKOTTO Daniel",
                "237699221100",
                "Veste en cuir noire North Face taille M trouvée au terrain de football",
                getTypeId("Vêtements"),
                twoDaysAgo,
                3
        );
        addObjetImagesFromRes(obj7, new int[]{R.drawable.vetement2});

        long obj8 = dbHelper.insertObjet(
                "Bibliothécaire ETO'O Samuel",
                "237699110099",
                "Lunettes de soleil Ray-Ban Aviator dorées trouvées en salle de lecture",
                getTypeId("Lunettes"),
                threeDaysAgo,
                2
        );
        addObjetImagesFromRes(obj8, new int[]{R.drawable.lunettes3});

        long obj9 = dbHelper.insertObjet(
                "Technicien ABENA Martin",
                "237699001188",
                "Clés USB Kingston 32GB trouvée en salle informatique B301",
                getTypeId("Électronique"),
                fiveDaysAgo,
                1
        );
        addObjetImagesFromRes(obj9, new int[]{R.drawable.electronique3});

        long obj10 = dbHelper.insertObjet(
                "Agent FOMO Rachel",
                "237698990077",
                "iPhone 13 avec coque transparente trouvé au restaurant universitaire",
                getTypeId("Téléphone"),
                twoDaysAgo,
                2
        );
        addObjetImagesFromRes(obj10, new int[]{R.drawable.telephone8, R.drawable.telephone9});

        long obj11 = dbHelper.insertObjet(
                "Gardien SONG Patrick",
                "237698880066",
                "Sac de sport Adidas noir avec tenue de basketball trouvé au gymnase",
                getTypeId("Sac"),
                weekAgo,
                1
        );
        addObjetImagesFromRes(obj11, new int[]{R.drawable.sac6});

        long obj12 = dbHelper.insertObjet(
                "Secrétaire MVONDO Alice",
                "237698770055",
                "Calculatrice scientifique Casio FX-991 trouvée en salle d'examen",
                getTypeId("Électronique"),
                yesterday,
                3
        );
        addObjetImagesFromRes(obj12, new int[]{R.drawable.electronique4});

        long obj13 = dbHelper.insertObjet(
                "Agent EBOLO Francis",
                "237698660044",
                "Cahier de TP Réseaux Informatiques avec nom OUSMANOU NANA sur la couverture",
                getTypeId("Livre"),
                fourDaysAgo,
                2
        );
        addObjetImagesFromRes(obj13, new int[]{R.drawable.livre3});

        long obj14 = dbHelper.insertObjet(
                "Technicienne FOSSO Nadine",
                "237698550033",
                "Parapluie automatique bleu marine trouvé à l'entrée du bâtiment C",
                getTypeId("Autre"),
                tenDaysAgo,
                1
        );
        addObjetImagesFromRes(obj14, new int[]{R.drawable.autre2});

        long obj15 = dbHelper.insertObjet(
                "Surveillant NJOYA Eric",
                "237698440022",
                "Casque audio JBL Bluetooth rouge trouvé à la cafétéria",
                getTypeId("Électronique"),
                yesterday,
                3
        );
        addObjetImagesFromRes(obj15, new int[]{R.drawable.electronique5, R.drawable.electronique6});

        Log.d(TAG, "15 objets trouvés créés");
    }

    /**
     * Crée des correspondances entre déclarations et objets
     */
    private void seedMatchings() {
        Log.d(TAG, "Création des correspondances...");

        String today = getCurrentDate();
        String yesterday = getDateDaysAgo(1);

        // Correspondance 1: Déclaration de portefeuille avec objet portefeuille
        dbHelper.createMatching(1, 1, 1, yesterday);
        dbHelper.updateDeclarationStatut(1, "En cours de vérification");
        dbHelper.updateObjetStatut(1, "En cours de vérification");

        // Correspondance 2: Déclaration de clés avec objet clés
        dbHelper.createMatching(10, 3, 2, yesterday);
        dbHelper.updateDeclarationStatut(10, "En cours de vérification");
        dbHelper.updateObjetStatut(3, "En cours de vérification");

        // Correspondance 3: Carte d'identité
        dbHelper.createMatching(7, 5, 1, today);
        dbHelper.updateDeclarationStatut(7, "Récupéré");
        dbHelper.updateObjetStatut(5, "Récupéré");

        // Correspondance 4: Téléphone
        dbHelper.createMatching(3, 10, 2, yesterday);
        dbHelper.updateDeclarationStatut(3, "En cours de vérification");
        dbHelper.updateObjetStatut(10, "En cours de vérification");

        // Correspondance 5: Lunettes
        dbHelper.createMatching(4, 8, 3, yesterday);
        dbHelper.updateDeclarationStatut(4, "Récupéré");
        dbHelper.updateObjetStatut(8, "Récupéré");

        Log.d(TAG, "5 correspondances créées");
    }

    /**
     * Ajoute des images depuis les ressources drawable à une déclaration
     */
    private void addDeclarationImagesFromRes(long declarationId, int[] drawableIds) {
        for (int i = 0; i < drawableIds.length; i++) {
            // Format: android.resource://[package]/[res id]
            String imagePath = "android.resource://" + context.getPackageName() + "/" + drawableIds[i];
            dbHelper.insertImageForDeclaration((int) declarationId, imagePath);
        }
    }

    /**
     * Ajoute des images depuis les ressources drawable à un objet
     */
    private void addObjetImagesFromRes(long objetId, int[] drawableIds) {
        for (int i = 0; i < drawableIds.length; i++) {
            // Format: android.resource://[package]/[res id]
            String imagePath = "android.resource://" + context.getPackageName() + "/" + drawableIds[i];
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
            return password;
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