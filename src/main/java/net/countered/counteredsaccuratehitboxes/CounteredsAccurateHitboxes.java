package net.countered.counteredsaccuratehitboxes;

import net.countered.counteredsaccuratehitboxes.networking.HitboxPayload;
import net.countered.counteredsaccuratehitboxes.util.HitboxAttachment;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounteredsAccurateHitboxes implements ModInitializer {
	public static final String MOD_ID = "countereds-accurate-hitboxes";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Countered's Accurate Hitboxes");

		HitboxAttachment.register();
		PayloadTypeRegistry.playC2S().register(HitboxPayload.ID, HitboxPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(HitboxPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			player.getServer().execute(() -> {
				// Weak Spot in der Entity speichern
				Entity entity = player.getWorld().getEntityById(payload.entityId());
				if (entity instanceof LivingEntity) {
					entity.setAttached(HitboxAttachment.HITBOXES, payload.vertexBoxList());
				}
			});
		});
	}
}