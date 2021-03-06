/*
 *   Copyright 2019 SharifPoetra
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.sharif.thunder;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sharif.thunder.api.ThunderApi;
import com.sharif.thunder.commands.Command;
import com.sharif.thunder.commands.Command.Category;
import com.sharif.thunder.commands.administration.*;
import com.sharif.thunder.commands.fun.*;
import com.sharif.thunder.commands.interaction.*;
import com.sharif.thunder.commands.music.*;
import com.sharif.thunder.commands.owner.*;
import com.sharif.thunder.commands.utilities.*;
import com.sharif.thunder.databasemanager.Database;
import com.sharif.thunder.datasources.*;
import com.sharif.thunder.utils.FormatUtil;
import com.sharif.thunder.utils.SenderUtil;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.Executors;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends ListenerAdapter {
  public static final String PLAY_EMOJI = "\u25B6"; // ▶
  public static final String PAUSE_EMOJI = "\u23F8"; // ⏸
  public static final String STOP_EMOJI = "\u23F9"; // ⏹
  public static final Permission[] RECOMMENDED_PERMS = new Permission[] {Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE};
  private static JDA jda;
  private static Command[] commands;
  private static BotConfig config;
  private static Thunder thunder;
  private static ThunderApi thunderApi;
  // datasources
  private static AFKs afks;
  private static InVCRoles inVcRoles;

  public static void main(String[] args) throws Exception {

    // Configuration initializations
    Logger logger = LoggerFactory.getLogger(Main.class);
    config = new BotConfig();
    logger.info("Loaded config from " + config.getConfigLocation());
    EventWaiter waiter = new EventWaiter(Executors.newSingleThreadScheduledExecutor(), false);
    Database database = new Database(config.getDbHost(), config.getDbUser(), config.getDbPass());
    thunder = new Thunder(waiter, config, database);
    logger.info("Starting ThunderApi...");
    thunderApi = new ThunderApi(thunder).start();

    // datasources initializations
    logger.info("Initializing datasources...");
    afks = new AFKs();
    inVcRoles = new InVCRoles();

    // reading datasources
    logger.info("Reading datasources...");
    afks.read();
    inVcRoles.read();

    // lists all the commands
    logger.info("Loading all commands...");
    commands = new Command[] {
      // administration
      new SetInVCRoleCommand(inVcRoles),
      new PrefixCommand(thunder),
      // fun
      new SayCommand(thunder),
      new BobRossCommand(thunder),
      new ChooseCommand(thunder),
      new BatSlapCommand(thunder),
      // interaction
      new PatCommand(thunder),
      new SlapCommand(thunder),
      new BlushCommand(thunder),
      new CryCommand(thunder),
      new DanceCommand(thunder),
      new PoutCommand(thunder),
      new LewdCommand(thunder),
      // owner
      new EvalCommand(thunder),
      new RestartCommand(thunder),
      new PlaylistCommand(thunder),
      new BotStatusCommand(),
      // utilities
      new StatsCommand(thunder),
      new PingCommand(thunder),
      new EmotesCommand(thunder),
      new AFKCommand(afks),
      new KitsuCommand(thunder),
      new AvatarCommand(thunder),
      // music
      new PlayCommand(thunder, config.getLoading()),
      new PlaylistsCommand(thunder),
      new NowplayingCommand(thunder),
      new VolumeCommand(thunder),
      new SkipCommand(thunder),
      new StopCommand(thunder),
      new ShuffleCommand(thunder),
      new QueueCommand(thunder),
      new SearchCommand(thunder, config.getSearching()),
      new SCSearchCommand(thunder, config.getSearching()),
      new RepeatCommand(thunder),
      new NightcoreCommand(thunder),
      new PitchCommand(thunder),
      new KaraokeCommand(thunder),
      new VaporwaveCommand(thunder),
      new BassboostCommand(thunder),
      new MoveTrackCommand(thunder),
      new PlaynextCommand(thunder, config.getLoading()),
      new LyricsCommand(thunder),
      new RemoveCommand(thunder),
      new PauseCommand(thunder),
      new SkiptoCommand(thunder)

    };

    try {
      logger.info("Running JDABuilder...");
      JDA jda = new JDABuilder(AccountType.BOT)
        .setToken(config.getToken())
        .addEventListeners(new Main(), waiter)
        .setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY))
        .build()
        .awaitReady();
      thunder.setJDA(jda);
    } catch (LoginException ex) {
      logger.error("Something went wrong when tried to login to discord: " + ex);
      System.exit(1);
    } catch (IllegalArgumentException ex) {
      logger.error("Some aspect of the configuration is invalid: " + ex + "\nConfig Location: " + config.getConfigLocation());
      System.exit(1);
    }
  }

  @Override
  public void onReady(ReadyEvent event) {
    AsyncInfoMonitor.start();
    System.out.println(event.getJDA().getSelfUser().getAsTag() + " is ready now!");
  }

  @Override
  public void onShutdown(ShutdownEvent event) {
    afks.shutdown();
    inVcRoles.shutdown();
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (afks.get(event.getAuthor().getId()) != null) {
      event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Welcome back, I have removed your AFK status.").queue();
      afks.remove(event.getAuthor().getId());
    }
    if (event.getChannelType() != ChannelType.PRIVATE && !event.getAuthor().isBot()) {
      String relate = "__" + event.getGuild().getName() + "__ <#" + event.getTextChannel().getId() + "> **" + event.getAuthor().getAsTag() + "**:\n" + event.getMessage().getContentRaw();
      event.getMessage().getMentionedUsers().stream().filter((u) -> (afks.get(u.getId()) != null)).forEach((u) -> SenderUtil.sendDM(u, relate));
    }
    if (event.getChannelType() != ChannelType.PRIVATE && !event.getMessage().getMentionedUsers().isEmpty() && !event.getAuthor().isBot()) {
      StringBuilder builder = new StringBuilder();
      event.getMessage().getMentionedUsers().stream().forEach(u -> {
        if (afks.get(u.getId()) != null) {
          String response = afks.get(u.getId())[AFKs.MESSAGE];
          if (response != null)
            builder.append("\n\uD83D\uDCA4 **").append(u.getName()).append("** is currently AFK:\n").append(response);
          }
        });
      String afkmessage = builder.toString().trim();
      if (!afkmessage.equals("")) event.getChannel().sendMessage(afkmessage).queue();
    }

    // get a prefixes
    String strippedMessage = null;
    String prefix = Thunder.getDatabase().guildSettings.getSettings(event.getGuild()).getPrefix();
    if (prefix == null) prefix = config.getPrefix();
    if (event.getMessage().getContentRaw().startsWith(prefix.toLowerCase())) {
      strippedMessage = event.getMessage().getContentRaw().substring(prefix.length()).trim();
    }

    if (strippedMessage != null && !event.getAuthor().isBot()) {
      strippedMessage = strippedMessage.trim();

      // help command
      if (strippedMessage.equalsIgnoreCase("help")) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(event.getGuild().getSelfMember().getColor());
        eb.setAuthor(event.getGuild().getSelfMember().getUser().getName() + " commands:", null, event.getGuild().getSelfMember().getUser().getEffectiveAvatarUrl());
        StringBuilder builder = new StringBuilder();
        Category category = null;
        for (Command command : commands) {
          if (command.isHidden()) continue;
          if (!Objects.equals(category, command.getCategory())) {
            category = command.getCategory();
            builder.append("\n\n**__").append(category == null ? "No Category" : category.getName()).append("__:**\n");
          }
          builder.append("`").append(prefix).append(command.getName()).append("`  ");
        }
        eb.setDescription(builder.toString());
        eb.setFooter("Do not include <> nor [] - <> means required and [] means optional.");
        event.getChannel().sendMessage(eb.build()).queue();
      } else {
        Command toRun = null;
        String[] args = FormatUtil.cleanSplit(strippedMessage);
        if (args[0].equalsIgnoreCase("help")) {
          String endhelp = args[1] + " " + args[0];
          args = FormatUtil.cleanSplit(endhelp);
        }
        args[1] = FormatUtil.appendAttachmentUrls(event.getMessage(), args[1]);
        for (Command com : commands)
          if (com.isCommandFor(args[0])) {
            toRun = com;
            break;
          }
        if (toRun != null) {
          toRun.run(args[1], event);
        }
      }
    }
  }

  @Override
  public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
    thunder.getNowplayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
  }

  @Override
  public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
    try {
      if (event.getMember().getUser().isBot()) return;
      if (inVcRoles.get(event.getGuild().getId()) != null) {
        event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(inVcRoles.get(event.getGuild().getId())[InVCRoles.ROLEID])).queue();
      }
    } catch (Exception ex) {
      System.out.println("Error when giving a member voice role: " + ex.toString());
    }
  }

  @Override
  public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
    try {
      if (event.getMember().getUser().isBot()) return;
      if (inVcRoles.get(event.getGuild().getId()) != null) {
        event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(inVcRoles.get(event.getGuild().getId())[InVCRoles.ROLEID])).queue();
      }
    } catch (Exception ex) {
      System.out.println("Error when removing a member voice role: " + ex.toString());
    }
  }
}
