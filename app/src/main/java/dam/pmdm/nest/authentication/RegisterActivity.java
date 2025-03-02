package dam.pmdm.nest.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dam.pmdm.nest.MainActivity;
import dam.pmdm.nest.R;
import dam.pmdm.nest.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnRegister.setOnClickListener(v -> validate());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void validate() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        boolean isValid = true;

        if (!isValidEmail(email)) {
            binding.tiEmailLayout.setError(getString(R.string.et_err_login_correo));
            isValid = false;
        } else {
            binding.tiEmailLayout.setError(null);
        }

        if (!isValidPassword(password)) {
            binding.tiPasswordLayout.setError(getString(R.string.et_err_login_password));
            isValid = false;
        } else {
            binding.tiPasswordLayout.setError(null);
        }

        if (isValid) {
            register(email, password);
        }
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6 && password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    public void register (String email, String password) {

        binding.pbRegister.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        binding.pbRegister.setVisibility(View.GONE);
                        binding.btnRegister.setEnabled(true);
                        binding.btnRegister.setText(R.string.btn_registrarse);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserData(user);
                            goToLogin(user);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.err_registro_usu_existe, Toast.LENGTH_SHORT).show();
                            goToLogin(null);
                        }
                    }
                });
    }

    private void saveUserData(FirebaseUser user) {
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

            userRef.child("avatar").setValue(String.valueOf(R.string.default_avatar));
            userRef.child("firstName").setValue("");
            userRef.child("lastName").setValue("");
            userRef.child("floor").setValue("");
            userRef.child("block").setValue("");
            userRef.child("phone").setValue("");
            userRef.child("isAdmin").setValue(false);
        }
    }

    private void goToLogin(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }
}