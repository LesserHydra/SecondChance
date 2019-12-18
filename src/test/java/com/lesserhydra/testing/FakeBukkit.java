package com.lesserhydra.testing;

import net.minecraft.server.v1_14_R1.DispenserRegistry;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.PlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_14_R1.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.SimplePluginManager;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class FakeBukkit {
	
	private static boolean done = false;
	private static List<World> worlds = new LinkedList<>();
	private static List<Player> players = new LinkedList<>();
	
	public static void setup() {
		if (done) return;
		done = true;
		
		//Initialize Minecraft stuffs
		DispenserRegistry.init();
		
		//Setup fake server
		Server server = PowerMockito.mock(Server.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS));
		Whitebox.setInternalState(Bukkit.class, server);
		when(server.getPluginManager()).thenReturn(new SimplePluginManager(server, new SimpleCommandMap(server)));
		//noinspection deprecation
		when(server.getUnsafe()).thenReturn(CraftMagicNumbers.INSTANCE);
		when(server.getItemFactory()).thenReturn(CraftItemFactory.instance());
		when(server.getScheduler()).thenReturn(new CraftScheduler());
		when(server.createInventory(any(InventoryHolder.class), anyInt(), anyString())).then(FakeBukkit::createMockInventory);
		
		//Worlds
		when(server.getWorlds()).thenReturn(worlds);
		when(server.getWorld(anyString())).then(i -> {
			String name = i.getArgumentAt(0, String.class);
			return worlds.stream()
					.filter(w -> name.equals(w.getName()))
					.findAny().orElse(null);
		});
		
		//Players
		when(server.getPlayer(any(UUID.class))).then(i -> {
			UUID id = i.getArgumentAt(0, UUID.class);
			return players.stream()
					.filter(p -> id.equals(p.getUniqueId()))
					.findAny().orElse(null);
		});
	}
	
	public static void clear() {
		worlds.clear();
		players.clear();
	}
	
	public static Player makePlayer(String name) {
		return makePlayer(name, new Location(null, 0, 0, 0));
	}
	
	public static Player makePlayer(String name, Location location) {
		CraftPlayer result = mock(CraftPlayer.class);
		when(result.getHandle()).thenReturn(mock(EntityPlayer.class));
		
		when(result.getName()).thenReturn(name);
		when(result.getUniqueId()).thenReturn(UUID.randomUUID());
		when(result.getType()).thenReturn(EntityType.PLAYER);
		when(result.getLocation()).thenReturn(location);
		when(result.getWorld()).thenReturn(location.getWorld());
		
		Capsule<Inventory> topInventory = new Capsule<>();
		when(result.getOpenInventory()).thenAnswer(i -> new CraftInventoryView(result, topInventory.get(), null));
		doAnswer(i -> topInventory.set((Inventory) i.getArguments()[0])).when(result).openInventory(any(Inventory.class));
		doAnswer(i -> topInventory.set(null)).when(result).closeInventory();
		
		EntityHuman human = mock(EntityHuman.class);
		when(human.getBukkitEntity()).thenReturn(result);
		when(result.getInventory()).thenReturn(new CraftInventoryPlayer(new PlayerInventory(human)));
		
		players.add(result);
		return result;
	}
	
	public static World makeWorld(String name) {
		World result = FakeWorld.mockBukkitWorld(name);
		worlds.add(result);
		return result;
	}
	
	private static Inventory createMockInventory(InvocationOnMock invoke) {
		Capsule<ItemStack[]> contents = new Capsule<>();
		Inventory inv = mock(Inventory.class);
		doAnswer(i -> contents.set(i.getArgumentAt(0, ItemStack[].class))).when(inv).setContents(any(ItemStack[].class));
		when(inv.getContents()).then(i -> contents.get());
		return inv;
	}
	
}
