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

	// other general variables
	private static boolean do_things = false; // whether the macro is active or not
	private static boolean writing = false; // whether or not a screenshot is being taken
	private static boolean go_backwards = false;
	private static int counter = 0; // how many screenshots we've actually taken (this is NOT equal to the number of ticks the macro has been active)

	private static float py_increment; // increment of pitch and yaw in degrees

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
		keyBinding = new KeyBinding("key.mcah-macro-pos.spook", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.mcah-macro-pos.test");

		KeyBindingHelper.registerKeyBinding(keyBinding);

		// this lambda gets called every game tick
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// inverts the do_things variable when the main key is pressed
			// this turns on the bot or turns it off, restarting it when it's stopped
			while (keyBinding.wasPressed()) {
				do_things = !do_things;

				counter = 0;

				py_increment = 0.15f; // when the mouse is moved 1px in alpha, the camera is rotated by 0.15 degrees (at default in-game sensitivity)

				// camera pitch
				 p_center = -2.5f;
				 p_radius = 2;
				 p_min = p_center - p_radius;
				 p_max = p_center + p_radius;
				 p = p_min;

				// camera yaw
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
					y -= py_increment;
				} else {
					y += py_increment;
				}

				// if we've hit the yaw edges, then increase pitch and bounce back
				if (y > y_max || y < y_min) {
					go_backwards = !go_backwards;
					p += py_increment;
				}

				// if we get to the end of the pitch bracket, we're done
				if (p > p_max) {
					System.out.println("bruh");
					do_things = false;
				}
			}
		});
	}
}
