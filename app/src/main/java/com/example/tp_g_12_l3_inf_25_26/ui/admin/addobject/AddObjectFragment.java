package com.example.tp_g_12_l3_inf_25_26.ui.admin.addobject;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.home.HomeAdminFragment;

public class AddObjectFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private AddObjectViewModel viewModel;

    private EditText editName;
    private EditText editPhone;
    private EditText editDescription;
    private Spinner spinnerObjectType;
    private Button buttonTakePhoto;
    private Button buttonSubmit;

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

        viewModel = new ViewModelProvider(this)
                .get(AddObjectViewModel.class);

        editName = view.findViewById(R.id.editName);
        editPhone = view.findViewById(R.id.editPhone);
        editDescription = view.findViewById(R.id.editDescription);
        spinnerObjectType = view.findViewById(R.id.spinnerObjectType);
        buttonTakePhoto = view.findViewById(R.id.buttonTakePhoto);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        imagesContainer = view.findViewById(R.id.imagesContainer);

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

        buttonTakePhoto.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(
                    requireActivity().getPackageManager()) != null) {

                startActivityForResult(
                        intent,
                        REQUEST_IMAGE_CAPTURE
                );
            }
        });

        buttonSubmit.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String description =
                    editDescription.getText().toString().trim();
            String type =
                    spinnerObjectType.getSelectedItem().toString();

            viewModel.validateForm(
                    name,
                    phone,
                    description,
                    type
            );
        });

        viewModel.isFormValid().observe(
                getViewLifecycleOwner(),
                valid -> {
                    if (!valid) {
                        Toast.makeText(
                                requireContext(),
                                "Formulaire incomplet",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    viewModel.submitDeclaration(
                            editName.getText().toString().trim(),
                            editPhone.getText().toString().trim(),
                            editDescription.getText().toString().trim(),
                            spinnerObjectType
                                    .getSelectedItem()
                                    .toString()
                    );

                    Toast.makeText(
                            requireContext(),
                            "Déclaration enregistrée",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        viewModel.getImages().observe(
                getViewLifecycleOwner(),
                images -> {
                    imagesContainer.removeAllViews();
                    for (Bitmap bitmap : images) {
                        ImageView image = new ImageView(requireContext());
                        image.setImageBitmap(bitmap);
                        image.setLayoutParams(
                                new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        300
                                )
                        );
                        image.setScaleType(
                                ImageView.ScaleType.CENTER_CROP
                        );
                        imagesContainer.addView(image);
                    }
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == getActivity().RESULT_OK
                && data != null) {

            Bitmap bitmap =
                    (Bitmap) data.getExtras().get("data");

            viewModel.addImage(bitmap);

            // TODO: sauvegarder les images temporairement
            // TODO: limiter le nombre d’images
        }
    }
}
