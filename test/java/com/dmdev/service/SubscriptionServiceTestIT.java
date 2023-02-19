package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.dmdev.validator.Error;
import org.mockito.ArgumentCaptor;


import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class SubscriptionServiceTestIT extends IntegrationTestBase {

    private SubscriptionDao subscriptionDao;
    private SubscriptionService subscriptionService;
    private CreateSubscriptionValidator createSubscriptionValidator;

    private CreateSubscriptionMapper createSubscriptionMapper;
    private Clock clock;

    @BeforeEach
    void init() {
        subscriptionDao = SubscriptionDao.getInstance();
        Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        subscriptionService = new SubscriptionService(
                subscriptionDao,
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                clock
        );
    }

    @Test
    void upsert() {
        var createSubscriptionDto = getCreateSubscriptionDto();
        var subscription = subscriptionDao.insert(getSubscription("Ivan"));

        var actualResult = subscriptionService.upsert(createSubscriptionDto);

        assertThat(actualResult.getName()).isEqualTo(subscription.getName());

    }

    @Test
    void cancel() {
        var subscription = subscriptionDao.insert(getSubscription("Ivan"));

        subscriptionService.cancel(subscription.getId());

        assertFalse(subscription.getStatus().equals(Status.CANCELED));
;
    }




    @Test
    void expire() {
        var subscription = subscriptionDao.insert(getSubscription("Ivan"));

        subscriptionService.expire(subscription.getId());

        assertFalse(subscription.getStatus().equals(Status.EXPIRED));
        assertEquals(subscription.getExpirationDate(), Instant.parse("2022-01-01T00:00:00Z"));
    }

    private CreateSubscriptionDto getCreateSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.ofEpochSecond(1676999999))
                .build();
    }

    private Subscription getSubscription(String name) {
        return Subscription.builder()
                .id(1)
                .userId(1)
                .name(name)
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2022-01-01T00:00:00Z"))
                .status(Status.ACTIVE)
                .build();
    }

}




