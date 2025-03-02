package dam.pmdm.nest.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dam.pmdm.nest.R;
import dam.pmdm.nest.databinding.FragmentEditIncidenceButtonSheetBinding;
import dam.pmdm.nest.model.Incidence;

import com.google.firebase.storage.StorageReference;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;


public class EditIncidenceButtonSheetFragment extends BottomSheetDialogFragment {

    private OnIncidenceUpdatedListener listener;
    public interface OnIncidenceUpdatedListener {
        void onIncidenceUpdated(Incidence updatedIncidence);
    }

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_CAMERA = 2;

    private FragmentEditIncidenceButtonSheetBinding binding;
    private String currentImageUrl;
    private HomeViewModel viewModel;
    private final Incidence incidence;
    private Uri selectedImageUri;
    MaterialSpinner statusSpinner, professionalSpinner;

    private static final String[] INCIDENCE_STATUS = {"PENDIENTE", "EN PROCESO", "COMPLETADA"};
    private static final String[] PROFESSIONALS = {"SIN ASIGNAR", "FONTANERO", "ALBAÑIL", "ELECTRICISTA"};

    public EditIncidenceButtonSheetFragment(Incidence incidence) {
        this.incidence = incidence;
    }

    public void setOnIncidenceUpdatedListener(OnIncidenceUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditIncidenceButtonSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        binding.edtTitle.setText(incidence.getTitle());
        binding.edtDescription.setText(incidence.getDescription());

        currentImageUrl = incidence.getImage();
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Picasso.get().load(currentImageUrl).fit().centerCrop().into(binding.imgSelectImage);
        } else {
            binding.imgSelectImage.setImageResource(R.drawable.ic_create_image);
        }

        setupSpinners();

        binding.imgSelectImage.setOnClickListener(v -> {
            showImagePickerDialog();
        });

        binding.btnSave.setOnClickListener(v -> {
            String newTitle = binding.edtTitle.getText().toString();
            String newDescription = binding.edtDescription.getText().toString();
            String newStatus = INCIDENCE_STATUS[statusSpinner.getSelectedIndex()];
            String newProfessional = PROFESSIONALS[professionalSpinner.getSelectedIndex()];

            binding.pbEditIncidence.setVisibility(View.VISIBLE);

            if (selectedImageUri != null) {
                viewModel.uploadImageToFirebase(selectedImageUri, new HomeViewModel.OnImageUploadedListener() {
                    @Override
                    public void onImageUploaded(String imageUrl) {
                        viewModel.updateIncidenceWithImage(incidence, newTitle, newDescription, newStatus, newProfessional, imageUrl);
                        saveIncidence();
                        binding.pbEditIncidence.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(String error) {
                        binding.pbEditIncidence.setVisibility(View.GONE);
                        System.out.println(error);
                    }
                });
            } else {
                viewModel.updateIncidenceWithImage(incidence, newTitle, newDescription, newStatus, newProfessional, currentImageUrl);
                saveIncidence();
                binding.pbEditIncidence.setVisibility(View.GONE);
            }
        });

    }

    private void setupSpinners() {
        statusSpinner = binding.spinnerStatus;
        statusSpinner.setItems(Arrays.asList(INCIDENCE_STATUS));
        statusSpinner.setBackgroundColor(getResources().getColor(R.color.secondary));
        int statusPos = getStatusPosition(incidence.getStatus());
        if (statusPos >= 0) {
            statusSpinner.setSelectedIndex(statusPos);
        }

        professionalSpinner = binding.spinnerProfessional;
        professionalSpinner.setItems(Arrays.asList(PROFESSIONALS));
        professionalSpinner.setBackgroundColor(getResources().getColor(R.color.secondary));
        int professionalPos = getProfessionalPosition(incidence.getProfessional());
        if (professionalPos >= 0) {
            professionalSpinner.setSelectedIndex(professionalPos);
        }

        binding.tiStatusLayout.setVisibility(View.VISIBLE);
        binding.tiProfessionalLayout.setVisibility(View.VISIBLE);
    }

    private void saveIncidence() {
        if (listener != null) {
            listener.onIncidenceUpdated(incidence);
        }
        dismiss();
    }


    private int getStatusPosition(String status) {
        for (int i = 0; i < INCIDENCE_STATUS.length; i++) {
            if (INCIDENCE_STATUS[i].equals(status)) {
                return i;
            }
        }
        return 0;
    }

    private int getProfessionalPosition(String professional) {
        for (int i = 0; i < PROFESSIONALS.length; i++) {
            if (PROFESSIONALS[i].equals(professional)) {
                return i;
            }
        }
        return 0;
    }

    private void showImagePickerDialog() {
        String[] options = {getString(R.string.create_photo_opt1), getString(R.string.create_photo_opt2), getString(R.string.create_photo_opt3)};
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.create_photo_opt_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickImageFromGallery();
                    } else if (which == 1) {
                        takePhoto();
                    } else {
                        selectedImageUri = null;
                        binding.imgSelectImage.setImageResource(R.drawable.ic_create_image);
                    }
                })
                .show();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null) {
                selectedImageUri = data.getData();
                Picasso.get().load(selectedImageUri).fit().centerCrop().into(binding.imgSelectImage);
            } else if (requestCode == REQUEST_CAMERA && data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                selectedImageUri = getImageUri(requireContext(), imageBitmap);
                Picasso.get().load(selectedImageUri).fit().centerCrop().into(binding.imgSelectImage);
            }
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "IncidenceImage", null);
        return Uri.parse(path);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
