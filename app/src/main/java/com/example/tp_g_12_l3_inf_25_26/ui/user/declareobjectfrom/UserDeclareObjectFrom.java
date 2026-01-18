package com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class UserDeclareObjectFrom extends Fragment {

    private UserDeclareObjectFromViewModel mViewModel;
    private EditText editName, editPhone, editMatricule, editDescription;
    private Spinner spinnerObjectType;
    private LinearLayout containerImages;
    private Button buttonSubmit, buttonClearCache;
    private ActivityResultLauncher<String[]> pickImagesLauncher;

    public static UserDeclareObjectFrom newInstance() {
        return new UserDeclareObjectFrom();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(UserDeclareObjectFromViewModel.class);
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

        initViews(view);
        setupSpinner();
        setupImagePicker();
        setupButtons();
        observeViewModel();
    }

    private void initViews(View view) {
        editName = view.findViewById(R.id.editName);
        editPhone = view.findViewById(R.id.editPhone);
        editMatricule = view.findViewById(R.id.editMatricule);
        editDescription = view.findViewById(R.id.editDescription);
        spinnerObjectType = view.findViewById(R.id.spinnerObjectType);
        containerImages = view.findViewById(R.id.containerImages);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonClearCache = view.findViewById(R.id.buttonClearCache);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, mViewModel.getObjectTypes());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerObjectType.setAdapter(adapter);
    }

    private void setupImagePicker() {
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        // Donner permission persistante pour accéder à l'URI
                        requireActivity().getContentResolver().takePersistableUriPermission(
                                uri,
                                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                        addImage(uri);
                    }
                }
        );

        addImageView(); // Premier slot d'image vide
    }

    private void setupButtons() {
        buttonSubmit.setOnClickListener(v -> submitForm());

        if (buttonClearCache != null) {
            buttonClearCache.setOnClickListener(v -> {
                mViewModel.clearUserCache();
                clearFormFields();
                Toast.makeText(requireContext(), "Cache effacé", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void observeViewModel() {
        // Observer les informations utilisateur depuis le cache
        mViewModel.getUserInfoLiveData().observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null && !userInfo.isEmpty()) {
                editName.setText(userInfo.getName());
                editPhone.setText(userInfo.getPhone());
                editMatricule.setText(userInfo.getMatricule());
            }
        });

        // Observer les images sélectionnées
        mViewModel.getSelectedImagesLiveData().observe(getViewLifecycleOwner(), images -> {
            // Optionnel: mettre à jour l'affichage si nécessaire
        });

        // Observer le résultat de soumission
        mViewModel.getSubmitResultLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_LONG).show();

                if (result.isSuccess()) {
                    clearFormFields();
                    clearImageViews();


                    // navigateToLostList();
                }
            }
        });
    }

    private void submitForm() {
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String matricule = editMatricule.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String type = spinnerObjectType.getSelectedItem().toString();

        if (!mViewModel.isFormValid(name, phone, matricule, description, type)) {
            Toast.makeText(requireContext(),
                    "Veuillez remplir tous les champs et ajouter au moins une image",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mViewModel.submitDeclaration(name, phone, matricule, description, type);
    }

    private void addImageView() {
        View item = getLayoutInflater().inflate(R.layout.image_item, containerImages, false);
        ImageView plusIcon = item.findViewById(R.id.plusIcon);
        ImageView image = item.findViewById(R.id.image);

        plusIcon.setVisibility(View.VISIBLE);
        image.setImageDrawable(null);

        plusIcon.setOnClickListener(v -> pickImagesLauncher.launch(new String[]{"image/*"}));
        containerImages.addView(item);
    }

    private void addImage(Uri uri) {

        View lastItem = containerImages.getChildAt(containerImages.getChildCount() - 1);
        ImageView image = lastItem.findViewById(R.id.image);
        ImageView plusIcon = lastItem.findViewById(R.id.plusIcon);

        image.setImageURI(uri);
        plusIcon.setVisibility(View.GONE);

        mViewModel.addImage(uri);


        addImageView();
    }

    private void clearFormFields() {
        editDescription.setText("");
        spinnerObjectType.setSelection(0);

    }

    private void clearImageViews() {
        containerImages.removeAllViews();
        mViewModel.clearImages();
        addImageView();
    }
}