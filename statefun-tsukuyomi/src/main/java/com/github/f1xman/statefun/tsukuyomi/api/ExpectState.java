package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.ManagedStateAccessor;
import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.hamcrest.Matcher;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ExpectState<T> implements ChangeMatcher {

    ValueSpec<T> spec;
    Matcher<T> matcher;

    @Override
    public void match(int order, TsukuyomiApi tsukuyomi) {
        ManagedStateAccessor stateAccessor = tsukuyomi.getStateAccessor();
        Optional<T> value;
        do {
            value = stateAccessor.getStateValue(spec);
        } while (!Thread.interrupted() && !matcher.matches(value.orElse(null)));
        assertThat(value.orElse(null), matcher);
    }
}
