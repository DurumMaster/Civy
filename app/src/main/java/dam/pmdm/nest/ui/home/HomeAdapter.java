package dam.pmdm.nest.ui.home;

import android.app.AlertDialog;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dam.pmdm.nest.R;
import dam.pmdm.nest.databinding.ItemIncidenciaBinding;
import dam.pmdm.nest.model.Incidence;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder>  {

    private List<Incidence> incidences = Collections.emptyList();
    private OnIncidenceClickListener listener;
    private FragmentActivity activity;

    public interface OnIncidenceClickListener {
        void onIncidenceClick(Incidence incidence);
    }

    private static final String[] INCIDENCE_STATUS = {"PENDIENTE", "EN PROCESO", "COMPLETADA"};
    private static final String[] PROFESSIONALS = {"SIN ASIGNAR", "FONTANERO", "ALBAÑIL", "ELECTRICISTA"};
    private static final int[] PROFESSIONAL_IMAGES = {R.drawable.ic_sin_asignar, R.drawable.ic_fontanero, R.drawable.ic_albanil, R.drawable.ic_electricista};
    private static final int[] PROFESSIONAL_COLORS = {R.color.inc_back_grey, R.color.inc_back_blue, R.color.inc_back_red, R.color.inc_back_yellow};


    public HomeAdapter(List<Incidence> incidences, OnIncidenceClickListener listener, FragmentActivity activity) {
        this.incidences = incidences;
        this.listener = listener;
        this.activity = activity;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIncidenciaBinding binding = ItemIncidenciaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HomeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        Incidence incidence = incidences.get(position);
        holder.bind(incidence, position);
    }

    @Override
    public int getItemCount() {
        return incidences.size();
    }

    public void updateList(List<Incidence> newIncidences) {
        this.incidences = new ArrayList<>(newIncidences);
        notifyDataSetChanged();
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder {

        private final ItemIncidenciaBinding binding;

        public HomeViewHolder(ItemIncidenciaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            HomeViewModel  viewModel = new ViewModelProvider((ViewModelStoreOwner) activity).get(HomeViewModel.class);

            binding.imgMore.setOnClickListener(v -> {

                View popupView = LayoutInflater.from(v.getContext()).inflate(R.layout.popup_menu, null);
                PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

                popupWindow.setElevation(10);
                int xOffset = (int) (5 * v.getResources().getDisplayMetrics().density);
                popupWindow.showAsDropDown(v, xOffset, 0);

                LinearLayout btnEdit = popupView.findViewById(R.id.btn_edit);
                LinearLayout btnDelete = popupView.findViewById(R.id.btn_delete);



                btnEdit.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Incidence currentIncidence = incidences.get(position);

                        EditIncidenceButtonSheetFragment bottomSheet = new EditIncidenceButtonSheetFragment(currentIncidence);

                        bottomSheet.show(activity.getSupportFragmentManager(), "EditIncidenceFragment");

                        bottomSheet.setOnIncidenceUpdatedListener(updatedIncidence -> {
                            incidences.set(position, updatedIncidence);
                            notifyItemChanged(position);
                        });
                    }
                    popupWindow.dismiss();
                });

                btnDelete.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.title_eliminar_in)
                                .setMessage(R.string.message_eliminar_in)
                                .setPositiveButton(R.string.confirmar, (dialog, which) -> {
                                    Incidence currentIncidence = incidences.get(position);
                                    String imageUrl = currentIncidence.getImage();
                                    viewModel.deleteIncidence(currentIncidence.getId(), imageUrl);
                                    incidences.remove(position);
                                    updateList(new ArrayList<>(incidences));
                                })
                                .setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.dismiss())
                                .setCancelable(true)
                                .show();
                    }
                    popupWindow.dismiss();
                });
            });

            binding.cvItem.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Incidence clickedIncidence = incidences.get(position);
                    if (listener != null) {
                        listener.onIncidenceClick(clickedIncidence);
                    }
                }
            });
        }

        public void bind(Incidence incidence, int position) {
            binding.txtTitle.setText(incidence.getTitle());
            binding.txtDate.setText(incidence.getCreationDate());

            Picasso.get()
                    .load(incidence.getImage())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(binding.imgIncidence);

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
            GradientDrawable drawable = (GradientDrawable) binding.vStatus.getBackground();
            drawable.setColor(colorEstado);

            String professional = incidence.getProfessional();
            int index = getProfessionalIndex(professional);
            binding.imgInTipe.setImageResource(PROFESSIONAL_IMAGES[index]);

            binding.cvItem.setCardBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), PROFESSIONAL_COLORS[index]));
        }

        private int getProfessionalIndex(String professional) {
            for (int i = 0; i < PROFESSIONALS.length; i++) {
                if (PROFESSIONALS[i].equals(professional)) {
                    return i;
                }
            }
            return 0;
        }
    }
}
