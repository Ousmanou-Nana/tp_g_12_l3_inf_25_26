package com.example.tp_g_12_l3_inf_25_26.ui.admin.listobject;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;
import com.example.tp_g_12_l3_inf_25_26.utils.ColumnDef;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListObjectViewModel extends AndroidViewModel {

    private final DatabaseHelper databaseHelper;
    private final MutableLiveData<List<TableRow>> rowsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Objet>> objetsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> actionResult = new MutableLiveData<>();

    public ListObjectViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        loadObjets();
    }

    // Colonnes du tableau
    public List<ColumnDef> getColumns() {
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));
        columns.add(new ColumnDef("Type", 2, true));
        columns.add(new ColumnDef("Description", 3, false));
        columns.add(new ColumnDef("Statut", 2, true));
        columns.add(new ColumnDef("Date", 2, true));
        return columns;
    }

    public LiveData<List<TableRow>> getRowsLiveData() {
        return rowsLiveData;
    }

    public LiveData<List<Objet>> getObjetsLiveData() {
        return objetsLiveData;
    }

    public LiveData<String> getActionResult() {
        return actionResult;
    }

    // Charger tous les objets
    public void loadObjets() {
        new Thread(() -> {
            List<Objet> objets = new ArrayList<>();
            List<TableRow> rows = new ArrayList<>();

            Cursor cursor = databaseHelper.getAllObjets();

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

    // Charger les objets par statut
    public void loadObjetsByStatut(String statut) {
        new Thread(() -> {
            List<Objet> objets = new ArrayList<>();
            List<TableRow> rows = new ArrayList<>();

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

    // Convertir un Cursor en Objet
    private Objet cursorToObjet(Cursor cursor) {
        Objet objet = new Objet();

        int idIndex = cursor.getColumnIndex("id_objet");
        int nomIndex = cursor.getColumnIndex("nom_declarant");
        int telIndex = cursor.getColumnIndex("telephone");
        int descIndex = cursor.getColumnIndex("description");
        int typeIdIndex = cursor.getColumnIndex("id_type");
        int typeNameIndex = cursor.getColumnIndex("nom_type");
        int dateIndex = cursor.getColumnIndex("date_declaration");
        int statutIndex = cursor.getColumnIndex("statut");
        int adminIdIndex = cursor.getColumnIndex("id_admin");

        if (idIndex != -1) objet.setIdObjet(cursor.getInt(idIndex));
        if (nomIndex != -1) objet.setNomDeclarant(cursor.getString(nomIndex));
        if (telIndex != -1) objet.setTelephone(cursor.getString(telIndex));
        if (descIndex != -1) objet.setDescription(cursor.getString(descIndex));
        if (typeIdIndex != -1) objet.setIdType(cursor.getInt(typeIdIndex));
        if (typeNameIndex != -1) objet.setNomType(cursor.getString(typeNameIndex));
        if (dateIndex != -1) objet.setDateDeclaration(cursor.getString(dateIndex));
        if (statutIndex != -1) objet.setStatut(cursor.getString(statutIndex));
        if (adminIdIndex != -1) objet.setIdAdmin(cursor.getInt(adminIdIndex));

        // Charger les images
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

    // Mettre à jour le statut d'un objet
    public void updateObjetStatut(int objetId, String newStatut) {
        new Thread(() -> {
            boolean success = databaseHelper.updateObjetStatut(objetId, newStatut);
            if (success) {
                actionResult.postValue("Statut mis à jour: " + newStatut);
                loadObjets();
            } else {
                actionResult.postValue("Erreur lors de la mise à jour");
            }
        }).start();
    }

    // Supprimer un objet
    public void deleteObjet(int objetId) {
        new Thread(() -> {
            // Supprimer d'abord les images
            databaseHelper.deleteImagesByObjet(objetId);
            // Puis supprimer l'objet
            boolean success = databaseHelper.deleteObjet(objetId);
            if (success) {
                actionResult.postValue("Objet supprimé");
                loadObjets();
            } else {
                actionResult.postValue("Erreur lors de la suppression");
            }
        }).start();
    }

    // Obtenir un objet par son ID
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

    // Utilitaires
    private String getStatusColor(String statut) {
        switch (statut) {
            case "En attente":
                return "yellow";
            case "En cours de vérification":
                return "orange";
            case "Récupéré":
                return "green";
            case "Rejeté":
                return "red";
            default:
                return "gray";
        }
    }

    private String truncateDescription(String description) {
        if (description.length() > 50) {
            return description.substring(0, 47) + "...";
        }
        return description;
    }

    private String formatDate(String date) {
        try {
            String[] parts = date.split(" ");
            if (parts.length > 0) {
                String[] dateParts = parts[0].split("-");
                if (dateParts.length == 3) {
                    return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}