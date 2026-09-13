package org.evlis.lunamatic;

import io.papermc.paper.datapack.DatapackRegistrar;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public final class LunamaticBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.DATAPACK_DISCOVERY.newHandler(event -> {
                    DatapackRegistrar registrar = event.registrar();

                    try {
                        URI packUri = Objects.requireNonNull(
                                LunamaticBootstrap.class.getResource("/pack"),
                                "Lunamatic datapack resource /pack was not found"
                        ).toURI();

                        registrar.discoverPack(
                                packUri,
                                "lunamatic",
                                config -> config
                                        .autoEnableOnServerStart(true)
                                        .position(
                                                true,
                                                io.papermc.paper.datapack.Datapack.Position.TOP
                                        )
                        );

                    } catch (URISyntaxException | IOException e) {
                        throw new IllegalStateException(
                                "Failed to discover the Lunamatic Blood Moon datapack",
                                e
                        );
                    }
                })
        );
    }
}