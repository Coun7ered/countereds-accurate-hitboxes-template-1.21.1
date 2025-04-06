package net.countered.counteredsaccuratehitboxes.networking;

import net.countered.counteredsaccuratehitboxes.CounteredsAccurateHitboxes;
import net.minecraft.client.model.ModelPart;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;

public record HitboxPayload(List<List<Vector3f>> vertexBoxList, int entityId) implements CustomPayload {

    public static final CustomPayload.Id<HitboxPayload> ID =
            new CustomPayload.Id<>(Identifier.of(CounteredsAccurateHitboxes.MOD_ID, "hitboxes"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    // Codec für eine einzelne Box
    private static final PacketCodec<RegistryByteBuf, Vector3f> VECTOR3F_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, Vector3f -> Vector3f.x,
            PacketCodecs.FLOAT, Vector3f -> Vector3f.y,
            PacketCodecs.FLOAT, Vector3f -> Vector3f.z,
            Vector3f::new
    );

    // Codec für die gesamte Payload
    public static final PacketCodec<RegistryByteBuf, HitboxPayload> CODEC = PacketCodec.tuple(
            VECTOR3F_CODEC.collect(PacketCodecs.toList()).collect(PacketCodecs.toList()), payload -> payload.vertexBoxList,
            PacketCodecs.INTEGER, payload -> payload.entityId,
            HitboxPayload::new
    );
}

