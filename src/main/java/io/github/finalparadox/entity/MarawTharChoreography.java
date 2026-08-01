package io.github.finalparadox.entity;

/**
 * Exact B9 phase/step dispatch reconstructed from the source datapack.
 *
 * <p>The step value mirrors the original {@code fase_aux danom} score. Keeping
 * that score model is important because an opening normally advances by two,
 * while the first hit during that opening subtracts one and conditionally
 * exposes the intervening Begone step.</p>
 */
final class MarawTharChoreography {
    enum Action {
        NONE,
        PUT_SWORD,
        RANDOM_PHASE_ONE,
        RANDOM_PHASE_TWO,
        OPENING,
        BEGONE,
        DARKNESS,
        JUDGMENT,
        INTERMISSION,
        ENRAGE,
        SWORDS_PHASE_TWO,
        LASERS_PHASE_TWO
    }

    private MarawTharChoreography() {
    }

    static Action actionFor(int phase, int step) {
        return switch (phase) {
            case 1 -> phaseOneOrTwo(step, Action.RANDOM_PHASE_ONE);
            case 2 -> phaseOneOrTwo(step, Action.RANDOM_PHASE_TWO);
            case 3 -> phaseThree(step);
            default -> Action.NONE;
        };
    }

    static int advanceAtStart(Action action) {
        return switch (action) {
            case PUT_SWORD, RANDOM_PHASE_ONE, RANDOM_PHASE_TWO,
                    BEGONE, DARKNESS, JUDGMENT -> 1;
            case OPENING -> 2;
            default -> 0;
        };
    }

    private static Action phaseOneOrTwo(int step, Action randomAction) {
        if (step == 0 || step == 10) {
            return Action.PUT_SWORD;
        }
        if (step == 1 || step == 5 || step == 11) {
            return randomAction;
        }
        if (step == 2 || step == 6 || step == 12 || step == 16) {
            return Action.OPENING;
        }
        if (step == 3 || step == 7 || step == 13) {
            return Action.BEGONE;
        }
        if (step == 4 || step == 9 || step == 15) {
            return Action.DARKNESS;
        }
        if (step == 8 || step == 14) {
            return Action.JUDGMENT;
        }
        return step >= 17 && step <= 100 ? Action.INTERMISSION : Action.NONE;
    }

    private static Action phaseThree(int step) {
        return switch (step) {
            case 0 -> Action.PUT_SWORD;
            case 1 -> Action.RANDOM_PHASE_ONE;
            case 2, 6, 10 -> Action.OPENING;
            case 3, 7 -> Action.BEGONE;
            case 4, 9 -> Action.DARKNESS;
            case 5 -> Action.RANDOM_PHASE_TWO;
            case 8 -> Action.JUDGMENT;
            default -> step >= 11 ? Action.ENRAGE : Action.NONE;
        };
    }
}
