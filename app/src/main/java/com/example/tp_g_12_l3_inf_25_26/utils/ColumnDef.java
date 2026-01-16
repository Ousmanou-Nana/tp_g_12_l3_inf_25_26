package com.example.tp_g_12_l3_inf_25_26.utils;

/**
 * Classe pour définir les colonnes du tableau
 */
public class ColumnDef {
    public String title;      // Titre de la colonne
    public int weight;        // Poids pour la largeur (utilisé dans LinearLayout)
    public boolean sortable;  // Si la colonne peut être triée

    public ColumnDef(String title, int weight, boolean sortable) {
        this.title = title;
        this.weight = weight;
        this.sortable = sortable;
    }

    public String getTitle() {
        return title;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isSortable() {
        return sortable;
    }
}