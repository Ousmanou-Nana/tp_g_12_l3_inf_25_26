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

public class MyDeclarationViewModel extends AndroidViewModel {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_MATRICULE = "userMatricule";

    private final DatabaseHelper databaseHelper;
    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<List<TableRow>> declarationsLiveData = new MutableLiveData<>();
    private final MutableLiveData<DeleteResult> deleteResultLiveData = new MutableLiveData<>();
    private String currentFilter = null; // null = toutes les déclarations

    public MyDeclarationViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadDeclarations();
    }

    /**
     * Change le filtre de statut et recharge les données
     * @param status Le statut à filtrer (null pour toutes les déclarations)
     */
    public void filterByStatus(String status) {
        currentFilter = status;
        loadDeclarations();
    }

    // Retourne les colonnes du tableau
    public List<ColumnDef> getColumns() {
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));
        columns.add(new ColumnDef("Type", 2, true));
        columns.add(new ColumnDef("Description", 3, false));
        columns.add(new ColumnDef("Date", 2, true));
        columns.add(new ColumnDef("Statut", 2, true));
        return columns;
    }

    // Retourne le LiveData des déclarations
    public LiveData<List<TableRow>> getDeclarationsLiveData() {
        return declarationsLiveData;
    }

    // Retourne le LiveData du résultat de suppression
    public LiveData<DeleteResult> getDeleteResultLiveData() {
        return deleteResultLiveData;
    }

    /**
     * Charge les déclarations de l'utilisateur actuel depuis la base de données
     */
    public void loadDeclarations() {
        new Thread(() -> {
            List<TableRow> rows = new ArrayList<>();

            // Récupérer le matricule de l'utilisateur depuis SharedPreferences
            String matricule = sharedPreferences.getString(KEY_USER_MATRICULE, "");

            if (matricule.isEmpty()) {
                // Aucun utilisateur connecté, retourner une liste vide
                declarationsLiveData.postValue(rows);
                return;
            }

            // Récupérer l'ID utilisateur à partir du matricule
            int userId = getUserIdByMatricule(matricule);
            if (userId == -1) {
                // Utilisateur non trouvé, retourner une liste vide
                declarationsLiveData.postValue(rows);
                return;
            }

            // Récupérer les déclarations de cet utilisateur
            Cursor cursor = databaseHelper.getDeclarationsByUser(userId);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int idIndex = cursor.getColumnIndex("id_declaration");
                    int typeIndex = cursor.getColumnIndex("nom_type");
                    int descIndex = cursor.getColumnIndex("description");
                    int dateIndex = cursor.getColumnIndex("date_declaration");
                    int statutIndex = cursor.getColumnIndex("statut");

                    if (idIndex != -1 && typeIndex != -1 && descIndex != -1
                            && dateIndex != -1 && statutIndex != -1) {

                        final int id = cursor.getInt(idIndex);
                        final String type = cursor.getString(typeIndex);
                        final String description = cursor.getString(descIndex);
                        final String date = cursor.getString(dateIndex);
                        final String statut = cursor.getString(statutIndex);

                        // Appliquer le filtre si défini
                        if (currentFilter != null && !statut.equals(currentFilter)) {
                            continue; // Ignorer cette déclaration
                        }

                        // Formater la date
                        final String formattedDate = date.contains(" ") ?
                                date.split(" ")[0] : date;

                        // Déterminer la couleur en fonction du statut
                        String colorCode = getColorForStatus(statut);

                        // Créer une TableRow
                        TableRow row = () -> Arrays.asList(
                                String.valueOf(id),
                                type,
                                description,
                                formattedDate,
                                statut,
                                colorCode
                        );

                        rows.add(row);
                    }
                }
                cursor.close();
            }

            declarationsLiveData.postValue(rows);
        }).start();
    }

    /**
     * Récupère l'ID utilisateur à partir du matricule
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

    /**
     * Supprime une déclaration
     */
    public void deleteDeclaration(int declarationId) {
        new Thread(() -> {
            try {
                // Supprimer les images associées
                boolean imagesDeleted = databaseHelper.deleteImagesByDeclaration(declarationId);

                // Supprimer la déclaration
                boolean declarationDeleted = databaseHelper.deleteDeclaration(declarationId);

                if (declarationDeleted) {
                    deleteResultLiveData.postValue(
                            new DeleteResult(true, "Déclaration supprimée avec succès")
                    );
                    // Recharger les déclarations
                    loadDeclarations();
                } else {
                    deleteResultLiveData.postValue(
                            new DeleteResult(false, "Erreur lors de la suppression")
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                deleteResultLiveData.postValue(
                        new DeleteResult(false, "Erreur: " + e.getMessage())
                );
            }
        }).start();
    }

    /**
     * Retourne un code couleur basé sur le statut
     */
    private String getColorForStatus(String statut) {
        switch (statut) {
            case "En attente":
                return "yellow";
            case "En cours de vérification":
                return "blue";
            case "Récupéré":
                return "green";
            case "Refusé":
                return "red";
            default:
                return "white";
        }
    }

    /**
     * Rafraîchir les données
     */
    public void refresh() {
        loadDeclarations();
    }

    /**
     * Retourne les lignes du tableau (version synchrone pour compatibilité)
     * @deprecated Utiliser getDeclarationsLiveData() à la place
     */
    @Deprecated
    public List<TableRow> getRows() {
        List<TableRow> current = declarationsLiveData.getValue();
        return current != null ? current : new ArrayList<>();
    }

    /**
     * Classe pour le résultat de suppression
     */
    public static class DeleteResult {
        private final boolean success;
        private final String message;

        public DeleteResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}