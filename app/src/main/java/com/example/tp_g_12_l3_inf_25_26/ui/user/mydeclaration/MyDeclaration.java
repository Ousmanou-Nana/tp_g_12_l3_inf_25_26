package com.example.tp_g_12_l3_inf_25_26.ui.user.mydeclaration;

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
import com.example.tp_g_12_l3_inf_25_26.ui.user.lostlist.UserLostList;
import com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom.UserDeclareObjectFrom;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

public class MyDeclaration extends Fragment {

    private MyDeclarationViewModel viewModel;
    private TableAdapter<TableRow> adapter;

    // Filter buttons
    private Button btnAll, btnPending, btnVerification, btnRecovered, btnRefresh;

    public static MyDeclaration newInstance() {
        return new MyDeclaration();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise le ViewModel
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(MyDeclarationViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_my_declaration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView(view);
        setupActionButtons(view);
        setupFilterButtons();
        observeViewModel();
    }

    private void initViews(View view) {
        // Filter buttons
        btnAll = view.findViewById(R.id.btnAll);
        btnPending = view.findViewById(R.id.btnPending);
        btnVerification = view.findViewById(R.id.btnVerification);
        btnRecovered = view.findViewById(R.id.btnRecovered);
        btnRefresh = view.findViewById(R.id.btnRefresh);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recycler = view.findViewById(R.id.recyclerMyDeclarations);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialise l'adapter avec une liste vide
        adapter = new TableAdapter<>(
                requireContext(),
                viewModel.getColumns(),
                viewModel.getRows(),
                this::showDeleteDialog
        );

        recycler.setAdapter(adapter);
    }

    private void setupActionButtons(View view) {
        Button btnNewDeclaration = view.findViewById(R.id.buttonNewDeclaration);
        Button btnLostList = view.findViewById(R.id.buttonLostList);

        btnNewDeclaration.setOnClickListener(v -> openNewDeclaration());
        btnLostList.setOnClickListener(v -> openLostList());
    }

    private void setupFilterButtons() {
        // Filter buttons
        btnAll.setOnClickListener(v -> {
            viewModel.filterByStatus(null); // null = toutes les déclarations
            highlightSelectedFilter(btnAll);
        });

        btnPending.setOnClickListener(v -> {
            viewModel.filterByStatus("En attente");
            highlightSelectedFilter(btnPending);
        });

        btnVerification.setOnClickListener(v -> {
            viewModel.filterByStatus("En cours de vérification");
            highlightSelectedFilter(btnVerification);
        });

        btnRecovered.setOnClickListener(v -> {
            viewModel.filterByStatus("Récupéré");
            highlightSelectedFilter(btnRecovered);
        });

        btnRefresh.setOnClickListener(v -> viewModel.refresh());

        // Sélectionner "Tous" par défaut
        highlightSelectedFilter(btnAll);
    }

    private void highlightSelectedFilter(Button selectedButton) {
        // Réinitialiser tous les boutons
        btnAll.setEnabled(true);
        btnPending.setEnabled(true);
        btnVerification.setEnabled(true);
        btnRecovered.setEnabled(true);

        // Désactiver le bouton sélectionné pour indiquer qu'il est actif
        selectedButton.setEnabled(false);
    }

    private void observeViewModel() {
        // Observer les déclarations
        viewModel.getDeclarationsLiveData().observe(getViewLifecycleOwner(), declarations -> {
            if (declarations != null) {
                adapter.updateData(declarations);
            }
        });

        // Observer le résultat de suppression
        viewModel.getDeleteResultLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les données quand on revient sur le fragment
        viewModel.refresh();
    }

    // Affiche une boîte de dialogue pour confirmer la suppression d'une déclaration
    private void showDeleteDialog(TableRow row) {
        // Le premier élément est l'ID de la déclaration
        String declarationId = row.cells().get(0);
        String type = row.cells().get(1);
        String description = row.cells().get(2);

        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la déclaration")
                .setMessage("Voulez-vous supprimer cette déclaration ?\n\n" +
                        "Type: " + type + "\n" +
                        "Description: " + description)
                .setPositiveButton("Oui", (dialog, which) -> {
                    try {
                        int id = Integer.parseInt(declarationId);
                        viewModel.deleteDeclaration(id);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(),
                                "Erreur: ID invalide",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }

    // Ouvre le fragment pour déclarer un nouvel objet perdu
    private void openNewDeclaration() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main_user_container, UserDeclareObjectFrom.newInstance())
                .addToBackStack(null)
                .commit();
    }

    // Ouvre le fragment affichant la liste des objets perdus
    private void openLostList() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main_user_container, UserLostList.newInstance())
                .addToBackStack(null)
                .commit();
    }
}