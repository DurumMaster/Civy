package dam.pmdm.nest.ui.create;

import android.net.Uri;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import dam.pmdm.nest.R;

public class CreateViewModel extends ViewModel {

    private static final String DEFAULT_STATUS = "PENDIENTE";
    private static final String DEFAULT_PROFESSIONAL = "SIN ASIGNAR";

    private final MutableLiveData<Boolean> isPublishing = new MutableLiveData<>(false);
    private final MutableLiveData<String> publishStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSuccessful = new MutableLiveData<>(false);

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final DatabaseReference incidencesRef = database.getReference("incidences");

    public LiveData<Boolean> isPublishing() {
        return isPublishing;
    }

    public LiveData<String> getPublishStatus() {
        return publishStatus;
    }

    public LiveData<Boolean> getIsSuccessful() {
        return isSuccessful;
    }

    public void publishIncident(String title, String description, Uri imageUri) {
        if (title.isEmpty() || description.isEmpty()) {
            return;
        }

        isPublishing.setValue(true);

        if (imageUri != null) {
            uploadImage(imageUri, title, description);
        } else {
            saveIncident(title, description, null);
        }
    }

    private void uploadImage(Uri imageUri, String title, String description) {
        StorageReference storageRef = storage.getReference().child("incidence_images/" + System.currentTimeMillis() + ".jpg");
        UploadTask uploadTask = storageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            saveIncident(title, description, uri.toString());
        })).addOnFailureListener(e -> {
            isPublishing.setValue(false);
            publishStatus.setValue(String.valueOf(R.string.err_push_image));
            isSuccessful.setValue(false);
        });
    }

    private void saveIncident(String title, String description, String imageUrl) {
        String incidenceId = incidencesRef.push().getKey();
        if (incidenceId != null) {
            long currentTimeMillis = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String creationDate = sdf.format(new Date(currentTimeMillis));

            String creator = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> incidenceData = new HashMap<>();
            incidenceData.put("title", title);
            incidenceData.put("description", description);
            incidenceData.put("image", imageUrl);
            incidenceData.put("creator", creator);
            incidenceData.put("creationDate", creationDate);
            incidenceData.put("status", DEFAULT_STATUS);
            incidenceData.put("professional", DEFAULT_PROFESSIONAL);

            incidencesRef.child(incidenceId).setValue(incidenceData)
                    .addOnSuccessListener(aVoid -> {
                        isPublishing.setValue(false);
                        isSuccessful.setValue(true);
                    })
                    .addOnFailureListener(e -> {
                        isPublishing.setValue(false);
                        publishStatus.setValue(String.valueOf(R.string.err_save_incidence));
                        isSuccessful.setValue(false);
                    });
        } else {
            isPublishing.setValue(false);
            publishStatus.setValue(String.valueOf(R.string.err_retry));
            isSuccessful.setValue(false);
        }
    }
}
