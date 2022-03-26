# Changelog

## Added

- New render logic (from Forge version) that greatly improves performance!
- Auto-populate block list with Ores
  - This only happens when the list is empty and opened for the first time. 

## Changed

- Updated to 1.18.2!
- We no longer store if `XRay` is on in a file so upon a game restart, it will now always be off by default.
- Use a different radius number. It's now calculated by chunks around the player. 1 = 1x1, 3 = 3x3 chunks, etc.

## Fixed

- Fixed `Show lava` not rendering working when the blocks to find is empty. 