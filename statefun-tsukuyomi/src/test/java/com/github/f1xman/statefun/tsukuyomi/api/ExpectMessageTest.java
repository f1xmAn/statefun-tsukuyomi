package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.val;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class ExpectMessageTest {

    @Test
    void throwsExceptionIfEnvelopeDoesNotMatch() {
        Envelope envelope = envelope();
        Envelope notMatchingEnvelope = swap(envelope);
        ExpectMessage expectMessage = ExpectMessage.of(is(notMatchingEnvelope));

        assertThatThrownBy(
                () -> expectMessage.match(0, () -> List.of(envelope)))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void nothingThrownWhenEnvelopeMatches() {
        Envelope envelope = envelope();
        ExpectMessage expectMessage = ExpectMessage.of(is(envelope));

        expectMessage.match(0, () -> List.of(envelope));
    }

    @Test
    void waitsUntilExpectedByOrderEnvelopeReceivedAndThenAsserts() {
        Envelope envelope = envelope();
        ExpectMessage expectMessage = ExpectMessage.of(is(envelope));
        AtomicInteger counter = new AtomicInteger();

        expectMessage.match(0, () -> {
            if (counter.getAndAdd(1) > 0) {
                return List.of(envelope);
            } else {
                return List.of();
            }
        });
    }

    @Test
    void interruptsAssertionIfThreadInterrupted() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            val interruptableThread = Thread.currentThread();
            Thread interrupterThread = new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    interruptableThread.interrupt();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            interrupterThread.start();
            ExpectMessage expectMessage = ExpectMessage.of(nullValue(Envelope.class));
            expectMessage.match(0, List::of);
        });
    }

    private Envelope envelope() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "foobar")
                .to(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }

    private Envelope swap(Envelope originalEnvelope) {
        return originalEnvelope.toBuilder()
                .to(originalEnvelope.getFrom())
                .from(originalEnvelope.getTo())
                .build();
    }
}