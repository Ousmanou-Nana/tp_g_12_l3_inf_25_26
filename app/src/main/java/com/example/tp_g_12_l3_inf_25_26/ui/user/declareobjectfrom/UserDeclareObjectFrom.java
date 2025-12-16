package com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tp_g_12_l3_inf_25_26.R;

public class UserDeclareObjectFrom extends Fragment {

    private UserDeclareObjectFromViewModel mViewModel;

    private EditText editName, editPhone, editMatricule, editDescription;
    private Spinner spinnerObjectType;
    private Button buttonAddImages, buttonSubmit;
    private ImageView imagePreview;

    private ActivityResultLauncher<String[]> pickImagesLauncher;

    public static UserDeclareObjectFrom newInstance() {
        return new UserDeclareObjectFrom();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_declare_object_from, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(UserDeclareObjectFromViewModel.class);

        editName = view.findViewById(R.id.editName);
        editPhone = view.findViewById(R.id.editPhone);
        editMatricule = view.findViewById(R.id.editMatricule);
        editDescription = view.findViewById(R.id.editDescription);
        spinnerObjectType = view.findViewById(R.id.spinnerObjectType);
        buttonAddImages = view.findViewById(R.id.buttonAddImages);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        imagePreview = view.findViewById(R.id.imagePreview);

        String[] objectTypes = {"Portefeuille", "Téléphone", "Clé", "Sac", "Autre"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, objectTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerObjectType.setAdapter(adapter);

        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        mViewModel.setSelectedImages(uris);
                        imagePreview.setImageURI(uris.get(0));
                        imagePreview.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(),
                                uris.size() + " image(s) sélectionnée(s)", Toast.LENGTH_SHORT).show();
                    } else {
                        imagePreview.setVisibility(View.GONE);
                        Toast.makeText(requireContext(),
                                "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
                    }
                });

        buttonAddImages.setOnClickListener(v -> pickImagesLauncher.launch(new String[]{"image/*"}));

        buttonSubmit.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String matricule = editMatricule.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String type = spinnerObjectType.getSelectedItem().toString();

            String message = mViewModel.submitForm(name, phone, matricule, description, type);
            if (message == null) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
