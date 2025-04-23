# Countered's Accurate Hitboxes

This Minecraft mod adds **precise per-cuboid hitbox detection** to entities using transformed `ModelPart` data. It leverages raycasting and rotated bounding boxes (OBBs) to deliver highly accurate interaction zones for complex mobs.

### Features
- Accurate per-part hitbox detection based on model geometry
- Debug visualization using particles / renderlayers

### Status
Currently built for **Minecraft 1.21.1** using Fabric.

A **backport to 1.20.1 is needed**, but the renderer before 1.21 make this difficult. In particular, transformations in the matrix stack seem to include unexpected rotations (possibly related to camera-space), which breaks accurate world-space vertex reconstruction. Help is welcome!

### Contributing
Contributions are highly appreciated — whether it's bug fixes, performance improvements, or help porting to older versions.

Feel free to open a PR or reach out with suggestions!
