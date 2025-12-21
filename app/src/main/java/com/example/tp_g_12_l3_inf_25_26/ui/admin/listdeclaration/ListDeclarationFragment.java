package com.example.tp_g_12_l3_inf_25_26.ui.admin.listdeclaration;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

public class ListDeclarationFragment extends Fragment {

    private ListDeclarationViewModel viewModel;

    public static ListDeclarationFragment newInstance() {
        return new ListDeclarationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_list_declaration,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this)
                .get(ListDeclarationViewModel.class);

        RecyclerView recycler = view.findViewById(R.id.table);
        recycler.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        TableAdapter<TableRow> adapter =
                new TableAdapter<>(
                        requireContext(),
                        viewModel.getColumns(),
                        viewModel.getRows(),
                        row -> showDialog(row)
                );

        recycler.setAdapter(adapter);
    }

    private void showDialog(TableRow row) {

        new AlertDialog.Builder(requireContext())
                .setTitle("Comparaison")
                .setMessage("Voulez-vous comparer cette déclaration à un objet précis ?")
                .setPositiveButton("Comparer", (dialog, which) -> {

                    // TODO: ouvrir l’écran de comparaison
                    // Exemple:
                    // Navigation vers ImageVerificationFragment ou LostObjectListFragment
                    // en passant l’id de la déclaration

                    Toast.makeText(
                            requireContext(),
                            "Ouverture de la page de comparaison",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

}
