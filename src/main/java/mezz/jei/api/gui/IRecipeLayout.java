package mezz.jei.api.gui;

import javax.annotation.Nonnull;

public interface IRecipeLayout {
	/**
	 * Contains all the ingredients of the specified type displayed on this recipe layout.
	 * Init them in your recipe category.
	 */
	@Nonnull
	<T> IGuiIngredientGroup<T> getIngredientGroup(Class<T> ingredientType);

	/**
	 * Moves the recipe transfer button's position relative to the recipe layout.
	 * By default the recipe transfer button is at the bottom, to the right of the recipe.
	 * If it doesn't fit there, you can use this to move it when you init the recipe layout.
	 */
	void setRecipeTransferButton(int posX, int posY);
}
