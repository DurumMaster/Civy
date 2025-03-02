package dam.pmdm.nest.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dam.pmdm.nest.model.Comment;
import dam.pmdm.nest.model.Incidence;
import dam.pmdm.nest.model.UserApp;

public class DetailViewModel extends ViewModel {


    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference incidenceRef = database.getReference("incidences");

    private String incidenceId;

    public void setIncidenceId(String id) {
        if (!id.isEmpty()) {
            this.incidenceId = id;
            loadIncidenceData();
            loadComments();
        }
    }

    private final MutableLiveData<Incidence> incidence = new MutableLiveData<>();
    public LiveData<Incidence> getIncidence() {return incidence;}

    private final MutableLiveData<UserApp> creator = new MutableLiveData<>();
    public LiveData<UserApp> getCreator() {return creator;}

    private final MutableLiveData<List<Comment>> comments = new MutableLiveData<>();
    public LiveData<List<Comment>> getComments() {return comments;}


    private void loadIncidenceData() {

        incidenceRef.child(incidenceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Incidence i = dataSnapshot.getValue(Incidence.class);

                if (i != null) {
                    incidence.setValue(i);

                    loadCreatorData(i.getCreator());
                }
            }

            @Override
            public void onCancelled(DatabaseError e) {
                System.out.println(e.getMessage());
            }
        });
    }

    private void loadCreatorData(String creatorId) {

        DatabaseReference creatorRef = database.getReference("users").child(creatorId);

        creatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String creatorFirstName = dataSnapshot.child("firstName").getValue(String.class);
                String creatorLastName = dataSnapshot.child("lastName").getValue(String.class);
                String creatorImage = dataSnapshot.child("avatar").getValue(String.class);

                if (creatorFirstName != null && creatorLastName != null) {
                    UserApp c = new UserApp(creatorImage, creatorFirstName, creatorLastName);
                    creator.setValue(c);
                }
            }

            @Override
            public void onCancelled(DatabaseError e) {
                System.out.println(e.getMessage());
            }
        });
    }

    public void addComment(String incidenciaId, String mensaje) {
        String creationDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        Comment comentario = new Comment(mensaje, creationDate);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("incidences");

        String comentarioId = dbRef.child(incidenciaId).child("comments").push().getKey();
        if (comentarioId != null) {
            dbRef.child(incidenciaId).child("comments").child(comentarioId).setValue(comentario);
        }
    }

    private void loadComments() {
        DatabaseReference commentsRef = database.getReference("incidences").child(incidenceId).child("comments");

        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Comment> comentarioList = new ArrayList<>();
                for (DataSnapshot comentarioSnapshot : snapshot.getChildren()) {
                    Comment comentario = comentarioSnapshot.getValue(Comment.class);
                    if (comentario != null) {
                        comentarioList.add(comentario);
                    }
                }
                comments.setValue(comentarioList);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error.getMessage());
            }
        });
    }
}