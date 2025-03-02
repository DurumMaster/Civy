package dam.pmdm.nest.ui.home;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import dam.pmdm.nest.R;
import dam.pmdm.nest.databinding.FragmentDetailBinding;
import dam.pmdm.nest.model.Incidence;
import dam.pmdm.nest.model.UserApp;

public class DetailFragment extends Fragment {

    private DetailViewModel viewModel;
    private HomeViewModel homeViewModel;
    private FragmentDetailBinding binding;
    private DetailCommentAdapter adapter;

    private static final String[] INCIDENCE_STATUS = {"PENDIENTE", "EN PROCESO", "COMPLETADA"};
    private static final String[] PROFESSIONALS = {"SIN ASIGNAR", "FONTANERO", "ALBAÑIL", "ELECTRICISTA"};
    private static final int[] PROFESSIONAL_IMAGES = {R.drawable.ic_sin_asignar, R.drawable.ic_fontanero, R.drawable.ic_albanil, R.drawable.ic_electricista};
    private static final int[] PROFESSIONAL_COLORS = {R.color.inc_back_grey, R.color.inc_back_blue, R.color.inc_back_red, R.color.inc_back_yellow};

    public static DetailFragment newInstance() {
        return new DetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DetailCommentAdapter(new ArrayList<>());
        binding.rvComments.setAdapter(adapter);


        viewModel = new ViewModelProvider(requireActivity()).get(DetailViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        binding.pbDetail.setVisibility(View.VISIBLE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId != null) {
            homeViewModel.loadUserStatusAndIncidences(userId);

            homeViewModel.getIsAdmin().observe(getViewLifecycleOwner(), isAdmin -> {
                if (isAdmin) {
                    binding.imgAddComment.setOnClickListener(v -> showAddCommentDialog());
                }
            });
        }

        viewModel.getComments().observe(getViewLifecycleOwner(), comentarios -> {
            if (comentarios != null) {
                adapter.setComments(comentarios);
            }
        });

        viewModel.getIncidence().observe(getViewLifecycleOwner(), incidence -> {

            if (incidence != null) {
                updateIncidenceUI(incidence);
                binding.pbDetail.setVisibility(View.GONE);
            }
        });

        viewModel.getCreator().observe(getViewLifecycleOwner(), creator -> {
            if (creator != null) {
                updateCreatorUI(creator);
                binding.pbDetail.setVisibility(View.GONE);
            }
        });

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_home));
        binding.btnShare.setOnClickListener(v -> shareIncidence());
    }

    private void showAddCommentDialog() {
        final EditText input = new EditText(getContext());
        input.setHint("Escribe tu comentario...");

        new AlertDialog.Builder(getContext())
                .setTitle("Agregar Comentario")
                .setMessage("Escribe un mensaje para agregar a la incidencia.")
                .setView(input)
                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mensaje = input.getText().toString().trim();
                        if (!mensaje.isEmpty()) {
                            String incidenciaId = viewModel.getIncidence().getValue().getId();
                            viewModel.addComment(incidenciaId, mensaje);
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateIncidenceUI(Incidence incidence) {
        binding.txtTitle.setText(incidence.getTitle());
        binding.txtDate.setText(incidence.getCreationDate());
        binding.txtDescription.setText(incidence.getDescription());
        binding.txtStatus.setText(incidence.getStatus());
        binding.txtProfessional.setText(incidence.getProfessional());

        if (incidence.getImage() != null && !incidence.getImage().isEmpty()) {
            Picasso.get().load(incidence.getImage()).fit().centerCrop().into(binding.imgIncidence);
        }

        int colorEstado;
        if (incidence.getStatus().equals(INCIDENCE_STATUS[0])) {
            colorEstado = ContextCompat.getColor(binding.getRoot().getContext(), R.color.status_red);
        } else if (incidence.getStatus().equals(INCIDENCE_STATUS[1])) {
            colorEstado = ContextCompat.getColor(binding.getRoot().getContext(), R.color.status_orange);
        } else if (incidence.getStatus().equals(INCIDENCE_STATUS[2])) {
            colorEstado = ContextCompat.getColor(binding.getRoot().getContext(), R.color.status_green);
        } else {
            colorEstado = ContextCompat.getColor(binding.getRoot().getContext(), R.color.black);
        }
        binding.cvStatus.setCardBackgroundColor(colorEstado);

        String professional = incidence.getProfessional();
        int index = getProfessionalIndex(professional);
        binding.imgProfessional.setImageResource(PROFESSIONAL_IMAGES[index]);
        binding.cvProfessional.setCardBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), PROFESSIONAL_COLORS[index]));
    }

    private int getProfessionalIndex(String professional) {
        for (int i = 0; i < PROFESSIONALS.length; i++) {
            if (PROFESSIONALS[i].equals(professional)) {
                return i;
            }
        }
        return 0;
    }

    private void updateCreatorUI(UserApp creator) {
        String fullName = creator.getFirstName() + " " + creator.getLastName();
        binding.txtCreator.setText(fullName);

        if (creator.getAvatar() != null && !creator.getAvatar().isEmpty()) {
            Picasso.get().load(creator.getAvatar()).fit().centerCrop().into(binding.imgCreator);
        }
    }

    private void shareIncidence() {
        Incidence incidence = viewModel.getIncidence().getValue();
        if (incidence != null) {
            String shareText = getString(R.string.share_incidence,
                    incidence.getTitle(),
                    incidence.getCreationDate(),
                    incidence.getDescription());

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
        } else {
            Toast.makeText(getContext(), R.string.err_no_incidence_data, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}