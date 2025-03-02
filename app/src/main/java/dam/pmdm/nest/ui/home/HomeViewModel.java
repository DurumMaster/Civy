package dam.pmdm.nest.ui.home;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dam.pmdm.nest.model.Incidence;

public class HomeViewModel extends ViewModel {

    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private final MutableLiveData<List<Incidence>> incidenciasLD = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAdminLD = new MutableLiveData<>();
    public LiveData<List<Incidence>> getIncidences() {return incidenciasLD;}
    public LiveData<Boolean> getIsAdmin() {return isAdminLD;}

    public void loadUserStatusAndIncidences(String userId) {
        db.getReference("users").child(userId).child("admin")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean isAdmin = dataSnapshot.getValue(Boolean.class);
                        if (isAdmin != null) {
                            isAdminLD.setValue(isAdmin);
                            loadIncidences(isAdmin, userId);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.print(databaseError.getMessage());
                    }
                });
    }

    private void loadIncidences(boolean isAdmin, String userId) {
        if (isAdmin) {
            db.getReference("incidences")
                    .orderByChild("creationDate")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<Incidence> incidences = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Incidence incidence = snapshot.getValue(Incidence.class);
                                if (incidence != null) {
                                    incidence.setId(snapshot.getKey());
                                    incidences.add(incidence);
                                }
                            }
                            incidenciasLD.setValue(incidences);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.print(databaseError.getMessage());
                        }
                    });
        } else {
            db.getReference("incidences")
                    .orderByChild("creator")
                    .equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<Incidence> incidences = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Incidence incidence = snapshot.getValue(Incidence.class);
                                if (incidence != null) {
                                    incidence.setId(snapshot.getKey());
                                    incidences.add(incidence);
                                }
                            }
                            Collections.reverse(incidences);
                            incidenciasLD.setValue(incidences);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.print(databaseError.getMessage());
                        }
                    });
        }

    }

    public void deleteIncidence(String incidenceId, String imageUrl) {
        db.getReference("incidences").child(incidenceId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        deleteImageFromStorage(imageUrl);
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println(e.getMessage());
                });
    }

    private void deleteImageFromStorage(String imageUrl) {
        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Imagen eliminada con éxito de Firebase Storage.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error al eliminar imagen: " + e.getMessage());
                });
    }

    public void uploadImageToFirebase(Uri imageUri, OnImageUploadedListener listener) {
        StorageReference storageReference = storage.getReference().child("incidence_images/" + System.currentTimeMillis() + ".jpg");

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        listener.onImageUploaded(uri.toString());
                    }).addOnFailureListener(e -> {
                        listener.onFailure(e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(e.getMessage());
                });
    }

    public void updateIncidenceWithImage(Incidence originalIncidence, String newTitle, String newDescription, String newStatus, String newProfessional, String newImageUrl) {
        originalIncidence.setTitle(newTitle != null ? newTitle : originalIncidence.getTitle());
        originalIncidence.setDescription(newDescription != null ? newDescription : originalIncidence.getDescription());
        originalIncidence.setStatus(newStatus != null ? newStatus : originalIncidence.getStatus());
        originalIncidence.setProfessional(newProfessional != null ? newProfessional : originalIncidence.getProfessional());


        String oldImageUrl = originalIncidence.getImage();
        if (newImageUrl != null && !newImageUrl.isEmpty()) {
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                deleteImageFromStorage(oldImageUrl);
            }
            originalIncidence.setImage(newImageUrl);
        } else if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            deleteImageFromStorage(oldImageUrl);
            originalIncidence.setImage(null);
        }

        db.getReference("incidences").child(originalIncidence.getId())
                .setValue(originalIncidence)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Actualizada");
                })
                .addOnFailureListener(e -> {
                    System.out.println(e.getMessage());
                });
    }

    public interface OnImageUploadedListener {
        void onImageUploaded(String imageUrl);
        void onFailure(String error);
    }
}