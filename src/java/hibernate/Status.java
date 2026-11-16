package hibernate;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "status")
public class Status implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "value", length = 20, nullable = false)
    private String value;

    // Constructors
    public Status() {}

    public Status(String value) {
        this.value = value;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // toString
    @Override
    public String toString() {
        return "Status{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }
}
