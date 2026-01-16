package com.example.tp_g_12_l3_inf_25_26.utils;

import java.util.List;

/**
 * Interface pour les lignes du tableau
 */
public interface TableRow {
    /**
     * Retourne les données de la ligne sous forme de liste de chaînes
     * @return Liste des valeurs de chaque cellule
     */
    List<String> cells();

    /**
     * Alias pour cells() - retourne les données de la ligne
     * @return Liste des valeurs de chaque cellule
     */
    default List<String> getData() {
        return cells();
    }
}