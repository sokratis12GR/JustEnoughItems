package mezz.jei.util;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import mezz.jei.api.recipe.IIngredientType;

public class IngredientTypeMap {
	@Nonnull
	private final Map<Class, IIngredientType> ingredientKeyMakerMap = new HashMap<>();

	public  void registerIngredientType(@Nonnull IIngredientType<?> ingredientType) {
		ingredientKeyMakerMap.put(ingredientType.getIngredientClass(), ingredientType);
	}

	public <T> IIngredientType<T> getIngredientType(T object) {
		Class objectClass = object.getClass();
		//noinspection unchecked
		IIngredientType<T> ingredientType = ingredientKeyMakerMap.get(objectClass);
		if (ingredientType == null) {
			throw new IllegalArgumentException("There is no IIngredientType registered for this ingredient. " + object);
		}
		return ingredientType;
	}
}
