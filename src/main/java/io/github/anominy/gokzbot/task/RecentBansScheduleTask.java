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
import java.util.Locale;
import java.util.StringJoiner;

public final class RecentBansScheduleTask implements Runnable {
    private final WebhookClient webhookClient;
    private Instant lastUpdateTimestamp;

    public RecentBansScheduleTask(String url) {
        this.webhookClient = WebhookClient.withUrl(url);
        this.lastUpdateTimestamp = Instant.now();
    }

    @Override
    public void run() {
        JsonArray jsonArray;
        try {
            String lastUpdateDateStr = UKreedzDate.FORMATTER.format(this.lastUpdateTimestamp);
            jsonArray = JsonParser.array()
                    .from(new URL("https://kztimerglobal.com/api/v2/bans?created_since=" + lastUpdateDateStr));
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

            String playerName = jsonObject.getString("player_name");
            String banType = jsonObject.getString("ban_type");
            String expireDateStr = jsonObject.getString("expires_on");
            String steamId64 = jsonObject.getString("steamid64");
            String notes = jsonObject.getString("notes");
            String stats = jsonObject.getString("stats");

            LocalDateTime expireDate = LocalDateTime.parse(expireDateStr, UKreedzDate.FORMATTER);

            StringJoiner sj = new StringJoiner(" ");
            for (String banTypeItem : banType.split("_")) {
                banTypeItem = (banTypeItem.charAt(0) + "")
                        .toUpperCase(Locale.ROOT) + banTypeItem.substring(1);

                sj.add(banTypeItem);
            }
            banType = sj.toString();

            if (expireDate.getYear() != 9999) {
                expireDateStr = expireDate.getDayOfMonth()
                        + "/" + expireDate.getMonthValue()
                        + "/" + expireDate.getYear();
            } else {
                expireDateStr = "Never";
            }

            StringBuilder description = new StringBuilder()
                    .append("Player: **[")
                    .append(playerName)
                    .append("](https://steamcommunity.com/profiles/")
                    .append(steamId64)
                    .append(")**")
                    .append("\nReason: **")
                    .append(banType)
                    .append("**")
                    .append("\nExpires: **")
                    .append(expireDateStr)
                    .append("**");

            if (!notes.isBlank() && !notes.contains("bhop hack")) {
                description.append("\n\nNotes: **")
                        .append(notes)
                        .append("**");
            }

            if (!stats.isBlank()) {
                sj = new StringJoiner("\n");
                for (String statItem : stats.split(", ")) {
                    String[] statItemSplit = statItem.split(": ", 2);

                    String key = statItemSplit[0];
                    String val = statItemSplit[1]
                            .replace("*", "\\*");

                    sj.add(key + ": **" + val + "**");
                }
                stats = sj.toString();

                description.append("\n\n")
                        .append(stats);
            }

            WebhookEmbed.EmbedTitle webhookEmbedTitle = new WebhookEmbed.EmbedTitle(
                    "Recent Global Ban", null);

            WebhookEmbed.EmbedFooter webhookEmbedFooter = new WebhookEmbed.EmbedFooter(
                    UKreedzIcon.TITLE, UKreedzIcon.URL);

            WebhookEmbed webhookEmbed = new WebhookEmbedBuilder()
                    .setTitle(webhookEmbedTitle)
                    .setFooter(webhookEmbedFooter)
                    .setColor(UKreedzColor.BAN)
                    .setTimestamp(createTimestamp)
                    .setDescription(description.toString())
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
