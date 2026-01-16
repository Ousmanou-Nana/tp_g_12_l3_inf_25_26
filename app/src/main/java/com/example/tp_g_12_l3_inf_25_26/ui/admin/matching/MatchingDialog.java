package com.example.tp_g_12_l3_inf_25_26.ui.admin.matching;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.models.Declaration;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;

import java.util.List;

/**
 * Dialog to show matching items for verification
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
        RecyclerView recyclerView = dialogView.findViewById(R.id.rvMatchingItems);
        Button btnNoMatch = dialogView.findViewById(R.id.btnNoMatch);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvTitle.setText("Objets trouvés similaires");
        tvInfo.setText(buildDeclarationInfo(declaration));

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        MatchingObjetAdapter adapter = new MatchingObjetAdapter(
                potentialMatches,
                objet -> {
                    listener.onObjetSelected(objet);
                }
        );
        recyclerView.setAdapter(adapter);

        AlertDialog dialog = builder.setView(dialogView).create();

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
        RecyclerView recyclerView = dialogView.findViewById(R.id.rvMatchingItems);
        Button btnNoMatch = dialogView.findViewById(R.id.btnNoMatch);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvTitle.setText("Déclarations similaires");
        tvInfo.setText(buildObjetInfo(objet));

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        MatchingDeclarationAdapter adapter = new MatchingDeclarationAdapter(
                potentialMatches,
                declaration -> {
                    listener.onDeclarationSelected(declaration);
                }
        );
        recyclerView.setAdapter(adapter);

        AlertDialog dialog = builder.setView(dialogView).create();

        btnNoMatch.setOnClickListener(v -> {
            listener.onNoMatch();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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