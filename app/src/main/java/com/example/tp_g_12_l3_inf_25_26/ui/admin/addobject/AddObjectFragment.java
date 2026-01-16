package com.example.tp_g_12_l3_inf_25_26.ui.admin.addobject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tp_g_12_l3_inf_25_26.R;

public class AddObjectFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private AddObjectViewModel viewModel;

    private EditText editName;
    private EditText editPhone;
    private EditText editDescription;
    private Spinner spinnerObjectType;
    private Button buttonTakePhoto;
    private Button buttonSubmit;
    private Button buttonClear;
    private TextView textImageCount;

    // Conteneur pour plusieurs images
    private LinearLayout imagesContainer;

    public static AddObjectFragment newInstance() {
        return new AddObjectFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_add_object, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(AddObjectViewModel.class);

        initViews(view);
        setupSpinner();
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        editName = view.findViewById(R.id.editName);
        editPhone = view.findViewById(R.id.editPhone);
        editDescription = view.findViewById(R.id.editDescription);
        spinnerObjectType = view.findViewById(R.id.spinnerObjectType);
        buttonTakePhoto = view.findViewById(R.id.buttonTakePhoto);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonClear = view.findViewById(R.id.buttonClear);
        textImageCount = view.findViewById(R.id.textImageCount);
        imagesContainer = view.findViewById(R.id.imagesContainer);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        viewModel.getObjectTypes()
                );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerObjectType.setAdapter(adapter);
    }

    private void setupListeners() {
        buttonTakePhoto.setOnClickListener(v -> {
            if (viewModel.canAddMoreImages()) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            } else {
                Toast.makeText(requireContext(),
                        "Maximum 5 images autorisées",
                        Toast.LENGTH_SHORT).show();
            }
        });

        buttonSubmit.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String type = spinnerObjectType.getSelectedItem().toString();

            viewModel.validateForm(name, phone, description, type);
        });

        buttonClear.setOnClickListener(v -> {
            clearForm();
        });
    }

    private void observeViewModel() {
        // Observer la validation du formulaire
        viewModel.isFormValid().observe(
                getViewLifecycleOwner(),
                valid -> {
                    if (!valid) {
                        Toast.makeText(
                                requireContext(),
                                "Veuillez remplir tous les champs et ajouter au moins une image",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    // Si valide, soumettre
                    viewModel.submitDeclaration(
                            editName.getText().toString().trim(),
                            editPhone.getText().toString().trim(),
                            editDescription.getText().toString().trim(),
                            spinnerObjectType.getSelectedItem().toString()
                    );
                }
        );

        // Observer les images
        viewModel.getImages().observe(
                getViewLifecycleOwner(),
                images -> {
                    updateImagesDisplay(images);
                    updateImageCount();
                }
        );

        // Observer le résultat de soumission
        viewModel.getSubmitResult().observe(
                getViewLifecycleOwner(),
                result -> {
                    if (result != null) {
                        Toast.makeText(
                                requireContext(),
                                result.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        if (result.isSuccess()) {
                            clearForm();
                        }
                    }
                }
        );
    }

    private void updateImagesDisplay(java.util.List<Bitmap> images) {
        imagesContainer.removeAllViews();

        for (int i = 0; i < images.size(); i++) {
            final int position = i;
            Bitmap bitmap = images.get(i);

            // Créer un conteneur pour chaque image
            LinearLayout imageLayout = new LinearLayout(requireContext());
            imageLayout.setOrientation(LinearLayout.VERTICAL);
            imageLayout.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            );
            imageLayout.setPadding(0, 8, 0, 8);

            // ImageView
            ImageView imageView = new ImageView(requireContext());
            imageView.setImageBitmap(bitmap);
            imageView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            300
                    )
            );
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // Bouton supprimer
            Button btnRemove = new Button(requireContext());
            btnRemove.setText("Supprimer cette image");
            btnRemove.setOnClickListener(v -> {
                viewModel.removeImage(position);
            });

            imageLayout.addView(imageView);
            imageLayout.addView(btnRemove);
            imagesContainer.addView(imageLayout);
        }
    }

    private void updateImageCount() {
        int count = viewModel.getImageCount();
        textImageCount.setText("Images: " + count + "/5");
        buttonTakePhoto.setEnabled(viewModel.canAddMoreImages());
    }

    private void clearForm() {
        editName.setText("");
        editPhone.setText("");
        editDescription.setText("");
        spinnerObjectType.setSelection(0);
        viewModel.clearImages();
        Toast.makeText(requireContext(),
                "Formulaire réinitialisé",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == Activity.RESULT_OK
                && data != null) {

            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap bitmap = (Bitmap) extras.get("data");
                if (bitmap != null) {
                    viewModel.addImage(bitmap);
                }
            }
        }
    }
}