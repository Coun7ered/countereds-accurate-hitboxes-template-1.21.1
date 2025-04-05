package net.countered.counteredsaccuratehitboxes.util;

import net.countered.counteredsaccuratehitboxes.CounteredsAccurateHitboxes;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class HitboxAttachment {

    public static final AttachmentType<List<Box>> HITBOXES = AttachmentRegistry.createDefaulted(
            Identifier.of(CounteredsAccurateHitboxes.MOD_ID, "hitboxes"),
            ArrayList::new
    );

    public static void register() {}
}

