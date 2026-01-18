package com.example.tp_g_12_l3_inf_25_26.ui.user.lostlist;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
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

/**
 * Fragment affichant la liste des objets perdus avec système de filtrage.
 *
 * CORRECTION: Initialisation de l'adapter dans setupRecyclerView()
 * pour éviter NullPointerException dans l'observateur.
 */
public class UserLostList extends Fragment {

    private UserLostListViewModel viewModel;
    private TableAdapter<TableRow> adapter;


    public static UserLostList newInstance() {
        return new UserLostList();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

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


        setupRecyclerView(view);

        observeViewModel();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
    /**
     * CORRECTION: Initialisation complète de l'adapter
     */
    private void setupRecyclerView(View view) {
        // Initialise le RecyclerView avec un layout vertical
        RecyclerView recyclerView = view.findViewById(R.id.recyclerLostObjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // CORRECTION: Initialisation de l'adapter AVANT l'observateur
        adapter = new TableAdapter<>(
                requireContext(),              // Contexte
                viewModel.getColumns(),        // Définition des colonnes
                viewModel.getRows(),           // Données initiales (liste vide)
                null                           // Pas de callback (pas de suppression)
        );

        // Attachement de l'adapter au RecyclerView
        recyclerView.setAdapter(adapter);
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

            if (objects != null && adapter != null) {
                adapter.updateData(objects);
            }
        });
    }
}