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
package com.sharif.thunder.commands.fun;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.sharif.thunder.Thunder;
import com.sharif.thunder.commands.FunCommand;
import com.sharif.thunder.utils.*;
import java.util.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

public class BatSlapCommand extends FunCommand {

  private final Thunder thunder;

  public BatSlapCommand(Thunder thunder) {
    this.thunder = thunder;
    this.name = "batslap";
    this.help = "Slap someone with batslap template.";
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
                        "https://emilia.shrf.xyz/api/batslap?slapper="
                            + event.getAuthor().getEffectiveAvatarUrl()
                            + "&slapped="
                            + list.get(0).getUser().getEffectiveAvatarUrl(),
                        headers);
                message.delete().submit();
                event
                    .getChannel()
                    .sendFile(image, "batslap.png")
                    .embed(
                        new EmbedBuilder()
                            .setAuthor(
                                list.get(0).getUser().getName()
                                    + " has been batslapped by "
                                    + event.getAuthor().getName(),
                                null,
                                null)
                            .setColor(event.getSelfMember().getColor())
                            .setImage("attachment://batslap.png")
                            .build())
                    .queue();
              });
    } catch (IllegalArgumentException ex) {
      event.replyError("Shomething went wrong while fetching the API! Please try again.");
      System.out.println(ex);
    }
  }
}
