# `DummyMeasureProvider.java`

In-memory `MeasureProvider<Measure, Unit>` fixture with a fixed measure catalogue: `system/Time` (ms…w), `base/Length`, `base/Mass`, `derived/Area`, `derived/Volume` and derived `Velocity`.
Adds test accessors `getTime`, `getLength`, `getMass`, `getArea`, `getVolume`; `getBaseMeasures` returns Length¹·Time⁻¹ only for `velocity`, `isBaseMeasure` is false for derived ones, and `setMeasureChangeHandler` is a no-op — change-notification tests must mock the provider instead.