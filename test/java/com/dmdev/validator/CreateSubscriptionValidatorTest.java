package com.dmdev.validator;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;


class CreateSubscriptionValidatorTest {

    CreateSubscriptionValidator subscriptionValidator = CreateSubscriptionValidator.getInstance();

    @Test
    void shouldPassValidation() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.ofEpochSecond(1676999999))
                .build();

        var actualResult = subscriptionValidator.validate(dto);

        assertFalse(actualResult.hasErrors());
    }

    @Test
    void invalidUserId() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.ofEpochSecond(1676999999))
                .build();

        var actualResult = subscriptionValidator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(100);
    }

    @Test
    void invalidName() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.ofEpochSecond(1676999999))
                .build();

        var actualResult = subscriptionValidator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(101);
    }


    @Test
    void invalidNameProvider() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider(null)
                .expirationDate(Instant.ofEpochSecond(1676999999))
                .build();

        var actualResult = subscriptionValidator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(102);
    }

    @Test
    void invalidExpirationDate() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(null)
                .build();

        var actualResult = subscriptionValidator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(103);
    }

    @Test
    void invalidExpirationDateBeforeInstantNow() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.now().minusSeconds(5))
                .build();

        var actualResult = subscriptionValidator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(103);
    }

    @Test
    void invalidUserIdNameProviderExpirationDate() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name(null)
                .provider("fake_provider")
                .expirationDate(null)
                .build();

        var actualResult = subscriptionValidator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(4);
        var errorCodes = actualResult.getErrors().stream()
                .map(Error::getCode)
                .toList();
        assertThat(errorCodes).contains(100, 101, 102, 103);


    }

}