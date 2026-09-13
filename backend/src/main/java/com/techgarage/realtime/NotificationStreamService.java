package com.techgarage.realtime;

import com.techgarage.dto.notification.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class NotificationStreamService {
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final long timeoutMs;

    public NotificationStreamService(@Value("${app.notifications.sse-timeout-ms:1800000}") long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        emitters.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup = () -> remove(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());
        try { emitter.send(SseEmitter.event().name("connected").data("ok")); } catch (IOException e) { cleanup.run(); }
        return emitter;
    }

    public void publish(Long userId, NotificationResponse notification) {
        List<SseEmitter> current = emitters.get(userId);
        if (current == null) return;
        for (SseEmitter emitter : current) {
            try { emitter.send(SseEmitter.event().name("notification").data(notification)); }
            catch (IOException ex) { remove(userId, emitter); }
        }
    }

    private void remove(Long userId, SseEmitter emitter) {
        var list = emitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) emitters.remove(userId, list);
        }
    }
}
