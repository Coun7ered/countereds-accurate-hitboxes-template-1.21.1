package net.countered.counteredsaccuratehitboxes.client;

import net.countered.counteredsaccuratehitboxes.CounteredsAccurateHitboxes;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import static net.minecraft.client.render.RenderPhase.*;

public class ModRenderLayers {
    public static final RenderLayer WEAK_SPOT_LAYER = RenderLayer.of(
            "weak_spot",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            256,
            true,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM)
                    .depthTest(LEQUAL_DEPTH_TEST)
                    .transparency(Transparency.TRANSLUCENT_TRANSPARENCY)
                    .lightmap(DISABLE_LIGHTMAP)
                    //.overlay(ENABLE_OVERLAY_COLOR)
                    .writeMaskState(COLOR_MASK)
                    //.cull(DISABLE_CULLING)
                    .target(OUTLINE_TARGET)
                    .layering(VIEW_OFFSET_Z_LAYERING)
                    .texture(new Texture(Identifier.of(CounteredsAccurateHitboxes.MOD_ID, "textures/white16x16.png"), false, false))
                    .build(true)
    );
}
