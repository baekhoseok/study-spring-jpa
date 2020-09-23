package jpabook.jpashop.domain.item;

import jpabook.jpashop.controller.BookForm;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Setter @Getter
public abstract class Item {
    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    private String name;

    private int price;

    private int stockQuantity;

    /**
     * stock 증가
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if(restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }

    /**
     * 업데이트
     */
    public void update(BookForm form) {
        this.price = form.getPrice();
        this.stockQuantity = form.getStockQuantity();
        this.name = form.getName();
    }

}
