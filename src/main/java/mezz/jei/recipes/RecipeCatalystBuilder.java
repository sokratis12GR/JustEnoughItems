package mezz.jei.recipes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.ingredients.IngredientRegistry;

public class RecipeCatalystBuilder {
	private final ImmutableListMultimap.Builder<IRecipeCategory, Object> recipeCatalystsBuilder = ImmutableListMultimap.builder();
	private final ImmutableMultimap.Builder<String, ResourceLocation> categoriesForRecipeCatalystKeysBuilder = ImmutableMultimap.builder();
	private final ImmutableMap<ResourceLocation, IRecipeCategory> recipeCategoriesMap;
	private final IngredientRegistry ingredientRegistry;

	public RecipeCatalystBuilder(
		ImmutableMap<ResourceLocation, IRecipeCategory> recipeCategoriesMap,
		IngredientRegistry ingredientRegistry
	) {
		this.recipeCategoriesMap = recipeCategoriesMap;
		this.ingredientRegistry = ingredientRegistry;
	}

	public void addCatalysts(ListMultiMap<ResourceLocation, Object> recipeCatalysts, RecipeMap recipeInputMap) {
		for (Map.Entry<ResourceLocation, List<Object>> recipeCatalystEntry : recipeCatalysts.entrySet()) {
			ResourceLocation recipeCategoryUid = recipeCatalystEntry.getKey();
			IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			if (recipeCategory != null) {
				Collection<Object> catalystIngredients = recipeCatalystEntry.getValue();
				recipeCatalystsBuilder.putAll(recipeCategory, catalystIngredients);
				for (Object catalystIngredient : catalystIngredients) {
					addCatalyst(catalystIngredient, recipeCategory, recipeInputMap);
				}
			}
		}
	}

	private <T> void addCatalyst(T catalystIngredient, IRecipeCategory recipeCategory, RecipeMap recipeInputMap) {
		IIngredientType<T> ingredientType = ingredientRegistry.getIngredientType(catalystIngredient);
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
		recipeInputMap.addRecipeCategory(recipeCategory, catalystIngredient, ingredientHelper);
		String catalystIngredientKey = getUniqueId(catalystIngredient, ingredientRegistry);
		categoriesForRecipeCatalystKeysBuilder.put(catalystIngredientKey, recipeCategory.getUid());
	}

	private static  <T> String getUniqueId(T ingredient, IIngredientRegistry ingredientRegistry) {
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		return ingredientHelper.getUniqueId(ingredient);
	}

	public ImmutableListMultimap<IRecipeCategory, Object> buildRecipeCatalysts() {
		return recipeCatalystsBuilder.build();
	}

	public ImmutableMultimap<String, ResourceLocation> buildCategoriesForRecipeCatalystKeys() {
		return categoriesForRecipeCatalystKeysBuilder.build();
	}
}