package net.countered.counteredsaccuratehitboxes.mixin.accessors;


import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnimalModel.class)
public interface AnimalModelAccessor<E extends Entity> {
    @Accessor("childHeadYOffset")
    float getChildHeadYOffset();
    @Accessor("childHeadZOffset")
    float getChildHeadZOffset();
}
