package ee.vikk.kanban.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Task model class representing a task in a KanBan board
 */
public class Task {
    private Integer id;
    private Integer columnId;
    private String title;
    private String description;
    private Priority priority;
    private Integer position;
    private LocalDateTime createdAt;
    private LocalDate dueDate;
    private List<Tag> tags;

    /**
     * Priority enum
     */
    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    /**
     * Default constructor
     */
    public Task() {
        this.tags = new ArrayList<>();
        this.priority = Priority.MEDIUM;
    }

    /**
     * Constructor with basic fields
     * @param columnId Column ID this task belongs to
     * @param title Task title
     * @param position Task position in column
     */
    public Task(Integer columnId, String title, Integer position) {
        this();
        this.columnId = columnId;
        this.title = title;
        this.position = position;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor with all fields
     * @param id Task ID
     * @param columnId Column ID
     * @param title Task title
     * @param description Task description
     * @param priority Task priority
     * @param position Task position
     * @param createdAt Creation timestamp
     * @param dueDate Due date
     */
    public Task(Integer id, Integer columnId, String title, String description, 
                Priority priority, Integer position, LocalDateTime createdAt, LocalDate dueDate) {
        this();
        this.id = id;
        this.columnId = columnId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.position = position;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getColumnId() {
        return columnId;
    }

    public void setColumnId(Integer columnId) {
        this.columnId = columnId;
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

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", columnId=" + columnId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", position=" + position +
                ", createdAt=" + createdAt +
                ", dueDate=" + dueDate +
                ", tags=" + tags.size() +
                '}';
    }
}
