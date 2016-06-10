package com.lesserhydra.secondchance;

import static com.lesserhydra.testing.FakeWorld.mockBukkitWorld;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import java.io.File;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.lesserhydra.secondchance.command.MainCommand;
import com.lesserhydra.secondchance.compat.Compat;
import com.lesserhydra.secondchance.configuration.ConfigOptions;
import com.lesserhydra.testing.TestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, YamlConfiguration.class})
public class PermissionsTest {
	
	private Server mockServer;
	private SecondChance plugin;
	private DeathpointHandler handler;
	
	private MapSaveHandler saveHandler;
	private ConfigOptions options;
	
	private File dataFolder;
	private File saveFolder;
	private File configFile;
	
	private World world;
	private Player player1;
	private Player player2;
	
	private Deathpoint deathpoint1;
	private ArmorStand hitbox1;
	
	private Deathpoint deathpoint2;
	private ArmorStand hitbox2;
	
	@Before
	public void init() {
		PowerMockito.mockStatic(Bukkit.class, YamlConfiguration.class);
		
		BukkitScheduler mockScheduler = mock(BukkitScheduler.class);
		when(mockScheduler.runTaskTimer(any(Plugin.class), any(Runnable.class), anyLong(), anyLong())).thenReturn(mock(BukkitTask.class));
		BDDMockito.given(Bukkit.getScheduler()).willReturn(mockScheduler);
		
		this.mockServer = mock(Server.class);
		when(mockServer.getPluginManager()).thenReturn(new SimplePluginManager(mockServer, new SimpleCommandMap(mockServer)));
		when(mockServer.createInventory(any(InventoryHolder.class), anyInt(), anyString())).then(TestUtils::createMockInventory);
		BDDMockito.given(Bukkit.getServer()).willReturn(mockServer);
		
		BDDMockito.given(YamlConfiguration.loadConfiguration(any(File.class))).willReturn(new YamlConfiguration());
		BDDMockito.given(YamlConfiguration.loadConfiguration(any(Reader.class))).willReturn(new YamlConfiguration());
		
		this.dataFolder = mock(File.class);
		when(dataFolder.exists()).thenReturn(true);
		this.saveFolder = mock(File.class);
		when(saveFolder.exists()).thenReturn(true);
		this.configFile = mock(File.class);
		when(configFile.exists()).thenReturn(true);
		
		this.plugin = spy(Whitebox.newInstance(SecondChance.class));
		Whitebox.setInternalState(SecondChance.class, SecondChance.class, plugin);
		Whitebox.setInternalState(plugin, Compat.class, new TestCompat());
		Whitebox.setInternalState(plugin, "saveFolder", saveFolder);
		Whitebox.setInternalState(plugin, "dataFolder", dataFolder, JavaPlugin.class);
		doReturn(new YamlConfiguration()).when(plugin).getConfig();
		doNothing().when(plugin).reloadConfig();
		doNothing().when(plugin).saveDefaultConfig();
		
		this.options = new ConfigOptions(new YamlConfiguration());
		
		this.handler = new DeathpointHandler(plugin);
		Whitebox.setInternalState(plugin, DeathpointHandler.class, handler);
		
		//Mock world and players
		this.world = mockBukkitWorld("world");
		when(world.getGameRuleValue(eq("keepInventory"))).thenReturn("false");
		BDDMockito.given(Bukkit.getWorld(eq("world"))).willReturn(world);
		BDDMockito.given(Bukkit.getWorlds()).willReturn(Arrays.asList(world));
		this.player1 = mock(Player.class);
		when(player1.getName()).thenReturn("Joe");
		when(player1.getUniqueId()).thenReturn(UUID.randomUUID());
		when(player1.getType()).thenReturn(EntityType.PLAYER);
		when(player1.getWorld()).thenReturn(world);
		this.player2 = mock(Player.class);
		when(player2.getName()).thenReturn("Bob");
		when(player2.getUniqueId()).thenReturn(UUID.randomUUID());
		when(player2.getType()).thenReturn(EntityType.PLAYER);
		when(player2.getWorld()).thenReturn(world);
		
		this.deathpoint1 = new Deathpoint(player1, new Location(world, 0, 0, 0), new ItemStack[41], 0, 1, -1L);
		this.hitbox1 = mock(ArmorStand.class);
		when(hitbox1.getType()).thenReturn(EntityType.ARMOR_STAND);
		when(hitbox1.getMetadata(eq("deathpoint"))).thenReturn(Arrays.asList(new FixedMetadataValue(plugin, deathpoint1)));
		
		this.deathpoint2 = new Deathpoint(player2, new Location(world, 0, 0, 0), new ItemStack[41], 0, 1, -1L);
		this.hitbox2 = mock(ArmorStand.class);
		when(hitbox2.getType()).thenReturn(EntityType.ARMOR_STAND);
		when(hitbox2.getMetadata(eq("deathpoint"))).thenReturn(Arrays.asList(new FixedMetadataValue(plugin, deathpoint2)));
		
		this.saveHandler = new MapSaveHandler();
		saveHandler.save(world, Arrays.asList(deathpoint1, deathpoint2));
		Whitebox.setInternalState(plugin, SaveHandler.class, saveHandler);
		
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
		when(player1.getMetadata("lastSafePosition")).thenReturn(Arrays.asList(new FixedMetadataValue(plugin, new Location(world, 0, 60, 0))));
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
		assertSame(deathpoint1, player1Deathpoints.get(0));;
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
		when(player1.getMetadata("lastSafePosition")).thenReturn(Arrays.asList(new FixedMetadataValue(plugin, new Location(world, 0, 60, 0))));
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
		assertSame(Whitebox.getInternalState(handler, ConfigOptions.class), options);
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
		assertNotSame(Whitebox.getInternalState(handler, ConfigOptions.class), options);
	}
	
}
