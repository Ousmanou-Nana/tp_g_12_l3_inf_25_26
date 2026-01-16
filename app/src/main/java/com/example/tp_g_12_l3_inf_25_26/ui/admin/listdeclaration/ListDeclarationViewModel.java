package com.example.tp_g_12_l3_inf_25_26.ui.admin.listdeclaration;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tp_g_12_l3_inf_25_26.DB.DatabaseHelper;
import com.example.tp_g_12_l3_inf_25_26.models.Declaration;
import com.example.tp_g_12_l3_inf_25_26.utils.ColumnDef;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListDeclarationViewModel extends AndroidViewModel {

    private final DatabaseHelper databaseHelper;
    private final MutableLiveData<List<TableRow>> rowsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Declaration>> declarationsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> actionResult = new MutableLiveData<>();

    public ListDeclarationViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        loadDeclarations();
    }

    // Colonnes du tableau
    public List<ColumnDef> getColumns() {
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));
        columns.add(new ColumnDef("Nom", 2, true));
        columns.add(new ColumnDef("Téléphone", 2, false));
        columns.add(new ColumnDef("Matricule", 2, true));
        columns.add(new ColumnDef("Type", 2, true));
        columns.add(new ColumnDef("Description", 3, false));
        columns.add(new ColumnDef("Statut", 2, true));
        columns.add(new ColumnDef("Date", 2, true));
        return columns;
    }

    public LiveData<List<TableRow>> getRowsLiveData() {
        return rowsLiveData;
    }

    public LiveData<List<Declaration>> getDeclarationsLiveData() {
        return declarationsLiveData;
    }

    public LiveData<String> getActionResult() {
        return actionResult;
    }

    // Charger toutes les déclarations
    public void loadDeclarations() {
        new Thread(() -> {
            List<Declaration> declarations = new ArrayList<>();
            List<TableRow> rows = new ArrayList<>();

            Cursor cursor = databaseHelper.getAllDeclarations();

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

    // Charger les déclarations par statut
    public void loadDeclarationsByStatut(String statut) {
        new Thread(() -> {
            List<Declaration> declarations = new ArrayList<>();
            List<TableRow> rows = new ArrayList<>();

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

    // Convertir un Cursor en Declaration
    private Declaration cursorToDeclaration(Cursor cursor) {
        Declaration declaration = new Declaration();

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

        // Charger les images
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

    // Valider une déclaration (mettre en cours de vérification)
    public void validateDeclaration(int declarationId) {
        new Thread(() -> {
            boolean success = databaseHelper.updateDeclarationStatut(declarationId, "En cours de vérification");
            if (success) {
                actionResult.postValue("Déclaration mise en cours de vérification");
                loadDeclarations();
            } else {
                actionResult.postValue("Erreur lors de la mise à jour");
            }
        }).start();
    }

    // Marquer comme récupéré
    public void markAsRecovered(int declarationId) {
        new Thread(() -> {
            boolean success = databaseHelper.updateDeclarationStatut(declarationId, "Récupéré");
            if (success) {
                actionResult.postValue("Objet marqué comme récupéré");
                loadDeclarations();
            } else {
                actionResult.postValue("Erreur lors de la mise à jour");
            }
        }).start();
    }

    // Rejeter une déclaration
    public void rejectDeclaration(int declarationId) {
        new Thread(() -> {
            boolean success = databaseHelper.updateDeclarationStatut(declarationId, "Rejeté");
            if (success) {
                actionResult.postValue("Déclaration rejetée");
                loadDeclarations();
            } else {
                actionResult.postValue("Erreur lors du rejet");
            }
        }).start();
    }

    // Supprimer une déclaration
    public void deleteDeclaration(int declarationId) {
        new Thread(() -> {
            // Supprimer d'abord les images
            databaseHelper.deleteImagesByDeclaration(declarationId);
            // Puis supprimer la déclaration
            boolean success = databaseHelper.deleteDeclaration(declarationId);
            if (success) {
                actionResult.postValue("Déclaration supprimée");
                loadDeclarations();
            } else {
                actionResult.postValue("Erreur lors de la suppression");
            }
        }).start();
    }

    // Obtenir une déclaration par son ID
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