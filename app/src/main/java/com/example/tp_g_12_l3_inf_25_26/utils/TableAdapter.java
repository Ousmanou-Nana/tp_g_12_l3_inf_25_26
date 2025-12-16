package com.example.tp_g_12_l3_inf_25_26.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tp_g_12_l3_inf_25_26.R;
import java.util.List;

public class TableAdapter<T extends TableRow> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ROW = 1;

    private final Context context;
    private final List<ColumnDef> columns;
    private final List<T> data;
    private final OnRowClickListener<T> rowClickListener;

    private int sortIndex = -1;
    private boolean asc = true;

    public interface OnRowClickListener<T> {
        void onRowClick(T row);
    }

    public TableAdapter(Context context, List<ColumnDef> columns, List<T> data, OnRowClickListener<T> rowClickListener) {
        this.context = context;
        this.columns = columns;
        this.data = data;
        this.rowClickListener = rowClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ROW;
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
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
            ((RowHolder) holder).bind(data.get(position - 1));
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

        void bind(T row) {
            container.removeAllViews();
            List<String> cells = row.cells();
            for (int i = 0; i < cells.size(); i++) {
                ColumnDef col = columns.get(i);
                container.addView(buildCell(cells.get(i), col.weight, false));
            }
            container.setOnClickListener(v -> {
                if (rowClickListener != null) rowClickListener.onRowClick(row);
            });
        }
    }

    private TextView buildCell(String text, int weight, boolean bold) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setPadding(8, 8, 8, 8);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private void sortBy(int index) {
        if (sortIndex == index) asc = !asc;
        else { sortIndex = index; asc = true; }

        data.sort((a, b) -> {
            String va = a.cells().get(index);
            String vb = b.cells().get(index);
            return asc ? va.compareTo(vb) : vb.compareTo(va);
        });
        notifyDataSetChanged();
    }
}
