package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import com.github.f1xman.statefun.tsukuyomi.core.capture.InvocationReport;
import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EnvelopeMatcherTest {

    @Mock
    TsukuyomiApi tsukuyomi;

    @Test
    void throwsAssertionErrorIfIndexesDoesNotMatch() {
        Envelope envelope1 = envelope1();
        EnvelopeMatcher matcher = EnvelopeMatcher.of(List.of(EnvelopeCriterion.ordered(0, envelope1)));
        InvocationReport invocationReport = InvocationReport.of(2, List.of(envelope0(), envelope1));
        given(tsukuyomi.getInvocationReport())
                .willReturn(Optional.of(invocationReport));
        given(tsukuyomi.getReceived()).willReturn(List.of(envelope0(), envelope1));

        assertThatThrownBy(() -> matcher.match(tsukuyomi))
                .isInstanceOf(AssertionError.class)
                .hasMessage(
                        "Envelope %s is expected to be sent in the following order: [%s], but the actual order is: [%s]. " +
                                "See the invocation report for more details: %s",
                        envelope1, 0, 1, invocationReport
                );
    }

    @Test
    void throwsAssertionErrorIfUnorderedEnvelopeIsMissingWhileTheSameOrderedEnvelopeIsPresent() {
        Envelope envelope1 = envelope1();
        EnvelopeMatcher matcher = EnvelopeMatcher.of(List.of(
                EnvelopeCriterion.unordered(envelope1),
                EnvelopeCriterion.ordered(0, envelope1))
        );
        InvocationReport invocationReport = InvocationReport.of(1, List.of(envelope1));
        given(tsukuyomi.getInvocationReport())
                .willReturn(Optional.of(invocationReport));
        given(tsukuyomi.getReceived()).willReturn(List.of(envelope0(), envelope1));

        assertThatThrownBy(() -> matcher.match(tsukuyomi))
                .isInstanceOf(AssertionError.class)
                .hasMessage(
                        "Missing envelope %s is expected to be sent in any order. " +
                                "See the invocation report for more details: %s",
                        envelope1, invocationReport
                );
    }

    @Test
    void throwsNothingWhenCriteriaMatched() {
        Envelope envelope = envelope0();
        EnvelopeMatcher matcher = EnvelopeMatcher.of(List.of(
                EnvelopeCriterion.ordered(0, envelope),
                EnvelopeCriterion.unordered(envelope)
        ));
        InvocationReport invocationReport = InvocationReport.of(2, List.of(envelope, envelope));
        given(tsukuyomi.getInvocationReport())
                .willReturn(Optional.of(invocationReport));
        given(tsukuyomi.getReceived()).willReturn(List.of(envelope, envelope));

        assertThatNoException().isThrownBy(() -> matcher.match(tsukuyomi));
    }

    private Envelope envelope0() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "0")
                .to(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }

    private Envelope envelope1() {
        return Envelope.builder()
                .from(TypeName.typeNameFromString("foo/bar"), "1")
                .to(TypeName.typeNameFromString("foo/baz"), "foobaz")
                .data(Types.stringType(), "foobarbaz")
                .build();
    }
}