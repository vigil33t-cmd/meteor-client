/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud.modules;

import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class ContainerViewerHud extends HudElement {
    private static final Identifier TEXTURE = new Identifier("meteor-client", "textures/container.png");
    private static final Identifier TEXTURE_TRANSPARENT = new Identifier("meteor-client", "textures/container-transparent.png");

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    private final Setting<Background> background = sgGeneral.add(new EnumSetting.Builder<Background>()
        .name("background")
        .description("Background of container viewer.")
        .defaultValue(Background.Texture)
        .build()
    );

    private final Setting<Integer> opacity = sgGeneral.add(new IntSetting.Builder()
        .name("background-opacity")
        .description("Background opacity.")
        .defaultValue(255)
        .min(0)
        .sliderRange(0, 255)
        .build()
    );

    private final Setting<Boolean> echestNoItem = sgGeneral.add(new BoolSetting.Builder()
        .name("echest-when-empty")
        .description("Display contents of ender chest if not holding any other container.")
        .defaultValue(false)
        .build()
    );

    private final ItemStack[] inventory = new ItemStack[9 * 3];

    public ContainerViewerHud(HUD hud) {
        super(hud, "container-viewer", "Displays held containers.", false);
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(background.get().width * scale.get(), background.get().height * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = box.getX();
        double y = box.getY();

        ItemStack container = getContainer();
        if (container == null) return;

        if (background.get() != Background.None) {
            drawBackground((int) x, (int) y, container);
        }

        Utils.getItemsInContainerItem(container, inventory);

        for (int row = 0; row < 3; row++) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = inventory[row * 9 + i];
                if (stack == null || stack.isEmpty()) continue;

                int itemX = background.get() == Background.Texture ? (int) (x + (8 + i * 18) * scale.get()) : (int) (x + (1 + i * 18) * scale.get());
                int itemY = background.get() == Background.Texture ? (int) (y + (7 + row * 18) * scale.get()) : (int) (y + (1 + row * 18) * scale.get());

                RenderUtils.drawItem(stack, itemX, itemY, scale.get(), true);
            }
        }
    }

    private ItemStack getContainer() {
        if (isInEditor()) return Items.ENDER_CHEST.getDefaultStack();

        ItemStack stack = mc.player.getOffHandStack();
        if (Utils.hasItems(stack)) return stack;

        stack = mc.player.getMainHandStack();
        if (Utils.hasItems(stack)) return stack;

        return echestNoItem.get() ? Items.ENDER_CHEST.getDefaultStack() : null;
    }

    private void drawBackground(int x, int y, ItemStack container) {
        int w = (int) box.width;
        int h = (int) box.height;

        switch (background.get()) {
            case Texture, Outline -> {
                GL.bindTexture(background.get() == Background.Texture ? TEXTURE : TEXTURE_TRANSPARENT);

                Renderer2D.TEXTURE.begin();
                Renderer2D.TEXTURE.texQuad(x, y, w, h, Utils.getShulkerColor(container).a(opacity.get()));
                Renderer2D.TEXTURE.render(null);
            }
            case Flat -> {
                Renderer2D.COLOR.begin();
                Renderer2D.COLOR.quad(x, y, w, h, Utils.getShulkerColor(container).a(opacity.get()));
                Renderer2D.COLOR.render(null);
            }
        }
    }

    public enum Background {
        None(162, 54),
        Texture(176, 67),
        Outline(162, 54),
        Flat(162, 54);

        private int width, height;

        Background(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

}
