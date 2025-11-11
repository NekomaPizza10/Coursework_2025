package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.*;

public class SimpleBoard implements Board {

    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private Point currentOffset;
    private final Score score;

    //Hold functionality fields
    private Brick heldBrick = null;         //Store the held brick
    private boolean hasUsedHold = false;

    // Prevent multiple holds per piece
    public SimpleBoard(int width, int height) {
        System.out.println("SimpleBoard creating matrix " + 20 + " × " + 10);
        this.width = width;     //10
        this.height = height;   //20

        // DEBUG: Print the dimensions BEFORE accessing the matrix
        System.out.println("=== BOARD DIMENSIONS ===");
        System.out.println("Constructor received - width: " + width + ", height: " + height);
        System.out.println("Matrix will be created as: currentGameMatrix[" + width + "][" + height + "]");

        currentGameMatrix = new int[20][10];     //NOTE:[rows][cols]
        currentOffset = new Point (0,0); // temp safe default

        // Now we can safely access it
        System.out.println("Matrix actual dimensions: " + currentGameMatrix.length + " rows x " +
                (currentGameMatrix.length > 0 ? currentGameMatrix[0].length : 0) + " columns");
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();
        score = new Score();
    }

    @Override
    public boolean moveBrickDown() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(0, 1);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            int brickHeight = brickRotator.getCurrentShape().length;
            int maxY = 20 - brickHeight;  //Snap to bottom row

            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }


    @Override
    public boolean moveBrickLeft() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(-1, 0);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean moveBrickRight() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(1, 0);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean rotateLeftBrick() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();
        boolean conflict = MatrixOperations.intersect(currentMatrix, nextShape.getShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
        if (conflict) {
            return false;
        } else {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
    }

    @Override
    public boolean createNewBrick() {
        Brick currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);
        int brickHeight = brickRotator.getCurrentShape()[0].length;
        if(brickHeight > 20){
            //Brick too tall - clamp to bottom
            currentOffset.y = 20 - brickHeight;
        } else{
            currentOffset.y =0;

            int brickWidth = brickRotator.getCurrentShape()[0].length;
            currentOffset.x = (width - brickWidth) / 2;

            System.out.println("=== NEW BRICK CREATED ===");
            System.out.println("CENTRE CHECK: brickWidth=" + brickWidth + "  x=" + currentOffset.x);
        }

        System.out.println("=== NEW BRICK CREATED ===");
        System.out.println("Starting position: X=" + currentOffset.x + ", Y=" + currentOffset.y);

        // Reset hold flag when new brick is created
        hasUsedHold = false;
        return MatrixOperations.intersect(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    @Override
    public ViewData getViewData() {
        return new ViewData(brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY(), brickGenerator.getNextBrick().getShapeMatrix().get(0));
    }

    @Override
    public void mergeBrickToBackground() {
        int brickHeight = brickRotator.getCurrentShape().length;
        int maxY = 20 -brickHeight;
        if (currentOffset.y > maxY){
            currentOffset.y = maxY;
        }
        currentGameMatrix = MatrixOperations.merge(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        return clearRow;

    }

    @Override
    public Score getScore() {
        return score;
    }


    @Override
    public void newGame() {
        currentGameMatrix = new int[20][10];
        score.reset();
        heldBrick = null;       // Clear held brick
        hasUsedHold = false;    // Reset hold flag
        createNewBrick();
    }

    // Hold current brick
    @Override
    public boolean holdCurrentBrick() {
        // Can't hold if already used hold for this piece
        if (hasUsedHold){
            return false;
        }

        //Get current brick before swapping
        Brick currentBrick = brickRotator.getBrick();
        //Reset the shape index to 0
        brickRotator.setCurrentShape(0);

        if (heldBrick == null){
            // FIRST time holding - store current brick and spawn new one
            heldBrick = currentBrick;
            createNewBrick();
        }
        else{
            // Swap current brick with held brick
            brickRotator.setBrick(heldBrick);
            heldBrick = currentBrick;

            // Reset position to top center
            currentOffset = new Point(3, 0);

            // Check if swapped brick can be placed
            if (MatrixOperations.intersect(currentGameMatrix, brickRotator.getCurrentShape(),
                    (int) currentOffset.getX(), (int) currentOffset.getY())) {
                // Can't place swapped brick - revert
                heldBrick = brickRotator.getBrick();
                brickRotator.setBrick(currentBrick);
                return false;
            }
        }

        // Mark that hold has been used for this piece
        hasUsedHold = true;
        return true;
    }

    //Get held brick data for preview
    @Override
    public int[][] getHeldBrickData() {
        if(heldBrick == null){
            return null;
        }
        return heldBrick.getShapeMatrix().get(0);
    }
}
