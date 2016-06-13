package mezz.jei.api.recipe;

import java.util.List;

public interface IRecipeIngredients<T> {
	void setSlotsLists(List<List<T>> ingredients);
	void setSlots(List<T> ingredients);
	void setSlot(int slotIndex, List<T> ingredients);
	void setSlot(int slotIndex, T ingredient);
	List<List<T>> get();
}
