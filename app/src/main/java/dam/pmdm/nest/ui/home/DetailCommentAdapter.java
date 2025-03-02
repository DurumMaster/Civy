package dam.pmdm.nest.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dam.pmdm.nest.R;
import dam.pmdm.nest.model.Comment;

public class DetailCommentAdapter extends RecyclerView.Adapter<DetailCommentAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public DetailCommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comentario = comments.get(position);
        holder.bind(comentario);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView txtMensaje, txtFecha;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMensaje = itemView.findViewById(R.id.txtMessage);
            txtFecha = itemView.findViewById(R.id.txtCommentDate);
        }

        public void bind(Comment comentario) {
            txtMensaje.setText(comentario.getMessage());
            txtFecha.setText(comentario.getCreationDate());
        }
    }
}
