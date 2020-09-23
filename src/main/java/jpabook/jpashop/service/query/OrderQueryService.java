package jpabook.jpashop.service.query;

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.domain.Order;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Transactional(readOnly = true)
public class OrderQueryService {
    public List<OrderApiController.OrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        for (Order order : orders) {
            System.out.println("order ref="+order+" id="+order.getId());
        }
        return orders.stream()
                .map(OrderApiController.OrderDto::new)
                .collect(toList());
    }
}
