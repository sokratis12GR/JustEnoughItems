package mezz.jei.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import net.minecraft.item.ItemStack;

/**
 * The IRecipeManager offers several functions for retrieving and handling recipes.
 * The IRecipeManager instance is provided in {@link IJeiRuntime#getRecipeRegistry()}.
 * Available to IModPlugins
 */
public interface IRecipeRegistry {

	/** Returns the IRecipeHandler associated with the recipeClass or null if there is none */
	@Nullable
	<T> IRecipeHandler<T> getRecipeHandler(@Nonnull Class<? extends T> recipeClass);

	/** Returns an unmodifiable list of all Recipe Categories */
	@Nonnull
	List<IRecipeCategory> getRecipeCategories();

	/**
	 * Returns an {@link IIngredientType} for the given ingredient if one exists.
	 * Throws an exception if the ingredientType has not been registered.
	 */
	@Nonnull
	<T> IIngredientType<T> getTypeForIngredient(@Nonnull Class<? extends T> ingredientClass);
	@Nonnull
	<T> IIngredientType<T> getTypeForIngredient(@Nonnull T ingredient);


	/** Returns an unmodifiable list of Recipe Categories */
	@Nonnull
	List<IRecipeCategory> getRecipeCategories(@Nonnull List<String> recipeCategoryUids);

	/** Returns an unmodifiable list of Recipe Categories that have the ingredient as an input */
	@Nonnull
	<T> List<IRecipeCategory> getRecipeCategoriesWithInput(@Nonnull T input);

	/** Returns an unmodifiable list of Recipe Categories that have the ingredient as an output */
	@Nonnull
	<T> List<IRecipeCategory> getRecipeCategoriesWithOutput(@Nonnull T output);

	/** Returns an unmodifiable list of Recipes of recipeCategory that have the ingredient as an input */
	@Nonnull
	<T> List<Object> getRecipesWithInput(@Nonnull IRecipeCategory recipeCategory, @Nonnull T input);

	/** Returns an unmodifiable list of Recipes of recipeCategory that have the ingredient as an output */
	@Nonnull
	<T> List<Object> getRecipesWithOutput(@Nonnull IRecipeCategory recipeCategory, @Nonnull T output);

	/** Returns an unmodifiable list of Recipes in recipeCategory */
	@Nonnull
	List<Object> getRecipes(@Nonnull IRecipeCategory recipeCategory);

	/**
	 * Returns an unmodifiable collection of ItemStacks that can craft recipes from recipeCategory.
	 * These are registered with {@link IModRegistry#addRecipeCategoryCraftingItem(ItemStack, String...)}.
	 *
	 * @since JEI 3.3.0
	 */
	@Nonnull
	Collection<ItemStack> getCraftingItems(@Nonnull IRecipeCategory recipeCategory);

	/**
	 * Add a new recipe while the game is running.
	 * This is only for things like gated recipes becoming available, like the ones in Thaumcraft.
	 * Use your IRecipeHandler.isValid to determine which recipes are hidden, and when a recipe becomes valid you can add it here.
	 * (note that IRecipeHandler.isValid must be true when the recipe is added here for it to work)
	 */
	void addRecipe(@Nonnull Object recipe);
}
