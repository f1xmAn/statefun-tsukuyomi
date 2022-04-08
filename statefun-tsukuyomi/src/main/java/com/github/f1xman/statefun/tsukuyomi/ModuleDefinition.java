package com.github.f1xman.statefun.tsukuyomi;

import com.github.f1xman.statefun.tsukuyomi.capture.MessageCaptureFunction;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.StatefulFunctionSpec;
import org.apache.flink.statefun.sdk.java.StatefulFunctions;
import org.apache.flink.statefun.sdk.java.TypeName;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
public class ModuleDefinition {

    FunctionDefinition functionUnderTest;
    @Singular
    Set<TypeName> collaborators;

    public StatefulFunctions toStatefulFunctions() {
        StatefulFunctionSpec functionUnderTestSpec = StatefulFunctionSpec
                .builder(functionUnderTest.getTypeName())
                .withSupplier(functionUnderTest::getInstance)
                .build();
        Stream<StatefulFunctionSpec> collaboratorSpecs = getCollaborators().stream()
                .map(t -> StatefulFunctionSpec
                        .builder(t)
                        .withSupplier(() -> MessageCaptureFunction.INSTANCE)
                        .build()
                );
        StatefulFunctions statefulFunctions = new StatefulFunctions();
        Stream
                .concat(Stream.of(functionUnderTestSpec), collaboratorSpecs)
                .forEach(statefulFunctions::withStatefulFunction);
        return statefulFunctions;
    }

    public String generateFunctionsString() {
        return Stream.concat(
                        Stream.of(functionUnderTest.getTypeName()),
                        collaborators.stream()
                )
                .map(TypeName::asTypeNameString)
                .collect(Collectors.joining(";"));
    }

    @RequiredArgsConstructor(staticName = "of")
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @Getter
    public static class FunctionDefinition {

        TypeName typeName;
        StatefulFunction instance;

    }

}
