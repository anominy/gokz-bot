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

package io.github.anominy.gokzbot.util;

import java.util.concurrent.TimeUnit;

public final class UKreedzSchedule {
    public static final int INITIAL_DELAY = 0;
    public static final int FIXED_PERIOD = 60;
    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private UKreedzSchedule() {
        throw new UnsupportedOperationException();
    }
}
