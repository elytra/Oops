package com.elytradev.oops;

import com.elytradev.concrete.config.ConcreteGuiFactory;

// basic concrete gui factory implementation
public class OopsGuiFactory extends ConcreteGuiFactory {

    public OopsGuiFactory() {
        super(OopsConfig.config);
    }
}
