package com.dimas.minidoodle.api;

import com.dimas.minidoodle.AbstractIntegrationTest;
import com.dimas.minidoodle.api.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/** Many clients race for the same slot: exactly one booking must win. */
class ConcurrentBookingIT extends AbstractIntegrationTest {

    @Test
    void onlyOneOfManyConcurrentBookingsSucceeds() throws Exception {
        UUID userId = rest.postForEntity("/api/users",
                new CreateUserRequest("Race", UUID.randomUUID() + "@example.com"), UserResponse.class).getBody().id();
        UUID slotId = rest.postForEntity("/api/users/" + userId + "/slots",
                new CreateSlotRequest(Instant.parse("2026-04-01T10:00:00Z"), 30), SlotResponse.class).getBody().id();

        int clients = 20;
        ExecutorService pool = Executors.newFixedThreadPool(clients);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<HttpStatus>> results = IntStream.range(0, clients).mapToObj(i -> pool.submit(() -> {
            go.await();
            var res = rest.postForEntity("/api/users/" + userId + "/meetings",
                    new CreateMeetingRequest(slotId, "Race " + i, null, Set.of()), String.class);
            return HttpStatus.valueOf(res.getStatusCode().value());
        })).toList();
        go.countDown();

        long created = 0, conflicts = 0;
        for (Future<HttpStatus> f : results) {
            HttpStatus s = f.get(30, TimeUnit.SECONDS);
            if (s == HttpStatus.CREATED) created++; else if (s == HttpStatus.CONFLICT) conflicts++;
        }
        pool.shutdown();

        assertThat(created).isEqualTo(1);
        assertThat(conflicts).isEqualTo(clients - 1);
    }
}
