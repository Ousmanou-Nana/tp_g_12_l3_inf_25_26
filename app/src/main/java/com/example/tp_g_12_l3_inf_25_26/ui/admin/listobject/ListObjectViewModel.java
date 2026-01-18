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

    private final DatabaseHelper databaseHelper;

    private final MutableLiveData<List<TableRow>> rowsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Objet>> objetsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Declaration>> potentialMatchesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> actionResult = new MutableLiveData<>();

    // =====  LIVEDATA =====
    /**
     * Liste des déclarations qui sont déjà matchées avec un objet spécifique
     * Utilisé pour afficher les déclarations associées lors du marquage comme récupéré
     */
    private final MutableLiveData<List<Declaration>> matchedDeclarationsLiveData = new MutableLiveData<>();

    private int currentAdminId = 1;

    public ListObjectViewModel(@NonNull Application application) {
        super(application);
        databaseHelper = new DatabaseHelper(application);
        loadObjets();
    }

    public List<ColumnDef> getColumns() {
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));
        columns.add(new ColumnDef("Type", 2, true));
        columns.add(new ColumnDef("Description", 3, false));
        columns.add(new ColumnDef("Statut", 2, true));
        columns.add(new ColumnDef("Date", 2, true));
        return columns;
    }

    // ===== GETTERS =====

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

    // ===== NOUVEAU GETTER =====
    public LiveData<List<Declaration>> getMatchedDeclarationsLiveData() {
        return matchedDeclarationsLiveData;
    }

    public void setCurrentAdminId(int adminId) {
        this.currentAdminId = adminId;
    }

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

    public void loadPotentialMatchingDeclarations(int typeId, int objetId) {
        new Thread(() -> {
            List<Declaration> declarations = new ArrayList<>();

            Cursor cursor = databaseHelper.getPotentialMatchingDeclarations(typeId, objetId);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Declaration declaration = cursorToDeclaration(cursor);
                    declarations.add(declaration);
                }
                cursor.close();
            }

            potentialMatchesLiveData.postValue(declarations);
        }).start();
    }

    // ===== NOUVELLE MÉTHODE =====
    /**
     * Charge les déclarations qui sont déjà matchées avec un objet spécifique
     * C'est différent de loadPotentialMatchingDeclarations qui charge les déclarations
     * potentiellement compatibles. Ici on charge uniquement celles qui ont déjà
     * un matching créé dans la table correspondances.
     *
     * @param objetId ID de l'objet pour lequel on veut les déclarations matchées
     */
    public void loadMatchedDeclarations(int objetId) {
        new Thread(() -> {
            List<Declaration> declarations = new ArrayList<>();

            // Récupère les déclarations qui ont un matching avec cet objet
            Cursor cursor = databaseHelper.getMatchingForObjet(objetId);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Declaration declaration = cursorToDeclaration(cursor);
                    declarations.add(declaration);
                }
                cursor.close();
            }

            // Met à jour le LiveData avec les déclarations matchées
            matchedDeclarationsLiveData.postValue(declarations);
        }).start();
    }

    public void createMatching(int declarationId, int objetId) {
        new Thread(() -> {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            long result = databaseHelper.createMatching(
                    declarationId,
                    objetId,
                    currentAdminId,
                    currentDate
            );

            if (result != -1) {
                actionResult.postValue("Correspondance créée avec succès");
            } else {
                actionResult.postValue("Erreur lors de la création de la correspondance");
            }
        }).start();
    }

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

    // ===== NOUVELLE MÉTHODE =====
    /**
     * Met à jour le statut d'une déclaration
     * Utilisé lorsqu'on marque une déclaration comme récupérée
     * depuis le dialogue de matching lors du marquage de l'objet comme récupéré
     *
     * @param declarationId ID de la déclaration à mettre à jour
     * @param newStatut Nouveau statut (généralement "Récupéré")
     */
    public void updateDeclarationStatut(int declarationId, String newStatut) {
        new Thread(() -> {
            boolean success = databaseHelper.updateDeclarationStatut(declarationId, newStatut);

            if (success) {
                actionResult.postValue("Déclaration mise à jour: " + newStatut);
            } else {
                actionResult.postValue("Erreur lors de la mise à jour de la déclaration");
            }
        }).start();
    }

    public void deleteObjet(int objetId) {
        new Thread(() -> {
            databaseHelper.deleteImagesByObjet(objetId);

            boolean success = databaseHelper.deleteObjet(objetId);

            if (success) {
                actionResult.postValue("Objet supprimé");
                loadObjets();
            } else {
                actionResult.postValue("Erreur lors de la suppression");
            }
        }).start();
    }

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

    private String getStatusColor(String statut) {
        switch (statut) {
            case "En attente":
                return "yellow";
            case "En cours de vérification":
                return "blue";
            case "Récupéré":
                return "green";
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