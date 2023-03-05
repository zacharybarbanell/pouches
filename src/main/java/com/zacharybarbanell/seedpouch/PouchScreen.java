/*
 * This class is adapted from part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package com.zacharybarbanell.seedpouch;

import org.lwjgl.opengl.GL11C;

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
    private final int containerRows;

    public PouchScreen(PouchContainer container, Inventory playerInv, Component title) {
        super(container, playerInv, title);
        this.containerRows = container.containerRows; // TODO do this properly
        this.imageHeight = 114 + this.containerRows * 18;

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
        this.blit(ms, k, l, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        this.blit(ms, k, l + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);

        for (Slot slot : menu.slots) {
            if (slot.container == menu.bagInv) {
                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;

                ms.pushPose();
                ms.translate(0, 0, mc.getItemRenderer().blitOffset + 200);
                this.blit(ms, x - 1, y - 1, 7, 139, 18, 18);
                ms.popPose();
            }
        }

        for (Slot slot : menu.slots) {
            if (slot.container == menu.bagInv && !slot.hasItem()) {
                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;

                ItemStack stack = new ItemStack(menu.bagType.getSlots().get(slot.index));

                mc.getItemRenderer().renderGuiItem(stack, x, y);

            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        for (Slot slot : menu.slots) {
            if (slot.container == menu.bagInv) {
                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;

                ms.pushPose();
                this.blit(ms, x - 1, y - 1, 7, 139, 18, 18);
                ms.popPose();
            }
        }
    }

}