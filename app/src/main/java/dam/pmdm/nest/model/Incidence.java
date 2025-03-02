package dam.pmdm.nest.model;

public class Incidence {

    private String id;
    private String title;
    private String description;
    private String image;
    private String creator;
    private String creationDate;
    private String professional;
    private String status;

    public Incidence() {};

    public Incidence(String title, String description, String image, String creator, String creationDate, String professional, String status) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.creator = creator;
        this.creationDate = creationDate;
        this.professional = professional;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getProfessional() {
        return professional;
    }

    public void setProfessional(String professional) {
        this.professional = professional;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
