package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }
    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;
        public void dbInit1() {
            Member member1 = getMember("userA", "서울", "1", "111-111");
            em.persist(member1);

            Book book1 = getBook("JPA1 Book", 10000, 100);
            em.persist(book1);

            Book book2 = getBook("JPA2 Book", 20000, 100);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);


            Delivery delivery = getDelivery(member1);
            Order order = Order.createOrder(member1, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        public void dbInit2() {
            Member member2 = getMember("userB", "경기", "수원", "111-222");
            em.persist(member2);

            Book book1 = getBook("Spring1 Book", 20000, 201);
            em.persist(book1);

            Book book2 = getBook("Spring2 Book", 40000, 200);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);


            Delivery delivery = getDelivery(member2);
            Order order = Order.createOrder(member2, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        private Delivery getDelivery(Member member1) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member1.getAddress());
            delivery.setStatus(DeliveryStatus.READY);
            return delivery;
        }

        private Book getBook(String name, int price, int stockQuantity) {
            Book book1 = new Book();
            book1.setName(name);
            book1.setPrice(price);
            book1.setStockQuantity(stockQuantity);
            return book1;
        }

        private Member getMember(String name, String city, String street, String zipcode) {
            Member member1 = new Member();
            member1.setName(name);
            member1.setAddress(new Address(city, street, zipcode));
            return member1;
        }
    }
}


