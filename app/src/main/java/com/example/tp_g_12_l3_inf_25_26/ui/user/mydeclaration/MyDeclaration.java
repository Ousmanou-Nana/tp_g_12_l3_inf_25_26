package com.example.tp_g_12_l3_inf_25_26.ui.user.mydeclaration;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.utils.ColumnDef;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyDeclaration extends Fragment {

    private MyDeclarationViewModel mViewModel;

    public static MyDeclaration newInstance() {
        return new MyDeclaration();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_declaration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recycler = view.findViewById(R.id.recyclerMyDeclarations);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Colonnes du tableau
        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));
        columns.add(new ColumnDef("Type", 2, true));
        columns.add(new ColumnDef("Description", 3, false));

        // Lignes de test
        List<TableRow> rows = new ArrayList<>();
        rows.add(() -> Arrays.asList("001", "Téléphone", "Perdu au bureau"));
        rows.add(() -> Arrays.asList("002", "Clé", "Perdu dans le jardin"));
        rows.add(() -> Arrays.asList("003", "Sac", "Perdu au marché"));

        // Adapter avec clic sur la ligne
        TableAdapter<TableRow> adapter = new TableAdapter<>(
                requireContext(),
                columns,
                rows,
                row -> {
                    // Action au clic : supprimer ou retirer
                    List<String> cells = row.cells();
                    String numero = cells.get(0);
                    String type = cells.get(1);

                    Toast.makeText(
                            requireContext(),
                            "Voulez-vous retirer cette déclaration ? " + numero + " - " + type,
                            Toast.LENGTH_SHORT
                    ).show();

                    // Ici tu peux utiliser AlertDialog pour Oui/Non
                }
        );

        recycler.setAdapter(adapter);
    }


}