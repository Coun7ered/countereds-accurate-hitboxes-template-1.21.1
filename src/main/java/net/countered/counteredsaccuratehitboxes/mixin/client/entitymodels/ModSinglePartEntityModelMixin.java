package net.countered.counteredsaccuratehitboxes.mixin.client.entitymodels;

import net.countered.counteredsaccuratehitboxes.util.ModelPartProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(SinglePartEntityModel.class)
public abstract class ModSinglePartEntityModelMixin<E extends Entity> extends EntityModel<E> implements ModelPartProvider {

    @Shadow public abstract ModelPart getPart();

    @Unique
    @Override
    public List<Pair<ModelPart, String>> countereds_accurate_hitboxes_template_1_21_1$getParts() {
        ModelPart root = this.getPart();

        // Nur ModelParts sammeln, die Cuboids enthalten
        List<Pair<ModelPart, String>> children = new ArrayList<>();

        if (root.hasChild(EntityModelPartNames.HEAD)) {
            children.add(new Pair<>(root.getChild(EntityModelPartNames.HEAD), "child_part"));
        }
        if (root.hasChild(EntityModelPartNames.BODY)) {
            children.add(new Pair<>(root.getChild(EntityModelPartNames.BODY), "child_part"));
        }
        if (root.hasChild(EntityModelPartNames.LEFT_ARM)) {
            children.add(new Pair<>(root.getChild(EntityModelPartNames.LEFT_ARM), "child_part"));
        }
        if (root.hasChild(EntityModelPartNames.RIGHT_ARM)) {
            children.add(new Pair<>(root.getChild(EntityModelPartNames.RIGHT_ARM), "child_part"));
        }
        if (root.hasChild(EntityModelPartNames.LEFT_LEG)) {
            children.add(new Pair<>(root.getChild(EntityModelPartNames.LEFT_LEG), "child_part"));
        }
        if (root.hasChild(EntityModelPartNames.RIGHT_LEG)) {
            children.add(new Pair<>(root.getChild(EntityModelPartNames.RIGHT_LEG), "child_part"));
        }

        if (root.hasChild("body_front")) {
            children.add(new Pair<>(root.getChild("body_front"), "child_part"));
        }
        if (root.hasChild("body_back")) {
            children.add(new Pair<>(root.getChild("body_back"), "child_part"));
        }

        if (root.hasChild(EntityModelPartNames.RIGHT_HIND_LEG)) {
            children.add(new Pair<>(root.getChild(EntityModelPartNames.RIGHT_HIND_LEG), "child_part"));
        }
        if (root.hasChild(EntityModelPartNames.LEFT_HIND_LEG)) {
            children.add(new Pair<>(root.getChild(EntityModelPartNames.LEFT_HIND_LEG), "child_part"));
        }
        if (root.hasChild("right_middle_hind_leg")) {
            children.add(new Pair<>(root.getChild("right_middle_hind_leg"), "child_part"));
        }
        if (root.hasChild("left_middle_hind_leg")) {
            children.add(new Pair<>(root.getChild("left_middle_hind_leg"), "child_part"));
        }
        if (root.hasChild("right_middle_front_leg")) {
            children.add(new Pair<>(root.getChild("right_middle_front_leg"), "child_part"));
        }
        if (root.hasChild("left_middle_front_leg")) {
            children.add(new Pair<>(root.getChild("left_middle_front_leg"), "child_part"));
        }
        if (root.hasChild(EntityModelPartNames.RIGHT_FRONT_LEG)) {
            children.add(new Pair<>(root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG), "child_part"));
        }
        if (root.hasChild(EntityModelPartNames.LEFT_FRONT_LEG)) {
            children.add(new Pair<>(root.getChild(EntityModelPartNames.LEFT_FRONT_LEG), "child_part"));
        }
        if (root.hasChild("body0")) {
            children.add(new Pair<>(root.getChild("body0"), "child_part"));
        }
        if (root.hasChild("body1")) {
            children.add(new Pair<>(root.getChild("body1"), "child_part"));
        }

        for (int i = 0; i < 7; i++) {
            if (root.hasChild("tentacle" + i)) {
                children.add(new Pair<>(root.getChild("tentacle" + i), "child_part"));
            }
        }

        if (!children.isEmpty()) {
            return children;
        }
        return new ArrayList<Pair<ModelPart, String>>(List.of(new Pair<>(root, "root")));
    }
}

