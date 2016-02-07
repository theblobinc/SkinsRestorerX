package skinsrestorer.bukkit;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_8_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R2.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R2.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R2.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R2.PacketPlayOutPosition;
import net.minecraft.server.v1_8_R2.PacketPlayOutPosition.EnumPlayerTeleportFlags;
import net.minecraft.server.v1_8_R2.PacketPlayOutRespawn;
import net.minecraft.server.v1_8_R2.WorldSettings.EnumGamemode;
import net.minecraft.server.v1_8_R2.EntityPlayer;
import skinsrestorer.shared.format.SkinProfile;
import skinsrestorer.shared.format.SkinProperty;
import skinsrestorer.shared.storage.SkinStorage;
import net.minecraft.server.v1_8_R2.PacketPlayOutHeldItemSlot;

public class SkinFactoryv1_8_R2 extends Factory {

	public static SkinFactoryv1_8_R2 skinfactory;
	public SkinFactoryv1_8_R2(){
		skinfactory = this;
	}
	public static SkinFactoryv1_8_R2 getFactory(){
		return skinfactory;
	}
	
	//Apply the skin to the player.
	@Override
	public void applySkin(final Player player){
		 SkinProfile skinprofile = SkinStorage.getInstance().getOrCreateSkinData(player.getName().toLowerCase());
		skinprofile.applySkin(new SkinProfile.ApplyFunction() {
			@Override
			public void applySkin(SkinProperty property) {
				EntityPlayer ep = ((CraftPlayer) player).getHandle();
					com.mojang.authlib.GameProfile eplayer = ep.getProfile();
					com.mojang.authlib.properties.Property prop = new com.mojang.authlib.properties.Property(property.getName(), property.getValue(), property.getSignature());
					 
					//Clear the current textures (skin & cape).
					eplayer.getProperties().get("textures").clear();
					//Putting the new one. 
					eplayer.getProperties().put(prop.getName(), prop);
					//Updating skin.
				    updateSkin(player, eplayer, false);

			}
		});
	}
	//Remove skin from player
	@Override
	public void removeSkin(final Player player){
				GameProfile profile = ((CraftPlayer) player).getProfile();
				updateSkin(player, profile, true); //Removing the skin.
	}
		
    //Update the skin without relog. (Using NMS and OBC)
	@Override
	@SuppressWarnings("deprecation")
	public void updateSkin(Player player, final GameProfile profile, boolean removeSkin) {
        try {
        Location l = player.getLocation();
		PacketPlayOutPlayerInfo removeInfo = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) player).getHandle());
        PacketPlayOutEntityDestroy removeEntity = new PacketPlayOutEntityDestroy(new int[] { player.getEntityId() });
        PacketPlayOutNamedEntitySpawn addNamed = new PacketPlayOutNamedEntitySpawn(((CraftPlayer) player).getHandle());
        PacketPlayOutPlayerInfo addInfo = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) player).getHandle());
        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(((CraftPlayer) player).getHandle().getWorld().worldProvider.getDimension(), ((CraftPlayer) player).getHandle().getWorld().getDifficulty(), ((CraftPlayer) player).getHandle().getWorld().G(), EnumGamemode.getById(player.getGameMode().getValue()));
        PacketPlayOutPosition pos = new PacketPlayOutPosition(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch(), new HashSet<EnumPlayerTeleportFlags>());
        PacketPlayOutHeldItemSlot slot = new PacketPlayOutHeldItemSlot(player.getInventory().getHeldItemSlot());
        for(Player online : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftOnline = (CraftPlayer) online;
            if (online.equals(player)){
                craftOnline.getHandle().playerConnection.sendPacket(removeInfo);
                if (removeSkin==false){
    	        craftOnline.getHandle().playerConnection.sendPacket(addInfo);
                }
    	        craftOnline.getHandle().playerConnection.sendPacket(respawn);
    	        craftOnline.getHandle().playerConnection.sendPacket(pos);
    	        craftOnline.getHandle().playerConnection.sendPacket(slot);
    	        craftOnline.updateInventory();
            Chunk chunk = player.getWorld().getChunkAt(player.getLocation());
            for(int x=-10; x<10; x++) for(int z=-10; z<10; z++)
                player.getWorld().refreshChunk(chunk.getX()+x, chunk.getZ()+z);
            	continue;
            }
            craftOnline.getHandle().playerConnection.sendPacket(removeEntity);
            craftOnline.getHandle().playerConnection.sendPacket(removeInfo);
            if (removeSkin==false){
	        craftOnline.getHandle().playerConnection.sendPacket(addInfo);
            }
            craftOnline.getHandle().playerConnection.sendPacket(addNamed);
        }
    } catch (Exception e){
    	//Player logging in isnt finished and the method will not be used.
    	//Player skin is already applied.
    }
    }
	
	//Just adding that, so the class will not be abstract. It will never be used.
	@Override
	public void updateSkin(Player player, net.minecraft.util.com.mojang.authlib.GameProfile profile, boolean removeSkin) {
	}
}