package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;


@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private Subscription subscription;
    @Mock
    private Clock clock;
    @InjectMocks
    private SubscriptionService subscriptionService;


    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(
                subscriptionDao,
                createSubscriptionMapper,
                createSubscriptionValidator,
                clock
        );
    }


    @Nested
    class TestUpsert {

        @Test
        void shouldThrowExceptionIfDtoIsInvalid() {
            CreateSubscriptionDto createSubscriptionDto = getCreateSubscriptionDto();
            ValidationResult validationResult = new ValidationResult();
            validationResult.add(Error.of(105, "message"));
            doReturn(validationResult).when(createSubscriptionValidator).validate(createSubscriptionDto);

            assertThrows(ValidationException.class, () -> subscriptionService.upsert(createSubscriptionDto));
            verifyNoInteractions(subscriptionDao, createSubscriptionMapper);
        }

        @Test
        void upsert () {
            CreateSubscriptionDto createSubscriptionDto = getCreateSubscriptionDto();
            Subscription subscription = getSubscription();
            doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(createSubscriptionDto);
            doReturn(Collections.emptyList()).when(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
            doReturn(subscription).when(createSubscriptionMapper).map(createSubscriptionDto);
            doReturn(subscription).when(subscriptionDao).upsert(any());


            Subscription actualResult = subscriptionService.upsert(createSubscriptionDto);

            assertNotNull(actualResult);
            assertThat(actualResult).isEqualTo(subscription);
            verify(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
            verify(subscriptionDao).upsert(any());
        }
    }

    @Nested
    class TestCancel {
        private static final int SUB_ID = 1;

        @Test
        void whenNotFoundByIdShouldThrowException() {
            doReturn(Optional.empty()).when(subscriptionDao).findById(SUB_ID);

            assertThrows(IllegalArgumentException.class, () -> subscriptionService.cancel(SUB_ID));
            verify(subscriptionDao).findById(SUB_ID);
            verifyNoMoreInteractions(subscriptionDao);
        }

        @Test
        void whenStatusIsNotActiveShouldThrowException() {
            var subscription = Subscription.builder().status(Status.EXPIRED).build();
            doReturn(Optional.of(subscription)).when(subscriptionDao).findById(SUB_ID);

            SubscriptionException exception = assertThrows(SubscriptionException.class, () -> subscriptionService.cancel(SUB_ID));
            assertEquals("Only active subscription 1 can be canceled", exception.getMessage());
        }

        @Nested
        class WhenActive {

            private Subscription subscription;

            @BeforeEach
            void setUp() {
                subscription = mock(Subscription.class);
                doReturn(Optional.of(subscription)).when(subscriptionDao).findById(SUB_ID);
                doReturn(Status.ACTIVE).when(subscription).getStatus();
//                when(subscriptionDao.findById(SUB_ID)).thenReturn(Optional.of(subscription));
//                when(subscription.getStatus()).thenReturn(Status.ACTIVE);
            }

            @Test
            void shouldSetStatusCancel() {
                subscriptionService.cancel(SUB_ID);

                verify(subscription).setStatus(Status.CANCELED);
            }


            @Test
            void shouldUpdateSubscription() {
                subscriptionService.cancel(SUB_ID);

                verify(subscriptionDao).update(subscription);
            }
        }
    }

    @Nested
    class TestExpire {
        private static final int SUB_ID = 1;

        @Test
        void whenNotFoundByIdShouldThrowException() {
            doReturn(Optional.empty()).when(subscriptionDao).findById(SUB_ID);

            assertThrows(IllegalArgumentException.class, () -> subscriptionService.expire(SUB_ID));
            verify(subscriptionDao).findById(SUB_ID);
            verifyNoMoreInteractions(subscriptionDao);
        }

        @Test
        void whenStatusExpiredShouldThrowException() {
            var subscription = Subscription.builder().status(Status.EXPIRED).build();
            doReturn(Optional.of(subscription)).when(subscriptionDao).findById(SUB_ID);

            var exception = assertThrows(SubscriptionException.class, () -> subscriptionService.expire(SUB_ID));
            assertEquals("Subscription 1 has already expired", exception.getMessage());
        }

        @Nested
        class WhenActive {
            private Subscription subscription;

            @BeforeEach
            void setUp() {
                subscription = mock(Subscription.class);
                when(subscriptionDao.findById(SUB_ID)).thenReturn(Optional.of(subscription));
                when(subscription.getStatus()).thenReturn(Status.ACTIVE);
            }

            @Test
            void shouldSetExpirationDate() {
                subscriptionService.expire(SUB_ID);

                verify(subscription).setExpirationDate(clock.instant());

            }

            @Test
            void shouldSetStatusExpired() {
                subscriptionService.expire(SUB_ID);

                verify(subscription).setStatus(Status.EXPIRED);
            }


            @Test
            void shouldUpdateSubscription() {
                subscriptionService.expire(SUB_ID);

                verify(subscriptionDao).update(subscription);
            }

        }

    }



//    @Test
//    void upsert() {
//        CreateSubscriptionDto createSubscriptionDto = getCreateSubscriptionDto();
//        Subscription subscription = getSubscription();
//        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(createSubscriptionDto);
//        doReturn(Collections.emptyList()).when(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
//        doReturn(subscription).when(createSubscriptionMapper).map(createSubscriptionDto);
//        doReturn(subscription).when(subscriptionDao).upsert(any());
//
//        Subscription actualResult = subscriptionService.upsert(createSubscriptionDto);
//
//        assertNotNull(actualResult);
//        assertThat(actualResult).isEqualTo(subscription);
//        verify(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
//        verify(subscriptionDao).upsert(any());
//
//    }
//
//    @Test
//    void shouldThrowExceptionIfDtoInvalid() {
//        CreateSubscriptionDto createSubscriptionDto = getCreateSubscriptionDto();
//        ValidationResult validationResult = new ValidationResult();
//        validationResult.add(Error.of(105, "message"));
//        doReturn(validationResult).when(createSubscriptionValidator).validate(createSubscriptionDto);
//
//        assertThrows(ValidationException.class, () -> subscriptionService.upsert(createSubscriptionDto));
//        verifyNoInteractions(subscriptionDao, createSubscriptionMapper);
//    }
//
//    @Test
//    void cancel() {
//        //cоздаем объект подписки
//        Subscription subscription = getSubscription();
//        //вызываем метод findById с getId
//        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());
//        // Вызываем cancel метод.
//        subscriptionService.cancel(subscription.getId());
//        //проверяем выполнение метода findById
//        verify(subscriptionDao).findById(subscription.getId());
//    }
//
//    //если подписка имеет статус Active, то метод cancel  обновляет статус подписки на Canceled
//    @Test
//    void testCancel_ChangeStatusFromActiveToCancel() {
//        var subscription = getSubscription();
//        Subscription activeStatusSubscription = getSubscriptionStatus(Status.ACTIVE);
//        doReturn(Optional.of(activeStatusSubscription)).when(subscriptionDao).findById(subscription.getId());
//
//        subscriptionService.cancel(subscription.getId());
//
//        ArgumentCaptor<Subscription> argumentCaptor = ArgumentCaptor.forClass(Subscription.class);
//        verify(subscriptionDao).update(argumentCaptor.capture());
//        assertEquals(Status.CANCELED, argumentCaptor.getValue().getStatus());
//    }
//
//    @Test
//    void exampleTest_CancelChangeStatusFromActiveToCancel() {
//        var subscription = getSubscription();
//        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());
//
//        subscriptionService.cancel(subscription.getId());
//
//        var argumentCaptor = ArgumentCaptor.forClass(Subscription.class);
//        verify(subscriptionDao).update(argumentCaptor.capture());
//        assertEquals(Status.CANCELED, argumentCaptor.getValue().getStatus());
//
//    }
//
//    //если подписка имеет статус отличный от Active, то метод cancel  выбрасывает ошибку
//    @Test
//    void testCancel_IfStatusIsNotActive() {
//        var subscription = getSubscriptionStatus(Status.EXPIRED);
//        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());
//
//        SubscriptionException exception = assertThrows(SubscriptionException.class, () -> subscriptionService.cancel(subscription.getId()));
//        assertEquals("Only active subscription 1 can be canceled", exception.getMessage());
////        verifyNoMoreInteractions(subscriptionDao);
//    }
//
//
//    // если подписка с указанным идентификатором не существует, то метод cancel выбрасывает ошибку
//    @Test
//    void testCancel_NonexistentSubscription() {
//        var subscription = getSubscription();
//        doReturn(Optional.empty()).when(subscriptionDao).findById(subscription.getId());
//
//        assertThrows(IllegalArgumentException.class, () -> subscriptionService.cancel(subscription.getId()));
//        verifyNoMoreInteractions(subscriptionDao);
//    }
//
//    //метод expire обновляет статус и дату истечения активной подписки.
//    @Test
//    void testExpire_ActiveSubscription() {
////        var subscription = getSubscription();
//        Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));
//        var subscriptionClock = getSubscriptionClock(1, Status.ACTIVE, Instant.parse("2022-01-01T00:00:00Z"));
////        Subscription activeStatusSubscription = getSubscriptionStatus(Status.ACTIVE);
//        doReturn(Optional.of(subscriptionClock)).when(subscriptionDao).findById(1);
//        doReturn(clock).when(subscriptionDao).findById(1).get();
//
//        subscriptionService.expire(subscriptionClock.getId());
//
//        verify(subscriptionDao).findById(1);
//        verify(subscriptionDao).update(subscriptionClock);
//        assertEquals(Status.EXPIRED, subscriptionClock.getStatus());
//        assertEquals(clock.instant(), subscriptionClock.getExpirationDate());
//
//    }
//
//    @Test
//    void testExpire_shouldSetExpirationDate() {
//        int subscriptionId = 1;
//        subscription = mock(Subscription.class);
//        var fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
//        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscriptionId);
//        doReturn(Status.ACTIVE).when(subscription).getStatus();
//
//        subscriptionService.expire(subscriptionId);
//
//        verify(subscription).setExpirationDate(fixedClock.instant());
//    }

//    @BeforeEach
//    void setUp() {
//        subscription = mock(Subscription.class);
//        when(subscriptionDao.findById(SUB_ID)).thenReturn(Optional.of(subscription));
//        when(subscription.getStatus()).thenReturn(Status.ACTIVE);
//    }
//
//    @Test
//    void shouldSetExpirationDate() {
//        subscriptionService.expire(SUB_ID);
//
//        verify(subscription).setExpirationDate(clock.instant());
//
//    }

    //если подписка уже истекла, то метод expire выбрасывает исключение.
//    @Test
//    void testExpireExpiredSubscription() {
//        Subscription expiredSubscription = getSubscriptionStatus(Status.EXPIRED);
//        doReturn(Optional.of(expiredSubscription)).when(subscriptionDao).findById(expiredSubscription.getId());
//
//        var exception = assertThrows(SubscriptionException.class, () -> subscriptionService.expire(expiredSubscription.getId()));
//        assertEquals("Subscription 1 has already expired", exception.getMessage());
//    }
//
//
//    // подписка не существует, то метод expire выбрасывает исключение.
//    @Test
//    void testExpireNonexistentSubscription() {
//        var subscription = getSubscription();
//        doReturn(Optional.empty()).when(subscriptionDao).findById(subscription.getId());
//
//
//        assertThrows(IllegalArgumentException.class, () -> subscriptionService.expire(subscription.getId()));
//        verifyNoMoreInteractions(subscriptionDao);
//    }

    private CreateSubscriptionDto getCreateSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.APPLE.name())
                .expirationDate(Instant.ofEpochSecond(169999999))
                .build();
    }

    private Subscription getSubscription() {
        return Subscription.builder()
                .id(1)
                .userId(1)
                .name("Ivan")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2022-01-01T00:00:00Z"))
                .status(Status.ACTIVE)
                .build();
    }
//    private Subscription getSubscriptionClock(Integer id, Status status, Instant instant) {
//        return Subscription.builder()
//                .id(id)
//                .userId(1)
//                .name("Ivan")
//                .provider(Provider.APPLE)
//                .expirationDate(instant)
//                .status(status)
//                .build();
//    }
//
//    private Subscription getSubscriptionStatus(Status status) {
//        return Subscription.builder()
//                .id(1)
//                .userId(1)
//                .name("Ivan")
//                .provider(Provider.APPLE)
//                .expirationDate(Instant.ofEpochSecond(169999999))
//                .status(status)
//                .build();
//    }


}
