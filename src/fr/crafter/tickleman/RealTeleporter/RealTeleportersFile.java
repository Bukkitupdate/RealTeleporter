package fr.crafter.tickleman.RealTeleporter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

//############################################################################# RealTeleportersFile
public class RealTeleportersFile
{

	private final RealTeleporterPlugin plugin;
	private final String fileName = "teleporters";

	/** Teleporters list : "world;x;y;z" => RealTeleporter */
	public HashMap<String, RealTeleporter> byLocation = new HashMap<String, RealTeleporter>();

	/** Teleporters list : "name" => RealTeleporter */
	public HashMap<String, RealTeleporter> byName = new HashMap<String, RealTeleporter>();

	//--------------------------------------------------------------------------- RealTeleportersFile
	public RealTeleportersFile(final RealTeleporterPlugin plugin)
	{
		this.plugin = plugin;
	}

	//------------------------------------------------------------------------------------------ load
	public RealTeleportersFile load()
	{
		byLocation = new HashMap<String, RealTeleporter>();
		byName = new HashMap<String, RealTeleporter>();
		try {
			plugin.log.info("load plugins/" + plugin.name + "/" + fileName + ".txt");
			BufferedReader reader = new BufferedReader(
				new FileReader("plugins/" + plugin.name + "/" + fileName + ".txt")
			);
			String buffer;
			while ((buffer = reader.readLine()) != null) {
				String[] line = buffer.split(",");
				if ((line.length > 6) && (buffer.charAt(0) != '#')) {
					try {
						String name = line[0].trim();
						String worldName = line[1].trim();
						long x = Long.parseLong(line[2].trim());
						long y = Long.parseLong(line[3].trim());
						long z = Long.parseLong(line[4].trim());
						String targetName = line[5].trim();
						char direction = (line[6].trim().length() > 0) ? line[6].trim().charAt(0) : 'N';
						String key = worldName + ";" + x + ";" + y + ";" + z;
						RealTeleporter teleporter = new RealTeleporter(
							name, worldName, x, y, z, targetName, direction
						);
						byLocation.put(key, teleporter);
						byName.put(name, teleporter);
					} catch (Exception e) {
						// when some values are not numbers, then ignore teleporter
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			plugin.log.warning(
				"Needs plugins/" + plugin.name + "/" + fileName + ".txt file (will auto-create)"
			);
		}
		solve();
		return this;
	}

	//------------------------------------------------------------------------------------------ save
	public void save()
	{
		try {
			BufferedWriter writer = new BufferedWriter(
				new FileWriter("plugins/" + plugin.name + "/" + fileName + ".txt")
			);
			writer.write("#name,world,x,y,z,target,direction\n");
			for (RealTeleporter teleporter : byName.values()) {
				writer.write(
					teleporter.name + ","
					+ teleporter.worldName + ","
					+ teleporter.x + ","
					+ teleporter.y + ","
					+ teleporter.z + ","
					+ teleporter.targetName + ","
					+ teleporter.direction
					+ "\n"
				);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			plugin.log.severe("Could not save plugins/" + plugin.name + "/" + fileName + ".txt file");
		}
	}

	//----------------------------------------------------------------------------------------- solve
	public void solve()
	{
		for (RealTeleporter teleporter : byName.values())
		{
			if (teleporter.target == null) {
				teleporter.target = byName.get(teleporter.targetName);
			}
		}
	}

	//---------------------------------------------------------------------------------- teleporterAt
	public RealTeleporter teleporterAt(Player player)
	{
		Location location = player.getLocation();
		return teleporterAt(
			player.getWorld().getName(),
			Math.round(Math.floor(location.getX())),
			Math.round(Math.floor(location.getY())),
			Math.round(Math.floor(location.getZ()))
		);
	}

	//---------------------------------------------------------------------------------- teleporterAt
	public RealTeleporter teleporterAt(String worldName, long x, long y, long z)
	{
		String key = worldName + ";" + x + ";" + y + ";" + z;
		return byLocation.get(key);
	}

}
