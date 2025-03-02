package dam.pmdm.nest.ui.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import dam.pmdm.nest.R;
import dam.pmdm.nest.databinding.FragmentCreateBinding;

public class CreateFragment extends Fragment {

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_CAMERA = 2;

    private FragmentCreateBinding binding;
    private CreateViewModel viewModel;
    private Uri selectedImageUri = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CreateViewModel.class);

        binding.imgSelected.setOnClickListener(v -> showImagePickerDialog());
        binding.btnPublish.setOnClickListener(v -> validateAndSubmit());

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(view);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (isDataSave()) {
                showAlertExit(navController, item.getItemId());
                return false;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        viewModel.getPublishStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isPublishing().observe(getViewLifecycleOwner(), isPublishing -> {
            if (isPublishing) {
                binding.btnPublish.setText("");
                binding.pbCreate.setVisibility(View.VISIBLE);
                binding.btnPublish.setEnabled(false);
            } else {
                binding.btnPublish.setText(R.string.btn_createa_publish);
                binding.pbCreate.setVisibility(View.GONE);
                binding.btnPublish.setEnabled(true);
            }
        });
        viewModel.getIsSuccessful().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                clearFields();
                Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
            }
        });
    }

    private boolean isDataSave() {
        if (binding == null) {
            return false;
        }
        return !binding.etTitle.getText().toString().trim().isEmpty() ||
                !binding.etDescription.getText().toString().trim().isEmpty() ||
                selectedImageUri != null;
    }

    private void showAlertExit(NavController navController, int destinoId) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.txt_title_alert_crete)
                .setMessage(R.string.txt_message_alert_create)
                .setPositiveButton(R.string.salir, (dialog, which) -> {
                    navController.popBackStack();
                    navController.navigate(destinoId);
                })
                .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void clearFields() {
        binding.etTitle.setText("");
        binding.etDescription.setText("");
        binding.imgSelected.setImageResource(R.drawable.ic_create_image);
    }

    private void validateAndSubmit() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            binding.tiTitleLayout.setError(getString(R.string.err_create_title));
        } else {
            binding.tiTitleLayout.setError(null);
        }

        if (description.isEmpty()) {
            binding.tiDescriptionLayout.setError(getString(R.string.err_create_description));
        } else {
            binding.tiDescriptionLayout.setError(null);
        }

        if (title.isEmpty() || description.isEmpty()){return;}

        viewModel.publishIncident(title, description, selectedImageUri);
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
                        binding.imgSelected.setImageResource(R.drawable.ic_create_image);
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
                Picasso.get().load(selectedImageUri).fit().centerCrop().into(binding.imgSelected);
            } else if (requestCode == REQUEST_CAMERA && data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                selectedImageUri = getImageUri(requireContext(), imageBitmap);
                Picasso.get().load(selectedImageUri).fit().centerCrop().into(binding.imgSelected);
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