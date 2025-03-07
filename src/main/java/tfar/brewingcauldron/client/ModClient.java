package tfar.brewingcauldron.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.FluidContainerColorer;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tfar.brewingcauldron.BrewingCauldronBlockEntity;
import tfar.brewingcauldron.Init;

public class ModClient {
    public static void init(IEventBus bus) {
        bus.addListener(ModClient::setup);
        bus.addListener(ModClient::blockColors);
        bus.addListener(ModClient::itemColors);
    }

    static BlockColor color = (state, level, pos, i) -> {
        if (level != null && pos != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BrewingCauldronBlockEntity brewingCauldronBlockEntity) {
                return brewingCauldronBlockEntity.getColor();
            }
        }
        return 0xffffffff;
    };


    static void itemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register(new FluidContainerColorer(), Init.ModItems.POTION_BUCKET);
    }


    static void blockColors(ColorHandlerEvent.Block event) {
        event.getBlockColors().register(color, Init.ModBlocks.WATER_BREWING_CAULDRON);
    }

    static void setup(FMLClientSetupEvent event) {
        MenuScreens.register(Init.ModMenuTypes.BREWING_CAULDRON, BrewingCauldronScreen::new);
        OverlayRegistry.registerOverlayTop("brewing_cauldron", overlay);
    }

    public static final IIngameOverlay overlay = (gui, poseStack, v, i, i1) -> {
        if (!Minecraft.getInstance().options.hideGui) {
            gui.setupOverlayRenderState(true, false);
            HitResult hitResult = Minecraft.getInstance().hitResult;
            if (hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() != HitResult.Type.MISS) {
                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(blockHitResult.getBlockPos());
                if (blockEntity instanceof BrewingCauldronBlockEntity cauldronBE) {
                    ItemStack stack = cauldronBE.handler.getIngredient();
                    int centerx = i / 2;
                    int centery = i1 / 2;
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    itemRenderer.renderAndDecorateFakeItem(stack, centerx-10, centery-20);
                    itemRenderer.renderGuiItemDecorations(gui.getFont(),stack,centerx-10,centery-20);

                    ItemStack fuel = cauldronBE.handler.getFuel();
                    itemRenderer.renderAndDecorateFakeItem(fuel, centerx - 30, centery-20);
                    itemRenderer.renderGuiItemDecorations(gui.getFont(),fuel,centerx-30,centery-20);
                }
            }
        }
    };

    public static void renderFluidInGui(PoseStack poseStack, int x, int y, FluidStack stack, String text) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        int color = stack.getFluid().getAttributes().getColor(stack);
        TextureAtlasSprite sprite = FluidSpriteCache.getStillTexture(stack);
        RenderSystem.setShaderColor((color >> 16 & 0xff) / 255f, (color >> 8 & 0xff) / 255f, (color & 0xff) / 255f, 1);
        RenderSystem.enableDepthTest();
        GuiComponent.blit(poseStack, x, y, 0, 16, 16, sprite);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        //StackSizeRenderer.renderSizeLabel(guiGraphics, Minecraft.getInstance().font, x,y,text,200);
        RenderSystem.disableBlend();
    }

    public static Player getLocalPlayer() {
        return Minecraft.getInstance().player;
    }
}
