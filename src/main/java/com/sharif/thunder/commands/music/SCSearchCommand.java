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
package com.sharif.thunder.commands.music;

import com.sharif.thunder.Thunder;

public class SCSearchCommand extends SearchCommand {
  public SCSearchCommand(Thunder thunder, String searchingEmoji) {
    super(thunder, searchingEmoji);
    this.searchPrefix = "scsearch:";
    this.name = "scsearch";
    this.help = "searches Soundcloud for a provided query.";
    this.aliases = new String[] {"soundcloud"};
    this.guildOnly = true;
    this.beListening = true;
    this.bePlaying = false;
  }
}
