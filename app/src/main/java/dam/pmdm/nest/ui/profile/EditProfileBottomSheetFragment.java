package dam.pmdm.nest.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dam.pmdm.nest.R;
import dam.pmdm.nest.databinding.FragmentEditProfileBinding;
import dam.pmdm.nest.model.UserApp;

public class EditProfileBottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private String avatar, firstName, lastName, floor, block, phone;
    private boolean isAdmin;
    private OnProfileUpdatedListener listener;
    private ProfileViewModel profileViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        loadUserData();

        binding.btnSaveProfile.setOnClickListener(v -> {
            firstName = binding.etFirstName.getText().toString().trim();
            lastName = binding.etLastName.getText().toString().trim();
            floor = binding.etFloor.getText().toString().trim();
            block = binding.etBlock.getText().toString().trim();
            phone = binding.etPhone.getText().toString().trim();

            if (validateFields()) {
                saveDataToFirebase();
            }
        });
    }

    public interface OnProfileUpdatedListener {
        void onProfileUpdated();
    }

    public void setOnProfileUpdatedListener(OnProfileUpdatedListener listener) {
        this.listener = listener;
    }


    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        databaseReference.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                UserApp userProfile = dataSnapshot.getValue(UserApp.class);
                if (userProfile != null) {
                    avatar = userProfile.getAvatar();
                    binding.etFirstName.setText(userProfile.getFirstName());
                    binding.etLastName.setText(userProfile.getLastName());
                    binding.etFloor.setText(userProfile.getFloor());
                    binding.etBlock.setText(userProfile.getBlock());
                    binding.etPhone.setText(userProfile.getPhone());
                    isAdmin = userProfile.isAdmin();
                }
            }
        });
    }

    private void saveDataToFirebase() {
        binding.pbProfile.setVisibility(View.VISIBLE);
        binding.btnSaveProfile.setEnabled(false);
        binding.btnSaveProfile.setText("");

        String userId = mAuth.getCurrentUser().getUid();

        UserApp userProfile = new UserApp(avatar, firstName, lastName, floor, block, phone, isAdmin);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        databaseReference.setValue(userProfile)
                .addOnSuccessListener(aVoid -> {
                    binding.pbProfile.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    binding.btnSaveProfile.setText(R.string.btn_guardar);

                    if (listener != null) {
                        listener.onProfileUpdated(); // Notificar que el perfil se ha actualizado
                    }
                    //profileViewModel.loadUserData();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    binding.pbProfile.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    binding.btnSaveProfile.setText(R.string.btn_guardar);
                    Toast.makeText(getContext(), R.string.err_retry, Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateFields() {

        if (TextUtils.isEmpty(firstName)) {
            binding.tiFirstNameLayout.setError(getString(R.string.error_nombre));
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            binding.tiLastNameLayout.setError(getString(R.string.error_apellidos));
            return false;
        }

        if (TextUtils.isEmpty(floor)) {
            binding.tiFloorLayout.setError(getString(R.string.error_piso));
            return false;
        }

        if (TextUtils.isEmpty(block)) {
            binding.tiBlockLayout.setError(getString(R.string.error_bloque));
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            binding.tiPhoneLayout.setError(getString(R.string.error_telefono));
            return false;
        }

        if (!phone.matches("\\d{9,}")) {
            binding.tiPhoneLayout.setError(getString(R.string.error_telefono_invalido));
            return false;
        }

        return true;
    }
}

