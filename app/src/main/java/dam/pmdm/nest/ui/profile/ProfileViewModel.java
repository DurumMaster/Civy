package dam.pmdm.nest.ui.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import dam.pmdm.nest.model.UserApp;

public class ProfileViewModel extends ViewModel {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference();
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();

    private final MutableLiveData<String> avatarUrl = new MutableLiveData<>();
    private final MutableLiveData<List<UserApp>> users = new MutableLiveData<>();
    private final MutableLiveData<UserApp> user = new MutableLiveData<>();

    public LiveData<UserApp> getUser() {
        return user;
    }
    public LiveData<List<UserApp>> getAllUsers() {return users;}
    public LiveData<String> getAvatarUrl() {return avatarUrl;}

    public void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.getReference("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String avatar = dataSnapshot.child("avatar").getValue(String.class);
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);
                        String phone = dataSnapshot.child("phone").getValue(String.class);
                        String floor = dataSnapshot.child("floor").getValue(String.class);
                        String block = dataSnapshot.child("block").getValue(String.class);
                        Boolean isAdmin = dataSnapshot.child("admin").getValue(Boolean.class);

                        if (avatar != null && !avatar.isEmpty() &&
                                firstName != null && !firstName.isEmpty() &&
                                lastName != null && !lastName.isEmpty() &&
                                floor != null && !floor.isEmpty() &&
                                block != null && !block.isEmpty() &&
                                phone != null && !phone.isEmpty()) {

                            UserApp userData = new UserApp(avatar, firstName, lastName, floor, block, phone, isAdmin);
                            user.setValue(userData);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println(databaseError.getMessage());
                }
            });
        }
    }

    public void loadAllUsers() {
        db.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<UserApp> userList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String avatar = snapshot.child("avatar").getValue(String.class);
                        String firstName = snapshot.child("firstName").getValue(String.class);
                        String lastName = snapshot.child("lastName").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        String floor = snapshot.child("floor").getValue(String.class);
                        String block = snapshot.child("block").getValue(String.class);
                        Boolean isAdmin = snapshot.child("admin").getValue(Boolean.class);

                        if (firstName != null && !firstName.isEmpty()) {
                            UserApp user = new UserApp(avatar, firstName, lastName, floor, block, phone, isAdmin);
                            userList.add(user);
                        }
                    }

                    users.setValue(userList);
                    }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());
            }
        });
    }

    public void uploadImage(Uri imageUri) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && imageUri != null) {
            String userId = currentUser.getUid();
            StorageReference fileRef = storageRef.child("avatars/" + userId + ".jpg");

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        db.getReference("users").child(userId).child("avatar").setValue(imageUrl)
                                .addOnSuccessListener(aVoid -> {
                                    avatarUrl.setValue(imageUrl);
                                })
                                .addOnFailureListener(Throwable::getMessage);
                    })
            ).addOnFailureListener(Throwable::getMessage);
        }
    }
}