package dam.pmdm.nest.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import dam.pmdm.nest.R;
import dam.pmdm.nest.authentication.LoginActivity;
import dam.pmdm.nest.databinding.FragmentHomeBinding;
import dam.pmdm.nest.model.Incidence;

public class HomeFragment extends Fragment implements HomeAdapter.OnIncidenceClickListener  {

    private FragmentHomeBinding binding;
    private HomeAdapter adapter;
    private HomeViewModel viewModel;
    private DetailViewModel dViewModel;
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getContext(), LoginActivity.class));
            return null;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        final boolean[] isProfileComplete = {true};
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String firstName = dataSnapshot.child("firstName").getValue(String.class);
                String lastName = dataSnapshot.child("lastName").getValue(String.class);
                String floor = dataSnapshot.child("floor").getValue(String.class);
                String block = dataSnapshot.child("block").getValue(String.class);
                String phone = dataSnapshot.child("phone").getValue(String.class);

                if (firstName == null || lastName == null || floor == null || block == null || phone == null ||
                        firstName.isEmpty() || lastName.isEmpty() || floor.isEmpty() || block.isEmpty() || phone.isEmpty()) {
                    isProfileComplete[0] = false;
                    startActivity(new Intent(getContext(), LoginActivity.class));
                } else {
                    isProfileComplete[0] = true;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println(error.getMessage());
            }
        });

        if (!isProfileComplete[0]) {
            return null;
        }

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvIncidence.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HomeAdapter(new ArrayList<>(), this, requireActivity());
        binding.rvIncidence.setAdapter(adapter);

        viewModel = new ViewModelProvider(HomeFragment.this).get(HomeViewModel.class);
        dViewModel = new ViewModelProvider(requireActivity()).get(DetailViewModel.class);

        viewModel.loadUserStatusAndIncidences(userId);

        viewModel.getIncidences().observe(getViewLifecycleOwner(), incidences -> {
            adapter.updateList(incidences);
            binding.pbHome.setVisibility(View.GONE);
            if (incidences.isEmpty()) {
                binding.llNoIncidences.setVisibility(View.VISIBLE);
            } else {
                binding.llNoIncidences.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onIncidenceClick(Incidence incidence) {
        dViewModel.setIncidenceId(incidence.getId());
        Navigation.findNavController(getView()).navigate(R.id.navigation_incidence_detail);
    }
}