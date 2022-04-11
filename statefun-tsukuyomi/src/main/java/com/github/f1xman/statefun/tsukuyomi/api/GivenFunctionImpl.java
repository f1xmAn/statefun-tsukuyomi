package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.ModuleDefinition;
import com.github.f1xman.statefun.tsukuyomi.core.StateSetter;
import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;
import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiManager;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class GivenFunctionImpl implements GivenFunction {

    TypedFunction typedFunction;
    StateSetter<?>[] stateSetters;
    TsukuyomiManager manager;
    @NonFinal
    TsukuyomiApi tsukuyomi;

    @Override
    public void interact(Interactor... interactors) {
        if (tsukuyomi != null) {
            throw new IllegalStateException("Already interacted. GivenFunction cannot be reused.");
        }
        ModuleDefinition.FunctionDefinition functionDefinition = ModuleDefinition.FunctionDefinition.builder()
                .typeName(typedFunction.getTypeName())
                .instance(typedFunction.getInstance())
                .stateSetters(List.of(stateSetters))
                .build();
        ModuleDefinition moduleDefinition = ModuleDefinition.builder()
                .functionUnderTest(functionDefinition)
                .collaborators(collectCollaborators(interactors))
                .build();
        tsukuyomi = manager.start(moduleDefinition);
        for (Interactor interactor : interactors) {
            interactor.interact(tsukuyomi);
        }
    }

    @Override
    public void expect(ChangeMatcher... matchers) {
        if (tsukuyomi == null) {
            throw new IllegalStateException("GivenFunction.interact(..) was not invoked");
        }
        for (int order = 0; order < matchers.length; order++) {
            matchers[order].match(order, tsukuyomi::getReceived);
        }
    }


    @NotNull
    private List<TypeName> collectCollaborators(Interactor[] interactors) {
        return Arrays.stream(interactors)
                .map(Interactor::getCollaborator)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    @Override
    public void shutdown() {
        if (manager != null) {
            manager.stop();
        }
    }
}
