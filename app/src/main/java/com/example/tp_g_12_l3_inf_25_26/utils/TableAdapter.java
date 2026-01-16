package com.example.tp_g_12_l3_inf_25_26.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp_g_12_l3_inf_25_26.R;

import java.util.ArrayList;
import java.util.List;

public class TableAdapter<T extends TableRow> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ROW = 1;

    private final Context context;
    private final List<ColumnDef> columns;
    private List<T> data;
    private final OnRowClickListener<T> rowClickListener;

    private int sortIndex = -1;
    private boolean asc = true;

    public interface OnRowClickListener<T> {
        void onRowClick(T row);
    }

    public TableAdapter(Context context, List<ColumnDef> columns, List<T> data, OnRowClickListener<T> rowClickListener) {
        this.context = context;
        this.columns = columns;
        this.data = data != null ? data : new ArrayList<>();
        this.rowClickListener = rowClickListener;
    }

    /**
     * Méthode pour mettre à jour les données du tableau
     */
    public void updateData(List<T> newData) {
        this.data = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Méthode pour ajouter des données
     */
    public void addData(T item) {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
        this.data.add(item);
        notifyItemInserted(data.size());
    }

    /**
     * Méthode pour supprimer des données
     */
    public void removeData(int position) {
        if (data != null && position >= 0 && position < data.size()) {
            data.remove(position);
            notifyItemRemoved(position + 1); // +1 car la position 0 est le header
        }
    }

    /**
     * Méthode pour effacer toutes les données
     */
    public void clearData() {
        if (data != null) {
            int size = data.size();
            data.clear();
            notifyItemRangeRemoved(1, size);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ROW;
    }

    @Override
    public int getItemCount() {
        return (data != null ? data.size() : 0) + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_table_header, parent, false));
        }
        return new RowHolder(inflater.inflate(R.layout.item_table_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            ((HeaderHolder) holder).bind();
        } else {
            ((RowHolder) holder).bind(data.get(position - 1), position - 1);
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {
        LinearLayout container;

        HeaderHolder(View v) {
            super(v);
            container = v.findViewById(R.id.headerContainer);
        }

        void bind() {
            container.removeAllViews();
            for (int i = 0; i < columns.size(); i++) {
                ColumnDef col = columns.get(i);
                TextView tv = buildCell(col.title, col.weight, true);
                if (col.sortable) {
                    int index = i;
                    tv.setOnClickListener(v -> sortBy(index));
                }
                container.addView(tv);
            }
        }
    }

    class RowHolder extends RecyclerView.ViewHolder {
        LinearLayout container;

        RowHolder(View v) {
            super(v);
            container = v.findViewById(R.id.rowContainer);
        }

        void bind(T row, int position) {
            container.removeAllViews();
            List<String> cells = row.cells();

            for (int i = 0; i < columns.size(); i++) {
                ColumnDef col = columns.get(i);
                String cellValue = i < cells.size() ? cells.get(i) : "";
                container.addView(buildCell(cellValue, col.weight, false));
            }

            // Le dernier élément est le code couleur du statut
            String status = cells.size() > 0 ? cells.get(cells.size() - 1) : "white";

            int backgroundColor = resolveStatusColor(status, position);
            container.setBackgroundColor(backgroundColor);

            container.setOnClickListener(v -> {
                if (rowClickListener != null) {
                    rowClickListener.onRowClick(row);
                }
            });
        }

        private int resolveStatusColor(String status, int position) {
            if ("red".equals(status)) {
                return alternateRed(position);
            }

            if ("yellow".equals(status)) {
                return alternateYellow(position);
            }

            if ("green".equals(status)) {
                return alternateGreen(position);
            }

            if ("blue".equals(status)) {
                return alternateBlue(position);
            }

            if ("gray".equals(status)) {
                return alternateGray(position);
            }

            return 0xFFFFFFFF;
        }

        private int alternateRed(int position) {
            int strong = 0xFFFFCDD2;
            int light = 0xFFFFEBEE;
            return position % 2 == 0 ? strong : light;
        }

        private int alternateYellow(int position) {
            int strong = 0xFFFFF9C4;
            int light = 0xFFFFFDE7;
            return position % 2 == 0 ? strong : light;
        }

        private int alternateGreen(int position) {
            int strong = 0xFFC8E6C9;
            int light = 0xFFE8F5E9;
            return position % 2 == 0 ? strong : light;
        }

        private int alternateBlue(int position) {
            int strong = 0xFFBBDEFB;
            int light = 0xFFE3F2FD;
            return position % 2 == 0 ? strong : light;
        }

        private int alternateGray(int position) {
            int strong = 0xFFE0E0E0;
            int light = 0xFFF5F5F5;
            return position % 2 == 0 ? strong : light;
        }
    }

    private TextView buildCell(String text, int weight, boolean bold) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextColor(bold ? Color.WHITE : Color.DKGRAY);
        tv.setPadding(16, 16, 16, 16);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private void sortBy(int index) {
        if (data == null || data.isEmpty()) return;

        if (sortIndex == index) {
            asc = !asc;
        } else {
            sortIndex = index;
            asc = true;
        }

        data.sort((a, b) -> {
            List<String> cellsA = a.cells();
            List<String> cellsB = b.cells();

            if (index >= cellsA.size() || index >= cellsB.size()) {
                return 0;
            }

            String va = cellsA.get(index);
            String vb = cellsB.get(index);
            return asc ? va.compareTo(vb) : vb.compareTo(va);
        });
        notifyDataSetChanged();
    }
}