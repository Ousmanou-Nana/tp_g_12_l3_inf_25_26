package com.example.tp_g_12_l3_inf_25_26.ui.admin.matching;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.models.Declaration;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;

import java.util.List;

/**
 * Adapter for matching Objects
 */
class MatchingObjetAdapter extends RecyclerView.Adapter<MatchingObjetAdapter.ViewHolder> {

    private final List<Objet> objets;
    private final OnObjetClickListener listener;

    interface OnObjetClickListener {
        void onObjetClick(Objet objet);
    }

    public MatchingObjetAdapter(List<Objet> objets, OnObjetClickListener listener) {
        this.objets = objets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_matching_objet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Objet objet = objets.get(position);
        holder.bind(objet, listener);
    }

    @Override
    public int getItemCount() {
        return objets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvObjetId;
        private final TextView tvObjetType;
        private final TextView tvObjetDesc;
        private final TextView tvObjetDate;
        private final TextView tvObjetDeclarant;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvObjetId = itemView.findViewById(R.id.tvObjetId);
            tvObjetType = itemView.findViewById(R.id.tvObjetType);
            tvObjetDesc = itemView.findViewById(R.id.tvObjetDesc);
            tvObjetDate = itemView.findViewById(R.id.tvObjetDate);
            tvObjetDeclarant = itemView.findViewById(R.id.tvObjetDeclarant);
        }

        public void bind(Objet objet, OnObjetClickListener listener) {
            tvObjetId.setText("N°" + objet.getIdObjet());
            tvObjetType.setText(objet.getNomType());
            tvObjetDesc.setText(truncate(objet.getDescription(), 100));
            tvObjetDate.setText(objet.getDateDeclaration());
            tvObjetDeclarant.setText(objet.getNomDeclarant());

            itemView.setOnClickListener(v -> listener.onObjetClick(objet));
        }

        private String truncate(String text, int maxLength) {
            if (text.length() > maxLength) {
                return text.substring(0, maxLength - 3) + "...";
            }
            return text;
        }
    }
}

/**
 * Adapter for matching Declarations
 */
class MatchingDeclarationAdapter extends RecyclerView.Adapter<MatchingDeclarationAdapter.ViewHolder> {

    private final List<Declaration> declarations;
    private final OnDeclarationClickListener listener;

    interface OnDeclarationClickListener {
        void onDeclarationClick(Declaration declaration);
    }

    public MatchingDeclarationAdapter(List<Declaration> declarations, OnDeclarationClickListener listener) {
        this.declarations = declarations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_matching_declaration, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Declaration declaration = declarations.get(position);
        holder.bind(declaration, listener);
    }

    @Override
    public int getItemCount() {
        return declarations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeclarationId;
        private final TextView tvDeclarationType;
        private final TextView tvDeclarationDesc;
        private final TextView tvDeclarationDate;
        private final TextView tvDeclarationUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeclarationId = itemView.findViewById(R.id.tvDeclarationId);
            tvDeclarationType = itemView.findViewById(R.id.tvDeclarationType);
            tvDeclarationDesc = itemView.findViewById(R.id.tvDeclarationDesc);
            tvDeclarationDate = itemView.findViewById(R.id.tvDeclarationDate);
            tvDeclarationUser = itemView.findViewById(R.id.tvDeclarationUser);
        }

        public void bind(Declaration declaration, OnDeclarationClickListener listener) {
            tvDeclarationId.setText("N°" + declaration.getIdDeclaration());
            tvDeclarationType.setText(declaration.getNomType());
            tvDeclarationDesc.setText(truncate(declaration.getDescription(), 100));
            tvDeclarationDate.setText(declaration.getDateDeclaration());
            tvDeclarationUser.setText(declaration.getUserName() + " - " + declaration.getUserMatricule());

            itemView.setOnClickListener(v -> listener.onDeclarationClick(declaration));
        }

        private String truncate(String text, int maxLength) {
            if (text.length() > maxLength) {
                return text.substring(0, maxLength - 3) + "...";
            }
            return text;
        }
    }
}