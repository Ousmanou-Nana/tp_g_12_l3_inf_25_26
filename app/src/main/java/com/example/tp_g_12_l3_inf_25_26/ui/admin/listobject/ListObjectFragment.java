// ============================================================================
// ListObjectFragment.java
// ============================================================================
// Fragment qui gère l'interface utilisateur pour la liste des objets trouvés
// C'est le pendant de ListDeclarationFragment mais pour les objets trouvés
// Permet aux admins de:
// - Visualiser tous les objets trouvés ou filtrer par statut
// - Voir les détails d'un objet trouvé
// - Mettre à jour le statut (en vérification, récupéré)
// - Associer un objet trouvé à une déclaration d'objet perdu (matching)
// - Supprimer un objet trouvé
// ============================================================================
package com.example.tp_g_12_l3_inf_25_26.ui.admin.listobject;

import androidx.lifecycle.ViewModelProvider;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
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
import com.example.tp_g_12_l3_inf_25_26.models.Declaration;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.matching.MatchingDialog;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;
import java.util.List;

public class ListObjectFragment extends Fragment {

    private ListObjectViewModel mViewModel;
    private TableAdapter<TableRow> adapter;

    private Button btnAll;
    private Button btnPending;
    private Button btnVerification;
    private Button btnRecovered;
    private Button btnRefresh;

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
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }


        mViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ListObjectViewModel.class);

        initViews(view);
        setupRecyclerView(view);
        setupButtons();
        observeViewModel();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
    private void initViews(View view) {
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
                Toast.makeText(requireContext(),
                        "Affichage de tous les objets",
                        Toast.LENGTH_SHORT).show();
            });
        }

        if (btnPending != null) {
            btnPending.setOnClickListener(v -> {
                mViewModel.loadObjetsByStatut("En attente");
                Toast.makeText(requireContext(),
                        "Affichage des objets en attente",
                        Toast.LENGTH_SHORT).show();
            });
        }

        if (btnVerification != null) {
            btnVerification.setOnClickListener(v -> {
                mViewModel.loadObjetsByStatut("En cours de vérification");
                Toast.makeText(requireContext(),
                        "Affichage des objets en cours de vérification",
                        Toast.LENGTH_SHORT).show();
            });
        }

        if (btnRecovered != null) {
            btnRecovered.setOnClickListener(v -> {
                mViewModel.loadObjetsByStatut("Récupéré");
                Toast.makeText(requireContext(),
                        "Affichage des objets récupérés",
                        Toast.LENGTH_SHORT).show();
            });
        }

        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                mViewModel.loadObjets();
                Toast.makeText(requireContext(),
                        "Liste actualisée",
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void observeViewModel() {
        mViewModel.getRowsLiveData().observe(getViewLifecycleOwner(), rows -> {
            if (rows != null) {
                adapter.updateData(rows);
            }
        });

        mViewModel.getActionResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showActionDialog(TableRow row) {
        List<String> data = row.getData();
        if (data == null || data.isEmpty()) return;

        int objetId;
        try {
            objetId = Integer.parseInt(data.get(0));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    "Erreur: ID invalide",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Objet objet = mViewModel.getObjetById(objetId);
        if (objet == null) {
            Toast.makeText(requireContext(),
                    "Objet introuvable",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String message = buildDetailMessage(objet);

        new AlertDialog.Builder(requireContext())
                .setTitle("Détails de l'objet")
                .setMessage(message)
                .setPositiveButton("Mettre en vérification", (dialog, which) -> {
                    showMatchingDeclarationsDialog(objet);
                })
                // ===== MODIFICATION ICI =====
                // Au lieu de marquer directement comme récupéré,
                // on vérifie d'abord s'il y a des déclarations matchées
                .setNeutralButton("Marquer récupéré", (dialog, which) -> {
                    handleMarkAsRecovered(objet);
                })
                .setNegativeButton("Supprimer", (dialog, which) -> {
                    confirmDelete(objetId);
                })
                .show();
    }

    // ===== NOUVELLE MÉTHODE =====
    /**
     * Gère le marquage comme récupéré
     * Vérifie d'abord s'il existe des déclarations matchées
     * Si oui, affiche la liste pour choisir laquelle marquer comme récupérée
     * Si non, marque juste l'objet comme récupéré
     */
    private void handleMarkAsRecovered(Objet objet) {
        // Charge les déclarations matchées à cet objet
        mViewModel.loadMatchedDeclarations(objet.getIdObjet());

        // Observer pour une seule fois (removeObservers après utilisation)
        mViewModel.getMatchedDeclarationsLiveData().observe(getViewLifecycleOwner(), declarations -> {
            if (declarations == null) {
                return; // Toujours en chargement
            }

            // Retire l'observer pour éviter des déclenchements multiples
            mViewModel.getMatchedDeclarationsLiveData().removeObservers(getViewLifecycleOwner());

            if (declarations.isEmpty()) {
                // ===== CAS 1: AUCUNE DÉCLARATION MATCHÉE =====
                // Marque simplement l'objet comme récupéré
                new AlertDialog.Builder(requireContext())
                        .setTitle("Aucune déclaration associée")
                        .setMessage("Cet objet n'est associé à aucune déclaration. Voulez-vous quand même le marquer comme récupéré ?")
                        .setPositiveButton("Oui", (d, w) -> {
                            mViewModel.updateObjetStatut(objet.getIdObjet(), "Récupéré");
                        })
                        .setNegativeButton("Non", null)
                        .show();
            } else {
                // ===== CAS 2: DÉCLARATIONS MATCHÉES TROUVÉES =====
                // Affiche un dialogue avec la liste des déclarations matchées
                showRecoveryDeclarationsDialog(objet, declarations);
            }
        });
    }

    // =====  MÉTHODE =====
    /**
     * Affiche un dialogue avec les déclarations matchées
     * Permet de choisir laquelle marquer comme récupérée
     */
    private void showRecoveryDeclarationsDialog(Objet objet, List<Declaration> declarations) {
        MatchingDialog.showMatchingDeclarationsForObjet(
                requireContext(),
                objet,
                declarations,
                new MatchingDialog.OnMatchSelectedListener() {
                    @Override
                    public void onDeclarationSelected(Declaration declaration) {
                        // Marque la déclaration sélectionnée comme récupérée
                        mViewModel.updateDeclarationStatut(
                                declaration.getIdDeclaration(),
                                "Récupéré"
                        );

                        // Marque aussi l'objet comme récupéré
                        mViewModel.updateObjetStatut(objet.getIdObjet(), "Récupéré");

                        Toast.makeText(requireContext(),
                                "Déclaration N°" + declaration.getIdDeclaration() + " marquée comme récupérée",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onObjetSelected(Objet o) {
                        // Non utilisé dans ce contexte
                    }

                    @Override
                    public void onNoMatch() {
                        // L'utilisateur a choisi "Aucune correspondance"
                        // Marque juste l'objet comme récupéré sans toucher aux déclarations
                        mViewModel.updateObjetStatut(objet.getIdObjet(), "Récupéré");
                    }
                }
        );
    }

    private void showMatchingDeclarationsDialog(Objet objet) {
        mViewModel.loadPotentialMatchingDeclarations(
                objet.getIdType(),
                objet.getIdObjet()
        );

        mViewModel.getPotentialMatchesLiveData().observe(getViewLifecycleOwner(), declarations -> {
            if (declarations == null || declarations.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Aucune déclaration correspondante")
                        .setMessage("Aucune déclaration de même type. Voulez-vous quand même mettre en vérification ?")
                        .setPositiveButton("Oui", (d, w) -> {
                            mViewModel.updateObjetStatut(objet.getIdObjet(), "En cours de vérification");
                        })
                        .setNegativeButton("Non", null)
                        .show();
                return;
            }

            MatchingDialog.showMatchingDeclarationsForObjet(
                    requireContext(),
                    objet,
                    declarations,
                    new MatchingDialog.OnMatchSelectedListener() {
                        @Override
                        public void onDeclarationSelected(Declaration declaration) {
                            mViewModel.createMatching(
                                    declaration.getIdDeclaration(),
                                    objet.getIdObjet()
                            );

                            mViewModel.updateObjetStatut(objet.getIdObjet(), "En cours de vérification");

                            Toast.makeText(requireContext(),
                                    "Objet lié à la déclaration N°" + declaration.getIdDeclaration(),
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onObjetSelected(Objet o) {
                            // Non utilisé
                        }

                        @Override
                        public void onNoMatch() {
                            mViewModel.updateObjetStatut(objet.getIdObjet(), "En cours de vérification");
                        }
                    }
            );
        });
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