package com.snowbud56.Commands;

import com.snowbud56.DataHandler;
import com.snowbud56.Utils.ChatUtils;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.Collator;
import java.util.*;

public class FriendCommand implements CommandExecutor {
    private HashMap<String, List<String>> requests = new HashMap<>();
    private String prefix = "&9&lFriends &8>> &7";
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("friend") || label.equalsIgnoreCase("f")) {
            if (!(sender instanceof Player)) return false;
            Player p = (Player) sender;
            if (args.length == 0) {
                p.sendMessage(ChatUtils.format(prefix + "Help page (1/1):"));
                p.sendMessage(ChatUtils.format(prefix + "/f list: Lists your friends."));
                p.sendMessage(ChatUtils.format(prefix + "/f requests: View your friend requests."));
                p.sendMessage(ChatUtils.format(prefix + "/f <player>: Accepts or sends a friend request to a player."));
                if (p.hasPermission("Friends.Admin")) {
                    p.sendMessage(ChatUtils.format(prefix + "/f forceadd <player>: Forcefully add someone to your friends list."));
                    p.sendMessage(ChatUtils.format(prefix + "/f silentadd <player>: Silently add someone who sent you a friend request."));
                }
            } else if (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("list")) {
                List<String> onlinefriends = new ArrayList<>();
                List<String> offlinefriends = new ArrayList<>();
                for (String friend : DataHandler.getPlayerFriends(p.getUniqueId().toString())) {
                    Player f = Bukkit.getPlayer(UUID.fromString(friend));
                    if (f != null) onlinefriends.add(Bukkit.getOfflinePlayer(UUID.fromString(friend)).getName());
                    else offlinefriends.add(Bukkit.getOfflinePlayer(UUID.fromString(friend)).getName());
                }
                if (onlinefriends.size() != 0) onlinefriends.sort(Collator.getInstance());
                if (offlinefriends.size() != 0) offlinefriends.sort(Collator.getInstance());
                if (onlinefriends.size() == 0 && offlinefriends.size() == 0)
                    p.sendMessage(ChatUtils.format(prefix + "You don't have any friends? Oh. That's unusual..."));
                else {
                    p.sendMessage(ChatUtils.format("&9-----------------------------------------------------"));
                    p.sendMessage(ChatUtils.format("&6Your Friends:"));
                    for (String friend : onlinefriends) p.sendMessage(ChatUtils.format("&7" + friend + " &a- Online"));
                    for (String friend : offlinefriends)
                        p.sendMessage(ChatUtils.format("&7" + friend + " &c- Offline"));
                    p.sendMessage(ChatUtils.format("&9-----------------------------------------------------"));
                }
            } else if (args[0].equalsIgnoreCase("silentadd")) {
                if (p.hasPermission("Friends.Admin")) {
                    if (args.length <= 1) p.sendMessage(ChatUtils.format(prefix + "Please specify a player."));
                    else {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                        List<String> preq = requests.get(p.getName());
                        if (preq == null) preq = new ArrayList<>();
                        if (!preq.contains(target.getName())) p.sendMessage(ChatUtils.format(prefix + "That player didn't send you a friend request!"));
                        else {
                            DataHandler.addPlayerFriend(p.getUniqueId().toString(), target.getUniqueId().toString());
                            DataHandler.addPlayerFriend(target.getUniqueId().toString(), p.getUniqueId().toString());
                            p.sendMessage(ChatUtils.format(prefix + "Silently added &c" + target.getName() + "&7 as a friend."));
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("forceadd")) {
                if (p.hasPermission("Friends.Admin")) {
                    if (args.length == 1) p.sendMessage(ChatUtils.format(prefix + "Please specify a player to add."));
                    else {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                        if (DataHandler.getPlayerFriends(p.getUniqueId().toString()).contains(target.getName())) p.sendMessage(ChatUtils.format(prefix + "You can't add someone who is already on your friends list!"));
                        else {
                            DataHandler.addPlayerFriend(p.getUniqueId().toString(), target.getUniqueId().toString());
                            DataHandler.addPlayerFriend(target.getUniqueId().toString(), p.getUniqueId().toString()); p.sendMessage(ChatUtils.format(prefix + "&7Successfully force-added &c" + target.getName() + " &7as a friend!")); p.sendMessage(ChatUtils.format("&e-----------------------------------")); p.sendMessage(ChatUtils.format("")); p.sendMessage(ChatUtils.format("&aYou and " + target.getName() + " are now friends!")); p.sendMessage(ChatUtils.format("")); p.sendMessage(ChatUtils.format("&e-----------------------------------"));
                            if (Bukkit.getOnlinePlayers().contains(target)) {  ((Player) target).sendMessage(ChatUtils.format("&e-----------------------------------")); ((Player) target).sendMessage(ChatUtils.format("")); ((Player) target).sendMessage(ChatUtils.format("&aYou and " + p.getName() + " are now friends!")); ((Player) target).sendMessage(ChatUtils.format("&aHm, it seems as though " + p.getName() + " forcefully added you as a friend, you must be special ;)")); ((Player) target).sendMessage(ChatUtils.format("")); ((Player) target).sendMessage(ChatUtils.format("&e-----------------------------------"));
                            }
                        }
                    }
                } else {
                    p.sendMessage(ChatUtils.format(prefix + "You don't have permission to execute this sub-command!"));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length < 2) p.sendMessage(ChatUtils.format(prefix + "Please specify a player you want to remove!"));
                else if (!DataHandler.removePlayerFriend(p.getUniqueId().toString(), Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString())) {
                    p.sendMessage(ChatUtils.format(prefix + "That person isn't on your friends list!"));
                } else {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    DataHandler.removePlayerFriend(target.getUniqueId().toString(), p.getUniqueId().toString());
                    p.sendMessage(ChatUtils.format(prefix + "Successfully removed " + target.getName() + " from your friends list."));
                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getOfflinePlayer(args[1]))) Bukkit.getPlayer(args[1]).sendMessage(ChatUtils.format(prefix + p.getName() + " has removed you from their friends list!"));
                }
            } else if (args[0].equalsIgnoreCase("requests")) {
                List<String> req = requests.get(p.getName());
                if (req == null) req = new ArrayList<>();
                p.sendMessage(ChatUtils.format(prefix + "&7Your friend requests:"));
                if (req.size() == 0) p.sendMessage(ChatUtils.format(prefix + "It seems as though you don't have any friend requests, this is awkward..."));
                else {
                    CraftPlayer cp = (CraftPlayer) p;
                    for (String player : req) {
                        PacketPlayOutChat message = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(""/*TODO JSON accept/deny buttons*/));
                        cp.getHandle().playerConnection.sendPacket(message);
                    }
                }
            } else {
                if (args[0].length() > 1) {
                    OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
                    if (DataHandler.getPlayerFriends(p.getUniqueId().toString()).contains(t.getUniqueId().toString())) p.sendMessage(ChatUtils.format(prefix + "You cannot add people who are already on your friends list!"));
                    else {
                        if (t == p)
                            p.sendMessage(ChatUtils.format(prefix + "You cannot send a friend request to yourself, silly!"));
                        else {
                            List<String> preq = requests.get(p.getName());
                            if (preq == null) preq = new ArrayList<>();
                            if (preq.contains(t.getName())) {
                                preq.remove(t.getName());
                                requests.put(p.getName(), preq);
                                DataHandler.addPlayerFriend(p.getUniqueId().toString(), t.getUniqueId().toString());
                                DataHandler.addPlayerFriend(t.getUniqueId().toString(), p.getUniqueId().toString());
                                p.sendMessage(ChatUtils.format(prefix + "You and " + t.getName() + " are now friends!"));
                                if (Bukkit.getOnlinePlayers().contains(t)) ((Player) t).sendMessage(ChatUtils.format(prefix + "You and " + p.getName() + " are now friends!"));
                            } else {
                                OfflinePlayer target = Bukkit.getOfflinePlayer(t.getName());
                                List<String> targetreq = requests.get(target.getName());
                                if (targetreq == null) targetreq = new ArrayList<>();
                                if (targetreq.contains(p.getName())) p.sendMessage(ChatUtils.format(prefix + "I know, you're impatient, but you have to wait until you can send this person another friend request."));
                                else if (DataHandler.getPlayerFriends(p.getUniqueId().toString()).contains(target.getUniqueId())) p.sendMessage(ChatUtils.format(prefix + "You must really like this person, but they're already on your friends list!"));
                                else {
                                    targetreq.add(p.getName());
                                    requests.put(target.getName(), targetreq);
                                    p.sendMessage(ChatUtils.format(prefix + "You've sent &c" + target.getName() + "&7 a friend request! They have 5 minutes to accept."));
                                    if (Bukkit.getOnlinePlayers().contains(target)) {
                                        Player targetplayer = Bukkit.getPlayer(target.getName());
                                        targetplayer.sendMessage(ChatUtils.format("&e-----------------------------------"));
                                        targetplayer.sendMessage(ChatUtils.format(""));
                                        targetplayer.sendMessage(ChatUtils.format("&e" + p.getName() + " has sent you a friend request!"));
                                        PacketPlayOutChat message = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{text:\"\",extra:[{text:\"[ACCEPT]\",color:green,bold:true,hoverEvent:{action:show_text,value:[{text:\"Click here to accept the friend request!\",bold:false,color:gray}]},clickEvent:{action:run_command,value:\"/f " + p.getName() + "\"}}]}"));
                                        ((CraftPlayer) targetplayer).getHandle().playerConnection.sendPacket(message);
                                        targetplayer.sendMessage(ChatUtils.format("&aYou have 5 minutes to accept it."));
                                        targetplayer.sendMessage(ChatUtils.format(""));
                                        targetplayer.sendMessage(ChatUtils.format("&e-----------------------------------"));
                                    }
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(com.snowbud56.Friends.getInstance(), new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            List<String> targetrequests = requests.get(target.getName());
                                            if (targetrequests.contains(p.getName())) {
                                                targetrequests.remove(p.getName());
                                                requests.put(t.getName(), targetrequests);
                                                p.sendMessage(ChatUtils.format(prefix + "Your friend request to " + t.getName() + " has expired."));
                                                if (Bukkit.getOnlinePlayers().contains(t)) {
                                                    ((Player) t).sendMessage(ChatUtils.format(prefix + "Your friend request from " + p.getName() + " has expired."));
                                                }
                                            }
                                        }
                                    }, 20 * 60 * 5);
                                }
                            }
                        }
                    }
                } else {
                    p.sendMessage(ChatUtils.format(prefix + "If you're sending a friend request, please provide a name greater than 1 character. If you're just trying to view the list of commands, type /f"));
                }
            }
        }
        return false;
    }
}
