package hibernate;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "order_status")
public class OrderStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "value", length = 20, nullable = false)
    private String value;

    // Constructors
    public OrderStatus() {}

    public OrderStatus(String value) {
        this.value = value;
    }

    // Getters & Setters
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

    @Override
    public String toString() {
        return "OrderStatus{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }
}
