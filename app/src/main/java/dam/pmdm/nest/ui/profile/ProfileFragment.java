package dam.pmdm.nest.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import dam.pmdm.nest.R;
import dam.pmdm.nest.databinding.FragmentProfileBinding;
import dam.pmdm.nest.model.UserApp;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ProfileViewModel viewModel;

    private FragmentProfileBinding binding;
    private UserAdapter userAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapter = new UserAdapter(new ArrayList<>());
        binding.rvUsers.setAdapter(userAdapter);

        binding.pbHome.setVisibility(View.VISIBLE);

        viewModel.getUser().observe(getViewLifecycleOwner(), new Observer<UserApp>() {
            @Override
            public void onChanged(UserApp user) {


                if (user != null) {
                    if (user.getAvatar() != null) {
                        Picasso.get().load(user.getAvatar()).fit().centerCrop().into(binding.imgProfile);
                    }
                    binding.txtFullName.setText(String.format(getString(R.string.txt_profile_fullName), user.getFirstName(), user.getLastName()));
                    binding.txtPhoneNumber.setText(user.getPhone());
                    binding.txtLocation.setText(String.format(getString(R.string.txt_profile_location), user.getFloor(), user.getBlock()));

                    if (user.isAdmin()) {
                        binding.rvUsers.setVisibility(View.VISIBLE);
                        viewModel.getAllUsers().observe(getViewLifecycleOwner(), userList ->  {
                            if (!userList.isEmpty()) {
                                userAdapter.updateList(userList);
                            }
                        });
                    } else {
                        binding.rvUsers.setVisibility(View.GONE);
                    }
                }
                binding.pbHome.setVisibility(View.GONE);
            }
        });

        viewModel.getAvatarUrl().observe(getViewLifecycleOwner(), url -> {
            if (url != null) {
                refresh();
            }
        });

        refresh();

        binding.fabEdit.setOnClickListener(v -> {
            EditProfileBottomSheetFragment editProfileBottomSheet = new EditProfileBottomSheetFragment();
            editProfileBottomSheet.setOnProfileUpdatedListener(this::refresh);
            editProfileBottomSheet.show(getParentFragmentManager(), editProfileBottomSheet.getTag());
        });
        binding.imgSettings.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_settings));

        binding.imgProfile.setOnClickListener(v -> openGallery());
    }

    private void refresh() {
        viewModel.loadUserData();
        viewModel.loadAllUsers();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.selecciona_una_imagen)), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            viewModel.uploadImage(imageUri);
            binding.pbHome.setVisibility(View.VISIBLE);
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}