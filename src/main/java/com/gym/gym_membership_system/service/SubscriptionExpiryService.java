package com.gym.gym_membership_system.service;

import com.gym.gym_membership_system.domain.MembershipStatus;
import com.gym.gym_membership_system.domain.Subscription;
import com.gym.gym_membership_system.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionExpiryService {

    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionExpiryService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Scheduled(cron = "0 0 1 * * *") // daily at 01:00
    @Transactional
    public void expireSubscriptions() {
        LocalDate today = LocalDate.now();
        List<Subscription> toExpire = subscriptionRepository.findByEndDateBeforeAndStatusNot(today.plusDays(1), MembershipStatus.EXPIRED);
        if (toExpire == null || toExpire.isEmpty()) {
            return;
        }
        for (Subscription s : toExpire) {
            if (s.getStatus() != MembershipStatus.EXPIRED) {
                s.setStatus(MembershipStatus.EXPIRED);
            }
        }
        subscriptionRepository.saveAll(toExpire);
    }
}
