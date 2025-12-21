package com.example.tp_g_12_l3_inf_25_26.ui.admin.listdeclaration;

import androidx.lifecycle.ViewModel;

import com.example.tp_g_12_l3_inf_25_26.utils.ColumnDef;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListDeclarationViewModel extends ViewModel {

    // Colonnes du tableau
    public List<ColumnDef> getColumns() {
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));
        columns.add(new ColumnDef("Nom", 2, true));
        columns.add(new ColumnDef("Téléphone", 2, false));
        columns.add(new ColumnDef("Matricule", 2, true));
        columns.add(new ColumnDef("Type", 2, true));
        columns.add(new ColumnDef("Description", 3, false));
        return columns;
    }

    // Données affichées
    public List<TableRow> getRows() {

        // TODO: récupérer la liste depuis la base de données
        // TODO: gérer pagination et chargement progressif
        // TODO: mapper les entités vers TableRow

        List<TableRow> rows = new ArrayList<>();

        rows.add(() ->
                Arrays.asList(
                        "001",
                        "Othman",
                        "699999999",
                        "99Z999FS",
                        "Téléphone",
                        "Perdu au bureau",
                        "red"
                )
        );

        rows.add(() ->
                Arrays.asList(
                        "002",
                        "Othman",
                        "699999999",
                        "99Z999FS",
                        "Clé",
                        "Perdu dans le jardin",
                        "red"
                )
        );

        rows.add(() ->
                Arrays.asList(
                        "003",
                        "Othman",
                        "699999999",
                        "99Z999FS",
                        "Sac",
                        "Perdu au marché",
                        "yellow"
                )
        );

        return rows;
    }

    public void validateDeclaration(TableRow row) {
        // TODO: mettre à jour le statut dans la base de données
        // TODO: notifier l’utilisateur propriétaire
        // TODO: journaliser l’action admin
    }

    public void rejectDeclaration(TableRow row) {
        // TODO: marquer la déclaration comme rejetée
        // TODO: envoyer une notification
    }
}
