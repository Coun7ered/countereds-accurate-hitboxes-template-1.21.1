package net.countered.counteredsaccuratehitboxes.mixin.client.entitymodels;

import net.countered.counteredsaccuratehitboxes.util.ModelPartProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(QuadrupedEntityModel.class)
public abstract class ModQuadrupedEntityModelMixin<T extends Entity> extends AnimalModel<T> implements ModelPartProvider {

    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightFrontLeg;
    @Shadow @Final public ModelPart leftFrontLeg;
    @Shadow @Final public ModelPart rightHindLeg;
    @Shadow @Final public ModelPart leftHindLeg;

    @Unique
    private List<Pair<ModelPart, String>> partTypes;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.partTypes = new ArrayList<>();
        partTypes.add(new Pair<>(this.head, "AnimalHEAD"));
        partTypes.add(new Pair<>(this.body, "BODY"));
        partTypes.add(new Pair<>(this.rightFrontLeg, "RIGHT_FRONT_LEG"));
        partTypes.add(new Pair<>(this.leftFrontLeg, "LEFT_FRONT_LEG"));
        partTypes.add(new Pair<>(this.rightHindLeg, "RIGHT_HIND_LEG"));
        partTypes.add(new Pair<>(this.leftHindLeg, "LEFT_HIND_LEG"));
    }

    @Override
    public List<Pair<ModelPart, String>> countereds_accurate_hitboxes_template_1_21_1$getParts() {
        return partTypes;
    }
}
