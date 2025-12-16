package com.example.tp_g_12_l3_inf_25_26.ui.admin.addobject;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom.UserDeclareObjectFromViewModel;

public class AddObjectFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private AddObjectViewModel mViewModel;

    private EditText editName, editPhone, editDescription;
    private Button buttonTakePhoto, buttonSubmit;
    private ImageView imagePreview;

    private Spinner spinnerObjectType;
    private Bitmap capturedImage;
    public static AddObjectFragment newInstance() {
        return new AddObjectFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_object, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AddObjectViewModel.class);


        editName = view.findViewById(R.id.editName);
        editPhone = view.findViewById(R.id.editPhone);
        editDescription = view.findViewById(R.id.editDescription);
        imagePreview = view.findViewById(R.id.imagePreview);
        buttonTakePhoto = view.findViewById(R.id.buttonTakePhoto);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        spinnerObjectType = view.findViewById(R.id.spinnerObjectType);

        String[] objectTypes = {"Portefeuille", "Téléphone", "Clé", "Sac", "Autre"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, objectTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerObjectType.setAdapter(adapter);

        buttonTakePhoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        buttonSubmit.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String type = spinnerObjectType.getSelectedItem().toString();


            if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || type.isEmpty() || capturedImage == null) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs et prendre une photo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ici, tu peux envoyer les données à ton ViewModel ou serveur
            Toast.makeText(requireContext(), "Déclaration enregistrée !", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            capturedImage = (Bitmap) extras.get("data");
            imagePreview.setImageBitmap(capturedImage);
            imagePreview.setVisibility(View.VISIBLE);
        }
    }
}