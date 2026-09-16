# `MeasureChangeAdapterTest.java`

Verifies `MeasureAdapter.dimensions` bookkeeping under provider change events.
`@BeforeEach` mocks `MeasureProvider` and captures the `MeasureChangedHandler` passed to `setMeasureChangeHandler`, then drives `measureAdded` / `measureChanged` / `measureRemoved` for base and derived measures.
Asserts dimensions map back to empty after removal; adding a derived measure whose base measures are not yet known throws `IllegalArgumentException`.