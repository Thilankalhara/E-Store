package hibernate;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cart")
public class Cart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // optional, but explicit
    private int id;

    @Column(name = "qty", nullable = false)
    private int qty;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER by default for ManyToOne, but explicit
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Required default constructor
    public Cart() {}

    // Convenience constructor
    public Cart(int qty, Product product, User user) {
        this.qty = qty;
        this.product = product;
        this.user = user;
    }

    // Getters & setters
    public int getId() {
        return id;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", qty=" + qty +
                ", product=" + (product != null ? product.getId() : null) +
                ", user=" + (user != null ? user.getId() : null) +
                '}';
    }
}
