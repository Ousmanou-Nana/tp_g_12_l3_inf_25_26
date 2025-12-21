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
import com.example.tp_g_12_l3_inf_25_26.utils.ColumnDef;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

import java.util.List;

public class MyDeclaration extends Fragment {

    private MyDeclarationViewModel viewModel;

    // Crée une instance du fragment
    public static MyDeclaration newInstance() {
        return new MyDeclaration();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise le ViewModel
        viewModel = new ViewModelProvider(this).get(MyDeclarationViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate le layout du fragment
        return inflater.inflate(R.layout.fragment_my_declaration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialise RecyclerView
        RecyclerView recycler = view.findViewById(R.id.recyclerMyDeclarations);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Récupère colonnes et lignes depuis le ViewModel
        List<ColumnDef> columns = viewModel.getColumns();
        List<TableRow> rows = viewModel.getRows(); // TODO: récupérer les déclarations réelles depuis la base

        TableAdapter<TableRow> adapter = new TableAdapter<>(
                requireContext(),
                columns,
                rows,
                row -> showDeleteDialog(row)
        );

        recycler.setAdapter(adapter);

        // Boutons pour ajouter une nouvelle déclaration ou aller à la liste des objets perdus
        Button btnNewDeclaration = view.findViewById(R.id.buttonNewDeclaration);
        Button btnLostList = view.findViewById(R.id.buttonLostList);

        btnNewDeclaration.setOnClickListener(v -> openNewDeclaration());
        btnLostList.setOnClickListener(v -> openLostList());
    }

    // Affiche une boîte de dialogue pour confirmer la suppression d'une déclaration
    private void showDeleteDialog(TableRow row) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la déclaration")
                .setMessage("Voulez-vous supprimer cette déclaration ?")
                .setPositiveButton("Oui", (dialog, which) ->
                        Toast.makeText(
                                requireContext(),
                                "Déclaration supprimée : " + row.cells().get(1), // TODO: remplacer par suppression réelle dans la DB
                                Toast.LENGTH_SHORT
                        ).show()
                )
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
