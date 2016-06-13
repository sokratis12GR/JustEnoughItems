package mezz.jei.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.api.recipe.IAllRecipeIngredients;
import mezz.jei.api.recipe.IRecipeIngredients;

public class AllRecipeIngredients implements IAllRecipeIngredients {
	Map<Class<?>, IRecipeIngredients<?>> ingredientMap = new HashMap<>();

	@Nonnull
	@Override
	public <T> IRecipeIngredients<T> get(Class<T> ingredientClass) {
		//noinspection unchecked
		IRecipeIngredients<T> recipeIngredientClass = (IRecipeIngredients<T>) ingredientMap.get(ingredientClass);
		if (recipeIngredientClass == null) {
			recipeIngredientClass = new RecipeIngredients<>();
			ingredientMap.put(ingredientClass, recipeIngredientClass);
		}
		return recipeIngredientClass;
	}

	private static class RecipeIngredients<T> implements IRecipeIngredients<T> {
		private List<List<T>> ingredients = new ArrayList<>();

		@Override
		public void setSlotsLists(List<List<T>> ingredients) {
			this.ingredients = ingredients;
		}

		@Override
		public void setSlots(List<T> ingredients) {
			this.ingredients.clear();
			for (T ingredient : ingredients) {
				List<T> slotContents = new ArrayList<>(1);
				slotContents.add(ingredient);
				this.ingredients.add(slotContents);
			}
		}

		@Override
		public void setSlot(int slotIndex, List<T> ingredients) {
			List<T> slotIngredients = getSlotIngredients(slotIndex);
			slotIngredients.addAll(ingredients);
		}

		@Override
		public void setSlot(int slotIndex, T ingredient) {
			List<T> slotIngredients = getSlotIngredients(slotIndex);
			slotIngredients.add(ingredient);
		}

		@Override
		public List<List<T>> get() {
			return this.ingredients;
		}

		private List<T> getSlotIngredients(int slotIndex) {
			while (this.ingredients.size() <= slotIndex) {
				this.ingredients.add(new ArrayList<T>());
			}
			return this.ingredients.get(slotIndex);
		}
	}
}
