package com.comp2042;

public interface Board {

    boolean moveBrickDown();

    boolean moveBrickLeft();

    boolean moveBrickRight();

    boolean rotateLeftBrick();

    boolean createNewBrick();

    int[][] getBoardMatrix();

    ViewData getViewData();

    void mergeBrickToBackground();

    ClearRow clearRows();

    Score getScore();

    void newGame();

    // HOLD Functionality
    /**
     * Swap current brick with the hold brick
     * @return true if hold was successful
     */
    boolean holdCurrentBrick();

    /**
     * Get the currently hold brick data for preview
     * @return 2D array of held brick shape, or null if no brick is held
     */
    int[][] getHeldBrickData();

}
