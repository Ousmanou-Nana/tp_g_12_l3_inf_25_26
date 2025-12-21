package com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.user.lostlist.UserLostList;
import com.example.tp_g_12_l3_inf_25_26.ui.user.lostlist.UserLostListViewModel;

public class UserDeclareObjectFrom extends Fragment {

    private UserDeclareObjectFromViewModel mViewModel;
    private EditText editName, editPhone, editMatricule, editDescription;
    private Spinner spinnerObjectType;
    private LinearLayout containerImages;
    private ActivityResultLauncher<String[]> pickImagesLauncher;

    // Crée une instance du fragment
    public static UserDeclareObjectFrom newInstance() {
        return new UserDeclareObjectFrom();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise le ViewModel
        mViewModel = new ViewModelProvider(this).get(UserDeclareObjectFromViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate le layout du formulaire de déclaration
        return inflater.inflate(R.layout.fragment_user_declare_object_from, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Initialisation des champs du formulaire
        editName = view.findViewById(R.id.editName);
        editPhone = view.findViewById(R.id.editPhone);
        editMatricule = view.findViewById(R.id.editMatricule);
        editDescription = view.findViewById(R.id.editDescription);
        spinnerObjectType = view.findViewById(R.id.spinnerObjectType);
        containerImages = view.findViewById(R.id.containerImages);

        // Configuration du spinner avec les types d'objets
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, mViewModel.getObjectTypes());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerObjectType.setAdapter(adapter);

        // Enregistre un launcher pour sélectionner des images depuis le stockage
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) addImage(uri); // TODO: gérer les permissions si besoin
                }
        );

        addImageView(); // ajoute le premier slot d'image vide

        // Bouton de soumission du formulaire
        view.findViewById(R.id.buttonSubmit).setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String matricule = editMatricule.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String type = spinnerObjectType.getSelectedItem().toString();

            if (!mViewModel.isFormValid(name, phone, matricule, description, type)) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: envoyer les données vers la base de données ou API
                Bundle args = new Bundle();
                args.putString("object_type", type);

                UserLostList fragment = UserLostList.newInstance();
                fragment.setArguments(args);

                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_user_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    // Ajoute un nouvel item image vide avec le plus (+) pour sélectionner une image
    private void addImageView() {
        View item = getLayoutInflater().inflate(R.layout.image_item, containerImages, false);
        ImageView plusIcon = item.findViewById(R.id.plusIcon);
        ImageView image = item.findViewById(R.id.image);

        plusIcon.setVisibility(View.VISIBLE);
        image.setImageDrawable(null);

        plusIcon.setOnClickListener(v -> pickImagesLauncher.launch(new String[]{"image/*"}));
        containerImages.addView(item);
    }

    // Ajoute l'image sélectionnée à l'UI et au ViewModel
    private void addImage(Uri uri) {
        View lastItem = containerImages.getChildAt(containerImages.getChildCount() - 1);
        ImageView image = lastItem.findViewById(R.id.image);
        ImageView plusIcon = lastItem.findViewById(R.id.plusIcon);

        image.setImageURI(uri);
        plusIcon.setVisibility(View.GONE);

        mViewModel.addImage(uri); // TODO: éventuellement uploader l'image vers le serveur

        addImageView(); // ajoute un nouveau slot pour une image suivante
    }
}
