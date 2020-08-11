package net.nodium.mcah_macro_pos;

import java.io.File;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotUtils;

public class MacroPOS implements ModInitializer {
	private static KeyBinding keyBinding;
	private static MinecraftClient mc = MinecraftClient.getInstance();

	private static boolean do_things = false;
	private static int counter = 0;
	private static boolean writing = false;
	private static boolean go_backwards = false;
	private static float increment;

	// camera pitch
	private static float p_center;
	private static float p_radius;
	private static float p_min;
	private static float p_max;
	private static float p;
	
	// camera yaw
	private static float y_center;
	private static float y_radius;
	private static float y_min;
	private static float y_max;
	private static float y;

	@Override
	public void onInitialize() {
		// key to start and stop the screenshotting
		keyBinding = new KeyBinding("key.mcah-macro-pos.spook", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R,
				"category.mcah-macro-pos.test");

		KeyBindingHelper.registerKeyBinding(keyBinding);

		// this lambda gets called every game tick
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// inverts the do_things variable when the main key is pressed
			// this turns on the bot or turns it off, restarting it when it's stopped
			while (keyBinding.wasPressed()) {
				do_things = !do_things;
				
				increment = 0.15f;

				// camera pitch
//				 p_min = -25;
//				 p_max = 15;
//				 p_min = -5;
//				 p_max = 5;
				 p_center = -2.5f;
				 p_radius = 2;
				 p_min = p_center - p_radius;
				 p_max = p_center + p_radius;
				 p = p_min;
				
				// camera yaw
//				 y_min = -150;
//				 y_max = -90;
//				 y_min = 35;
//				 y_max = 55;
				 y_center = -120f;
				 y_radius = .3f;
				 y_min = y_center - y_radius;
				 y_max = y_center + y_radius;
				 y = y_max;
			}
			if (do_things) {
				// this is the part where we actually set the player position
				mc.player.yaw = y;
				mc.player.pitch = p;

				// for some reason setting position and taking a screenshot on the same tick results in
				// it taking a screenshot of the previous tick, not the current one, so we have to set the first position and then skip a tick
				if (counter == 0) {
					return;
				}

				// this constructs the path of the current screenshot we are dealing with this loop
				// the game's built in screenshot function does this already but we also want to check if the file exists
				// so we're also doing it again here
				// also for some reason mcpath.getAbsolutePath() returns the minecraft directory with a dot at the end
				// so we have to remove it, that's what substring() is for
				String mcpath = mc.runDirectory.getAbsolutePath();
				File screenshot = new File(mcpath + String.format("/screenshots/%.2f_%.2f_%04d.png", p, y, counter));

				if (!writing) {
					if (screenshot.exists()) {
						screenshot.delete();
					}
					ScreenshotUtils.saveScreenshot(mc.runDirectory, String.format("%.2f_%.2f_%04d.png", p, y, counter),
							mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(),
							mc.getFramebuffer(), (text) -> {
								mc.execute(() -> {
									mc.inGameHud.getChatHud().addMessage(text);
								});
							});
					writing = true;
				}

				// loop until the screenshot is finished writing (or maybe it waits until the previous one idk)
				if (!screenshot.exists()) {
//					System.out.println(screenshot.getAbsolutePath());
//					System.out.println("screenshot does not exist");
					return;
				}

//				System.out.println(String.format("%s %s", y, p));
//				mc.player.sendChatMessage(String.format("%s %s", y, p));
//				System.out.println(mc.player.yaw);

				// if we've gotten to this point that means the screenshot has finished writing and we can take another one
				writing = false;
				counter++;
//				System.out.println(counter);

				// increment yaw in the current direction (forwards or backwards)
				if (!go_backwards) {
					y -= increment;
				} else {
					y += increment;
				}

				// if we've hit the yaw edges, then increase pitch and bounce back
				if (y > y_max || y < y_min) {
					go_backwards = !go_backwards;
					p += increment;
				}

				// if we get to the end of the pitch bracket, we're done
				if (p > p_max) {
					System.out.println("bruh");
					System.out.println(String.format("%s %s", p, p_max));
					do_things = false;
					y = y_max;
					p = p_min;
					counter = 0;
					writing = false;
				}
			// this happens if do_things becomes false before the end, which probably means we pressed R to cancel
			// so we reset everything
			} else {
				y = y_max;
				p = p_min;
				counter = 0;
				writing = false;
			}
		});
	}
}
