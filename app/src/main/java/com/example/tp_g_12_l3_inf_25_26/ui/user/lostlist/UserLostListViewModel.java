package com.example.tp_g_12_l3_inf_25_26.ui.user.lostlist;

import android.app.Application;
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

public class UserLostListViewModel extends AndroidViewModel {

    private final DatabaseHelper databaseHelper;
    private final MutableLiveData<List<TableRow>> objectsLiveData = new MutableLiveData<>();
    private String currentFilter = "En attente"; // Filtre par défaut

    public UserLostListViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        loadObjects();
    }

    // Retourne les colonnes du tableau
    public List<ColumnDef> getColumns() {
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));
        columns.add(new ColumnDef("Type", 2, true));
        columns.add(new ColumnDef("Description", 3, false));
        columns.add(new ColumnDef("Date", 2, true));
        return columns;
    }

    // Retourne le LiveData des objets
    public LiveData<List<TableRow>> getObjectsLiveData() {
        return objectsLiveData;
    }

    /**
     * Change le filtre de statut et recharge les données
     * @param status Le statut à filtrer (null pour tous les objets)
     */
    public void filterByStatus(String status) {
        currentFilter = status;
        loadObjects();
    }

    /**
     * Charge tous les objets perdus depuis la base de données
     * Filtre par statut si un filtre est défini
     */
    public void loadObjects() {
        new Thread(() -> {
            List<TableRow> rows = new ArrayList<>();

            Cursor cursor;

            // Si aucun filtre, récupérer tous les objets
            if (currentFilter == null) {
                cursor = databaseHelper.getAllObjets();
            } else {
                // Sinon, filtrer par statut
                cursor = databaseHelper.getObjetsByStatut(currentFilter);
            }

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // Récupérer les indices des colonnes
                    int idIndex = cursor.getColumnIndex("id_objet");
                    int typeIndex = cursor.getColumnIndex("nom_type");
                    int descIndex = cursor.getColumnIndex("description");
                    int dateIndex = cursor.getColumnIndex("date_declaration");
                    int statutIndex = cursor.getColumnIndex("statut");

                    // Vérifier que tous les indices sont valides
                    if (idIndex != -1 && typeIndex != -1 && descIndex != -1 && dateIndex != -1 && statutIndex != -1) {
                        final int id = cursor.getInt(idIndex);
                        final String type = cursor.getString(typeIndex);
                        final String description = cursor.getString(descIndex);
                        final String date = cursor.getString(dateIndex);
                        final String statut = cursor.getString(statutIndex);

                        // Formater la date (prendre seulement la date sans l'heure)
                        final String formattedDate = date.contains(" ") ?
                                date.split(" ")[0] : date;


                        // Créer une TableRow
                        TableRow row = () -> Arrays.asList(
                                String.valueOf(id),
                                type,
                                description,
                                formattedDate,
                                "white"
                        );

                        rows.add(row);
                    }
                }
                cursor.close();
            }

            // Mettre à jour le LiveData sur le thread principal
            objectsLiveData.postValue(rows);
        }).start();
    }

    /**
     * Retourne un code couleur basé sur le statut
     */


    /**
     * Recharger les objets (utile après une mise à jour)
     */
    public void refresh() {
        loadObjects();
    }

    /**
     * Retourne les lignes du tableau (version synchrone pour compatibilité)
     * @deprecated Utiliser getObjectsLiveData() à la place
     */
    @Deprecated
    public List<TableRow> getRows() {
        List<TableRow> current = objectsLiveData.getValue();
        return current != null ? current : new ArrayList<>();
    }
}