package com.xinyihl.ymadditions.common.integration;

import com.xinyihl.ymadditions.common.integration.top.TileTOPDataProvider;
import mcjty.theoneprobe.api.ITheOneProbe;

import java.util.function.Function;

public class TheOneProbe implements Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe registrar) {
        registrar.registerProvider(new TileTOPDataProvider());
        return null;
    }
}