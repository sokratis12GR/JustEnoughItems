package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IAllRecipeIngredients;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;

public class TippedArrowRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper {
	@Nonnull
	private final List<ItemStack> inputs;
	@Nonnull
	private final ItemStack outputStack;

	public TippedArrowRecipeWrapper(@Nonnull PotionType type) {
		ItemStack arrowStack = new ItemStack(Items.ARROW);
		ItemStack lingeringPotion = PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), type);
		this.inputs = Arrays.asList(
				arrowStack, arrowStack, arrowStack,
				arrowStack, lingeringPotion, arrowStack,
				arrowStack, arrowStack, arrowStack
		);
		outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
		PotionUtils.addPotionToItemStack(outputStack, type);
	}

	@Override
	public void getInputs(IAllRecipeIngredients inputs) {
		inputs.get(ItemStack.class).setSlots(this.inputs);
	}

	@Override
	public void getOutputs(IAllRecipeIngredients outputs) {
		outputs.get(ItemStack.class).setSlot(0, outputStack);
	}

	@Override
	public int getWidth() {
		return 3;
	}

	@Override
	public int getHeight() {
		return 3;
	}
}
