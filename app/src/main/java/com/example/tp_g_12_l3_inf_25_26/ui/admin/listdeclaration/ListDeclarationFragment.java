package com.example.tp_g_12_l3_inf_25_26.ui.admin.listdeclaration;

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
import android.widget.Toast;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.utils.ColumnDef;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListDeclarationFragment extends Fragment {

    private ListDeclarationViewModel mViewModel;

    public static ListDeclarationFragment newInstance() {
        return new ListDeclarationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_declaration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recycler = view.findViewById(R.id.table);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        mViewModel = new ViewModelProvider(this).get(ListDeclarationViewModel.class);



        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));
        columns.add(new ColumnDef("Nom", 2, true));
        columns.add(new ColumnDef("téléphone", 2, false));
        columns.add(new ColumnDef("Matricule", 2, true));
        columns.add(new ColumnDef("Type", 2, true));
        columns.add(new ColumnDef("Description", 3, false));

        List<TableRow> rows = new ArrayList<>();
        rows.add(() -> Arrays.asList("001","othman","699999999","99Z999FS", "Téléphone", "Perdu au bureau","red"));
        rows.add(() -> Arrays.asList("002", "othman","699999999","99Z999FS","Clé", "Perdu dans le jardin","red"));
        rows.add(() -> Arrays.asList("003","othman","699999999","99Z999FS", "Sac", "Perdu au marché","yellow"));
        TableAdapter<TableRow> adapter = new TableAdapter<>(
                requireContext(),
                columns,
                rows,
                row -> new AlertDialog.Builder(requireContext())
                        .setTitle("Vérification")
                        .setMessage("Voulez-vous vérifier que c'est votre objet ?")
                        .setPositiveButton("Oui", (dialog, which) ->
                                Toast.makeText(requireContext(),
                                        "Action Oui pour " + row.cells().get(1),
                                        Toast.LENGTH_SHORT).show())
                        .setNegativeButton("Non", (dialog, which) ->
                                Toast.makeText(requireContext(),
                                        "Action Non pour " + row.cells().get(1),
                                        Toast.LENGTH_SHORT).show())
                        .show()
        );

        recycler.setAdapter(adapter);

    }

}