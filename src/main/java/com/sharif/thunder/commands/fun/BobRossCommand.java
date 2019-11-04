package com.sharif.thunder.commands.fun;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.sharif.thunder.Thunder;
import com.sharif.thunder.commands.FunCommand;
import com.sharif.thunder.utils.*;
import java.util.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

public class BobRossCommand extends FunCommand {

  private final Thunder thunder;

  public BobRossCommand(Thunder thunder) {
    this.thunder = thunder;
    this.name = "bobross";
    this.help = "Draws a user's avatar over 'Bob Ross' canvas.";
    this.arguments = "<user>";
  }

  public void execute(CommandEvent event) {
    try {
      event
          .getChannel()
          .sendMessage("Please wait...")
          .queue(
              message -> {
                if (event.getArgs().isEmpty()) {
                  message
                      .editMessage(event.getClient().getWarning() + " You need to mention a user")
                      .queue();
                  return;
                }
                event.getChannel().sendTyping().queue();
                Map<String, String> headers = new HashMap<>();
                headers.put("authorization", "Bearer " + thunder.getConfig().getEmiliaKey());
                List<Member> list = FinderUtil.findMembers(event.getArgs(), event.getGuild());
                byte[] image =
                    UnirestUtil.getBytes(
                        "https://emilia.shrf.xyz/api/bob-ross?image="
                            + list.get(0).getUser().getEffectiveAvatarUrl()
                            + "",
                        headers);
                message.delete().submit();
                event
                    .getChannel()
                    .sendFile(image, "bobross.png")
                    .embed(
                        new EmbedBuilder()
                            .setAuthor(
                                event.getMember().getUser().getName(),
                                null,
                                event.getAuthor().getEffectiveAvatarUrl())
                            .setColor(event.getSelfMember().getColor())
                            .setImage("attachment://bobross.png")
                            .build())
                    .queue();
              });
    } catch (IllegalArgumentException ex) {
      event.replyError("Shomething went wrong while fetching the API! Please try again.");
      System.out.println(ex);
    }
  }
}