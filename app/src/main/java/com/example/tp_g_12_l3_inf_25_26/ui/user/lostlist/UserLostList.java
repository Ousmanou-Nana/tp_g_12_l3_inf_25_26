package com.example.tp_g_12_l3_inf_25_26.ui.user.lostlist;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom.UserDeclareObjectFrom;
import com.example.tp_g_12_l3_inf_25_26.ui.user.mydeclaration.MyDeclaration;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

public class UserLostList extends Fragment {

    private UserLostListViewModel viewModel;
    private TableAdapter<TableRow> adapter;

    // Filter buttons
    private Button btnAll, btnPending, btnVerification, btnRecovered, btnRefresh;

    public static UserLostList newInstance() {
        return new UserLostList();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise le ViewModel
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(UserLostListViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_user_lost_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView(view);
        setupButtons();
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
        // Initialise le RecyclerView avec un layout vertical
        RecyclerView recyclerView = view.findViewById(R.id.recyclerLostObjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

    }

    private void setupButtons() {
        // Action buttons (already set up in onViewCreated)
        // No need to reference them again here

        // Filter buttons
        btnAll.setOnClickListener(v -> {
            viewModel.filterByStatus(null); // null = tous les objets
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

        // Sélectionner "En attente" par défaut
        highlightSelectedFilter(btnPending);
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

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les données quand on revient sur le fragment
        viewModel.refresh();
    }

    private void observeViewModel() {
        // Observer les objets perdus
        viewModel.getObjectsLiveData().observe(getViewLifecycleOwner(), objects -> {
            if (objects != null) {
                adapter.updateData(objects);
            }
        });
    }



}