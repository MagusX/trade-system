open-order:
- if order quantity <= current tick quantity:
  ```json
  request: order(quantiy=2), currentTick(quantity=2)
  response:
    order(id=a, quantity=2, status=FILLED)
      trade(id=1, orderId=a, filled=2, status=COMPLETED)

- if order quantity > current tick quantity:
  ```json
  request: order(quantiy=3),
  response:
    -- first tick (quantity=2)
    order(id=a, quantity=3, status=PARTIALLY_FILLED)
      trade(id=1, orderId=a, filled=2, status=COMPLETED)
      trade(id=2, orderId=a, filled=0, status=OPEN) -- will be processed in background in the next tick

    -- second tick (quantity=3) (enough balance)
    order(id=a, quantity=3, status=FILLED)
      trade(id=1, orderId=a, filled=2, status=COMPLETED)
      trade(id=2, orderId=a, filled=1, status=COMPLETED)

    -- second tick (quantity=3) (NOT enough balance)
    order(id=a, quantity=3, status=PARTIALLY_FILLED)
      trade(id=1, orderId=a, filled=2, status=COMPLETED)
      trade(id=2, orderId=a, filled=1, status=FAILED)

If the spawned trade keeps having larger quantity than the tick quantity, the cycle of filling trade and spawning new trade for the remaning quantity repeats
User could pay with higher cost for BUY or get less profit for SELL if the order is partially filled because the system will try to place and fill trade for the remaining unfilled quantity in the next tick at any cost (market price)
