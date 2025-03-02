package dam.pmdm.nest.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import dam.pmdm.nest.R;
import dam.pmdm.nest.databinding.ItemUserBinding;
import dam.pmdm.nest.model.UserApp;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<UserApp> users;

    public UserAdapter(List<UserApp> users) {this.users = users;}

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserApp user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateList(List<UserApp> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        private final ItemUserBinding binding;

        public UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UserApp user) {
            Context context = itemView.getContext();

            binding.txtUserName.setText(String.format(context.getString(R.string.txt_profile_fullName), user.getFirstName(), user.getLastName()));
            binding.txtUserLocation.setText(String.format(context.getString(R.string.txt_profile_location), user.getFloor(), user.getBlock()));
            binding.txtUserPhone.setText(user.getPhone());

            if (!user.getAvatar().isEmpty()) {
                Picasso.get().load(user.getAvatar()).fit().centerCrop().into(binding.imgUserAvatar);
            }

        }
    }
}

