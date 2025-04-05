package net.countered.counteredsaccuratehitboxes.networking;

import net.countered.counteredsaccuratehitboxes.CounteredsAccurateHitboxes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.joml.Vector3f;

import java.util.List;

public record HitboxPayload(List<Box> boxList, int entityId) implements CustomPayload {

    public static final CustomPayload.Id<HitboxPayload> ID =
            new CustomPayload.Id<>(Identifier.of(CounteredsAccurateHitboxes.MOD_ID, "hitboxes"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    // Codec für eine einzelne Box
    private static final PacketCodec<RegistryByteBuf, Box> BOX_CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, box -> box.minX,
            PacketCodecs.DOUBLE, box -> box.minY,
            PacketCodecs.DOUBLE, box -> box.minZ,
            PacketCodecs.DOUBLE, box -> box.maxX,
            PacketCodecs.DOUBLE, box -> box.maxY,
            PacketCodecs.DOUBLE, box -> box.maxZ,
            Box::new
    );

    // Codec für die gesamte Payload
    public static final PacketCodec<RegistryByteBuf, HitboxPayload> CODEC = PacketCodec.tuple(
            BOX_CODEC.collect(PacketCodecs.toList()), payload -> payload.boxList,
            PacketCodecs.INTEGER, payload -> payload.entityId,
            HitboxPayload::new
    );
}

