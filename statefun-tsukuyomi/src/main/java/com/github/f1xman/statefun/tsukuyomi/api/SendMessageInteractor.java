package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.TsukuyomiApi;
import com.github.f1xman.statefun.tsukuyomi.core.capture.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class SendMessageInteractor implements Interactor {

    Envelope envelope;

    @Override
    public Optional<TypeName> getCollaborator() {
        return Optional.ofNullable(envelope.getFrom())
                .map(Envelope.NodeAddress::getType)
                .map(TypeName::typeNameFromString);
    }

    @Override
    public void interact(TsukuyomiApi tsukuyomi) {
        tsukuyomi.send(envelope);
    }
}
