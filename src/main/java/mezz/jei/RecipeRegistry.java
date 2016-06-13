package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.config.Config;
import mezz.jei.gui.RecipeClickableArea;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import mezz.jei.util.RecipeCategoryComparator;
import mezz.jei.util.RecipeMap;
import mezz.jei.util.StackHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class RecipeRegistry implements IRecipeRegistry {
	private final List<IRecipeHandler> recipeHandlers;
	private final ImmutableTable<Class, String, IRecipeTransferHandler> recipeTransferHandlers;
	private final ImmutableMultimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap;
	private final ImmutableMultimap<IRecipeCategory, ItemStack> craftItemsForCategories;
	private final ImmutableMultimap<String, String> categoriesForCraftItemKeys;
	private final ImmutableMap<String, IRecipeCategory> recipeCategoriesMap;
	private final ImmutableMap<Class, IIngredientType> ingredientTypes;
	private final ListMultimap<IRecipeCategory, Object> recipesForCategories;
	private final RecipeMap recipeInputMap;
	private final RecipeMap recipeOutputMap;
	private final Set<Class> unhandledRecipeClasses;

	public RecipeRegistry(
			@Nonnull List<IRecipeCategory> recipeCategories,
			@Nonnull List<IRecipeHandler> recipeHandlers,
			@Nonnull List<IRecipeTransferHandler> recipeTransferHandlers,
			@Nonnull List<Object> recipes,
			@Nonnull List<IIngredientType> ingredientTypes,
			@Nonnull Multimap<Class<? extends GuiContainer>, RecipeClickableArea> recipeClickableAreasMap,
			@Nonnull Multimap<String, ItemStack> craftItemsForCategories
	) {
		this.recipeCategoriesMap = buildRecipeCategoriesMap(recipeCategories);
		this.recipeTransferHandlers = buildRecipeTransferHandlerTable(recipeTransferHandlers);
		this.recipeHandlers = buildRecipeHandlersList(recipeHandlers);
		this.ingredientTypes = buildIngredientTypeMap(ingredientTypes);
		this.recipeClickableAreasMap = ImmutableMultimap.copyOf(recipeClickableAreasMap);

		RecipeCategoryComparator recipeCategoryComparator = new RecipeCategoryComparator(recipeCategories);
		this.recipeInputMap = new RecipeMap(recipeCategoryComparator);
		this.recipeOutputMap = new RecipeMap(recipeCategoryComparator);

		this.unhandledRecipeClasses = new HashSet<>();

		this.recipesForCategories = ArrayListMultimap.create();
		addRecipes(recipes);

		StackHelper stackHelper = Internal.getStackHelper();

		ImmutableMultimap.Builder<IRecipeCategory, ItemStack> craftItemsForCategoriesBuilder = ImmutableMultimap.builder();
		ImmutableMultimap.Builder<String, String> categoriesForCraftItemKeysBuilder = ImmutableMultimap.builder();
		for (String recipeCategoryUid : craftItemsForCategories.keySet()) {
			IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			Collection<ItemStack> craftItems = craftItemsForCategories.get(recipeCategoryUid);
			craftItemsForCategoriesBuilder.putAll(recipeCategory, craftItems);
			for (ItemStack craftItem : craftItems) {
				IIngredientType<ItemStack> ingredientType = getTypeForIngredient(craftItem.getClass());
				recipeInputMap.addRecipeCategory(recipeCategory, craftItem, ingredientType);
				String craftItemKey = stackHelper.getUniqueIdentifierForStack(craftItem);
				categoriesForCraftItemKeysBuilder.put(craftItemKey, recipeCategoryUid);
			}
		}

		this.craftItemsForCategories = craftItemsForCategoriesBuilder.build();
		this.categoriesForCraftItemKeys = categoriesForCraftItemKeysBuilder.build();
	}

	private static ImmutableMap<String, IRecipeCategory> buildRecipeCategoriesMap(@Nonnull List<IRecipeCategory> recipeCategories) {
		ImmutableMap.Builder<String, IRecipeCategory> mapBuilder = ImmutableMap.builder();
		for (IRecipeCategory recipeCategory : recipeCategories) {
			mapBuilder.put(recipeCategory.getUid(), recipeCategory);
		}
		return mapBuilder.build();
	}

	private static ImmutableMap<Class, IIngredientType> buildIngredientTypeMap(@Nonnull List<IIngredientType> ingredientTypes) {
		ImmutableMap.Builder<Class, IIngredientType> mapBuilder = ImmutableMap.builder();
		for (IIngredientType ingredientType : ingredientTypes) {
			mapBuilder.put(ingredientType.getClass(), ingredientType);
		}
		return mapBuilder.build();
	}

	private static ImmutableList<IRecipeHandler> buildRecipeHandlersList(@Nonnull List<IRecipeHandler> recipeHandlers) {
		ImmutableList.Builder<IRecipeHandler> listBuilder = ImmutableList.builder();
		Set<Class> recipeHandlerClasses = new HashSet<>();
		for (IRecipeHandler recipeHandler : recipeHandlers) {
			if (recipeHandler == null) {
				continue;
			}

			Class recipeClass = recipeHandler.getRecipeClass();

			if (recipeHandlerClasses.contains(recipeClass)) {
				throw new IllegalArgumentException("A Recipe Handler has already been registered for this recipe class: " + recipeClass.getName());
			}

			recipeHandlerClasses.add(recipeClass);
			listBuilder.add(recipeHandler);
		}
		return listBuilder.build();
	}

	private static ImmutableTable<Class, String, IRecipeTransferHandler> buildRecipeTransferHandlerTable(@Nonnull List<IRecipeTransferHandler> recipeTransferHandlers) {
		ImmutableTable.Builder<Class, String, IRecipeTransferHandler> builder = ImmutableTable.builder();
		for (IRecipeTransferHandler recipeTransferHelper : recipeTransferHandlers) {
			builder.put(recipeTransferHelper.getContainerClass(), recipeTransferHelper.getRecipeCategoryUid(), recipeTransferHelper);
		}
		return builder.build();
	}

	private void addRecipes(@Nullable List<Object> recipes) {
		if (recipes == null) {
			return;
		}

		for (Object recipe : recipes) {
			addRecipe(recipe);
		}
	}

	@Override
	public void addRecipe(@Nullable Object recipe) {
		if (recipe == null) {
			Log.error("Null recipe", new NullPointerException());
			return;
		}

		addRecipe(recipe, recipe.getClass());
	}

	private <T> void addRecipe(@Nonnull T recipe, Class<? extends T> recipeClass) {
		IRecipeHandler<T> recipeHandler = getRecipeHandler(recipeClass);
		if (recipeHandler == null) {
			if (!unhandledRecipeClasses.contains(recipeClass)) {
				unhandledRecipeClasses.add(recipeClass);
				if (Config.isDebugModeEnabled()) {
					Log.debug("Can't handle recipe: {}", recipeClass);
				}
			}
			return;
		}

		String recipeCategoryUid;
		try {
			recipeCategoryUid = recipeHandler.getRecipeCategoryUid(recipe);
		} catch (AbstractMethodError ignored) { // legacy handlers don't have that method
			recipeCategoryUid = recipeHandler.getRecipeCategoryUid();
		}

		IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
		if (recipeCategory == null) {
			Log.error("No recipe category registered for recipeCategoryUid: {}", recipeCategoryUid);
			return;
		}

		if (!recipeHandler.isRecipeValid(recipe)) {
			return;
		}

		try {
			addRecipeUnchecked(recipe, recipeCategory, recipeHandler);
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getInfoFromBrokenRecipe(recipe, recipeHandler);

			// suppress the null item in stack exception, that information is redundant here.
			if (e.getMessage().equals(StackHelper.nullItemInStack)) {
				Log.error("Found a broken recipe: {}\n", recipeInfo);
			} else {
				Log.error("Found a broken recipe: {}\n", recipeInfo, e);
			}
		}
	}

	private <T> void addRecipeUnchecked(@Nonnull T recipe, IRecipeCategory recipeCategory, IRecipeHandler<T> recipeHandler) {
		IRecipeWrapper recipeWrapper = recipeHandler.getRecipeWrapper(recipe);

		for (IIngredientType ingredientType : ingredientTypes.values()) {
			addRecipeUnchecked(recipe, recipeWrapper, recipeCategory, ingredientType);
		}

		recipesForCategories.put(recipeCategory, recipe);
	}

	private <T, V> void addRecipeUnchecked(@Nonnull T recipe, IRecipeWrapper recipeWrapper, IRecipeCategory recipeCategory, IIngredientType<V> ingredientType) {
		List<List<V>> inputs = recipeWrapper.getInputs(ingredientType);
		if (inputs != null) {
			Iterable<V> inputsFlat = Iterables.concat(inputs);
			recipeInputMap.addRecipe(recipe, recipeCategory, inputsFlat, ingredientType);
		}

		List<List<V>> outputs = recipeWrapper.getOutputs(ingredientType);
		if (outputs != null) {
			Iterable<V> outputsFlat = Iterables.concat(outputs);
			recipeOutputMap.addRecipe(recipe, recipeCategory, outputsFlat, ingredientType);
		}
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories() {
		ImmutableList.Builder<IRecipeCategory> builder = ImmutableList.builder();
		for (IRecipeCategory recipeCategory : recipeCategoriesMap.values()) {
			if (!getRecipes(recipeCategory).isEmpty()) {
				builder.add(recipeCategory);
			}
		}
		return builder.build();
	}

	@Nonnull
	@Override
	public <T> IIngredientType<T> getTypeForIngredient(@Nullable Class<? extends T> ingredientClass) {
		if (ingredientClass == null) {
			throw new NullPointerException("Null ingredientClass");
		}
		//noinspection unchecked
		IIngredientType<T> ingredientType = ingredientTypes.get(ingredientClass);
		if (ingredientType == null) {
			throw new NullPointerException("No ingredient type registered for " + ingredientClass);
		}
		return ingredientType;
	}

	@Nonnull
	@Override
	public <T> IIngredientType<T> getTypeForIngredient(@Nullable T ingredient) {
		if (ingredient == null) {
			throw new NullPointerException("Null ingredient");
		}

		//noinspection unchecked
		Class<? extends T> ingredientClass = (Class<? extends T>) ingredient.getClass();
		return getTypeForIngredient(ingredientClass);
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories(@Nullable List<String> recipeCategoryUids) {
		if (recipeCategoryUids == null) {
			Log.error("Null recipeCategoryUids", new NullPointerException());
			return ImmutableList.of();
		}

		ImmutableList.Builder<IRecipeCategory> builder = ImmutableList.builder();
		for (String recipeCategoryUid : recipeCategoryUids) {
			IRecipeCategory recipeCategory = recipeCategoriesMap.get(recipeCategoryUid);
			if (recipeCategory != null && !getRecipes(recipeCategory).isEmpty()) {
				builder.add(recipeCategory);
			}
		}
		return builder.build();
	}

	@Nullable
	@Override
	public <T> IRecipeHandler<T> getRecipeHandler(@Nullable Class<? extends T> recipeClass) {
		if (recipeClass == null) {
			Log.error("Null recipeClass", new NullPointerException());
			return null;
		}

		for (IRecipeHandler<?> recipeHandler : recipeHandlers) {
			if (recipeHandler.getRecipeClass().isAssignableFrom(recipeClass)) {
				// noinspection unchecked
				return (IRecipeHandler<T>) recipeHandler;
			}
		}

		return null;
	}

	@Nullable
	public RecipeClickableArea getRecipeClickableArea(@Nonnull GuiContainer gui, int mouseX, int mouseY) {
		ImmutableCollection<RecipeClickableArea> recipeClickableAreas = recipeClickableAreasMap.get(gui.getClass());
		for (RecipeClickableArea recipeClickableArea : recipeClickableAreas) {
			if (recipeClickableArea.checkHover(mouseX, mouseY)) {
				return recipeClickableArea;
			}
		}
		return null;
	}

	@Nonnull
	@Override
	public <T> ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(@Nullable T input) {
		if (input == null) {
			Log.error("Null input", new NullPointerException());
			return ImmutableList.of();
		}
		IIngredientType<T> ingredientType = getTypeForIngredient(input);
		return recipeInputMap.getRecipeCategories(input, ingredientType);
	}

	@Nonnull
	@Override
	public <T> ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable T output) {
		if (output == null) {
			Log.error("Null output", new NullPointerException());
			return ImmutableList.of();
		}
		IIngredientType<T> ingredientType = getTypeForIngredient(output);
		return recipeOutputMap.getRecipeCategories(output, ingredientType);
	}

	@Nonnull
	@Override
	public <T> List<Object> getRecipesWithInput(@Nullable IRecipeCategory recipeCategory, @Nullable T input) {
		if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return ImmutableList.of();
		} else if (input == null) {
			Log.error("Null input", new NullPointerException());
			return ImmutableList.of();
		}

		IIngredientType<T> ingredientType = getTypeForIngredient(input);
		ImmutableList<Object> recipes = recipeInputMap.getRecipes(recipeCategory, input, ingredientType);

		String recipeCategoryUid = recipeCategory.getUid();

		Set<String> inputKeys = new HashSet<>();
		inputKeys.add(ingredientType.getKey(input));
		inputKeys.add(ingredientType.getWildcardKey(input));

		for (String inputKey : inputKeys) {
			if (categoriesForCraftItemKeys.get(inputKey).contains(recipeCategoryUid)) {
				ImmutableSet<Object> specificRecipes = ImmutableSet.copyOf(recipes);
				List<Object> recipesForCategory = recipesForCategories.get(recipeCategory);
				List<Object> allRecipes = new ArrayList<>(recipes);
				for (Object recipe : recipesForCategory) {
					if (!specificRecipes.contains(recipe)) {
						allRecipes.add(recipe);
					}
				}
				return allRecipes;
			}
		}

		return recipes;
	}

	@Nonnull
	@Override
	public <T> ImmutableList<Object> getRecipesWithOutput(@Nullable IRecipeCategory recipeCategory, @Nullable T output) {
		if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return ImmutableList.of();
		} else if (output == null) {
			Log.error("Null output", new NullPointerException());
			return ImmutableList.of();
		}
		IIngredientType<T> ingredientType = getTypeForIngredient(output);
		return recipeOutputMap.getRecipes(recipeCategory, output, ingredientType);
	}

	@Nonnull
	@Override
	public List<Object> getRecipes(@Nullable IRecipeCategory recipeCategory) {
		if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return ImmutableList.of();
		}
		return Collections.unmodifiableList(recipesForCategories.get(recipeCategory));
	}

	@Nonnull
	@Override
	public ImmutableCollection<ItemStack> getCraftingItems(@Nonnull IRecipeCategory recipeCategory) {
		return craftItemsForCategories.get(recipeCategory);
	}

	@Nullable
	public IRecipeTransferHandler getRecipeTransferHandler(@Nullable Container container, @Nullable IRecipeCategory recipeCategory) {
		if (container == null) {
			Log.error("Null container", new NullPointerException());
			return null;
		} else if (recipeCategory == null) {
			Log.error("Null recipeCategory", new NullPointerException());
			return null;
		}

		return recipeTransferHandlers.get(container.getClass(), recipeCategory.getUid());
	}
}
