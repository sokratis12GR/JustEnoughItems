package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.recipe.IAllRecipeIngredients;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.plugins.vanilla.VanillaRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedOreRecipeWrapper extends VanillaRecipeWrapper implements IShapedCraftingRecipeWrapper {

	@Nonnull
	private final ShapedOreRecipe recipe;
	private final List<List<ItemStack>> inputs;
	private final int width;
	private final int height;

	public ShapedOreRecipeWrapper(@Nonnull ShapedOreRecipe recipe) {
		this.recipe = recipe;
		this.inputs = new ArrayList<>();
		for (Object input : this.recipe.getInput()) {
			inputs.add(VanillaPlugin.jeiHelpers.getStackHelper().toItemStackList(input));
		}
		this.width = ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, this.recipe, "width");
		this.height = ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, this.recipe, "height");
	}

	@Override
	public void getInputs(IAllRecipeIngredients inputs) {
		inputs.get(ItemStack.class).setSlotsLists(this.inputs);
	}

	@Override
	public void getOutputs(IAllRecipeIngredients outputs) {
		outputs.get(ItemStack.class).setSlot(0, recipe.getRecipeOutput());
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

}
