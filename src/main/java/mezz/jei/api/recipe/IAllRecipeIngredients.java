package mezz.jei.api.recipe;

import javax.annotation.Nonnull;

public interface IAllRecipeIngredients {
	@Nonnull
	<T> IRecipeIngredients<T> get(Class<T> ingredientClass);
}
