package net.gudenau.minecraft.realip;

import net.fabricmc.api.ModInitializer;

/**
 * The entrypoint of this mod, only used to load the configuration and initialize the validation helper.
 */
public final class RealIp implements ModInitializer {
	@Override
	public void onInitialize() {
		Configuration.load();
		ValidationHelper.init();
	}
}
