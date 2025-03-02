package dam.pmdm.nest.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dam.pmdm.nest.MainActivity;
import dam.pmdm.nest.R;
import dam.pmdm.nest.completeProfileInfo.CompleteProfileActivity;
import dam.pmdm.nest.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    private ActivityLoginBinding binding;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            enterApp(currentUser);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            login(email, password);
        });

        binding.btnRegiserEP.setOnClickListener(v-> {
            Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(i);
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, R.string.err_retry, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkAndCreateUserInDatabase(user);
                        enterApp(user);
                    } else {
                        Toast.makeText(this, R.string.err_retry, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkAndCreateUserInDatabase(FirebaseUser user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    userRef.child("avatar").setValue(String.valueOf(R.string.default_avatar));
                    userRef.child("firstName").setValue("");
                    userRef.child("lastName").setValue("");
                    userRef.child("floor").setValue("");
                    userRef.child("block").setValue("");
                    userRef.child("phone").setValue("");
                    userRef.child("isAdmin").setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), R.string.err_datos_inicio, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void login (String email, String password) {

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.err_btn_login, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.pbLogin.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        binding.pbLogin.setVisibility(View.GONE);
                        binding.btnLogin.setEnabled(true);
                        binding.btnLogin.setText(R.string.btn_iniciar_sesion);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            enterApp(user);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.err_login, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void enterApp(FirebaseUser user) {
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    String floor = dataSnapshot.child("floor").getValue(String.class);
                    String block = dataSnapshot.child("block").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);

                    if (firstName == null || firstName.isEmpty() ||
                            lastName == null || lastName.isEmpty() ||
                            floor == null || floor.isEmpty() ||
                            block == null || block.isEmpty() ||
                            phone == null || phone.isEmpty()) {
                        Intent intent = new Intent(getApplicationContext(), CompleteProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), R.string.err_datos_inicio, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}