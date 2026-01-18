// ============================================================================
// ListDeclarationViewModel.java
// ============================================================================
// ViewModel qui gère la logique métier pour la liste des déclarations d'objets perdus
// Permet de:
// - Afficher toutes les déclarations ou filtrer par statut
// - Valider/rejeter/supprimer des déclarations
// - Créer des correspondances entre déclarations et objets trouvés
// - Charger les objets correspondants potentiels
// ============================================================================

package com.example.tp_g_12_l3_inf_25_26.ui.admin.listdeclaration;

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

public class ListDeclarationViewModel extends AndroidViewModel {

    // ===== DÉPENDANCES =====
    private final DatabaseHelper databaseHelper;

    // ===== LIVEDATA - DONNÉES OBSERVABLES =====

    /**
     * Liste des lignes à afficher dans le tableau (RecyclerView)
     * Chaque TableRow contient les données formatées d'une déclaration
     */
    private final MutableLiveData<List<TableRow>> rowsLiveData = new MutableLiveData<>();

    /**
     * Liste complète des objets Declaration
     * Utilisée pour récupérer les détails complets d'une déclaration
     */
    private final MutableLiveData<List<Declaration>> declarationsLiveData = new MutableLiveData<>();

    /**
     * Liste des objets trouvés qui correspondent potentiellement à une déclaration
     * Basé sur le type d'objet (ex: toutes les clés trouvées pour une déclaration de clés perdues)
     */
    private final MutableLiveData<List<Objet>> potentialMatchesLiveData = new MutableLiveData<>();

    /**
     * Résultat des actions effectuées (validation, rejet, suppression)
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
     * Charge automatiquement toutes les déclarations au démarrage
     */
    public ListDeclarationViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        // Charge toutes les déclarations dès l'initialisation
        loadDeclarations();
    }

    // ===== CONFIGURATION DU TABLEAU =====

    /**
     * Définit les colonnes du tableau de déclarations
     * Chaque colonne a:
     * - Un nom (titre)
     * - Un poids (largeur relative, 1-3)
     * - Une visibilité (true = visible par défaut)
     *
     * @return Liste des définitions de colonnes
     */
    public List<ColumnDef> getColumns() {
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));              // ID de la déclaration
        columns.add(new ColumnDef("Nom", 2, true));             // Nom du déclarant
        columns.add(new ColumnDef("Téléphone", 2, false));      // Téléphone (masqué par défaut)
        columns.add(new ColumnDef("Matricule", 2, true));       // Matricule étudiant
        columns.add(new ColumnDef("Type", 2, true));            // Type d'objet perdu
        columns.add(new ColumnDef("Description", 3, false));    // Description (masquée)
        columns.add(new ColumnDef("Statut", 2, true));          // Statut actuel
        columns.add(new ColumnDef("Date", 2, true));            // Date de déclaration
        return columns;
    }

    // ===== GETTERS POUR LIVEDATA =====

    public LiveData<List<TableRow>> getRowsLiveData() {
        return rowsLiveData;
    }

    public LiveData<List<Declaration>> getDeclarationsLiveData() {
        return declarationsLiveData;
    }

    public LiveData<List<Objet>> getPotentialMatchesLiveData() {
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

    // ===== CHARGEMENT DES DÉCLARATIONS =====

    /**
     * Charge TOUTES les déclarations depuis la base de données
     * Processus:
     * 1. Récupère les déclarations via Cursor
     * 2. Pour chaque déclaration:
     *    - Convertit le Cursor en objet Declaration
     *    - Charge les images associées
     *    - Crée une TableRow pour l'affichage
     * 3. Met à jour les LiveData
     *
     * Cette opération se fait dans un thread séparé pour ne pas bloquer l'UI
     */
    public void loadDeclarations() {
        new Thread(() -> {
            // Listes de stockage temporaires
            List<Declaration> declarations = new ArrayList<>();
            List<TableRow> rows = new ArrayList<>();

            // ===== RÉCUPÉRATION DEPUIS LA BDD =====
            Cursor cursor = databaseHelper.getAllDeclarations();

            if (cursor != null) {
                // Parcourt chaque déclaration
                while (cursor.moveToNext()) {
                    // Convertit le cursor en objet Declaration
                    Declaration declaration = cursorToDeclaration(cursor);
                    declarations.add(declaration);

                    // ===== CRÉATION DE LA LIGNE DU TABLEAU =====
                    // Utilise une référence finale pour l'accès depuis la lambda
                    final Declaration finalDeclaration = declaration;

                    // TableRow est une interface fonctionnelle qui retourne les données
                    TableRow row = () -> {
                        // Détermine la couleur selon le statut
                        String statusColor = getStatusColor(finalDeclaration.getStatut());

                        // Retourne une liste de String représentant chaque cellule
                        return Arrays.asList(
                                String.valueOf(finalDeclaration.getIdDeclaration()),   // Colonne 1: ID
                                finalDeclaration.getUserName(),                         // Colonne 2: Nom
                                finalDeclaration.getUserPhone(),                        // Colonne 3: Tél
                                finalDeclaration.getUserMatricule(),                    // Colonne 4: Matricule
                                finalDeclaration.getNomType(),                          // Colonne 5: Type
                                truncateDescription(finalDeclaration.getDescription()), // Colonne 6: Description tronquée
                                finalDeclaration.getStatut(),                           // Colonne 7: Statut
                                formatDate(finalDeclaration.getDateDeclaration()),      // Colonne 8: Date formatée
                                statusColor                                             // Colonne 9: Couleur de statut
                        );
                    };
                    rows.add(row);
                }
                cursor.close(); // Libère les ressources
            }

            // ===== MISE À JOUR DES LIVEDATA =====
            // postValue() est thread-safe et peut être appelé depuis n'importe quel thread
            declarationsLiveData.postValue(declarations);
            rowsLiveData.postValue(rows);
        }).start();
    }

    /**
     * Charge les déclarations filtrées par un statut spécifique
     *
     * @param statut Statut à filtrer ("En attente", "En cours de vérification", "Récupéré", "Rejeté")
     *
     * Le processus est identique à loadDeclarations() mais avec un filtre
     */
    public void loadDeclarationsByStatut(String statut) {
        new Thread(() -> {
            List<Declaration> declarations = new ArrayList<>();
            List<TableRow> rows = new ArrayList<>();

            // Utilise une méthode de DatabaseHelper qui filtre par statut
            Cursor cursor = databaseHelper.getDeclarationsByStatut(statut);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Declaration declaration = cursorToDeclaration(cursor);
                    declarations.add(declaration);

                    final Declaration finalDeclaration = declaration;
                    TableRow row = () -> {
                        String statusColor = getStatusColor(finalDeclaration.getStatut());
                        return Arrays.asList(
                                String.valueOf(finalDeclaration.getIdDeclaration()),
                                finalDeclaration.getUserName(),
                                finalDeclaration.getUserPhone(),
                                finalDeclaration.getUserMatricule(),
                                finalDeclaration.getNomType(),
                                truncateDescription(finalDeclaration.getDescription()),
                                finalDeclaration.getStatut(),
                                formatDate(finalDeclaration.getDateDeclaration()),
                                statusColor
                        );
                    };
                    rows.add(row);
                }
                cursor.close();
            }

            declarationsLiveData.postValue(declarations);
            rowsLiveData.postValue(rows);
        }).start();
    }

    // ===== CHARGEMENT DES CORRESPONDANCES POTENTIELLES =====

    /**
     * Charge les objets trouvés qui pourraient correspondre à une déclaration
     * Basé sur le type d'objet (même type = correspondance potentielle)
     *
     * Exemple: Si quelqu'un déclare avoir perdu des clés (typeId=3),
     * cette méthode charge toutes les clés trouvées (objets avec typeId=3)
     *
     * @param typeId ID du type d'objet recherché
     * @param declarationId ID de la déclaration (pour éviter les doublons avec objets déjà associés)
     */
    public void loadPotentialMatchingObjets(int typeId, int declarationId) {
        new Thread(() -> {
            List<Objet> objets = new ArrayList<>();

            // Récupère les objets du même type qui ne sont pas déjà associés
            Cursor cursor = databaseHelper.getPotentialMatchingObjets(typeId, declarationId);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // Convertit chaque objet depuis le cursor
                    Objet objet = cursorToObjet(cursor);
                    objets.add(objet);
                }
                cursor.close();
            }

            // Met à jour le LiveData avec les objets correspondants
            potentialMatchesLiveData.postValue(objets);
        }).start();
    }

    // ===== CRÉATION DE CORRESPONDANCE =====

    /**
     * Crée une correspondance (matching) entre une déclaration et un objet trouvé
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
     * Convertit un Cursor de base de données en objet Declaration
     * Récupère toutes les colonnes et charge les images associées
     *
     * @param cursor Cursor positionné sur une ligne de déclaration
     * @return Objet Declaration complet avec toutes ses propriétés
     */
    private Declaration cursorToDeclaration(Cursor cursor) {
        Declaration declaration = new Declaration();

        // ===== RÉCUPÉRATION DES INDEX DES COLONNES =====
        // Chaque index correspond à une position de colonne dans le résultat SQL
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
        // Vérifie que chaque colonne existe avant de lire (évite les erreurs)
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

        // ===== CHARGEMENT DES IMAGES ASSOCIÉES =====
        // Récupère tous les chemins d'images pour cette déclaration
        Cursor imageCursor = databaseHelper.getImagesByDeclaration(declaration.getIdDeclaration());
        if (imageCursor != null) {
            while (imageCursor.moveToNext()) {
                int cheminIndex = imageCursor.getColumnIndex("chemin_image");
                if (cheminIndex != -1) {
                    // Ajoute chaque chemin d'image à la déclaration
                    declaration.addCheminImage(imageCursor.getString(cheminIndex));
                }
            }
            imageCursor.close();
        }

        return declaration;
    }

    /**
     * Convertit un Cursor de base de données en objet Objet (objet trouvé)
     * Similaire à cursorToDeclaration mais pour les objets trouvés
     *
     * @param cursor Cursor positionné sur une ligne d'objet
     * @return Objet complet avec toutes ses propriétés et images
     */
    private Objet cursorToObjet(Cursor cursor) {
        Objet objet = new Objet();

        // ===== RÉCUPÉRATION DES INDEX =====
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
        if (idIndex != -1) objet.setIdObjet(cursor.getInt(idIndex));
        if (nomIndex != -1) objet.setNomDeclarant(cursor.getString(nomIndex));
        if (telIndex != -1) objet.setTelephone(cursor.getString(telIndex));
        if (descIndex != -1) objet.setDescription(cursor.getString(descIndex));
        if (typeIdIndex != -1) objet.setIdType(cursor.getInt(typeIdIndex));
        if (typeNameIndex != -1) objet.setNomType(cursor.getString(typeNameIndex));
        if (dateIndex != -1) objet.setDateDeclaration(cursor.getString(dateIndex));
        if (statutIndex != -1) objet.setStatut(cursor.getString(statutIndex));
        if (adminIdIndex != -1) objet.setIdAdmin(cursor.getInt(adminIdIndex));

        // ===== CHARGEMENT DES IMAGES =====
        Cursor imageCursor = databaseHelper.getImagesByObjet(objet.getIdObjet());
        if (imageCursor != null) {
            while (imageCursor.moveToNext()) {
                int cheminIndex = imageCursor.getColumnIndex("chemin_image");
                if (cheminIndex != -1) {
                    objet.addCheminImage(imageCursor.getString(cheminIndex));
                }
            }
            imageCursor.close();
        }

        return objet;
    }

    // ===== ACTIONS SUR LES DÉCLARATIONS =====

    /**
     * Met une déclaration en "cours de vérification"
     * Utilisé quand un admin commence à traiter une déclaration
     *
     * @param declarationId ID de la déclaration à valider
     */
    public void validateDeclaration(int declarationId) {
        new Thread(() -> {
            // Met à jour le statut dans la base de données
            boolean success = databaseHelper.updateDeclarationStatut(
                    declarationId,
                    "En cours de vérification"
            );

            if (success) {
                actionResult.postValue("Déclaration mise en cours de vérification");
                loadDeclarations(); // Recharge la liste pour afficher le nouveau statut
            } else {
                actionResult.postValue("Erreur lors de la mise à jour");
            }
        }).start();
    }

    /**
     * Marque une déclaration comme "Récupéré"
     * Utilisé quand le propriétaire a récupéré son objet
     *
     * @param declarationId ID de la déclaration
     */
    public void markAsRecovered(int declarationId) {
        new Thread(() -> {
            boolean success = databaseHelper.updateDeclarationStatut(
                    declarationId,
                    "Récupéré"
            );

            if (success) {
                actionResult.postValue("Objet marqué comme récupéré");
                loadDeclarations();
            } else {
                actionResult.postValue("Erreur lors de la mise à jour");
            }
        }).start();
    }

    /**
     * Rejette une déclaration
     * Utilisé quand une déclaration est invalide ou frauduleuse
     *
     * @param declarationId ID de la déclaration à rejeter
     */
    public void rejectDeclaration(int declarationId) {
        new Thread(() -> {
            boolean success = databaseHelper.updateDeclarationStatut(
                    declarationId,
                    "Rejeté"
            );

            if (success) {
                actionResult.postValue("Déclaration rejetée");
                loadDeclarations();
            } else {
                actionResult.postValue("Erreur lors du rejet");
            }
        }).start();
    }

    /**
     * Supprime définitivement une déclaration et ses images
     * ATTENTION: Cette action est irréversible
     *
     * @param declarationId ID de la déclaration à supprimer
     */
    public void deleteDeclaration(int declarationId) {
        new Thread(() -> {
            // Supprime d'abord les images associées
            databaseHelper.deleteImagesByDeclaration(declarationId);

            // Puis supprime la déclaration elle-même
            boolean success = databaseHelper.deleteDeclaration(declarationId);

            if (success) {
                actionResult.postValue("Déclaration supprimée");
                loadDeclarations();
            } else {
                actionResult.postValue("Erreur lors de la suppression");
            }
        }).start();
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Récupère une déclaration spécifique par son ID
     * Cherche dans la liste en mémoire (pas dans la BDD)
     *
     * @param declarationId ID de la déclaration recherchée
     * @return Objet Declaration ou null si introuvable
     */
    public Declaration getDeclarationById(int declarationId) {
        List<Declaration> declarations = declarationsLiveData.getValue();
        if (declarations != null) {
            for (Declaration declaration : declarations) {
                if (declaration.getIdDeclaration() == declarationId) {
                    return declaration;
                }
            }
        }
        return null;
    }

    /**
     * Détermine la couleur d'affichage selon le statut
     * Utilisé pour colorer les lignes du tableau
     *
     * @param statut Statut de la déclaration
     * @return Nom de couleur (yellow, blue, green, red, gray)
     */
    private String getStatusColor(String statut) {
        switch (statut) {
            case "En attente":
                return "yellow";    // Jaune = en attente de traitement
            case "En cours de vérification":
                return "blue";      // Bleu = en cours de traitement
            case "Récupéré":
                return "green";     // Vert = résolu positivement
            case "Rejeté":
                return "red";       // Rouge = rejeté
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
