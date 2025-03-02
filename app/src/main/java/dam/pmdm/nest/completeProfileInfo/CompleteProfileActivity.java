package dam.pmdm.nest.completeProfileInfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dam.pmdm.nest.MainActivity;
import dam.pmdm.nest.R;
import dam.pmdm.nest.databinding.ActivityCompleteProfileBinding;
import dam.pmdm.nest.model.UserApp;

public class CompleteProfileActivity extends AppCompatActivity {

    private ActivityCompleteProfileBinding binding;

    private FirebaseAuth mAuth;

    private String firstName, lastName, floor, block, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompleteProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

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

    private void saveDataToFirebase() {
        binding.pbProfile.setVisibility(View.VISIBLE);
        binding.btnSaveProfile.setEnabled(false);
        binding.btnSaveProfile.setText("");

        String userId = mAuth.getCurrentUser().getUid();

        UserApp userProfile = new UserApp(getString(R.string.default_avatar), firstName, lastName, floor, block, phone);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        databaseReference.setValue(userProfile)
                .addOnSuccessListener(aVoid -> {
                    binding.pbProfile.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    binding.btnSaveProfile.setText(R.string.btn_guardar);
                    enterApp();
                })
                .addOnFailureListener(e -> {
                    binding.pbProfile.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    binding.btnSaveProfile.setText(R.string.btn_guardar);
                    Toast.makeText(CompleteProfileActivity.this, R.string.err_retry, Toast.LENGTH_SHORT).show();
                });
    }

    private void enterApp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
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