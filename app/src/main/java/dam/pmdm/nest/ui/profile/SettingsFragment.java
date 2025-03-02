package dam.pmdm.nest.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;

import dam.pmdm.nest.R;
import dam.pmdm.nest.authentication.LoginActivity;
import dam.pmdm.nest.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setUpListeners();

        return root;
    }

    private void setUpListeners() {
        binding.imgBack.setOnClickListener(view ->
                Navigation.findNavController(view).navigate(R.id.navigation_profile)
        );

        binding.contentFaq.setOnClickListener(view -> showFaq());
        binding.contentRequestAdmin.setOnClickListener(view -> requestAdmin());
        binding.contentLogout.setOnClickListener(view -> logout());
    }

    private void showFaq() {
        Toast.makeText(getContext(), "Mostrando Preguntas Frecuentes...", Toast.LENGTH_SHORT).show();
    }

    private void requestAdmin() {
        String emailBody = getString(R.string.email_body);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"civy.support@civy.es"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.solicitud_de_permisos_de_administrador));
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.elige_una_aplicaci_n_de_correo)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.no_se_puede_enviar_el_correo_no_hay_aplicaci_n_disponible, Toast.LENGTH_SHORT).show();
        }
    }


    private void logout() {
        showConfirmationDialog(getString(R.string.txt_message_alert_settings), () -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(requireActivity(), LoginActivity.class);
            startActivity(i);
            requireActivity().finish();
        });
    }


    private void showConfirmationDialog(String message, Runnable onConfirmAction) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.txt_title_alert_settings)
                .setMessage(message)
                .setPositiveButton(R.string.confirmar, (dialog, which) -> onConfirmAction.run())
                .setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar el binding para evitar fugas de memoria
        binding = null;
    }
}