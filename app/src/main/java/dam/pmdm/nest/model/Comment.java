package dam.pmdm.nest.model;

public class Comment {

    private String message;
    private String creationDate;

    public Comment(String message, String creationDate) {
        this.message = message;
        this.creationDate = creationDate;
    }

    public Comment() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
