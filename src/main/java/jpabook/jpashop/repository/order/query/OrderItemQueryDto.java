package jpabook.jpashop.repository.order.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderItemQueryDto {

    private Long orderId;
    private String itemName;
    private int orderPrice;
    private int orderCount;

    public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int orderCount) {
        this.orderId = orderId;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.orderCount = orderCount;
    }

}
