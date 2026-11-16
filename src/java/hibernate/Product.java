package hibernate;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "product")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name", length = 45)
    private String name;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "price")
    private double price;

    @Column(name = "qty")
    private int qty;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created_at;

    @Column(name = "status_id")
    private int statusId;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "seller_id")
    private int sellerId;

    @Column(name = "image_1", length = 45)
    private String image1;

    @Column(name = "image_2", length = 45)
    private String image2;

    // Constructors
    public Product() {}

    public Product(String name, String title, double price, int qty, String description,
                   int statusId, int userId, int sellerId, String image1, String image2) {
        this.name = name;
        this.title = title;
        this.price = price;
        this.qty = qty;
        this.description = description;
        this.created_at = new Date();
        this.statusId = statusId;
        this.userId = userId;
        this.sellerId = sellerId;
        this.image1 = image1;
        this.image2 = image2;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getImage1() { return image1; }
    public void setImage1(String image1) { this.image1 = image1; }

    public String getImage2() { return image2; }
    public void setImage2(String image2) { this.image2 = image2; }

    // toString
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", qty=" + qty +
                ", description='" + description + '\'' +
                ", created_at=" + created_at +
                ", statusId=" + statusId +
                ", userId=" + userId +
                ", sellerId=" + sellerId +
                ", image1='" + image1 + '\'' +
                ", image2='" + image2 + '\'' +
                '}';
    }
}
