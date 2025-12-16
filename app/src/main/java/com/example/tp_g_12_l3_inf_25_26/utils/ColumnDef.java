package com.example.tp_g_12_l3_inf_25_26.utils;

public class ColumnDef {
    public final String title;
    public final int weight;
    public final boolean sortable;

    public ColumnDef(String title, int weight, boolean sortable) {
        this.title = title;
        this.weight = weight;
        this.sortable = sortable;
    }
}
