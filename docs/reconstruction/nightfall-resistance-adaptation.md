# Nightfall combo resistance adaptation

This is a user-requested item adaptation rather than original reward-item
behavior.

- Every active Nightfall mode uses the shared `NightfallAbilityState`, so the
  caster receives Resistance V (effect amplifier 4) from successful activation
  through normal completion or cancellation.
- The server refreshes a six-tick effect while the ability state is active.
  Failed activation and cooldown rejection do not grant resistance.
- An existing resistance effect is serialized before activation. When the
  Nightfall-owned effect ends, Minecraft's hidden prior effect is restored
  first; the serialized copy, reduced by elapsed ability ticks, is the fallback.
- A stronger or longer resistance effect supplied by another source during the
  combo is left untouched during cleanup.
- Death and external cleansing do not resurrect the saved pre-combo effect.
