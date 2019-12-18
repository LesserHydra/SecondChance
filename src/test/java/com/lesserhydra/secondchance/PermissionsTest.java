package com.lesserhydra.secondchance;

import com.lesserhydra.secondchance.command.MainCommand;
import com.lesserhydra.secondchance.configuration.ConfigOptions;
import com.lesserhydra.testing.FakeBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(YamlConfiguration.class)
@PowerMockIgnore("javax.management.*")
public class PermissionsTest {
	
	private SecondChance plugin;
	private DeathpointHandler handler;
	
	private MapSaveHandler saveHandler;
	private ConfigOptions options;
	
	private World world;
	private Player player1;
	
	private Deathpoint deathpoint1;
	
	private Deathpoint deathpoint2;
	private ArmorStand hitbox2;
	
	@BeforeClass public static void beforeAll() {
		FakeBukkit.setup();
		
		PowerMockito.mockStatic(YamlConfiguration.class);
		BDDMockito.given(YamlConfiguration.loadConfiguration(any(File.class))).willReturn(new YamlConfiguration());
		BDDMockito.given(YamlConfiguration.loadConfiguration(any(Reader.class))).willReturn(new YamlConfiguration());
	}
	
	@Before
	public void init() {
		FakeBukkit.clear();
		
		//Mock plugin setup
		plugin = spy(Whitebox.newInstance(SecondChance.class));
		Whitebox.setInternalState(SecondChance.class, plugin);
		Whitebox.setInternalState(plugin, "isEnabled", true);
		Whitebox.setInternalState(plugin, "description", new PluginDescriptionFile("SecondChance", "TEST", "com.lesserhydra.secondchance.SecondChance"));
		doReturn(new YamlConfiguration()).when(plugin).getConfig();
		doNothing().when(plugin).reload();
		doNothing().when(plugin).reloadConfig();
		doNothing().when(plugin).saveDefaultConfig();
		
		handler = new DeathpointHandler(plugin);
		Whitebox.setInternalState(plugin, DeathpointHandler.class, handler);
		
		options = new ConfigOptions(new YamlConfiguration());
		
		saveHandler = new MapSaveHandler();
		Whitebox.setInternalState(plugin, SaveHandler.class, saveHandler);
		
		//Mock world and players
		world = FakeBukkit.makeWorld("world");
		when(world.getGameRuleValue(eq("keepInventory"))).thenReturn("false");
		
		player1 = FakeBukkit.makePlayer("Joe", new Location(world, 0, 0, 0));
		Player player2 = FakeBukkit.makePlayer("Bob", new Location(world, 0, 0, 0));
		
		deathpoint1 = new Deathpoint(player1, new Location(world, 0, 0, 0), new ItemStack[41], 0, 1, -1L);
		ArmorStand hitbox1 = mock(ArmorStand.class);
		when(hitbox1.getType()).thenReturn(EntityType.ARMOR_STAND);
		when(hitbox1.getMetadata(eq("deathpoint"))).thenReturn(Collections.singletonList(new FixedMetadataValue(plugin, deathpoint1)));
		
		deathpoint2 = new Deathpoint(player2, new Location(world, 0, 0, 0), new ItemStack[41], 0, 1, -1L);
		hitbox2 = mock(ArmorStand.class);
		when(hitbox2.getType()).thenReturn(EntityType.ARMOR_STAND);
		when(hitbox2.getMetadata(eq("deathpoint"))).thenReturn(Collections.singletonList(new FixedMetadataValue(plugin, deathpoint2)));
		
		saveHandler.save(world, Arrays.asList(deathpoint1, deathpoint2));
		handler.init(options);
	}
	
	/*
	 * Players that do not have the "enabled" permission should not spawn new deathpoints or forget old ones on death.
	 */
	@Test
	public void noEnabled() {
		/*----------Given----------*/
		Whitebox.setInternalState(options, "holdItems", false);
		Whitebox.setInternalState(options, "holdExp", true);
		when(player1.hasPermission(eq(SecondChance.enabledPermission))).thenReturn(false);
		when(player1.getMetadata("lastSafePosition")).thenReturn(Collections.singletonList(new FixedMetadataValue(plugin, new Location(world, 0, 60, 0))));
		when(player1.getLevel()).thenReturn(10);
		
		/*----------When----------*/
		PlayerDeathEvent event = new PlayerDeathEvent(player1, Collections.emptyList(), 100, "Joe died of rage after finding a stick.");
		handler.onPlayerDeath(event);
		
		handler.deinit();
		
		/*----------Then----------*/
		List<Deathpoint> player1Deathpoints = saveHandler.load(world).stream()
				.filter(point -> point.getOwnerUniqueId().equals(player1.getUniqueId()))
				.collect(Collectors.toList());
		assertEquals(1, player1Deathpoints.size());
		assertSame(deathpoint1, player1Deathpoints.get(0));
	}
	
	/*
	 * Players that have the "enabled" permission should spawn a new deathpoint and forget old ones on death.
	 */
	@Test
	public void enabled() {
		/*----------Given----------*/
		Whitebox.setInternalState(options, "holdItems", false);
		Whitebox.setInternalState(options, "holdExp", true);
		when(player1.hasPermission(eq(SecondChance.enabledPermission))).thenReturn(true);
		when(player1.getMetadata("lastSafePosition")).thenReturn(Collections.singletonList(new FixedMetadataValue(plugin, new Location(world, 0, 60, 0))));
		when(player1.getLevel()).thenReturn(10);
		
		/*----------When----------*/
		PlayerDeathEvent event = new PlayerDeathEvent(player1, Collections.emptyList(), 100, "Joe died of rage after finding a stick.");
		handler.onPlayerDeath(event);
		
		handler.deinit();
		
		/*----------Then----------*/
		List<Deathpoint> player1Deathpoints = saveHandler.load(world).stream()
				.filter(point -> point.getOwnerUniqueId().equals(player1.getUniqueId()))
				.collect(Collectors.toList());
		assertEquals(1, player1Deathpoints.size());
		assertNotSame(deathpoint1, player1Deathpoints.get(0));
	}
	
	/*
	 * Players that do not have the "thief" permission should not be able to punch protected deathpoints.
	 */
	@Test
	@SuppressWarnings("deprecation")
	public void noThiefPunch() {
		/*----------Given----------*/
		Whitebox.setInternalState(options, "breakOnHit", true);
		Whitebox.setInternalState(options, "isProtected", true);
		when(player1.hasPermission(eq(SecondChance.thiefPermission))).thenReturn(false);
		
		/*----------When----------*/
		EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player1, hitbox2, DamageCause.ENTITY_ATTACK, 10);
		handler.onArmorStandPunched(event);
		
		/*----------Then----------*/
		assertFalse(deathpoint2.isInvalid());
	}
	
	/*
	 * Players that have the "thief" permission should be able to punch protected deathpoints.
	 */
	@Test
	@SuppressWarnings("deprecation")
	public void thiefPunch() {
		/*----------Given----------*/
		Whitebox.setInternalState(options, "breakOnHit", true);
		Whitebox.setInternalState(options, "isProtected", true);
		when(player1.hasPermission(eq(SecondChance.thiefPermission))).thenReturn(true);
		
		/*----------When----------*/
		EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player1, hitbox2, DamageCause.ENTITY_ATTACK, 10);
		handler.onArmorStandPunched(event);
		
		/*----------Then----------*/
		assertTrue(deathpoint2.isInvalid());
	}
	
	/*
	 * Players that do not have the "thief" permission should not be able to access protected deathpoints.
	 */
	@Test
	public void noThiefAccess() {
		/*----------Given----------*/
		Whitebox.setInternalState(options, "breakOnHit", true);
		Whitebox.setInternalState(options, "isProtected", true);
		when(player1.hasPermission(eq(SecondChance.thiefPermission))).thenReturn(false);
		
		/*----------When----------*/
		PlayerInteractAtEntityEvent event = new PlayerInteractAtEntityEvent(player1, hitbox2, null);
		handler.onPlayerClickArmorStand(event);
		
		/*----------Then----------*/
		assertFalse(deathpoint2.isInvalid());
	}
	
	/*
	 * Players that have the "thief" permission should be able to access protected deathpoints.
	 */
	@Test
	public void thiefAccess() {
		/*----------Given----------*/
		Whitebox.setInternalState(options, "breakOnHit", true);
		Whitebox.setInternalState(options, "isProtected", true);
		when(player1.hasPermission(eq(SecondChance.thiefPermission))).thenReturn(true);
		
		/*----------When----------*/
		PlayerInteractAtEntityEvent event = new PlayerInteractAtEntityEvent(player1, hitbox2, null);
		handler.onPlayerClickArmorStand(event);
		
		/*----------Then----------*/
		assertTrue(deathpoint2.isInvalid());
	}
	
	/*
	 * Players that do not have the "command" permission should not be able to use admin commands.
	 */
	@Test
	public void noCommand() {
		/*----------Given----------*/
		CommandExecutor mainCmd = new MainCommand();
		CommandSender sender = mock(CommandSender.class);
		when(sender.hasPermission(eq(SecondChance.commandPermission))).thenReturn(false);
		
		Command command = mock(Command.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
		command.setName("SecondChance");
		String[] args = new String[]{"reload"};
		
		/*----------When----------*/
		mainCmd.onCommand(sender, command, "secondchance", args);
		
		/*----------Then----------*/
		verify(plugin, never()).reload();
	}
	
	/*
	 * Players that have the "command" permission should be able to use admin commands.
	 */
	@Test
	public void command() {
		/*----------Given----------*/
		CommandExecutor mainCmd = new MainCommand();
		CommandSender sender = mock(CommandSender.class);
		when(sender.hasPermission(eq(SecondChance.commandPermission))).thenReturn(true);
		
		Command command = mock(Command.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
		command.setName("SecondChance");
		String[] args = new String[]{"reload"};
		
		/*----------When----------*/
		mainCmd.onCommand(sender, command, "secondchance", args);
		
		/*----------Then----------*/
		verify(plugin).reload();
	}
	
}
