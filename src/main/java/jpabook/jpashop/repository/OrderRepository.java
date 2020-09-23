package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        return em.createQuery("select o from Order o join o.member m",
                Order.class)
                .getResultList();
//        return em.createQuery("select o from Order o join o.member m" +
//                        " where o.status = :status " +
//                        " and m.name like :name",
//                Order.class)
//                .setParameter("status", orderSearch.getOrderStatus())
//                .setParameter("name", orderSearch.getMemberName())
//                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o " +
                        "   join fetch o.member m" +
                        "   join fetch o.delivery d ",
                Order.class
        ).getResultList();
    }


    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o " +
                        "   join fetch o.member m" +
                        "   join fetch o.delivery d " +
                        "   join fetch o.orderItems oi " +
                        "   join fetch oi.item",
                Order.class
        )
                .setFirstResult(1)
                .setMaxResults(2)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o " +
                        "   join fetch o.member m" +
                        "   join fetch o.delivery d ",
                Order.class
        )
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
