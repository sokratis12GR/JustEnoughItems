package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.Collection;

import mezz.jei.api.recipe.IIngredientType;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackType implements IIngredientType<FluidStack> {
	@Nonnull
	@Override
	public Class<FluidStack> getIngredientClass() {
		return FluidStack.class;
	}

	@Nonnull
	@Override
	public String getKey(FluidStack ingredient) {
		if (ingredient.tag != null) {
			return "fluid:" + ingredient.getFluid().getName() + ":" + ingredient.tag;
		}
		return "fluid:" + ingredient.getFluid().getName();
	}

	@Nonnull
	@Override
	public String getWildcardKey(@Nonnull FluidStack ingredient) {
		return "fluid:" + ingredient.getFluid().getName();
	}

	@Nonnull
	@Override
	public Collection<FluidStack> expandSubtypes(Collection<FluidStack> contained) {
		return contained;
	}

	@Override
	public FluidStack getMatch(Iterable<FluidStack> ingredients, @Nonnull FluidStack toMatch) {
		for (FluidStack fluidStack : ingredients) {
			if (toMatch.isFluidEqual(fluidStack)) {
				return fluidStack;
			}
		}
		return null;
	}
}
