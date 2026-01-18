package com.example.tp_g_12_l3_inf_25_26.ui.admin.matching;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.models.Declaration;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;

import java.io.File;
import java.util.List;

/**
 * Dialog to show matching items for verification with image support (sans Glide)
 */
public class MatchingDialog {

    public interface OnMatchSelectedListener {
        void onDeclarationSelected(Declaration declaration);
        void onObjetSelected(Objet objet);
        void onNoMatch();
    }

    /**
     * Show dialog to match a Declaration with Objects
     */
    public static void showMatchingObjectsForDeclaration(
            Context context,
            Declaration declaration,
            List<Objet> potentialMatches,
            OnMatchSelectedListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(
                R.layout.dialog_matching_list,
                null
        );

        TextView tvTitle = dialogView.findViewById(R.id.tvMatchingTitle);
        TextView tvInfo = dialogView.findViewById(R.id.tvMatchingInfo);
        ImageView ivMainImage = dialogView.findViewById(R.id.ivMainItemImage);
        RecyclerView recyclerView = dialogView.findViewById(R.id.rvMatchingItems);
        Button btnNoMatch = dialogView.findViewById(R.id.btnNoMatch);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvTitle.setText("Objets trouvés similaires");
        tvInfo.setText(buildDeclarationInfo(declaration));

        // Charger l'image de la déclaration principale
        loadFirstImage(declaration.getCheminImages(), ivMainImage);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        MatchingObjetAdapter adapter = new MatchingObjetAdapter(
                context,
                potentialMatches,
                objet -> {
                    listener.onObjetSelected(objet);
                }
        );
        recyclerView.setAdapter(adapter);

        AlertDialog dialog = builder.setView(dialogView).create();

        // Configurer pour orientation paysage
        configureDialogForLandscape(dialog);

        btnNoMatch.setOnClickListener(v -> {
            listener.onNoMatch();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Show dialog to match an Objet with Declarations
     */
    public static void showMatchingDeclarationsForObjet(
            Context context,
            Objet objet,
            List<Declaration> potentialMatches,
            OnMatchSelectedListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(
                R.layout.dialog_matching_list,
                null
        );

        TextView tvTitle = dialogView.findViewById(R.id.tvMatchingTitle);
        TextView tvInfo = dialogView.findViewById(R.id.tvMatchingInfo);
        ImageView ivMainImage = dialogView.findViewById(R.id.ivMainItemImage);
        RecyclerView recyclerView = dialogView.findViewById(R.id.rvMatchingItems);
        Button btnNoMatch = dialogView.findViewById(R.id.btnNoMatch);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvTitle.setText("Déclarations similaires");
        tvInfo.setText(buildObjetInfo(objet));

        // Charger l'image de l'objet principal
        loadFirstImage(objet.getCheminImages(), ivMainImage);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        MatchingDeclarationAdapter adapter = new MatchingDeclarationAdapter(
                context,
                potentialMatches,
                declaration -> {
                    listener.onDeclarationSelected(declaration);
                }
        );
        recyclerView.setAdapter(adapter);

        AlertDialog dialog = builder.setView(dialogView).create();

        // Configurer pour orientation paysage
        configureDialogForLandscape(dialog);

        btnNoMatch.setOnClickListener(v -> {
            listener.onNoMatch();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Configure le dialogue pour une meilleure utilisation en mode paysage
     */
    private static void configureDialogForLandscape(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
        }
    }

    /**
     * Charge la première image d'une liste de chemins d'images
     */
    private static void loadFirstImage(List<String> imagePaths, ImageView imageView) {
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

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

    private static String buildDeclarationInfo(Declaration declaration) {
        return "Déclaration N°" + declaration.getIdDeclaration() + "\n" +
                "Type: " + declaration.getNomType() + "\n" +
                "Description: " + declaration.getDescription();
    }

    private static String buildObjetInfo(Objet objet) {
        return "Objet N°" + objet.getIdObjet() + "\n" +
                "Type: " + objet.getNomType() + "\n" +
                "Description: " + objet.getDescription();
    }
}