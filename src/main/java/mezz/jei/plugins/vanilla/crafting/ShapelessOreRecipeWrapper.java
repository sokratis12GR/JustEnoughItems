package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.recipe.IAllRecipeIngredients;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ShapelessOreRecipeWrapper extends AbstractShapelessRecipeWrapper {

	@Nonnull
	private final ShapelessOreRecipe recipe;
	@Nonnull
	private final List<List<ItemStack>> inputs;

	public ShapelessOreRecipeWrapper(@Nonnull IGuiHelper guiHelper, @Nonnull ShapelessOreRecipe recipe) {
		super(guiHelper);
		this.recipe = recipe;
		this.inputs = new ArrayList<>();
		for (Object input : this.recipe.getInput()) {
			inputs.add(VanillaPlugin.jeiHelpers.getStackHelper().toItemStackList(input));
		}
	}

	@Override
	public void getInputs(IAllRecipeIngredients inputs) {
		inputs.get(ItemStack.class).setSlotsLists(this.inputs);
	}

	@Override
	public void getOutputs(IAllRecipeIngredients outputs) {
		outputs.get(ItemStack.class).setSlot(0, recipe.getRecipeOutput());
	}
}
