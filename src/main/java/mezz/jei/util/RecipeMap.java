package mezz.jei.util;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import mezz.jei.Internal;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * A RecipeMap efficiently links Recipes, IRecipeCategory, and Ingredients.
 */
public class RecipeMap {

	@Nonnull
	private final Table<IRecipeCategory, String, List<Object>> recipeTable = HashBasedTable.create();
	@Nonnull
	private final ArrayListMultimap<String, IRecipeCategory> categoryMap = ArrayListMultimap.create();
	@Nonnull
	private final Ordering<IRecipeCategory> recipeCategoryOrdering;

	public RecipeMap(final RecipeCategoryComparator recipeCategoryComparator) {
		this.recipeCategoryOrdering = Ordering.from(recipeCategoryComparator);
	}

	@Nonnull
	public <T> ImmutableList<IRecipeCategory> getRecipeCategories(@Nonnull T ingredient, @Nonnull IIngredientType<T> ingredientType) {
		Set<IRecipeCategory> recipeCategories = new HashSet<>();
		Set<String> stackKeys = new HashSet<>();
		stackKeys.add(ingredientType.getKey(ingredient));
		stackKeys.add(ingredientType.getWildcardKey(ingredient));
		for (String stackKey : stackKeys) {
			recipeCategories.addAll(categoryMap.get(stackKey));
		}
		return recipeCategoryOrdering.immutableSortedCopy(recipeCategories);
	}

	public <T> void addRecipeCategory(@Nonnull IRecipeCategory recipeCategory, @Nonnull T ingredient, @Nonnull IIngredientType<T> ingredientType) {
		String stackKey = ingredientType.getKey(ingredient);
		List<IRecipeCategory> recipeCategories = categoryMap.get(stackKey);
		if (!recipeCategories.contains(recipeCategory)) {
			recipeCategories.add(recipeCategory);
		}
	}

	@Nonnull
	public <T> ImmutableList<Object> getRecipes(@Nonnull IRecipeCategory recipeCategory, @Nonnull T ingredient, @Nonnull IIngredientType<T> ingredientType) {
		Map<String, List<Object>> recipesForType = recipeTable.row(recipeCategory);

		Set<String> stackKeys = new HashSet<>();
		stackKeys.add(ingredientType.getKey(ingredient));
		stackKeys.add(ingredientType.getWildcardKey(ingredient));

		ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();
		for (String name : stackKeys) {
			List<Object> recipes = recipesForType.get(name);
			if (recipes != null) {
				listBuilder.addAll(recipes);
			}
		}
		return listBuilder.build();
	}

	public <T> void addRecipe(@Nonnull Object recipe, @Nonnull IRecipeCategory recipeCategory, @Nonnull Iterable<T> ingredients, @Nonnull IIngredientType<T> ingredientType) {
		Map<String, List<Object>> recipesForType = recipeTable.row(recipeCategory);

		for (T ingredient : ingredients) {
			if (ingredient == null) {
				continue;
			}

			String stackKey = ingredientType.getKey(ingredient);
			List<Object> recipes = recipesForType.get(stackKey);
			if (recipes == null) {
				recipes = Lists.newArrayList();
				recipesForType.put(stackKey, recipes);
			}
			recipes.add(recipe);

			addRecipeCategory(recipeCategory, ingredient, ingredientType);
		}
	}
}
