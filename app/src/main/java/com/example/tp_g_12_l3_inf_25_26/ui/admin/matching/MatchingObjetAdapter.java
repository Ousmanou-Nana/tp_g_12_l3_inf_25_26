package com.example.tp_g_12_l3_inf_25_26.ui.admin.matching;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.models.Declaration;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;

import java.io.File;
import java.util.List;

/**
 * Adapter for matching Objects with image support (sans Glide)
 */
class MatchingObjetAdapter extends RecyclerView.Adapter<MatchingObjetAdapter.ViewHolder> {

    private final Context context;
    private final List<Objet> objets;
    private final OnObjetClickListener listener;

    interface OnObjetClickListener {
        void onObjetClick(Objet objet);
    }

    public MatchingObjetAdapter(Context context, List<Objet> objets, OnObjetClickListener listener) {
        this.context = context;
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
        holder.bind(context, objet, listener);
    }

    @Override
    public int getItemCount() {
        return objets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivObjetImage;
        private final TextView tvObjetId;
        private final TextView tvObjetType;
        private final TextView tvObjetDesc;
        private final TextView tvObjetDate;
        private final TextView tvObjetDeclarant;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivObjetImage = itemView.findViewById(R.id.ivObjetImage);
            tvObjetId = itemView.findViewById(R.id.tvObjetId);
            tvObjetType = itemView.findViewById(R.id.tvObjetType);
            tvObjetDesc = itemView.findViewById(R.id.tvObjetDesc);
            tvObjetDate = itemView.findViewById(R.id.tvObjetDate);
            tvObjetDeclarant = itemView.findViewById(R.id.tvObjetDeclarant);
        }

        public void bind(Context context, Objet objet, OnObjetClickListener listener) {
            tvObjetId.setText("N°" + objet.getIdObjet());
            tvObjetType.setText(objet.getNomType());
            tvObjetDesc.setText(truncate(objet.getDescription(), 100));
            tvObjetDate.setText(objet.getDateDeclaration());
            tvObjetDeclarant.setText(objet.getNomDeclarant());

            // Charger l'image
            loadFirstImage(objet.getCheminImages(), ivObjetImage);

            itemView.setOnClickListener(v -> listener.onObjetClick(objet));
        }

        private void loadFirstImage(List<String> imagePaths, ImageView imageView) {
            if (imagePaths != null && !imagePaths.isEmpty()) {
                String firstImagePath = imagePaths.get(0);
                File imageFile = new File(firstImagePath);

                if (imageFile.exists()) {
                    try {
                        // Charger l'image avec échantillonnage pour économiser la mémoire
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(firstImagePath, options);

                        // Calculer le facteur d'échantillonnage
                        int targetSize = 200; // Taille cible en pixels
                        options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize);

                        // Décoder l'image avec échantillonnage
                        options.inJustDecodeBounds = false;
                        Bitmap bitmap = BitmapFactory.decodeFile(firstImagePath, options);

                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else {
                            imageView.setImageResource(R.drawable.ic_placeholder_image);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        imageView.setImageResource(R.drawable.ic_error_image);
                    }
                } else {
                    imageView.setImageResource(R.drawable.ic_placeholder_image);
                }
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder_image);
            }
        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }

        private String truncate(String text, int maxLength) {
            if (text != null && text.length() > maxLength) {
                return text.substring(0, maxLength - 3) + "...";
            }
            return text;
        }
    }
}

/**
 * Adapter for matching Declarations with image support (sans Glide)
 */
class MatchingDeclarationAdapter extends RecyclerView.Adapter<MatchingDeclarationAdapter.ViewHolder> {

    private final Context context;
    private final List<Declaration> declarations;
    private final OnDeclarationClickListener listener;

    interface OnDeclarationClickListener {
        void onDeclarationClick(Declaration declaration);
    }

    public MatchingDeclarationAdapter(Context context, List<Declaration> declarations, OnDeclarationClickListener listener) {
        this.context = context;
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
        holder.bind(context, declaration, listener);
    }

    @Override
    public int getItemCount() {
        return declarations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivDeclarationImage;
        private final TextView tvDeclarationId;
        private final TextView tvDeclarationType;
        private final TextView tvDeclarationDesc;
        private final TextView tvDeclarationDate;
        private final TextView tvDeclarationUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDeclarationImage = itemView.findViewById(R.id.ivDeclarationImage);
            tvDeclarationId = itemView.findViewById(R.id.tvDeclarationId);
            tvDeclarationType = itemView.findViewById(R.id.tvDeclarationType);
            tvDeclarationDesc = itemView.findViewById(R.id.tvDeclarationDesc);
            tvDeclarationDate = itemView.findViewById(R.id.tvDeclarationDate);
            tvDeclarationUser = itemView.findViewById(R.id.tvDeclarationUser);
        }

        public void bind(Context context, Declaration declaration, OnDeclarationClickListener listener) {
            tvDeclarationId.setText("N°" + declaration.getIdDeclaration());
            tvDeclarationType.setText(declaration.getNomType());
            tvDeclarationDesc.setText(truncate(declaration.getDescription(), 100));
            tvDeclarationDate.setText(declaration.getDateDeclaration());
            tvDeclarationUser.setText(declaration.getUserName() + " - " + declaration.getUserMatricule());

            // Charger l'image
            loadFirstImage(declaration.getCheminImages(), ivDeclarationImage);

            itemView.setOnClickListener(v -> listener.onDeclarationClick(declaration));
        }

        private void loadFirstImage(List<String> imagePaths, ImageView imageView) {
            if (imagePaths != null && !imagePaths.isEmpty()) {
                String firstImagePath = imagePaths.get(0);
                File imageFile = new File(firstImagePath);

                if (imageFile.exists()) {
                    try {
                        // Charger l'image avec échantillonnage pour économiser la mémoire
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(firstImagePath, options);

                        // Calculer le facteur d'échantillonnage
                        int targetSize = 200; // Taille cible en pixels
                        options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize);

                        // Décoder l'image avec échantillonnage
                        options.inJustDecodeBounds = false;
                        Bitmap bitmap = BitmapFactory.decodeFile(firstImagePath, options);

                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else {
                            imageView.setImageResource(R.drawable.ic_placeholder_image);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        imageView.setImageResource(R.drawable.ic_error_image);
                    }
                } else {
                    imageView.setImageResource(R.drawable.ic_placeholder_image);
                }
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder_image);
            }
        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }

        private String truncate(String text, int maxLength) {
            if (text != null && text.length() > maxLength) {
                return text.substring(0, maxLength - 3) + "...";
            }
            return text;
        }
    }
}