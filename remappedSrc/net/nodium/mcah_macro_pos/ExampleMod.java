package net.nodium.mcah_macro_pos;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class ExampleMod implements ModInitializer {
	private static KeyBinding keyBinding;
	
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Hello Fabric world!");
		
		keyBinding = new KeyBinding(
				"key.mcah-macro-pos.spook",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"category.mcah-macro-pos.test"
		);

		KeyBindingHelper.registerKeyBinding(keyBinding);
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
		    while (keyBinding.wasPressed()) System.out.println("was pressed!");
		});
	}
}
