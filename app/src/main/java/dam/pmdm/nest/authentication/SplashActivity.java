package dam.pmdm.nest.authentication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dam.pmdm.nest.MainActivity;
import dam.pmdm.nest.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }

        checkAuthentication();
    }

    private void checkAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            goToLogin();
        } else {
            goToMain();
        }
    }

    private void goToLogin() {
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }

    private void goToMain() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }
}
