package ee.vikk.kanban.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Column model class representing a column in a KanBan board
 */
public class Column {
    private Integer id;
    private Integer boardId;
    private String name;
    private Integer position;
    private String color;
    private List<Task> tasks;

    /**
     * Default constructor
     */
    public Column() {
        this.tasks = new ArrayList<>();
        this.color = "#808080"; // Default gray color
    }

    /**
     * Constructor with basic fields
     * @param boardId Board ID this column belongs to
     * @param name Column name
     * @param position Column position
     */
    public Column(Integer boardId, String name, Integer position) {
        this();
        this.boardId = boardId;
        this.name = name;
        this.position = position;
    }

    /**
     * Constructor with all fields
     * @param id Column ID
     * @param boardId Board ID
     * @param name Column name
     * @param position Column position
     * @param color Column color
     */
    public Column(Integer id, Integer boardId, String name, Integer position, String color) {
        this();
        this.id = id;
        this.boardId = boardId;
        this.name = name;
        this.position = position;
        this.color = color;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBoardId() {
        return boardId;
    }

    public void setBoardId(Integer boardId) {
        this.boardId = boardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

    @Override
    public String toString() {
        return "Column{" +
                "id=" + id +
                ", boardId=" + boardId +
                ", name='" + name + '\'' +
                ", position=" + position +
                ", color='" + color + '\'' +
                ", tasks=" + tasks.size() +
                '}';
    }
}
