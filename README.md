open-order:
- if order quantity <= current tick quantity:
  request: order(quantiy=2), currentTick(quantity=2)
  response:
    order(id=a, quantity=2, status=FILLED)
      trade(id=1, orderId=a, filled=2, status=COMPLETED)

- if order quantity > current tick quantity:
  request: order(quantiy=3), currentTick(quantity=2)
  response:
    -- first tick
    order(id=a, quantity=3, status=PARTIALLY_FILLED)
      trade(id=1, orderId=a, filled=2, status=COMPLETED)
      trade(id=2, orderId=a, filled=0, status=OPEN) -- will be processed next tick

    -- second tick (enough balance)
    order(id=a, quantity=3, status=FILLED)
      trade(id=1, orderId=a, filled=2, status=COMPLETED)
      trade(id=2, orderId=a, filled=1, status=COMPLETED)

    -- second tick (NOT enough balance)
    order(id=a, quantity=3, status=PARTIALLY_FILLED)
      trade(id=1, orderId=a, filled=2, status=COMPLETED)
      trade(id=2, orderId=a, filled=1, status=FAILED)
