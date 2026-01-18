// ============================================================================
// ListObjectViewModel.java
// ============================================================================
// ViewModel qui gère la logique métier pour la liste des objets trouvés
// C'est le pendant de ListDeclarationViewModel mais pour les objets trouvés
// Permet de:
// - Afficher tous les objets trouvés ou filtrer par statut
// - Mettre à jour le statut d'un objet
// - Créer des correspondances entre objets trouvés et déclarations d'objets perdus
// - Supprimer des objets
// ============================================================================

package com.example.tp_g_12_l3_inf_25_26.ui.admin.listobject;

import android.app.Application;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;
import com.example.tp_g_12_l3_inf_25_26.models.Declaration;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;
import com.example.tp_g_12_l3_inf_25_26.utils.ColumnDef;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListObjectViewModel extends AndroidViewModel {

    // ===== DÉPENDANCES =====
    private final DatabaseHelper databaseHelper;

    // ===== LIVEDATA - DONNÉES OBSERVABLES =====

    /**
     * Liste des lignes à afficher dans le tableau (RecyclerView)
     * Chaque TableRow contient les données formatées d'un objet trouvé
     */
    private final MutableLiveData<List<TableRow>> rowsLiveData = new MutableLiveData<>();

    /**
     * Liste complète des objets Objet trouvés
     * Utilisée pour récupérer les détails complets d'un objet
     */
    private final MutableLiveData<List<Objet>> objetsLiveData = new MutableLiveData<>();

    /**
     * Liste des déclarations qui correspondent potentiellement à un objet trouvé
     * Basé sur le type d'objet (ex: toutes les déclarations de clés perdues 
     * pour un objet trouvé de type "Clés")
     */
    private final MutableLiveData<List<Declaration>> potentialMatchesLiveData = new MutableLiveData<>();

    /**
     * Résultat des actions effectuées (mise à jour statut, suppression, etc.)
     * Contient un message à afficher à l'utilisateur
     */
    private final MutableLiveData<String> actionResult = new MutableLiveData<>();

    // ===== ÉTAT =====
    /**
     * ID de l'administrateur actuellement connecté
     * Utilisé lors de la création de correspondances (matchings)
     * Valeur par défaut: 1 (devrait être définie depuis la session de connexion)
     */
    private int currentAdminId = 1;

    /**
     * Constructeur du ViewModel
     * Charge automatiquement tous les objets trouvés au démarrage
     */
    public ListObjectViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        // Charge tous les objets dès l'initialisation
        loadObjets();
    }

    // ===== CONFIGURATION DU TABLEAU =====

    /**
     * Définit les colonnes du tableau d'objets trouvés
     * Note: Plus simple que le tableau des déclarations (moins de colonnes)
     *
     * Chaque colonne a:
     * - Un nom (titre)
     * - Un poids (largeur relative, 1-3)
     * - Une visibilité (true = visible par défaut)
     *
     * @return Liste des définitions de colonnes
     */
    public List<ColumnDef> getColumns() {
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));              // ID de l'objet
        columns.add(new ColumnDef("Type", 2, true));            // Type d'objet trouvé
        columns.add(new ColumnDef("Description", 3, false));    // Description (masquée)
        columns.add(new ColumnDef("Statut", 2, true));          // Statut actuel
        columns.add(new ColumnDef("Date", 2, true));            // Date de déclaration
        return columns;
    }

    // ===== GETTERS POUR LIVEDATA =====

    public LiveData<List<TableRow>> getRowsLiveData() {
        return rowsLiveData;
    }

    public LiveData<List<Objet>> getObjetsLiveData() {
        return objetsLiveData;
    }

    public LiveData<List<Declaration>> getPotentialMatchesLiveData() {
        return potentialMatchesLiveData;
    }

    public LiveData<String> getActionResult() {
        return actionResult;
    }

    /**
     * Définit l'ID de l'admin actuellement connecté
     * Devrait être appelé au démarrage du Fragment avec l'ID réel de la session
     */
    public void setCurrentAdminId(int adminId) {
        this.currentAdminId = adminId;
    }

    // ===== CHARGEMENT DES OBJETS TROUVÉS =====

    /**
     * Charge TOUS les objets trouvés depuis la base de données
     *
     * Processus:
     * 1. Récupère les objets via Cursor
     * 2. Pour chaque objet:
     *    - Convertit le Cursor en objet Objet
     *    - Charge les images associées
     *    - Crée une TableRow pour l'affichage
     * 3. Met à jour les LiveData
     *
     * Cette opération se fait dans un thread séparé pour ne pas bloquer l'UI
     */
    public void loadObjets() {
        new Thread(() -> {
            // Listes de stockage temporaires
            List<Objet> objets = new ArrayList<>();
            List<TableRow> rows = new ArrayList<>();

            // ===== RÉCUPÉRATION DEPUIS LA BDD =====
            Cursor cursor = databaseHelper.getAllObjets();

            if (cursor != null) {
                // Parcourt chaque objet trouvé
                while (cursor.moveToNext()) {
                    // Convertit le cursor en objet Objet
                    Objet objet = cursorToObjet(cursor);
                    objets.add(objet);

                    // ===== CRÉATION DE LA LIGNE DU TABLEAU =====
                    // Utilise une référence finale pour l'accès depuis la lambda
                    final Objet finalObjet = objet;

                    // TableRow est une interface fonctionnelle qui retourne les données
                    TableRow row = () -> {
                        // Détermine la couleur selon le statut
                        String statusColor = getStatusColor(finalObjet.getStatut());

                        // Retourne une liste de String représentant chaque cellule
                        return Arrays.asList(
                                String.valueOf(finalObjet.getIdObjet()),           // Colonne 1: ID
                                finalObjet.getNomType(),                            // Colonne 2: Type
                                truncateDescription(finalObjet.getDescription()),   // Colonne 3: Description tronquée
                                finalObjet.getStatut(),                             // Colonne 4: Statut
                                formatDate(finalObjet.getDateDeclaration()),        // Colonne 5: Date formatée
                                statusColor                                         // Colonne 6: Couleur de statut
                        );
                    };
                    rows.add(row);
                }
                cursor.close(); // Libère les ressources
            }

            // ===== MISE À JOUR DES LIVEDATA =====
            // postValue() est thread-safe et peut être appelé depuis n'importe quel thread
            objetsLiveData.postValue(objets);
            rowsLiveData.postValue(rows);
        }).start();
    }

    /**
     * Charge les objets trouvés filtrés par un statut spécifique
     *
     * @param statut Statut à filtrer ("En attente", "En cours de vérification", "Récupéré")
     *
     * Le processus est identique à loadObjets() mais avec un filtre
     */
    public void loadObjetsByStatut(String statut) {
        new Thread(() -> {
            List<Objet> objets = new ArrayList<>();
            List<TableRow> rows = new ArrayList<>();

            // Utilise une méthode de DatabaseHelper qui filtre par statut
            Cursor cursor = databaseHelper.getObjetsByStatut(statut);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Objet objet = cursorToObjet(cursor);
                    objets.add(objet);

                    final Objet finalObjet = objet;
                    TableRow row = () -> {
                        String statusColor = getStatusColor(finalObjet.getStatut());
                        return Arrays.asList(
                                String.valueOf(finalObjet.getIdObjet()),
                                finalObjet.getNomType(),
                                truncateDescription(finalObjet.getDescription()),
                                finalObjet.getStatut(),
                                formatDate(finalObjet.getDateDeclaration()),
                                statusColor
                        );
                    };
                    rows.add(row);
                }
                cursor.close();
            }

            objetsLiveData.postValue(objets);
            rowsLiveData.postValue(rows);
        }).start();
    }

    // ===== CHARGEMENT DES CORRESPONDANCES POTENTIELLES =====

    /**
     * Charge les déclarations qui pourraient correspondre à un objet trouvé
     * C'est l'inverse de loadPotentialMatchingObjets() dans ListDeclarationViewModel
     *
     * Basé sur le type d'objet (même type = correspondance potentielle)
     *
     * Exemple: Si on a trouvé des clés (typeId=3),
     * cette méthode charge toutes les déclarations de clés perdues (typeId=3)
     *
     * @param typeId ID du type d'objet
     * @param objetId ID de l'objet (pour éviter les doublons avec déclarations déjà associées)
     */
    public void loadPotentialMatchingDeclarations(int typeId, int objetId) {
        new Thread(() -> {
            List<Declaration> declarations = new ArrayList<>();

            // Récupère les déclarations du même type qui ne sont pas déjà associées
            Cursor cursor = databaseHelper.getPotentialMatchingDeclarations(typeId, objetId);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // Convertit chaque déclaration depuis le cursor
                    Declaration declaration = cursorToDeclaration(cursor);
                    declarations.add(declaration);
                }
                cursor.close();
            }

            // Met à jour le LiveData avec les déclarations correspondantes
            potentialMatchesLiveData.postValue(declarations);
        }).start();
    }

    // ===== CRÉATION DE CORRESPONDANCE =====

    /**
     * Crée une correspondance (matching) entre un objet trouvé et une déclaration
     * C'est la même méthode que dans ListDeclarationViewModel mais appelée depuis l'objet
     *
     * Cela signifie qu'on pense que l'objet trouvé correspond à l'objet déclaré perdu
     *
     * Enregistre:
     * - L'ID de la déclaration
     * - L'ID de l'objet
     * - L'ID de l'admin qui fait le matching
     * - La date du matching
     *
     * @param declarationId ID de la déclaration d'objet perdu
     * @param objetId ID de l'objet trouvé correspondant
     */
    public void createMatching(int declarationId, int objetId) {
        new Thread(() -> {
            // Génère la date actuelle
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            // Insère le matching dans la base de données
            long result = databaseHelper.createMatching(
                    declarationId,      // Déclaration d'objet perdu
                    objetId,            // Objet trouvé
                    currentAdminId,     // Admin qui crée la correspondance
                    currentDate         // Date du matching
            );

            // Notifie le résultat
            if (result != -1) {
                actionResult.postValue("Correspondance créée avec succès");
            } else {
                actionResult.postValue("Erreur lors de la création de la correspondance");
            }
        }).start();
    }

    // ===== CONVERSION CURSOR → OBJET =====

    /**
     * Convertit un Cursor de base de données en objet Objet (objet trouvé)
     * Récupère toutes les colonnes et charge les images associées
     *
     * @param cursor Cursor positionné sur une ligne d'objet
     * @return Objet complet avec toutes ses propriétés et images
     */
    private Objet cursorToObjet(Cursor cursor) {
        Objet objet = new Objet();

        // ===== RÉCUPÉRATION DES INDEX DES COLONNES =====
        // Chaque index correspond à une position de colonne dans le résultat SQL
        int idIndex = cursor.getColumnIndex("id_objet");
        int nomIndex = cursor.getColumnIndex("nom_declarant");
        int telIndex = cursor.getColumnIndex("telephone");
        int descIndex = cursor.getColumnIndex("description");
        int typeIdIndex = cursor.getColumnIndex("id_type");
        int typeNameIndex = cursor.getColumnIndex("nom_type");
        int dateIndex = cursor.getColumnIndex("date_declaration");
        int statutIndex = cursor.getColumnIndex("statut");
        int adminIdIndex = cursor.getColumnIndex("id_admin");

        // ===== EXTRACTION DES DONNÉES =====
        // Vérifie que chaque colonne existe avant de lire (évite les erreurs)
        if (idIndex != -1) objet.setIdObjet(cursor.getInt(idIndex));
        if (nomIndex != -1) objet.setNomDeclarant(cursor.getString(nomIndex));
        if (telIndex != -1) objet.setTelephone(cursor.getString(telIndex));
        if (descIndex != -1) objet.setDescription(cursor.getString(descIndex));
        if (typeIdIndex != -1) objet.setIdType(cursor.getInt(typeIdIndex));
        if (typeNameIndex != -1) objet.setNomType(cursor.getString(typeNameIndex));
        if (dateIndex != -1) objet.setDateDeclaration(cursor.getString(dateIndex));
        if (statutIndex != -1) objet.setStatut(cursor.getString(statutIndex));
        if (adminIdIndex != -1) objet.setIdAdmin(cursor.getInt(adminIdIndex));

        // ===== CHARGEMENT DES IMAGES ASSOCIÉES =====
        // Récupère tous les chemins d'images pour cet objet
        Cursor imageCursor = databaseHelper.getImagesByObjet(objet.getIdObjet());
        if (imageCursor != null) {
            while (imageCursor.moveToNext()) {
                int cheminIndex = imageCursor.getColumnIndex("chemin_image");
                if (cheminIndex != -1) {
                    // Ajoute chaque chemin d'image à l'objet
                    objet.addCheminImage(imageCursor.getString(cheminIndex));
                }
            }
            imageCursor.close();
        }

        return objet;
    }

    /**
     * Convertit un Cursor de base de données en objet Declaration
     * Similaire à cursorToObjet mais pour les déclarations d'objets perdus
     *
     * @param cursor Cursor positionné sur une ligne de déclaration
     * @return Declaration complète avec toutes ses propriétés et images
     */
    private Declaration cursorToDeclaration(Cursor cursor) {
        Declaration declaration = new Declaration();

        // ===== RÉCUPÉRATION DES INDEX =====
        int idIndex = cursor.getColumnIndex("id_declaration");
        int userIdIndex = cursor.getColumnIndex("id_user");
        int nameIndex = cursor.getColumnIndex("name");
        int phoneIndex = cursor.getColumnIndex("phone");
        int matriculeIndex = cursor.getColumnIndex("matricule");
        int descIndex = cursor.getColumnIndex("description");
        int typeIdIndex = cursor.getColumnIndex("id_type");
        int typeNameIndex = cursor.getColumnIndex("nom_type");
        int dateIndex = cursor.getColumnIndex("date_declaration");
        int statutIndex = cursor.getColumnIndex("statut");
        int adminIdIndex = cursor.getColumnIndex("id_admin");

        // ===== EXTRACTION DES DONNÉES =====
        if (idIndex != -1) declaration.setIdDeclaration(cursor.getInt(idIndex));
        if (userIdIndex != -1) declaration.setIdUser(cursor.getInt(userIdIndex));
        if (nameIndex != -1) declaration.setUserName(cursor.getString(nameIndex));
        if (phoneIndex != -1) declaration.setUserPhone(cursor.getString(phoneIndex));
        if (matriculeIndex != -1) declaration.setUserMatricule(cursor.getString(matriculeIndex));
        if (descIndex != -1) declaration.setDescription(cursor.getString(descIndex));
        if (typeIdIndex != -1) declaration.setIdType(cursor.getInt(typeIdIndex));
        if (typeNameIndex != -1) declaration.setNomType(cursor.getString(typeNameIndex));
        if (dateIndex != -1) declaration.setDateDeclaration(cursor.getString(dateIndex));
        if (statutIndex != -1) declaration.setStatut(cursor.getString(statutIndex));
        if (adminIdIndex != -1) declaration.setIdAdmin(cursor.getInt(adminIdIndex));

        // ===== CHARGEMENT DES IMAGES =====
        Cursor imageCursor = databaseHelper.getImagesByDeclaration(declaration.getIdDeclaration());
        if (imageCursor != null) {
            while (imageCursor.moveToNext()) {
                int cheminIndex = imageCursor.getColumnIndex("chemin_image");
                if (cheminIndex != -1) {
                    declaration.addCheminImage(imageCursor.getString(cheminIndex));
                }
            }
            imageCursor.close();
        }

        return declaration;
    }

    // ===== ACTIONS SUR LES OBJETS =====

    /**
     * Met à jour le statut d'un objet trouvé
     *
     * Statuts possibles:
     * - "En attente" → Nouvellement déclaré
     * - "En cours de vérification" → En traitement (souvent associé à une déclaration)
     * - "Récupéré" → Rendu à son propriétaire
     *
     * @param objetId ID de l'objet à modifier
     * @param newStatut Nouveau statut à appliquer
     */
    public void updateObjetStatut(int objetId, String newStatut) {
        new Thread(() -> {
            // Met à jour le statut dans la base de données
            boolean success = databaseHelper.updateObjetStatut(objetId, newStatut);

            if (success) {
                actionResult.postValue("Statut mis à jour: " + newStatut);
                loadObjets(); // Recharge la liste pour afficher le nouveau statut
            } else {
                actionResult.postValue("Erreur lors de la mise à jour");
            }
        }).start();
    }

    /**
     * Supprime définitivement un objet trouvé et ses images
     * ATTENTION: Cette action est irréversible
     *
     * @param objetId ID de l'objet à supprimer
     */
    public void deleteObjet(int objetId) {
        new Thread(() -> {
            // Supprime d'abord les images associées
            databaseHelper.deleteImagesByObjet(objetId);

            // Puis supprime l'objet lui-même
            boolean success = databaseHelper.deleteObjet(objetId);

            if (success) {
                actionResult.postValue("Objet supprimé");
                loadObjets(); // Recharge la liste
            } else {
                actionResult.postValue("Erreur lors de la suppression");
            }
        }).start();
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Récupère un objet spécifique par son ID
     * Cherche dans la liste en mémoire (pas dans la BDD)
     *
     * @param objetId ID de l'objet recherché
     * @return Objet ou null si introuvable
     */
    public Objet getObjetById(int objetId) {
        List<Objet> objets = objetsLiveData.getValue();
        if (objets != null) {
            for (Objet objet : objets) {
                if (objet.getIdObjet() == objetId) {
                    return objet;
                }
            }
        }
        return null;
    }

    /**
     * Détermine la couleur d'affichage selon le statut
     * Utilisé pour colorer les lignes du tableau
     *
     * Note: Pas de statut "Rejeté" pour les objets trouvés
     * (on ne rejette pas un objet trouvé, on le supprime si invalide)
     *
     * @param statut Statut de l'objet
     * @return Nom de couleur (yellow, blue, green, gray)
     */
    private String getStatusColor(String statut) {
        switch (statut) {
            case "En attente":
                return "yellow";    // Jaune = en attente de traitement
            case "En cours de vérification":
                return "blue";      // Bleu = en cours de traitement
            case "Récupéré":
                return "green";     // Vert = résolu positivement
            default:
                return "gray";      // Gris = statut inconnu
        }
    }

    /**
     * Tronque une description longue pour l'affichage dans le tableau
     * Limite à 50 caractères et ajoute "..." si trop long
     *
     * @param description Description complète
     * @return Description tronquée ou complète si < 50 caractères
     */
    private String truncateDescription(String description) {
        if (description.length() > 50) {
            return description.substring(0, 47) + "...";
        }
        return description;
    }

    /**
     * Formate une date du format BDD (yyyy-MM-dd HH:mm:ss) 
     * au format d'affichage (dd/MM/yyyy)
     *
     * @param date Date au format "2026-01-18 14:30:00"
     * @return Date formatée "18/01/2026" ou date originale si erreur
     */
    private String formatDate(String date) {
        try {
            // Sépare la date et l'heure
            String[] parts = date.split(" ");
            if (parts.length > 0) {
                // Sépare année, mois, jour
                String[] dateParts = parts[0].split("-");
                if (dateParts.length == 3) {
                    // Réorganise en jour/mois/année
                    return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retourne la date originale si le formatage échoue
        return date;
    }

    /**
     * Appelé quand le ViewModel est détruit
     * Permet de libérer les ressources si nécessaire
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Possibilité de fermer la BDD ou libérer d'autres ressources
    }
}
