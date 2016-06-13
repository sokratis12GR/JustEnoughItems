package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.Arrays;

import mezz.jei.api.recipe.IAllRecipeIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.plugins.vanilla.VanillaRecipeWrapper;

public class ShapedRecipesWrapper extends VanillaRecipeWrapper implements IShapedCraftingRecipeWrapper {

	@Nonnull
	private final ShapedRecipes recipe;

	public ShapedRecipesWrapper(@Nonnull ShapedRecipes recipe) {
		this.recipe = recipe;

		fixVanillaRecipesIngots();
	}

	/**
	 * Some Vanilla recipes have a bug where they have each ingredient with a stack size of 9.
	 * See https://bugs.mojang.com/browse/MC-103403
	 * @see net.minecraft.item.crafting.RecipesIngots#recipeItems
	 */
	private void fixVanillaRecipesIngots() {
		for (ItemStack itemStack : this.recipe.recipeItems) {
			if (itemStack == null || itemStack.stackSize != 9) {
				return;
			}
		}

		for (ItemStack itemStack : this.recipe.recipeItems) {
			itemStack.stackSize = 1;
		}
	}

	@Override
	public void getInputs(IAllRecipeIngredients inputs) {
		inputs.get(ItemStack.class).setSlots(Arrays.asList(recipe.recipeItems));
	}

	@Override
	public void getOutputs(IAllRecipeIngredients outputs) {
		outputs.get(ItemStack.class).setSlot(0, recipe.getRecipeOutput());
	}

	@Override
	public int getWidth() {
		return recipe.recipeWidth;
	}

	@Override
	public int getHeight() {
		return recipe.recipeHeight;
	}
}
