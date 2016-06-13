package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.Collection;

import mezz.jei.Internal;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.util.StackHelper;
import net.minecraft.item.ItemStack;

public class ItemStackType implements IIngredientType<ItemStack> {
	@Nonnull
	@Override
	public Class<ItemStack> getIngredientClass() {
		return ItemStack.class;
	}

	@Nonnull
	@Override
	public String getKey(ItemStack ingredient) {
		return Internal.getStackHelper().getUniqueIdentifierForStack(ingredient);
	}

	@Nonnull
	@Override
	public String getWildcardKey(@Nonnull ItemStack ingredient) {
		return Internal.getStackHelper().getUniqueIdentifierForStack(ingredient, StackHelper.UidMode.WILDCARD);
	}

	@Nonnull
	@Override
	public Collection<ItemStack> expandSubtypes(Collection<ItemStack> contained) {
		return Internal.getStackHelper().getAllSubtypes(contained);
	}

	@Override
	public ItemStack getMatch(Iterable<ItemStack> ingredients, @Nonnull ItemStack toMatch) {
		return Internal.getStackHelper().getMatch(ingredients, toMatch);
	}
}
