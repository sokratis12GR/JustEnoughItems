package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.recipe.IAllRecipeIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

public class ShapelessRecipesWrapper extends AbstractShapelessRecipeWrapper {

	@Nonnull
	private final ShapelessRecipes recipe;

	public ShapelessRecipesWrapper(@Nonnull IGuiHelper guiHelper, @Nonnull ShapelessRecipes recipe) {
		super(guiHelper);
		this.recipe = recipe;
	}

	@Override
	public void getInputs(IAllRecipeIngredients inputs) {
		inputs.get(ItemStack.class).setSlots(recipe.recipeItems);
	}

	@Override
	public void getOutputs(IAllRecipeIngredients outputs) {
		outputs.get(ItemStack.class).setSlot(0, recipe.getRecipeOutput());
	}
}
