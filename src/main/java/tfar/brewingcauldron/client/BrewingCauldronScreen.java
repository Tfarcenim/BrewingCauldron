package tfar.brewingcauldron.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import tfar.brewingcauldron.BrewingCauldron;
import tfar.brewingcauldron.BrewingCauldronMenu;
import tfar.brewingcauldron.FluidSlot;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class BrewingCauldronScreen extends AbstractContainerScreen<BrewingCauldronMenu> {
    private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

    @Nullable
    protected FluidSlot hoveredFluidSlot;

    private static final ResourceLocation BREWING_STAND_LOCATION = BrewingCauldron.id("textures/gui/brewing_cauldron.png");


    public BrewingCauldronScreen(BrewingCauldronMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pX, int pY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BREWING_STAND_LOCATION);
        int $$4 = (this.width - this.imageWidth) / 2;
        int $$5 = (this.height - this.imageHeight) / 2;
        this.blit(pPoseStack, $$4, $$5, 0, 0, this.imageWidth, this.imageHeight);
        int $$6 = menu.getFuel();
        int $$7 = Mth.clamp((18 * $$6 + 20 - 1) / 20, 0, 18);
        if ($$7 > 0) {
            this.blit(pPoseStack, $$4 + 60, $$5 + 44, 176, 29, $$7, 4);
        }

        int $$8 = menu.getBrewingTicks();
        if ($$8 > 0) {
            int $$9 = (int)(28.0F * (1.0F - (float)$$8 / 400.0F));
            if ($$9 > 0) {
                this.blit(pPoseStack, $$4 + 97, $$5 + 16, 176, 0, 9, $$9);
            }

            $$9 = BUBBLELENGTHS[$$8 / 2 % 7];
            if ($$9 > 0) {
                this.blit(pPoseStack, $$4 + 63, $$5 + 14 + 29 - $$9, 185, 29 - $$9, 12, $$9);
            }
        }

    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
        if(this.menu.getCarried().isEmpty() && this.hoveredFluidSlot != null && this.hoveredFluidSlot.hasFluid()) {
            renderTooltip(pPoseStack, List.of(hoveredFluidSlot.getFluid().getDisplayName()), Optional.empty(),pX,pY,font);
        }
    }

    private void renderFluidSlot(PoseStack pGuiGraphics, FluidSlot pSlot) {
        int x = pSlot.x;
        int y = pSlot.y;
        FluidStack stack = pSlot.getFluid();
        pGuiGraphics.pushPose();

        if (!stack.isEmpty()) {
            ModClient.renderFluidInGui(pGuiGraphics,x, y, stack,"");
        }

        pGuiGraphics.popPose();
    }

    @Nullable
    private FluidSlot findFluidSlot(double pMouseX, double pMouseY) {
        for (int i = 0; i < this.menu.fluidSlots.size(); ++i) {
            FluidSlot slot = this.menu.fluidSlots.get(i);
            if (this.isHovering(slot, pMouseX, pMouseY)) {
                return slot;
            }
        }

        return null;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.hoveredFluidSlot = null;
        pPoseStack.pushPose();
        pPoseStack.translate(leftPos, topPos, 0);
        for (int k = 0; k < this.menu.fluidSlots.size(); ++k) {
            FluidSlot slot = this.menu.fluidSlots.get(k);
            this.renderFluidSlot(pPoseStack, slot);

            if (this.isHovering(slot, pMouseX, pMouseY)) {
                this.hoveredFluidSlot = slot;
                int j2 = slot.x;
                int k2 = slot.y;
                renderSlotHighlight(pPoseStack, j2, k2, 0);
            }
        }

        pPoseStack.popPose();

        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    private boolean isHovering(FluidSlot $$0, double $$1, double $$2) {
        return this.isHovering($$0.x, $$0.y, 16, 16, $$1, $$2);
    }
}
