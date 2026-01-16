package com.example.tp_g_12_l3_inf_25_26.ui.admin.listobject;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

import java.util.List;

public class ListObjectFragment extends Fragment {

    private ListObjectViewModel mViewModel;
    private TableAdapter<TableRow> adapter;
    private Button btnAll, btnPending, btnVerification, btnRecovered, btnRefresh;

    public static ListObjectFragment newInstance() {
        return new ListObjectFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_object, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ListObjectViewModel.class);

        initViews(view);
        setupRecyclerView(view);
        setupButtons();
        observeViewModel();
    }

    private void initViews(View view) {
        // Si vous avez des boutons de filtrage dans votre layout
        btnAll = view.findViewById(R.id.btnAll);
        btnPending = view.findViewById(R.id.btnPending);
        btnVerification = view.findViewById(R.id.btnVerification);
        btnRecovered = view.findViewById(R.id.btnRecovered);
        btnRefresh = view.findViewById(R.id.btnRefresh);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recycler = view.findViewById(R.id.table);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TableAdapter<>(
                requireContext(),
                mViewModel.getColumns(),
                null,
                row -> showActionDialog(row)
        );

        recycler.setAdapter(adapter);
    }

    private void setupButtons() {
        if (btnAll != null) {
            btnAll.setOnClickListener(v -> {
                mViewModel.loadObjets();
                Toast.makeText(requireContext(), "Affichage de tous les objets", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnPending != null) {
            btnPending.setOnClickListener(v -> {
                mViewModel.loadObjetsByStatut("En attente");
                Toast.makeText(requireContext(), "Affichage des objets en attente", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnVerification != null) {
            btnVerification.setOnClickListener(v -> {
                mViewModel.loadObjetsByStatut("En cours de vérification");
                Toast.makeText(requireContext(), "Affichage des objets en cours de vérification", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnRecovered != null) {
            btnRecovered.setOnClickListener(v -> {
                mViewModel.loadObjetsByStatut("Récupéré");
                Toast.makeText(requireContext(), "Affichage des objets récupérés", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                mViewModel.loadObjets();
                Toast.makeText(requireContext(), "Liste actualisée", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void observeViewModel() {
        // Observer les lignes du tableau
        mViewModel.getRowsLiveData().observe(getViewLifecycleOwner(), rows -> {
            if (rows != null) {
                adapter.updateData(rows);
            }
        });

        // Observer les résultats des actions
        mViewModel.getActionResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showActionDialog(TableRow row) {
        // Récupérer l'ID de l'objet (première colonne)
        List<String> data = row.getData();
        if (data == null || data.isEmpty()) return;

        int objetId;
        try {
            objetId = Integer.parseInt(data.get(0));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Erreur: ID invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        Objet objet = mViewModel.getObjetById(objetId);
        if (objet == null) {
            Toast.makeText(requireContext(), "Objet introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer le message avec les détails
        String message = buildDetailMessage(objet);

        // Afficher le dialogue avec les options
        new AlertDialog.Builder(requireContext())
                .setTitle("Détails de l'objet")
                .setMessage(message)
                .setPositiveButton("Mettre en vérification", (dialog, which) -> {
                    mViewModel.updateObjetStatut(objetId, "En cours de vérification");
                })
                .setNeutralButton("Marquer récupéré", (dialog, which) -> {
                    mViewModel.updateObjetStatut(objetId, "Récupéré");
                })
                .setNegativeButton("Supprimer", (dialog, which) -> {
                    confirmDelete(objetId);
                })
                .show();
    }

    private String buildDetailMessage(Objet objet) {
        StringBuilder message = new StringBuilder();
        message.append("N° : ").append(objet.getIdObjet()).append("\n\n");
        message.append("Déclarant : ").append(objet.getNomDeclarant()).append("\n");
        message.append("Téléphone : ").append(objet.getTelephone()).append("\n");
        message.append("Type : ").append(objet.getNomType()).append("\n");
        message.append("Statut : ").append(objet.getStatut()).append("\n");
        message.append("Date : ").append(objet.getDateDeclaration()).append("\n\n");
        message.append("Description :\n").append(objet.getDescription()).append("\n\n");
        message.append("Nombre d'images : ").append(objet.getCheminImages().size());
        return message.toString();
    }

    private void confirmDelete(int objetId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer définitivement cet objet ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    mViewModel.deleteObjet(objetId);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}