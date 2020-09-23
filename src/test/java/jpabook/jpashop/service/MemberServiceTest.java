package jpabook.jpashop.service;


import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EntityManager em;


    @Test
    @Rollback(value = false)
    public void joinTest() throws Exception {
        //given
        Member member = new Member("Kim");

        //when
        Long savedId = memberService.join(member);

        //then
        assertThat(member).isEqualTo(memberRepository.findOne(savedId));

    }

    @Test(expected = IllegalStateException.class)
    public void duplicatedMemberException() throws Exception {
        //given
        Member member = new Member("hoseok");
        memberService.join(member);

        //when
        Member member1 = new Member("hoseok");
        memberService.join(member1);

        //then
        Assert.fail("예외가 발생해야 한다.");

    }
}