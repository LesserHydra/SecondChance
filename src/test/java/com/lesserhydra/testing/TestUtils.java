package com.lesserhydra.testing;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mockito.invocation.InvocationOnMock;

public class TestUtils {
	
	public static Inventory createMockInventory(InvocationOnMock invoke) {
		Capsule<ItemStack[]> contents = new Capsule<>();
		Inventory inv = mock(Inventory.class);
		doAnswer(i -> contents.set(i.getArgumentAt(0, ItemStack[].class))).when(inv).setContents(any(ItemStack[].class));
		when(inv.getContents()).then(i -> contents.get());
		when(inv.getStorageContents()).then(i -> contents.get());
		return inv;
	}
	
}
