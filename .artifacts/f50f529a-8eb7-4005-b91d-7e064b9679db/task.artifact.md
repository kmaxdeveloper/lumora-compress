# Before/After Comparison Optimization Tasks

- [ ] Optimize `ComparisonView.kt` for performance and 60 FPS slider movement.
    - [ ] Move object allocations out of `onDraw`.
    - [ ] Pre-calculate fit-center matrices.
    - [ ] Use `postInvalidateOnAnimation`.
- [ ] Update `CompareFragment.kt` to ensure image accuracy.
    - [ ] Disable Coil memory and disk cache for comparison images.
    - [ ] Disable crossfade to avoid visual blending.
    - [ ] Verify `allowHardware(false)` is still used.
- [ ] Verify that `Before` is truly `Original` and `After` is truly `Compressed`.
