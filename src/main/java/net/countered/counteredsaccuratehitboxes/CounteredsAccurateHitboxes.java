package net.countered.counteredsaccuratehitboxes;

import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounteredsAccurateHitboxes implements ModInitializer {
	public static final String MOD_ID = "countereds-accurate-hitboxes";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Countered's Accurate Hitboxes");

		HitboxAttachment.register();

		//spawnMobsDebug();
	}

	private void spawnMobsDebug() {
		KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.mymod.spawn_all_entities",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F9,
				"category.mymod.debug"
		));

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			while (keyBinding.wasPressed()) {
				int spacing = 4;
				int gridWidth = 10;

				int count = 0;
				for (EntityType<?> type : Registries.ENTITY_TYPE) {
					if (type != EntityType.PLAYER) {
						for (int i = 0; i < 1; i++) {
							Entity entity = type.create(server.getOverworld());
							if (entity instanceof MobEntity living) {
								int x = (count % gridWidth) * spacing;
								int z = (count / gridWidth) * spacing;

								BlockPos spawnPos = i == 0 ? server.getOverworld().getSpawnPos().add(x, 50, z) : server.getOverworld().getSpawnPos().add(x, 55, z);
								if (i == 1) living.setBaby(true);
								living.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);
								living.setAiDisabled(true);
								living.setNoGravity(true);
								living.setInvulnerable(true);
								server.getOverworld().spawnEntity(living);
								count++;
							}
						}
					}
				}
			}
		});
	}
}