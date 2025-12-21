package com.example.tp_g_12_l3_inf_25_26.ui.user.lostlist;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom.UserDeclareObjectFrom;
import com.example.tp_g_12_l3_inf_25_26.ui.user.vmageverification.ImageVerificationFragment;
import com.example.tp_g_12_l3_inf_25_26.ui.user.mydeclaration.MyDeclaration;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

public class UserLostList extends Fragment {

    private UserLostListViewModel viewModel;

    // Crée une instance du fragment
    public static UserLostList newInstance() {
        return new UserLostList();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise le ViewModel
        viewModel = new ViewModelProvider(this).get(UserLostListViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate le layout du fragment
        return inflater.inflate(R.layout.fragment_user_lost_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Initialise le RecyclerView avec un layout vertical
        RecyclerView recyclerView = view.findViewById(R.id.recyclerLostObjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialise l'adapter du tableau avec les colonnes et les lignes
        TableAdapter<TableRow> adapter = new TableAdapter<>(
                requireContext(),
                viewModel.getColumns(),
                viewModel.getRows(),  // TODO: Remplacer ces lignes statiques par des données réelles de la base
                this::showVerificationDialog
        );

        recyclerView.setAdapter(adapter);

        // Boutons pour déclarer un objet perdu ou voir ses propres déclarations
        view.findViewById(R.id.buttonDeclareLost)
                .setOnClickListener(v -> openDeclareForm());

        view.findViewById(R.id.buttonViewDeclarations)
                .setOnClickListener(v -> openMyDeclarations());
    }

    // Affiche une boîte de dialogue pour vérifier si l'objet appartient à l'utilisateur
    private void showVerificationDialog(TableRow row) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Vérification de l’objet")
                .setMessage("Confirmez-vous que cet objet vous appartient")
                .setPositiveButton("Oui", (d, w) -> openImageVerification(row))
                .setNegativeButton("Non", null)
                .show();
    }

    // Ouvre le fragment de vérification par image
    private void openImageVerification(TableRow row) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main_user_container,
                        ImageVerificationFragment.newInstance(row.cells().get(0)))  // TODO: passer l'ID correct ou récupérer l'objet depuis la DB
                .addToBackStack(null)
                .commit();
    }

    // Ouvre le formulaire pour déclarer un nouvel objet perdu
    private void openDeclareForm() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main_user_container, UserDeclareObjectFrom.newInstance())
                .addToBackStack(null)
                .commit();
    }

    // Ouvre la liste des déclarations de l'utilisateur
    private void openMyDeclarations() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main_user_container, MyDeclaration.newInstance())
                .addToBackStack(null)
                .commit();
    }
}
