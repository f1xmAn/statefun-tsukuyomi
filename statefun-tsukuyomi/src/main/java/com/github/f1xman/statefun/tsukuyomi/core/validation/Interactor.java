package com.github.f1xman.statefun.tsukuyomi.core.validation;

import com.github.f1xman.statefun.tsukuyomi.core.dispatcher.TsukuyomiApi;

public interface Interactor {

    void interact(TsukuyomiApi tsukuyomi);
}
