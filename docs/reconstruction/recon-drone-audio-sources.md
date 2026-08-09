# Recon drone audio sources

The recon-drone Gatling sounds are distributable CC0 assets downloaded or
derived from Freesound's public previews on 2026-08-09.

## Spin-up

- In-mod file: `assets/finalparadox/sounds/entity/drone_gatling_spinup.ogg`
- Source: `minigun.mp3` by scorepion
- Source page: https://freesound.org/people/scorepion/sounds/424917/
- License: Creative Commons Zero 1.0 (CC0)
- Use: playback begins while the trigger is held and is stopped by the client
  sound instance when the 20-tick warm-up ends or the trigger is released.

## Sustained fire

- In-mod file: `assets/finalparadox/sounds/entity/drone_gatling_fire.ogg`
- Source: `Minigun Burst Audio` by rob762x51
- Source page: https://freesound.org/people/rob762x51/sounds/165042/
- License: Creative Commons Zero 1.0 (CC0)
- Source note: the author describes this as a real Garwood M134G Minigun demo,
  recorded with the camera microphones about four feet from the gun.
- Processing: cropped the stable 10.0--14.5 second section from the public
  high-quality preview, mixed it to mono for correct positional playback,
  applied a 200 ms loop crossfade, rotated the loop boundary to reduce the
  discontinuity, normalized it to approximately -11 dB RMS, and encoded it as
  44.1 kHz Ogg Vorbis.
- Use: attached positional loop while the synchronized drone gun state is
  `FIRING`; it stops immediately when that state ends or the drone disappears.

Attribution is not required by CC0, but the source record is retained here so
the provenance and redistribution permission remain auditable.
