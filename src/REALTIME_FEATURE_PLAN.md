# SmartPos — Real-Time Updates Feature Plan
**Status:** ON HOLD — Implement in v2  
**Raised:** April 17, 2026  
**Decision date:** Pending both teams' agreement  

---

## Why This Matters

The KDS (Kitchen Display System), table grid, and order list all need to reflect live state. Right now:
- If counter 1 places an order, the KDS on the kitchen screen does not update until manually refreshed
- If a table becomes occupied, other counters don't see it until they pull-to-refresh
- The pending order badge count only updates on navigation

For v1 launch this is acceptable. For real restaurant usage — especially multi-counter setups — this becomes a noticeable UX gap quickly.

---

## What Is on Hold

Building a **server-push mechanism** so the backend notifies all connected clients (mobile tablets, KDS screen) when:
- A new order is created
- An order status changes (PENDING → IN_PROGRESS → COMPLETED)
- An order item status changes (KDS marks item READY)
- A table status changes (AVAILABLE → OCCUPIED → AVAILABLE)
- A payment is completed (table freed)

---

## Current State (v1 Workaround)

Mobile team: implement polling until the push mechanism is built.

| Screen | Endpoint to poll | Interval |
|---|---|---|
| KDS / Order List | `GET /api/v1/restaurants/{rId}/orders/active` | Every 15 seconds |
| Pending badge | `GET /api/v1/restaurants/{rId}/orders/count/pending` | Every 30 seconds |
| Table grid | `GET /api/v1/restaurants/{rId}/tables` | Every 30 seconds |

**Rules for polling:**
- Start polling when the screen becomes visible (`onStart` / `onResume`)
- Stop polling when the screen is not visible (`onStop` / `onPause`)
- Stop polling immediately if a 401 is received (token expired — redirect to login)
- Use a coroutine with `delay()` or a `ViewModel` timer — not `Handler.postDelayed` on the main thread

This is battery-friendly and uses endpoints that already exist today with zero backend changes.

---

## Three Options Evaluated

### Option A — Polling ✅ (Current v1 approach)
- **Backend work:** Zero
- **Mobile work:** ~4 hours (timer logic per screen)
- **Latency:** Up to 15 seconds
- **Verdict:** Good enough for v1. Not good enough for a busy restaurant at peak hour.

---

### Option B — Server-Sent Events (SSE) ⭐ Recommended for v2
- **What it is:** The mobile app opens a long-lived HTTP connection. The backend pushes events down it whenever something changes. One-way: server → client only.
- **Backend work:** ~1.5 days
- **Mobile work:** ~1 day
- **Latency:** Under 1 second
- **Pros:** Simple. Works over existing HTTP/HTTPS. No new library needed on backend (Spring MVC supports `SseEmitter` natively). Easy to debug.
- **Cons:** One-way only (client cannot send messages over SSE — but we don't need that; all writes go through REST)
- **Verdict:** Best fit for this product. All the real-time benefit, half the complexity of WebSocket.

**Backend design (when we build it):**
```
GET /api/v1/restaurants/{restaurantId}/events
Authorization: Bearer <token>
Accept: text/event-stream

← event: order_created
   data: { "order_id": 7, "table_number": "T3", "order_number": "ORD-..." }

← event: order_status_changed
   data: { "order_id": 7, "new_status": "IN_PROGRESS" }

← event: item_status_changed
   data: { "order_id": 7, "item_id": 11, "new_status": "READY" }

← event: table_status_changed
   data: { "table_id": 4, "new_status": "OCCUPIED" }

← event: payment_completed
   data: { "order_id": 7, "table_id": 4 }
```

**Backend implementation plan:**
1. Add `SseEmitter` registry class (holds one emitter per connected client, keyed by `restaurantId`)
2. Add `GET /events` endpoint to a new `EventsController`
3. Inject the registry into `OrderService`, `TableService`, `PaymentService`
4. After each state-changing operation, call `registry.push(restaurantId, eventType, payload)`
5. Handle client disconnect (remove dead emitters from registry)
6. Handle token expiry: validate JWT on connect; return `401` if invalid — client falls back to polling

---

### Option C — STOMP over WebSocket
- **Backend work:** ~2 days
- **Mobile work:** ~2 days  
- **Latency:** Under 1 second
- **Pros:** Full two-way communication. Industry standard for chat/real-time apps.
- **Cons:** Overkill for a POS. We don't need client → server messaging over the socket. Requires `spring-boot-starter-websocket` + STOMP broker config + authentication on the handshake + reconnect logic on both sides.
- **Verdict:** More complexity than we need. SSE does the same job for less work.

---

## Decision Needed Before Building

Both teams must agree on:

1. **Which option** — recommendation is SSE (Option B)
2. **Which events** — the list above covers the obvious ones; mobile team to confirm if anything is missing
3. **Reconnect behaviour** — what should the mobile app do if the SSE connection drops? (Auto-reconnect with exponential back-off? Fall back to polling?)
4. **Auth on reconnect** — if the JWT expires while the SSE stream is open, what happens? (Server closes the stream with a specific event type; client detects it → re-validate token → reconnect)
5. **Release milestone** — target sprint / version for v2

---

## Things to Remember When We Build This

- [ ] The SSE emitter registry must be thread-safe (`ConcurrentHashMap`)
- [ ] Dead emitters (client disconnected) must be removed immediately — memory leak risk if not
- [ ] Events must include `restaurantId` so multi-tenant isolation is preserved (a tablet at restaurant A never receives events from restaurant B)
- [ ] Token must be re-validated on SSE reconnect, not just on the initial connect
- [ ] Mobile must fall back to polling if the SSE connection fails more than N times (graceful degradation)
- [ ] Add a `heartbeat` event every 30 seconds to keep the connection alive through proxies/load balancers that close idle connections
- [ ] SSE endpoint must be excluded from Spring's `SessionCreationPolicy.STATELESS` check (it already is — stateless is fine for SSE)
- [ ] Load testing required before enabling in production: 50 concurrent SSE connections per restaurant × N restaurants

---

## Files to Update When This Is Built

| File | Change |
|---|---|
| `build.gradle.kts` | No new dependency needed — `SseEmitter` is in `spring-webmvc` |
| New: `SseEmitterRegistry.kt` | Thread-safe registry, push method, cleanup |
| New: `EventsController.kt` | `GET /api/v1/restaurants/{rId}/events` |
| `OrderServiceImpl.kt` | Push event after create/status change |
| `TableServiceImpl.kt` | Push event after status change |
| `PaymentServiceImpl.kt` (or `BillServiceImpl.kt`) | Push event after payment complete |
| `SecurityConfig.kt` | Ensure `/api/v1/restaurants/*/events` requires auth |
| Mobile: `SseEventManager` | Replace `SmartPosWebSocketManager` |

---

*Documented April 17, 2026 — revisit at v2 planning*

