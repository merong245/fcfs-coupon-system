package com.example.api.service;

import com.example.api.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    @DisplayName("한번만 응모")
    public void applyOnce(){
        applyService.apply(1L);

        long count = couponRepository.count();

        assertThat(count).isEqualTo(1);

    }


    @Test
    @DisplayName("동시에 여러명 응모")
    public void applyMulti() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 다른 쓰레드에서 수행하는 작업을 기다리게해줌
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i< threadCount; i++){
            long userId = i;
            executorService.submit(()->{

                try {
                    applyService.apply(userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        long count = couponRepository.count();

        assertThat(count).isEqualTo(100);

        /*
        테스트 실패
        why?
        race condition에 의해서
        멀티 쓰레드 작업으로 어플리케이션 단에서 count가 99일 때, 여러 쓰레드에서 apply 메서드가 수행되어 count 조건을 넘어선다면
        모두 apply되버리는 문제가 발생하게 된다.
         */
    }


}