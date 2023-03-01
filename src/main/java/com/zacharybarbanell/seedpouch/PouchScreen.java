/*
 * This class is adapted from part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package com.zacharybarbanell.seedpouch;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PouchScreen extends AbstractContainerScreen<PouchContainer> {

    private static final ResourceLocation texture = new ResourceLocation("seedpouch:textures/gui/blanktallgui.png");

    public PouchScreen(PouchContainer container, Inventory playerInv, Component title) {
        super(container, playerInv, title);
        this.imageHeight += 36;

        // recompute, same as super
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);
        this.renderTooltip(ms, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
        int k = (width - imageWidth) / 2;
        int l = (height - imageHeight) / 2;
        blit(ms, k, l, 0, 0, imageWidth, imageHeight);

        for (Slot slot : menu.slots) {
            if (slot.container == menu.bagInv && !slot.hasItem()) {
                ItemStack stack = new ItemStack(menu.bagType.getSlots().get(slot.index));

                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;
                mc.getItemRenderer().renderGuiItem(stack, x, y);
                ms.pushPose();
                ms.translate(0, 0, mc.getItemRenderer().blitOffset + 200); // similar to
                                                                           // ItemRenderer.renderGuiItemDecorations
                mc.font.drawShadow(ms, "0", x + 11, y + 9, 0xFF6666);
                ms.popPose();
            }
        }
    }

}