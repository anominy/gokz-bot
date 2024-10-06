package io.github.anominy.gokzbot;

import io.github.anominy.gokzbot.task.RecentBansScheduleTask;
import io.github.anominy.gokzbot.task.RecentWrsScheduleTask;
import io.github.anominy.gokzbot.util.UKreedzSchedule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class Main {

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = SingletonScheduledExecutor.INSTANCE;

        // Recent bans
        scheduler.scheduleAtFixedRate(SingletonRecentBansScheduleTask.INSTANCE,
                UKreedzSchedule.INITIAL_DELAY, UKreedzSchedule.FIXED_PERIOD, UKreedzSchedule.TIME_UNIT);

        // Recent WRs
        scheduler.scheduleAtFixedRate(SingletonRecentWrsScheduleTask.INSTANCE,
                UKreedzSchedule.INITIAL_DELAY, UKreedzSchedule.FIXED_PERIOD, UKreedzSchedule.TIME_UNIT);
    }

    private static final class SingletonScheduledExecutor {
        public static final ScheduledExecutorService INSTANCE
                = Executors.newScheduledThreadPool(2);

        private SingletonScheduledExecutor() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class SingletonRecentBansScheduleTask {
        public static final Runnable INSTANCE;

        static {
            String webhookUrl = System.getenv("RECENT_BANS_WEBHOOK_URL");
            if (webhookUrl == null || webhookUrl.isBlank()) {
                throw new IllegalArgumentException("Webhook URL mustn't be <null/blank>");
            }

            INSTANCE = new RecentBansScheduleTask(webhookUrl);
        }

        private SingletonRecentBansScheduleTask() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class SingletonRecentWrsScheduleTask {
        public static final Runnable INSTANCE;

        static {
            String webhookUrl = System.getenv("RECENT_WRS_WEBHOOK_URL");
            if (webhookUrl == null || webhookUrl.isBlank()) {
                throw new IllegalArgumentException("Webhook URL mustn't be <null/blank>");
            }

            INSTANCE = new RecentWrsScheduleTask(webhookUrl);
        }

        private SingletonRecentWrsScheduleTask() {
            throw new UnsupportedOperationException();
        }
    }

    private Main() {
        throw new UnsupportedOperationException();
    }
}
