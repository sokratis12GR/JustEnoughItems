package mezz.jei.api.recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * Each type of recipe ingredient like ItemStack or FluidStack needs an {@link IIngredientType} to be understood by JEI.

 * @param <T> The ingredient type.
 */
public interface IIngredientType<T> {
	/**
	 * @return the class handled by this
	 */
	@Nonnull
	Class<T> getIngredientClass();

	/**
	 * @return a unique key for this ingredient, used for comparison and map keys.
	 */
	@Nonnull
	String getKey(T ingredient);

	/**
	 * @return a wildcard key for this ingredient if one exists, used for comparison and map keys.
	 */
	@Nonnull
	String getWildcardKey(@Nonnull T ingredient);

	/**
	 * Detects wildcard ingredients and expands them out to a complete list.
	 * Used for displaying rotating lists of ingredients.
	 * If there are no wildcards, just returns contained.
	 */
	@Nonnull
	Collection<T> expandSubtypes(Collection<T> contained);

	/**
	 * Finds a matching element in ingredients and returns it.
	 */
	@Nullable
	T getMatch(Iterable<T> ingredients, @Nonnull T toMatch);
}
