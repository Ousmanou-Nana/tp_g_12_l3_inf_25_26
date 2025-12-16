package com.example.tp_g_12_l3_inf_25_26.ui.user.LostList;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.utils.ColumnDef;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserLostList extends Fragment {

    private UserLostListViewModel mViewModel;
    public static UserLostList newInstance() { return new UserLostList(); }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_lost_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recycler = view.findViewById(R.id.recyclerLostObjects);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<ColumnDef> columns = new ArrayList<>();
        columns.add(new ColumnDef("N°", 1, true));
        columns.add(new ColumnDef("Type", 2, true));
        columns.add(new ColumnDef("Description", 3, false));

        List<TableRow> rows = new ArrayList<>();
        rows.add(() -> Arrays.asList("001", "Téléphone", "Perdu au bureau"));
        rows.add(() -> Arrays.asList("002", "Clé", "Perdu dans le jardin"));
        rows.add(() -> Arrays.asList("003", "Sac", "Perdu au marché"));

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
