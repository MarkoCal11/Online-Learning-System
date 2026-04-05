package hr.javafx.onlinelearningsystem.model;

public abstract class Entity implements Identifiable {
    private Long id;

    protected Entity(){}

    protected Entity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }
}
