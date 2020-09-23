package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    /**
     * Entity 직접반환
     *  1. api 와 db 가 분리되지 않아 유지보수 힘
     *  2. 연관 관계 객체들을 N + 1 방식으로 읽어온다.
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                orderItem.getItem().getName();
            }
        }
        return all;
    }

    /**
     * Entity -> DTO로 변환해서 반환듬
     *  1. api 와 db 가 분리됨
     *  2. 연관 관계 객체들을 N + 1 방식으로 읽어온다.
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * Entity -> DTO로 변환해서 반환
     * OneToMany 관계에서 fetch join 사용
     *  1. 동일한 One(Order)쪽의 데이터를 Many(Item)쪽의 개수만큼 중복해서 읽어온다.
     *  2. 이를 해결하기 위해 select 절에 Object용 distinct를 사용한다.
     *      (distinct사용하 jpa 에서 order중복을 막아준)
     *  3. 페이징 적용시 DB에서 적용되지 않고 메모리에서 모든데이터를 로딩하여 메모리상에서 페이징 처리한다.
     *     대용량 데이터에서 페이징 처리시 큰 문제가 된다.
     *     ( One(Order)쪽의 데이터가 중복됨으로 DB에서 페이징 자체가 불가능하다.
     *     따라서 OneToMany 에서는 페이징처리 하면 안된다.)
     *  4. Collection 페치 조인은 두개이상 사용하면 안됨다.
     *      One(Order)쪽의 데이터 개수가 1 + n + m 번 중복된다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        for (Order order : orders) {
            System.out.println("order ref="+order+" id="+order.getId());
        }
        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * fetch join 사용시 페이징 하는 방법
     *  1. xToOne 관계는 모두 fetch join 한다.
     *  2. Collection은 Lazy로딩한다.
     *  3. hibernate.default_batch_fetch_size, @BatchSize 적용 하면 Lazy 로딩시 설정값 만큼 동시에 읽어온다
     *     따라서 N + 1 문제 해결
     *  4. 페이징도 해결
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * DTO 로 Order 조회
     *  1. xToOne 관계는(Member, Delivery) join 으로 1번 조회
     *  2. xToMany 관걔는(OrderItem) N번 조회
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDto();
    }

    /**
     * DTO 로 order 조회시 N + 1 문제 해결
     * 1. xToOne 관계는(Member, Delivery) join 으로 1번 조회
     * 2. ToMany 관걔는(OrderItem) in query 로 동시에 읽어와 메모리상에 Map 저장한후 Order에 set
     * 3. 1 + 1 로 읽어옴
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();

    }

    /**
     * DTO 로 order 와 연관된 객체를 한번에 조회한다.
     *  1. 한번조회로 읽어어면 여전히 Order는 중복된다.
     *  2. 중복된 Order 와 Item 리스트를 분리해서 코드상으로 Map 으로 분리한다.
     *  3. 코드상 추가작업이 크다
     *  4. 페이징 불가능하다.
     *  5. 유지보수가 어렵
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getOrderCount()), toList()) ))
                .entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }


    /**
     * 권장순서
     *  1. 우선 V3.1 방식 적
     *      - Entity 조회 방식은 fetch join, hiernate_defatul_batch_fetch_size, @BatchSize 설정으로
     *        코드 수정없이 다양한 성능 최적화를 시도할 수 있음
     *  2. V3.1 방식으로 해결 어려울 시 V5 권장
     *      - 성능최적화나 변경이 필요할 시 많은 코드 변경이 필요하여 유지보수가 힘들다.
     *  3. 위 두가지모두 힘들경우 NativeSQL or SpringJbdcTemplate 적용
     */

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        public OrderDto(Order o) {
            this.orderId = o.getId();
            this.name = o.getMember().getName();
            this.orderDate = o.getOrderDate();
            this.orderStatus = o.getStatus();
            this.address = o.getDelivery().getAddress();
            this.orderItems = o.getOrderItems().stream()
                            .map(OrderItemDto::new)
                            .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String name;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            this.name = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}
