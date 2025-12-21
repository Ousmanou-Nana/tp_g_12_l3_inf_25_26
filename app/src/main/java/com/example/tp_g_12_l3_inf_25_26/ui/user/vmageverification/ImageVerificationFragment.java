package com.example.tp_g_12_l3_inf_25_26.ui.user.vmageverification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tp_g_12_l3_inf_25_26.R;

public class ImageVerificationFragment extends Fragment {

    private static final String ARG_OBJECT_ID = "object_id"; // clé pour passer l'ID de l'objet
    private ImageVerificationViewModel viewModel;
    private String objectId;

    // Création du fragment avec un argument : l'ID de l'objet
    public static ImageVerificationFragment newInstance(String objectId) {
        ImageVerificationFragment fragment = new ImageVerificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OBJECT_ID, objectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise le ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ImageVerificationViewModel.class);

        // Récupère l'ID de l'objet passé en argument
        if (getArguments() != null) {
            objectId = getArguments().getString(ARG_OBJECT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate le layout avec la grille d'images
        return inflater.inflate(R.layout.fragment_image_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        GridView gridView = view.findViewById(R.id.gridVerificationImages);
        gridView.setAdapter(new VerificationImageAdapter(
                requireContext(),
                viewModel.getVerificationImages(objectId) // TODO: remplacer par les images réelles depuis DB
        ));

        // Gestion du clic sur une image : envoie de la demande de récupération
        gridView.setOnItemClickListener((parent, v, position, id) -> {
            Toast.makeText(requireContext(),
                    "Demande de récupération envoyée",
                    Toast.LENGTH_SHORT
            ).show();

            // TODO: envoyer la demande de récupération vers le serveur ou DB

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack(); // revient à la liste précédente
            }
        });
    }
}
