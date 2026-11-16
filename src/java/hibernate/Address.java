package hibernate;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "address")
public class Address implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "line_1", nullable = true)
    private String line_1;

    @Column(name = "line_2", nullable = true)
    private String line_2;

    @Column(name = "postal_code", length = 10, nullable = true)
    private String postal_code;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public Address() {}

    public Address(String line_1, String line_2, String postal_code, User user) {
        this.line_1 = line_1;
        this.line_2 = line_2;
        this.postal_code = postal_code;
        this.user = user;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLine_1() {
        return line_1;
    }

    public void setLine_1(String line_1) {
        this.line_1 = line_1;
    }

    public String getLine_2() {
        return line_2;
    }

    public void setLine_2(String line_2) {
        this.line_2 = line_2;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", line_1='" + line_1 + '\'' +
                ", line_2='" + line_2 + '\'' +
                ", postal_code='" + postal_code + '\'' +
                ", userId=" + (user != null ? user.getId() : "null") +
                '}';
    }
}
