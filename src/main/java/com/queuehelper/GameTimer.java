package com.queuehelper;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import static net.runelite.client.util.RSTimeUnit.GAME_TICKS;

class GameTimer
{
    final private Instant startTime = Instant.now();
    int getPBTime()
    {
        return (int)Duration.between(startTime, Instant.now()).minus(Duration.of(1, GAME_TICKS)).getSeconds();
    }

}
