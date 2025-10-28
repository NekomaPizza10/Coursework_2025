package com.comp2042;

public interface InputEventListener {

    DownData onDownEvent(MoveEvent event);

    ViewData onLeftEvent(MoveEvent event);

    ViewData onRightEvent(MoveEvent event);

    ViewData onRotateEvent(MoveEvent event);

    void createNewGame();

    // Hold event
    /**
     * Called when player presses hold Key
     * @return true if hold was successful
     */
    boolean onHoldEvent();

}
