package com.example.tp_g_12_l3_inf_25_26.ui.admin.listdeclaration;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.models.Declaration;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.matching.MatchingDialog;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

import java.util.List;

public class ListDeclarationFragment extends Fragment {

    private ListDeclarationViewModel viewModel;
    private TableAdapter<TableRow> adapter;
    private Button btnAll, btnPending, btnValidated, btnRecovered, btnRefresh;

    public static ListDeclarationFragment newInstance() {
        return new ListDeclarationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_declaration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ListDeclarationViewModel.class);

        initViews(view);
        setupRecyclerView(view);
        setupButtons();
        observeViewModel();
    }

    private void initViews(View view) {
        btnAll = view.findViewById(R.id.btnAll);
        btnPending = view.findViewById(R.id.btnPending);
        btnValidated = view.findViewById(R.id.btnValidated);
        btnRecovered = view.findViewById(R.id.btnRecovered);
        btnRefresh = view.findViewById(R.id.btnRefresh);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recycler = view.findViewById(R.id.table);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TableAdapter<>(
                requireContext(),
                viewModel.getColumns(),
                null,
                row -> showActionDialog(row)
        );

        recycler.setAdapter(adapter);
    }

    private void setupButtons() {
        btnAll.setOnClickListener(v -> {
            viewModel.loadDeclarations();
            Toast.makeText(requireContext(), "Affichage de toutes les déclarations", Toast.LENGTH_SHORT).show();
        });

        btnPending.setOnClickListener(v -> {
            viewModel.loadDeclarationsByStatut("En attente");
            Toast.makeText(requireContext(), "Affichage des déclarations en attente", Toast.LENGTH_SHORT).show();
        });

        btnValidated.setOnClickListener(v -> {
            viewModel.loadDeclarationsByStatut("En cours de vérification");
            Toast.makeText(requireContext(), "Affichage des déclarations en cours de vérification", Toast.LENGTH_SHORT).show();
        });

        btnRecovered.setOnClickListener(v -> {
            viewModel.loadDeclarationsByStatut("Récupéré");
            Toast.makeText(requireContext(), "Affichage des objets récupérés", Toast.LENGTH_SHORT).show();
        });

        btnRefresh.setOnClickListener(v -> {
            viewModel.loadDeclarations();
            Toast.makeText(requireContext(), "Liste actualisée", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        viewModel.getRowsLiveData().observe(getViewLifecycleOwner(), rows -> {
            if (rows != null) {
                adapter.updateData(rows);
            }
        });

        viewModel.getActionResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showActionDialog(TableRow row) {
        List<String> data = row.getData();
        if (data == null || data.isEmpty()) return;

        int declarationId;
        try {
            declarationId = Integer.parseInt(data.get(0));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Erreur: ID invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        Declaration declaration = viewModel.getDeclarationById(declarationId);
        if (declaration == null) {
            Toast.makeText(requireContext(), "Déclaration introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = buildDetailMessage(declaration);

        new AlertDialog.Builder(requireContext())
                .setTitle("Détails de la déclaration")
                .setMessage(message)
                .setPositiveButton("Mettre en vérification", (dialog, which) -> {
                    showMatchingObjectsDialog(declaration);
                })
                .setNeutralButton("Marquer récupéré", (dialog, which) -> {
                    viewModel.markAsRecovered(declarationId);
                })
                .setNegativeButton("Rejeter", (dialog, which) -> {
                    confirmReject(declarationId);
                })
                .setNeutralButton("Supprimer", (dialog, which) -> {
                    confirmDelete(declarationId);
                })
                .show();
    }

    private void showMatchingObjectsDialog(Declaration declaration) {
        // Load potential matching objects
        viewModel.loadPotentialMatchingObjets(declaration.getIdType(), declaration.getIdDeclaration());

        viewModel.getPotentialMatchesLiveData().observe(getViewLifecycleOwner(), objets -> {
            if (objets == null || objets.isEmpty()) {
                // No matches found, just update status
                new AlertDialog.Builder(requireContext())
                        .setTitle("Aucun objet correspondant")
                        .setMessage("Aucun objet trouvé de même type. Voulez-vous quand même mettre en vérification ?")
                        .setPositiveButton("Oui", (d, w) -> {
                            viewModel.validateDeclaration(declaration.getIdDeclaration());
                        })
                        .setNegativeButton("Non", null)
                        .show();
                return;
            }

            // Show matching dialog
            MatchingDialog.showMatchingObjectsForDeclaration(
                    requireContext(),
                    declaration,
                    objets,
                    new MatchingDialog.OnMatchSelectedListener() {
                        @Override
                        public void onDeclarationSelected(Declaration d) {
                            // Not used here
                        }

                        @Override
                        public void onObjetSelected(Objet objet) {
                            // Link declaration with object
                            viewModel.createMatching(
                                    declaration.getIdDeclaration(),
                                    objet.getIdObjet()
                            );
                            viewModel.validateDeclaration(declaration.getIdDeclaration());
                            Toast.makeText(requireContext(),
                                    "Déclaration liée à l'objet N°" + objet.getIdObjet(),
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onNoMatch() {
                            // Just update status without linking
                            viewModel.validateDeclaration(declaration.getIdDeclaration());
                        }
                    }
            );
        });
    }

    private String buildDetailMessage(Declaration declaration) {
        StringBuilder message = new StringBuilder();
        message.append("N° : ").append(declaration.getIdDeclaration()).append("\n\n");
        message.append("Déclarant : ").append(declaration.getUserName()).append("\n");
        message.append("Téléphone : ").append(declaration.getUserPhone()).append("\n");
        message.append("Matricule : ").append(declaration.getUserMatricule()).append("\n");
        message.append("Type : ").append(declaration.getNomType()).append("\n");
        message.append("Statut : ").append(declaration.getStatut()).append("\n");
        message.append("Date : ").append(declaration.getDateDeclaration()).append("\n\n");
        message.append("Description :\n").append(declaration.getDescription()).append("\n\n");
        message.append("Nombre d'images : ").append(declaration.getCheminImages().size());
        return message.toString();
    }

    private void confirmReject(int declarationId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir rejeter cette déclaration ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    viewModel.rejectDeclaration(declarationId);
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void confirmDelete(int declarationId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer définitivement cette déclaration ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    viewModel.deleteDeclaration(declarationId);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}