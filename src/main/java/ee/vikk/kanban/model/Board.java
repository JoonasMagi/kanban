package ee.vikk.kanban.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Board model class representing a KanBan board
 */
public class Board {
    private Integer id;
    private String name;
    private LocalDateTime createdAt;
    private List<Column> columns;

    /**
     * Default constructor
     */
    public Board() {
        this.columns = new ArrayList<>();
    }

    /**
     * Constructor with name
     * @param name Board name
     */
    public Board(String name) {
        this();
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor with all fields
     * @param id Board ID
     * @param name Board name
     * @param createdAt Creation timestamp
     */
    public Board(Integer id, String name, LocalDateTime createdAt) {
        this();
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    @Override
    public String toString() {
        return "Board{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", columns=" + columns.size() +
                '}';
    }
}
