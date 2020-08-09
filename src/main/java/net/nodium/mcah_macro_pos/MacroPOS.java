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
	private static int counter = 0; // number of screenshots taken
	private static int timer = 0; // number of ticks the program has been running
	private static boolean writing = false; // whether we're writing to a screenshot file or not
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
			}
			
			if (do_things) {
				timer++;
				
				// this constructs the path of the current screenshot we are dealing with this loop
				// the game's built in screenshot function does this already but we also want to check if the file exists
				// so we're also doing it again here
				// also for some reason mcpath.getAbsolutePath() returns the minecraft directory with a dot at the end
				// so we have to remove it, that's what substring() is for
				String mcpath = mc.runDirectory.getAbsolutePath();
				File screenshot = new File(mcpath + String.format("/screenshots/%d.png", counter));

				if (!writing) {
					if (screenshot.exists()) {
						screenshot.delete();
					}
					ScreenshotUtils.saveScreenshot(mc.runDirectory, String.format("%d.png", counter),
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
					System.out.println(screenshot.getAbsolutePath());
//					System.out.println("screenshot does not exist");
					return;
				}
				
				if (timer % 100 != 0) {
					return;
				}

//				System.out.println(String.format("%s %s", y, p));
//				mc.player.sendChatMessage(String.format("%s %s", y, p));
//				System.out.println(mc.player.yaw);

				// if we've gotten to this point that means the screenshot has finished writing and we can take another one
				writing = false;
				counter++;
//				System.out.println(counter);
			// this happens if do_things becomes false before the end, which probably means we pressed R to cancel
			// so we reset everything
			} else {
//				counter = 0;
				writing = false;
			}
		});
	}
}
