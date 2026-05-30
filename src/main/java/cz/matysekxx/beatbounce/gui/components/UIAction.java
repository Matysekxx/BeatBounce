package cz.matysekxx.beatbounce.gui.components;

/**
 * Represents the set of possible actions that can be triggered from the game's UI,
 * such as in the pause menu or revive screen.
 *
 * @author Matysekxx
 */
public enum UIAction {
    /**
     * No action.
     */
    NONE,
    /**
     * Resume the game from a paused state.
     */
    RESUME,
    /**
     * Restart the current level.
     */
    RESTART,
    /**
     * Quit the current game and return to the main menu.
     */
    QUIT,
    /**
     * Use a revive to continue playing after falling.
     */
    REVIVE,
    /**
     * Decline the option to revive and end the game.
     */
    DECLINE_REVIVE
}
