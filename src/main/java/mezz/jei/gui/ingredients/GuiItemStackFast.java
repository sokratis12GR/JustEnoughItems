package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexTransformer;

import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;

@SuppressWarnings("deprecation")
public class GuiItemStackFast {
	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	private static final float RADS = (float) (180.0 / Math.PI);

	private final Matrix4f tempMat = new Matrix4f();
	private final int xPosition;
	private final int yPosition;
	private final int width;
	private final int height;
	private final int padding;

	private ItemStack itemStack;
	private IBakedModel bakedModel;

	public GuiItemStackFast(int xPosition, int yPosition, int padding) {
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.padding = padding;
		this.width = 16 + (2 * padding);
		this.height = 16 + (2 * padding);
	}

	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
		this.bakedModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(itemStack);
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public boolean isBuiltInRenderer() {
		return bakedModel != null && bakedModel.isBuiltInRenderer();
	}

	public boolean isGui3d() {
		return bakedModel != null && bakedModel.isGui3d();
	}

	public void clear() {
		this.itemStack = null;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return (itemStack != null) && (mouseX >= xPosition) && (mouseY >= yPosition) && (mouseX < xPosition + width) && (mouseY < yPosition + height);
	}

	public void renderItemAndEffectIntoGUI(boolean isGui3d) {
		if (itemStack == null) {
			return;
		}

		if (Config.editModeEnabled) {
			renderEditMode();
		}

		GlStateManager.pushMatrix();

		int x = xPosition + padding + 8;
		int y = yPosition + padding + 8;

		Matrix4f transformMat = new Matrix4f();
		transformMat.setIdentity();

		if (isGui3d) {
			tempMat.setIdentity();
			tempMat.setTranslation(new Vector3f(((float) x) / 20f, ((float) y) / 20f, (100.0F + 50f) / -20f));
			transformMat.mul(tempMat);
			tempMat.rotX(210f / RADS);
			transformMat.mul(tempMat);
			tempMat.rotY(-135f / RADS);
			transformMat.mul(tempMat);
		} else {
			tempMat.setIdentity();
			tempMat.setTranslation(new Vector3f(((float) x) / 32f, ((float) y) / 32f, (100.0F + 50f) / -32f));
			transformMat.mul(tempMat);
			tempMat.rotX(180f / RADS);
			transformMat.mul(tempMat);
		}

		handleCameraTransforms(transformMat, this.bakedModel, ItemCameraTransforms.TransformType.GUI);

		tempMat.setIdentity();
		tempMat.setScale(0.5f);
		transformMat.mul(tempMat);

		tempMat.setIdentity();
		tempMat.setTranslation(new Vector3f(-0.5f, -0.5f, -0.5f));
		transformMat.mul(tempMat);

		Matrix3f invTransposeMat = new Matrix3f();
		transformMat.getRotationScale(invTransposeMat);
		invTransposeMat.invert();
		invTransposeMat.transpose();

//		ForgeHooksClient.multiplyCurrentGlMatrix(transformMat);
//
//		transformMat.setIdentity();
//		invTransposeMat.setIdentity();

		renderModel(transformMat, invTransposeMat, bakedModel, itemStack);

		if (itemStack.hasEffect()) {
			renderEffect(transformMat, invTransposeMat, bakedModel);
		}

		GlStateManager.popMatrix();
	}

	public void handleCameraTransforms(Matrix4f transformMat, IBakedModel model, ItemCameraTransforms.TransformType cameraTransformType) {
		if (model instanceof IPerspectiveAwareModel) {
			Pair<IBakedModel, Matrix4f> pair = ((IPerspectiveAwareModel) model).handlePerspective(cameraTransformType);

			if (pair.getRight() != null) {
				transformMat.mul(pair.getRight());
			}
		} else {
			applyVanillaTransform(transformMat, model.getItemCameraTransforms().gui);
		}
	}

	public void applyVanillaTransform(Matrix4f transformMat, ItemTransformVec3f transform) {
		if (transform != ItemTransformVec3f.DEFAULT) {
			tempMat.setIdentity();
			tempMat.setTranslation(transform.translation);
			transformMat.mul(tempMat);

			tempMat.rotY(transform.rotation.y);
			transformMat.mul(tempMat);

			tempMat.rotX(transform.rotation.x);
			transformMat.mul(tempMat);

			tempMat.rotZ(transform.rotation.z);
			transformMat.mul(tempMat);

			tempMat.setIdentity();
			tempMat.setM00(transform.scale.x);
			tempMat.setM11(transform.scale.y);
			tempMat.setM22(transform.scale.z);
			transformMat.mul(tempMat);
		}
	}

	public static BakedQuad applyToQuad(final Matrix4f transform, final Matrix3f invtranspose, BakedQuad quad) {
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
		IVertexConsumer cons = new VertexTransformer(builder)
		{
			@Override public void put(int element, float... data)
			{
				VertexFormatElement el = DefaultVertexFormats.ITEM.getElement(element);

				switch(el.getUsage())
				{
					case POSITION:
						float[] newData = new float[4];
						Vector4f vec = new Vector4f(data);
						transform.transform(vec);
						vec.get(newData);
						parent.put(element, newData);
						break;
					case NORMAL:
						float[] newData2 = new float[4];
						Vector3f vec2 = new Vector3f(data);
						invtranspose.transform(vec2);
						vec2.get(newData2);
						newData2[3] = 0;
						parent.put(element, newData2);
						break;
					default:
						parent.put(element, data);
						break;
				}
			}
		};
		quad.pipe(cons);
		return builder.build();
	}

	private void renderModel(Matrix4f transformMat, Matrix3f invTransposeMat,IBakedModel model, ItemStack stack) {
		renderModel(transformMat, invTransposeMat, model, -1, stack);
	}

	private void renderModel(Matrix4f transformMat, Matrix3f invTransposeMat, IBakedModel model, int color, ItemStack stack) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.startDrawingQuads();
		worldrenderer.setVertexFormat(DefaultVertexFormats.ITEM);

		for (EnumFacing enumfacing : EnumFacing.VALUES) {
			this.renderQuads(transformMat, invTransposeMat, worldrenderer, model.getFaceQuads(enumfacing), color, stack);
		}

		this.renderQuads(transformMat, invTransposeMat, worldrenderer, model.getGeneralQuads(), color, stack);
		tessellator.draw();
	}

	private void renderQuads(Matrix4f transformMat, Matrix3f invTransposeMat, WorldRenderer renderer, List quads, int color, ItemStack stack) {
		boolean flag = color == -1 && stack != null;
		BakedQuad bakedquad;
		int j;

		for (Object quad : quads) {
			bakedquad = (BakedQuad) quad;
			j = color;

			bakedquad = applyToQuad(transformMat, invTransposeMat, bakedquad);

			if (flag && bakedquad.hasTintIndex()) {
				j = stack.getItem().getColorFromItemStack(stack, bakedquad.getTintIndex());

				if (EntityRenderer.anaglyphEnable) {
					j = TextureUtil.anaglyphColor(j);
				}

				j |= -16777216;
			}
			LightUtil.renderQuadColor(renderer, bakedquad, j);
		}
	}

	private void renderEffect(Matrix4f transformMat, Matrix3f invTransposeMat, IBakedModel model) {
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(514);
		GlStateManager.blendFunc(768, 1);
		textureManager.bindTexture(RES_ITEM_GLINT);
		GlStateManager.matrixMode(5890);

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
		GlStateManager.translate(f, 0.0F, 0.0F);
		GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
		this.renderModel(transformMat, invTransposeMat, model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
		GlStateManager.translate(-f1, 0.0F, 0.0F);
		GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
		this.renderModel(transformMat, invTransposeMat, model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.matrixMode(5888);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
	}

	private void renderModel(Matrix4f transformMat, Matrix3f invTransposeMat, IBakedModel model, int color) {
		this.renderModel(transformMat, invTransposeMat, model, color, null);
	}

	public void renderSlow() {
		if (Config.editModeEnabled) {
			renderEditMode();
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		FontRenderer font = getFontRenderer(minecraft, itemStack);
		RenderItem renderItem = minecraft.getRenderItem();
		renderItem.renderItemAndEffectIntoGUI(itemStack, xPosition + padding, yPosition + padding);
		renderItem.renderItemOverlayIntoGUI(font, itemStack, xPosition + padding, yPosition + padding, null);
	}

	private void renderEditMode() {
		if (Config.isItemOnConfigBlacklist(itemStack, false)) {
			GuiScreen.drawRect(xPosition + padding, yPosition + padding, xPosition + 8 + padding, yPosition + 16 + padding, 0xFFFFFF00);
		}
		if (Config.isItemOnConfigBlacklist(itemStack, true)) {
			GuiScreen.drawRect(xPosition + 8 + padding, yPosition + padding, xPosition + 16 + padding, yPosition + 16 + padding, 0xFFFF0000);
		}
	}

	public FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}

	public void drawHovered(Minecraft minecraft, int mouseX, int mouseY) {
		try {
			Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + width, 0x7FFFFFFF);

			renderSlow();

			List<String> tooltip = getTooltip(minecraft, itemStack);
			FontRenderer fontRenderer = getFontRenderer(minecraft, itemStack);
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);
		} catch (RuntimeException e) {
			Log.error("Exception when rendering tooltip on {}.", itemStack, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		List<String> list = itemStack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, itemStack.getRarity().rarityColor + list.get(k));
			} else {
				list.set(k, EnumChatFormatting.GRAY + list.get(k));
			}
		}

		if (Config.editModeEnabled) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + Translator.translateToLocal("gui.jei.editMode.description"));
			if (Config.isItemOnConfigBlacklist(itemStack, false)) {
				String description = EnumChatFormatting.YELLOW + Translator.translateToLocal("gui.jei.editMode.description.show");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
			} else {
				String description = EnumChatFormatting.YELLOW + Translator.translateToLocal("gui.jei.editMode.description.hide");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
			}

			Item item = itemStack.getItem();
			if (item.getHasSubtypes()) {
				if (Config.isItemOnConfigBlacklist(itemStack, true)) {
					String description = EnumChatFormatting.RED + Translator.translateToLocal("gui.jei.editMode.description.show.wild");
					list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
				} else {
					String description = EnumChatFormatting.RED + Translator.translateToLocal("gui.jei.editMode.description.hide.wild");
					list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
				}
			}
		}

		return list;
	}
}
