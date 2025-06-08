package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.util.TextBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class IgnoreCommand extends BaseCommand {

    public IgnoreCommand(final DuelsPlugin plugin) {
        super(plugin, "ignore", "ignore [player]", "Ignore or unignore duel requests from a player.", 1, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final UserData user = userManager.get(player);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.load-failure");
            return;
        }

        // If no player specified, show ignored players list
        if (args.length == 1) {
            final Set<UUID> ignoredPlayers = user.getIgnoredPlayers();

            if (ignoredPlayers.isEmpty()) {
                lang.sendMessage(sender, "COMMAND.duel.ignore.list-empty");
                return;
            }

            lang.sendMessage(sender, "COMMAND.duel.ignore.list-header");

            for (final UUID ignoredUuid : ignoredPlayers) {
                final String ignoredName = Bukkit.getOfflinePlayer(ignoredUuid).getName();
                if (ignoredName != null) {
                    TextBuilder
                            .of(lang.getMessage("COMMAND.duel.ignore.list-format", "name", ignoredName))
                            .setClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel ignore " + ignoredName)
                            .setHoverEvent(HoverEvent.Action.SHOW_TEXT, lang.getMessage("COMMAND.duel.ignore.list-hover"))
                            .send(player);
                }
            }

            lang.sendMessage(sender, "COMMAND.duel.ignore.list-footer");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        if (player.equals(target)) {
            lang.sendMessage(sender, "ERROR.duel.is-self");
            return;
        }

        final UUID targetUuid = target.getUniqueId();

        if (user.isIgnoring(targetUuid)) {
            user.removeIgnoredPlayer(targetUuid);
            lang.sendMessage(sender, "COMMAND.duel.ignore.removed", "name", target.getName());
        } else {
            user.addIgnoredPlayer(targetUuid);
            lang.sendMessage(sender, "COMMAND.duel.ignore.added", "name", target.getName());
        }
    }
} 