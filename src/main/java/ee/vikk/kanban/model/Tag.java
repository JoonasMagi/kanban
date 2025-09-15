package ee.vikk.kanban.model;

/**
 * Tag model class representing a tag for tasks
 */
public class Tag {
    private Integer id;
    private String name;
    private String color;

    /**
     * Default constructor
     */
    public Tag() {
    }

    /**
     * Constructor with name and color
     * @param name Tag name
     * @param color Tag color (hex format)
     */
    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
    }

    /**
     * Constructor with all fields
     * @param id Tag ID
     * @param name Tag name
     * @param color Tag color (hex format)
     */
    public Tag(Integer id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        if (id != null ? !id.equals(tag.id) : tag.id != null) return false;
        if (name != null ? !name.equals(tag.name) : tag.name != null) return false;
        return color != null ? color.equals(tag.color) : tag.color == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }
}
