package com.example.tp_g_12_l3_inf_25_26.ui.user.lostlist;

import androidx.lifecycle.ViewModel;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.utils.ColumnDef;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserLostListViewModel extends ViewModel {

    // Retourne les colonnes du tableau
    public List<ColumnDef> getColumns() {
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));
        columns.add(new ColumnDef("Type", 2, true));
        columns.add(new ColumnDef("Description", 3, false));
        return columns;
    }

    // Retourne les lignes du tableau
    public List<TableRow> getRows() {

        // TODO: Remplacer ces données statiques par une récupération depuis la base de données
        List<TableRow> rows = new ArrayList<>();
        rows.add(() -> Arrays.asList("1", "Téléphone", "Perdu au bureau"));
        rows.add(() -> Arrays.asList("2", "Clé", "Perdu dans le jardin"));
        rows.add(() -> Arrays.asList("3", "Sac", "Perdu au marché"));
        return rows;
    }


}
