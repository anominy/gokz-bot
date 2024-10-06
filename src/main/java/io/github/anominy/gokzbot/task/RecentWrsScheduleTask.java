/*
 * Copyright 2024 anominy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.anominy.gokzbot.task;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import io.github.anominy.gokzbot.util.UKreedzColor;
import io.github.anominy.gokzbot.util.UKreedzDate;
import io.github.anominy.gokzbot.util.UKreedzIcon;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class RecentWrsScheduleTask implements Runnable {
    private final WebhookClient webhookClient;
    private Instant lastUpdateTimestamp;

    public RecentWrsScheduleTask(String url) {
        this.webhookClient = WebhookClient.withUrl(url);
        this.lastUpdateTimestamp = Instant.now();
    }

    @Override
    public void run() {
        JsonArray jsonArray;
        try {
            String lastUpdateDateStr = UKreedzDate.FORMATTER.format(this.lastUpdateTimestamp);
            jsonArray = JsonParser.array()
                    .from(new URL("https://kztimerglobal.com/api/v2.0/records/top/recent"
                            + "?modes_list_string=kz_vanilla"
                            + "&place_top_at_least=1"
                            + "&created_since=" + lastUpdateDateStr));
        } catch (JsonParserException | MalformedURLException ignored) {
            return;
        }

        Instant createTimestamp = null;
        for (Object obj : jsonArray) {
            //noinspection DuplicatedCode
            JsonObject jsonObject = (JsonObject) obj;

            String createDateStr = jsonObject.getString("created_on");
            LocalDateTime createDate = LocalDateTime.parse(createDateStr, UKreedzDate.FORMATTER);

            createTimestamp = createDate.toInstant(ZoneOffset.UTC);
            if (createTimestamp.getEpochSecond()
                    <= this.lastUpdateTimestamp.getEpochSecond()) {
                continue;
            }

            String mapName = jsonObject.getString("map_name");
            String modeName = jsonObject.getString("mode");
            String playerName = jsonObject.getString("player_name");
            String steamid64 = jsonObject.getString("steamid64");
            float time = jsonObject.getFloat("time");
            String serverName = jsonObject.getString("server_name");
            int teleportCount = jsonObject.getInt("teleports");

            modeName = switch (modeName) {
                case "kz_vanilla" -> "VNL";
                case "kz_simple" -> "SKZ";
                case "kz_timer" -> "KZT";
                default -> modeName;
            };

            //noinspection ExtractMethodRecommender
            int totalHours = (int) (time / 3600);
            int totalMinutes = (int) (time / 60);
            int totalSeconds = (int) time;
            int totalMilliseconds = (int) (time * 1000);

            int minutes = totalMinutes % 60;
            int seconds = totalSeconds % 60;
            int milliseconds = totalMilliseconds % 1000;

            String timeStr;
            if (totalHours != 0) {
                timeStr = String.format("%d:%02d:%02d.%03d", totalHours, minutes, seconds, milliseconds);
            } else if (totalMinutes != 0) {
                timeStr = String.format("%d:%02d.%03d", minutes, seconds, milliseconds);
            } else {
                timeStr =  String.format("%d.%03d", seconds, milliseconds);
            }

            String description = "Map: **"
                    + mapName
                    + "**"
                    + "\nMode: **"
                    + modeName
                    + "**"
                    + "\n\nPlayer: **["
                    + playerName
                    + "](https://steamcommunity.com/profiles/"
                    + steamid64
                    + ")**"
                    + "\nTime: **"
                    + timeStr
                    + "** ["
                    + teleportCount
                    + " TP"
                    + (teleportCount != 1 ? "s" : "")
                    + "]"
                    + "\nServer: **"
                    + serverName
                    + "**";

            String mapImageUrl = "https://raw.githubusercontent.com/KZGlobalTeam/map-images/refs/heads/master/images/"
                    + mapName + ".jpg";

            WebhookEmbed.EmbedTitle webhookEmbedTitle = new WebhookEmbed.EmbedTitle(
                    "Recent World Record", null);

            WebhookEmbed.EmbedFooter webhookEmbedFooter = new WebhookEmbed.EmbedFooter(
                    UKreedzIcon.TITLE, UKreedzIcon.URL);

            WebhookEmbed webhookEmbed = new WebhookEmbedBuilder()
                    .setTitle(webhookEmbedTitle)
                    .setFooter(webhookEmbedFooter)
                    .setColor(UKreedzColor.WR)
                    .setTimestamp(createTimestamp)
                    .setDescription(description)
                    .setImageUrl(mapImageUrl)
                    .build();

            WebhookMessage webhookMessage = new WebhookMessageBuilder()
                    .setAvatarUrl(UKreedzIcon.URL)
                    .setUsername(UKreedzIcon.TITLE)
                    .addEmbeds(webhookEmbed)
                    .build();

            this.webhookClient.send(webhookMessage);
        }

        if (createTimestamp != null) {
            this.lastUpdateTimestamp = createTimestamp;
        }
    }
}
