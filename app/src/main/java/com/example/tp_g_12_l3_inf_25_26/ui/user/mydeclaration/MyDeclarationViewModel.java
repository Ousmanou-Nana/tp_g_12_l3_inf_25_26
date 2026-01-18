package com.example.tp_g_12_l3_inf_25_26.ui.user.mydeclaration;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;
import com.example.tp_g_12_l3_inf_25_26.utils.ColumnDef;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ViewModel pour la gestion des déclarations personnelles de l'utilisateur.
 *
 * Ce ViewModel gère l'affichage, le filtrage et la suppression des déclarations
 * d'objets perdus créées par l'utilisateur actuellement connecté.
 *
 * Fonctionnalités principales:
 * - Chargement des déclarations de l'utilisateur connecté uniquement
 * - Filtrage par statut (En attente, En vérification, Récupéré, Refusé)
 * - Suppression de déclarations avec gestion des images associées
 * - Codes couleur visuels selon le statut
 * - Communication réactive via LiveData
 *
 * Architecture:
 * - Pattern MVVM pour séparation des préoccupations
 * - SharedPreferences pour identification de l'utilisateur connecté
 * - Threading pour opérations asynchrones
 * - LiveData pour communication réactive
 * - Gestion automatique des ressources associées (images)
 *
 * États de filtrage supportés:
 * - null : Affiche toutes les déclarations de l'utilisateur
 * - "En attente" : Déclarations en attente de traitement
 * - "En cours de vérification" : Déclarations en cours de vérification
 * - "Récupéré" : Objets déjà récupérés
 * - "Refusé" : Déclarations refusées
 *
 * Codes couleur:
 * - Jaune : En attente
 * - Bleu : En cours de vérification
 * - Vert : Récupéré
 * - Rouge : Refusé
 * - Blanc : Statut inconnu
 *
 * @author Votre équipe
 * @version 1.0
 */
public class MyDeclarationViewModel extends AndroidViewModel {

    // ==================== CONSTANTES SHAREDPREFERENCES ====================

    /**
     * Nom du fichier SharedPreferences pour accéder aux données utilisateur.
     * Doit être cohérent avec les autres parties de l'application.
     */
    private static final String PREFS_NAME = "UserPrefs";

    /**
     * Clé pour récupérer le matricule de l'utilisateur connecté.
     * Le matricule sert d'identifiant unique pour charger les déclarations.
     */
    private static final String KEY_USER_MATRICULE = "userMatricule";

    // ==================== ATTRIBUTS ====================

    /**
     * Helper pour accéder à la base de données SQLite.
     * Gère toutes les opérations CRUD sur les déclarations.
     */
    private final DatabaseHelper databaseHelper;

    /**
     * SharedPreferences pour récupérer l'utilisateur connecté.
     * Permet d'identifier quel utilisateur consulte ses déclarations.
     */
    private final SharedPreferences sharedPreferences;

    /**
     * LiveData contenant la liste des déclarations de l'utilisateur.
     *
     * Chaque TableRow contient:
     * - ID de la déclaration
     * - Type d'objet
     * - Description
     * - Date de déclaration
     * - Statut
     * - Code couleur
     */
    private final MutableLiveData<List<TableRow>> declarationsLiveData = new MutableLiveData<>();

    /**
     * LiveData pour le résultat des opérations de suppression.
     * Permet de notifier l'interface du succès ou de l'échec de la suppression.
     */
    private final MutableLiveData<DeleteResult> deleteResultLiveData = new MutableLiveData<>();

    /**
     * Filtre de statut actuellement appliqué.
     *
     * Valeurs possibles:
     * - null : Toutes les déclarations (par défaut)
     * - "En attente"
     * - "En cours de vérification"
     * - "Récupéré"
     * - "Refusé"
     */
    private String currentFilter = null; // null = toutes les déclarations

    // ==================== CONSTRUCTEUR ====================

    /**
     * Constructeur du ViewModel.
     *
     * Initialise les dépendances et charge immédiatement les déclarations
     * de l'utilisateur connecté.
     *
     * @param application Contexte de l'application
     */
    public MyDeclarationViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Chargement initial des déclarations
        loadDeclarations();
    }

    // ==================== FILTRAGE ====================

    /**
     * Change le filtre de statut et recharge les données.
     *
     * Cette méthode permet de filtrer dynamiquement les déclarations affichées
     * selon leur statut. Le rechargement est effectué automatiquement.
     *
     * Comportement:
     * 1. Sauvegarde le nouveau filtre
     * 2. Déclenche le rechargement des déclarations
     * 3. Notifie les observateurs via LiveData
     *
     * Exemples d'utilisation:
     * - filterByStatus(null) : Afficher toutes les déclarations
     * - filterByStatus("En attente") : Afficher uniquement les déclarations en attente
     * - filterByStatus("Récupéré") : Afficher uniquement les objets récupérés
     *
     * @param status Le statut à filtrer (null pour toutes les déclarations)
     */
    public void filterByStatus(String status) {
        currentFilter = status;
        loadDeclarations(); // Rechargement immédiat avec le nouveau filtre
    }

    // ==================== CONFIGURATION DES COLONNES ====================

    /**
     * Retourne la définition des colonnes du tableau.
     *
     * Configuration du tableau:
     * - N° : Numéro de la déclaration (poids: 1, centré)
     * - Type : Type d'objet (poids: 2, centré)
     * - Description : Description détaillée (poids: 3, alignée à gauche)
     * - Date : Date de déclaration (poids: 2, centré)
     * - Statut : État de la déclaration (poids: 2, centré)
     *
     * Note: Cette configuration inclut une colonne "Statut" supplémentaire
     * par rapport à UserLostListViewModel car l'utilisateur doit voir
     * l'état de ses propres déclarations.
     *
     * @return Liste des définitions de colonnes pour le tableau
     */
    public List<ColumnDef> getColumns() {
        List<ColumnDef> columns = new ArrayList<>();

        // Colonne 1: Numéro (ID de la déclaration)
        columns.add(new ColumnDef("N°", 1, true));

        // Colonne 2: Type d'objet
        columns.add(new ColumnDef("Type", 2, true));

        // Colonne 3: Description (plus large car contenu variable)
        columns.add(new ColumnDef("Description", 3, false));

        // Colonne 4: Date de déclaration
        columns.add(new ColumnDef("Date", 2, true));

        // Colonne 5: Statut (spécifique aux déclarations personnelles)
        columns.add(new ColumnDef("Statut", 2, true));

        return columns;
    }

    // ==================== GETTERS ====================

    /**
     * Retourne le LiveData des déclarations de l'utilisateur.
     *
     * Le fragment observe ce LiveData pour:
     * - Recevoir les mises à jour de la liste
     * - Mettre à jour automatiquement le RecyclerView
     * - Réagir aux changements de filtre
     * - Mettre à jour après suppression
     *
     * @return LiveData observable contenant la liste des déclarations formatées
     */
    public LiveData<List<TableRow>> getDeclarationsLiveData() {
        return declarationsLiveData;
    }

    /**
     * Retourne le LiveData du résultat de suppression.
     *
     * Le fragment observe ce LiveData pour:
     * - Afficher un message de confirmation/erreur
     * - Rafraîchir l'interface après suppression réussie
     * - Gérer les erreurs de suppression
     *
     * @return LiveData observable contenant le résultat de suppression
     */
    public LiveData<DeleteResult> getDeleteResultLiveData() {
        return deleteResultLiveData;
    }

    // ==================== CHARGEMENT DES DONNÉES ====================

    /**
     * Charge les déclarations de l'utilisateur actuel depuis la base de données.
     *
     * Cette méthode effectue les opérations suivantes:
     * 1. Récupération du matricule depuis SharedPreferences
     * 2. Résolution du matricule en ID utilisateur
     * 3. Requête des déclarations de cet utilisateur
     * 4. Application du filtre de statut si défini
     * 5. Formatage des données en TableRow
     * 6. Attribution de codes couleur selon le statut
     * 7. Mise à jour du LiveData
     *
     * SÉCURITÉ:
     * Seules les déclarations de l'utilisateur connecté sont chargées.
     * Le matricule stocké dans SharedPreferences sert de clé d'identification.
     *
     * FILTRAGE:
     * - Si currentFilter == null : Toutes les déclarations
     * - Sinon : Filtrage côté client après récupération
     *
     * FORMAT DES DONNÉES:
     * Chaque ligne du tableau contient:
     * - id_declaration : Numéro de la déclaration
     * - nom_type : Type de l'objet
     * - description : Description détaillée
     * - date : Date formatée (YYYY-MM-DD)
     * - statut : État de la déclaration
     * - colorCode : Code couleur pour affichage visuel
     *
     * THREADING:
     * Exécuté dans un thread séparé pour ne pas bloquer l'UI.
     * Utilise postValue() pour mise à jour thread-safe du LiveData.
     */
    public void loadDeclarations() {
        // Exécution dans un thread séparé
        new Thread(() -> {
            List<TableRow> rows = new ArrayList<>();

            // ==========  IDENTIFICATION DE L'UTILISATEUR ==========

            // Récupération du matricule de l'utilisateur depuis SharedPreferences
            String matricule = sharedPreferences.getString(KEY_USER_MATRICULE, "");

            // Vérification: utilisateur connecté ?
            if (matricule.isEmpty()) {
                // Aucun utilisateur connecté, retourner une liste vide
                // Cela peut arriver si l'utilisateur n'est pas authentifié
                declarationsLiveData.postValue(rows);
                return;
            }

            // ========== RÉSOLUTION DU MATRICULE EN ID ==========

            // Récupération de l'ID utilisateur à partir du matricule
            int userId = getUserIdByMatricule(matricule);
            if (userId == -1) {
                // Utilisateur non trouvé dans la base de données
                // Situation anormale: matricule en cache mais pas en BD
                declarationsLiveData.postValue(rows);
                return;
            }

            // ==========  RÉCUPÉRATION DES DÉCLARATIONS ==========

            // Récupération des déclarations de cet utilisateur uniquement
            Cursor cursor = databaseHelper.getDeclarationsByUser(userId);

            if (cursor != null) {
                // Parcours de toutes les déclarations de l'utilisateur
                while (cursor.moveToNext()) {

                    // Récupération sécurisée des indices de colonnes
                    int idIndex = cursor.getColumnIndex("id_declaration");
                    int typeIndex = cursor.getColumnIndex("nom_type");
                    int descIndex = cursor.getColumnIndex("description");
                    int dateIndex = cursor.getColumnIndex("date_declaration");
                    int statutIndex = cursor.getColumnIndex("statut");

                    // Vérification de la validité des indices
                    if (idIndex != -1 && typeIndex != -1 && descIndex != -1
                            && dateIndex != -1 && statutIndex != -1) {

                        // Extraction des données
                        final int id = cursor.getInt(idIndex);
                        final String type = cursor.getString(typeIndex);
                        final String description = cursor.getString(descIndex);
                        final String date = cursor.getString(dateIndex);
                        final String statut = cursor.getString(statutIndex);

                        // ==========APPLICATION DU FILTRE ==========

                        // Appliquer le filtre si défini
                        // Filtrage côté client après récupération
                        if (currentFilter != null && !statut.equals(currentFilter)) {
                            continue; // Ignorer cette déclaration
                        }

                        // ==========  FORMATAGE DE LA DATE ==========

                        // Extraction de la partie date seulement
                        final String formattedDate = date.contains(" ") ?
                                date.split(" ")[0] : date;

                        // ==========ATTRIBUTION DE LA COULEUR ==========

                        // Déterminer la couleur en fonction du statut
                        // Améliore l'expérience visuelle pour l'utilisateur
                        String colorCode = getColorForStatus(statut);

                        // ==========  CRÉATION DE LA TABLEROW ==========

                        /**
                         * TableRow pour cette déclaration.
                         *
                         * Format: [ID, Type, Description, Date, Statut, Couleur]
                         *
                         * La colonne supplémentaire "Statut" permet à l'utilisateur
                         * de voir l'état de ses déclarations.
                         */
                        TableRow row = () -> Arrays.asList(
                                String.valueOf(id),    // Colonne 1: N°
                                type,                  // Colonne 2: Type
                                description,           // Colonne 3: Description
                                formattedDate,         // Colonne 4: Date
                                statut,                // Colonne 5: Statut
                                colorCode              // Colonne 6: Couleur de fond
                        );

                        rows.add(row);
                    }
                }

                // Fermeture du cursor
                cursor.close();
            }

            // ==========  MISE À JOUR DU LIVEDATA ==========

            // Publication des résultats sur le thread principal
            declarationsLiveData.postValue(rows);

        }).start(); // Démarrage du thread
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Récupère l'ID utilisateur à partir du matricule.
     *
     * Cette méthode effectue une requête synchrone à la base de données
     * pour résoudre le matricule en ID utilisateur.
     *
     * Processus:
     * 1. Requête à la BD avec le matricule
     * 2. Extraction de l'ID si trouvé
     * 3. Fermeture du cursor
     * 4. Retour de l'ID ou -1 si non trouvé
     *
     * @param matricule Matricule de l'utilisateur
     * @return ID de l'utilisateur, ou -1 si non trouvé
     */
    private int getUserIdByMatricule(String matricule) {
        Cursor cursor = databaseHelper.getUserByMatricule(matricule);
        int userId = -1;

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id_user");
            if (idIndex != -1) {
                userId = cursor.getInt(idIndex);
            }
            cursor.close();
        }

        return userId;
    }

    // ==================== SUPPRESSION ====================

    /**
     * Supprime une déclaration et toutes ses données associées.
     *
     * Cette méthode effectue les opérations suivantes:
     * 1. Suppression des images associées à la déclaration
     * 2. Suppression de la déclaration elle-même
     * 3. Notification du résultat via LiveData
     * 4. Rechargement automatique de la liste si succès
     *
     * GESTION DES RESSOURCES:
     * Les images liées à la déclaration sont supprimées en premier
     * pour maintenir l'intégrité référentielle de la base de données.
     *
     * SÉCURITÉ:
     * Aucune vérification de propriété n'est effectuée ici.
     * L'appelant (fragment) doit s'assurer que l'utilisateur a le droit
     * de supprimer cette déclaration.
     *
     * THREADING:
     * Exécuté dans un thread séparé car implique des opérations BD.
     *
     * RECHARGEMENT:
     * En cas de succès, loadDeclarations() est appelé automatiquement
     * pour mettre à jour la liste affichée.
     *
     * @param declarationId ID de la déclaration à supprimer
     */
    public void deleteDeclaration(int declarationId) {
        // Exécution dans un thread séparé
        new Thread(() -> {
            try {
                // ========== ÉTAPE 1: SUPPRESSION DES IMAGES ==========

                // Suppression de toutes les images associées
                // Retourne true si succès ou si aucune image
                boolean imagesDeleted = databaseHelper.deleteImagesByDeclaration(declarationId);

                // ========== ÉTAPE 2: SUPPRESSION DE LA DÉCLARATION ==========

                // Suppression de l'enregistrement de la déclaration
                boolean declarationDeleted = databaseHelper.deleteDeclaration(declarationId);

                if (declarationDeleted) {
                    // ✅ SUCCÈS: Déclaration supprimée

                    // Notification du succès
                    deleteResultLiveData.postValue(
                            new DeleteResult(true, "Déclaration supprimée avec succès")
                    );

                    // Rechargement automatique de la liste
                    // Met à jour l'interface pour refléter la suppression
                    loadDeclarations();
                } else {
                    // ❌ ÉCHEC: Erreur lors de la suppression

                    deleteResultLiveData.postValue(
                            new DeleteResult(false, "Erreur lors de la suppression")
                    );
                }

            } catch (Exception e) {
                // ❌ EXCEPTION: Erreur inattendue

                e.printStackTrace();
                deleteResultLiveData.postValue(
                        new DeleteResult(false, "Erreur: " + e.getMessage())
                );
            }
        }).start(); // Démarrage du thread
    }

    /**
     * Retourne un code couleur basé sur le statut.
     *
     * Cette méthode mappe chaque statut à un code couleur pour
     * améliorer la lisibilité visuelle du tableau.
     *
     * Mapping des couleurs:
     * - "En attente" → Jaune (attention requise)
     * - "En cours de vérification" → Bleu (en traitement)
     * - "Récupéré" → Vert (succès)
     * - "Refusé" → Rouge (rejeté)
     * - Autre → Blanc (par défaut)
     *
     * Ces codes couleur sont interprétés par le TableAdapter
     * pour appliquer des styles visuels aux lignes.
     *
     * @param statut Le statut de la déclaration
     * @return Code couleur (chaîne de caractères)
     */
    private String getColorForStatus(String statut) {
        switch (statut) {
            case "En attente":
                return "yellow";  // Jaune - Attention requise

            case "En cours de vérification":
                return "blue";    // Bleu - En traitement

            case "Récupéré":
                return "green";   // Vert - Succès

            case "Refusé":
                return "red";     // Rouge - Rejeté

            default:
                return "white";   // Blanc - Par défaut
        }
    }

    /**
     * Rafraîchit les données en rechargeant les déclarations.
     *
     * Utilisations:
     * - Rafraîchir après une modification externe
     * - Actualiser périodiquement les données
     * - Réagir à un événement (notification, etc.)
     *
     * Cette méthode est un alias de loadDeclarations() pour
     * plus de clarté dans le code appelant.
     */
    public void refresh() {
        loadDeclarations();
    }

    /**
     * Retourne les lignes du tableau (version synchrone).
     *
     * @deprecated Cette méthode est conservée pour compatibilité ascendante
     *             mais ne devrait plus être utilisée. Utilisez
     *             getDeclarationsLiveData() à la place pour bénéficier
     *             de la réactivité de LiveData.
     *
     * Limitations:
     * - Ne garantit pas que les données sont à jour
     * - Retourne les dernières données chargées
     * - Peut retourner null si aucune donnée n'a été chargée
     *
     * @return Liste actuelle des déclarations, ou liste vide
     */
    @Deprecated
    public List<TableRow> getRows() {
        List<TableRow> current = declarationsLiveData.getValue();
        return current != null ? current : new ArrayList<>();
    }

    // ==================== CLASSE INTERNE ====================

    /**
     * Classe conteneur pour le résultat d'une opération de suppression.
     *
     * Encapsule:
     * - Le statut de succès/échec
     * - Un message descriptif pour l'utilisateur
     *
     * Utilisée pour communiquer les résultats de suppression via LiveData.
     */
    public static class DeleteResult {
        /**
         * Indique si la suppression a réussi
         */
        private final boolean success;

        /**
         * Message descriptif du résultat
         */
        private final String message;

        /**
         * Constructeur.
         *
         * @param success true si la suppression a réussi
         * @param message Message descriptif du résultat
         */
        public DeleteResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        /**
         * @return true si la suppression a réussi
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * @return Message descriptif du résultat
         */
        public String getMessage() {
            return message;
        }
    }


}