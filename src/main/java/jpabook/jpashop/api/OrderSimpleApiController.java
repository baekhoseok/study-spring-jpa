package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSImpleQueryRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSImpleQueryRepository orderSImpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        for (Order order : orders) {
            order.getMember().getName(); //lazy 강제 초기화
            order.getDelivery().getAddress();
        }
        return orders;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<OrderSimpleDto> ordersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<OrderSimpleDto> orderDtos = orders.stream()
                .map(o -> new OrderSimpleDto(o))
                .collect(Collectors.toList());
        return orderDtos;
    }

    @GetMapping("/api/v3/simple-orders")
    public List<OrderSimpleDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<OrderSimpleDto> orderDtos = orders.stream()
                .map(o -> new OrderSimpleDto(o))
                .collect(Collectors.toList());
        return orderDtos;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSImpleQueryRepository.findOrderDtos();
    }

    @Data
    static class OrderSimpleDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public OrderSimpleDto(Order o) {
            this.orderId = o.getId();
            this.name = o.getMember().getName();
            this.orderDate = o.getOrderDate();
            this.orderStatus = o.getStatus();
            this.address = o.getDelivery().getAddress();
        }
    }

}
