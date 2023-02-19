package com.dmdev.dao;

import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionDaoTestIT extends IntegrationTestBase {



    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();
    @Test
    void findAll() {
        Subscription subscription1 = subscriptionDao.insert(getSubscription(111, "Alex"));
        Subscription subscription2 = subscriptionDao.insert(getSubscription(222, "Petr"));
        Subscription subscription3 = subscriptionDao.insert(getSubscription(333, "Vova"));

        var actualResult = subscriptionDao.findAll();

        assertThat(actualResult).hasSize(3);
        var subscriptionIds = actualResult.stream()
                .map(Subscription::getId)
                .toList();
        assertThat(subscriptionIds).contains(subscription1.getId(), subscription2.getId(), subscription3.getId());
    }

    @Test
    void findById() {
        Subscription subscription = subscriptionDao.insert(getSubscription(111, "Alex"));

        var actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get()).isEqualTo(subscription);
    }

    @Test
    void deleteExistingSubscription() {
        Subscription subscription = subscriptionDao.insert(getSubscription(111, "Alex"));

        var actualResult = subscriptionDao.delete(subscription.getId());

        assertTrue(actualResult);
    }

    @Test
    void deleteNotExistingSubscription() {
        subscriptionDao.insert(getSubscription(111, "Alex"));

        var actualResult = subscriptionDao.delete(100);

        assertFalse(actualResult);
    }

    @Test
    void update() {
        var subscription = getSubscription(111, "Alex");
        subscriptionDao.insert(subscription);
        subscription.setName("Ivan-updated");
        subscription.setProvider(Provider.GOOGLE);

        subscriptionDao.update(subscription);

        var updatedSubscription = subscriptionDao.findById(subscription.getId()).get();
        assertThat(updatedSubscription).isEqualTo(subscription);

    }

    @Test
    void insert() {
        var subscription = getSubscription(111, "Alex");

        var actualResult = subscriptionDao.insert(subscription);

        assertNotNull(actualResult.getId());

    }

    @Test
    void findByUserId() {
        Subscription subscription = subscriptionDao.insert(getSubscription(111, "Alex"));

        var actualResult = subscriptionDao.findByUserId(subscription.getUserId());

        assertThat(actualResult).hasSize(1);
        var subscriptionsUserId = actualResult.stream()
                .map(Subscription::getUserId)
                .toList();
        assertThat(subscriptionsUserId).contains(subscription.getUserId());
    }

    @Test
    void shouldNotFindByUserIdSubscriptionDoesNotExist() {
        subscriptionDao.insert(getSubscription(111, "Alex"));

        var actualResult = subscriptionDao.findByUserId(null);

        assertThat(actualResult).isEmpty();
    }

    private Subscription getSubscription(Integer userId, String name) {
        return Subscription.builder()
                .userId(userId)
                .name(name)
                .provider(Provider.APPLE)
                .expirationDate(Instant.ofEpochSecond(169999999))
                .status(Status.ACTIVE)
                .build();
    }

}