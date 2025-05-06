package pacman;

import java.io.InputStream;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import pacman.PacManModel.CellValue;

public class PacManView extends Group {
    public final static double CELL_WIDTH = 20.0;

    @FXML private int rowCount;
    @FXML private int columnCount;
    private ImageView[][] cellViews;
    private Image pacmanRightImage;
    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image ghost1Image;
    private Image ghost2Image;
    private Image blueGhostImage;
    private Image wallImage;
    private Image bigDotImage;
    private Image smallDotImage;

    public PacManView() {
        try {
            this.pacmanRightImage = loadImageResource("/pacman/res/pacmanRight.gif");
            this.pacmanUpImage = loadImageResource("/pacman/res/pacmanUp.gif");
            this.pacmanDownImage = loadImageResource("/pacman/res/pacmanDown.gif");
            this.pacmanLeftImage = loadImageResource("/pacman/res/pacmanLeft.gif");
            this.ghost1Image = loadImageResource("/pacman/res/redghost.gif");
            this.ghost2Image = loadImageResource("/pacman/res/ghost2.gif");
            this.blueGhostImage = loadImageResource("/pacman/res/blueghost.gif");
            this.wallImage = loadImageResource("/pacman/res/wall.png");
            this.bigDotImage = loadImageResource("/pacman/res/whitedot.png");
            this.smallDotImage = loadImageResource("/pacman/res/smalldot.png");
        } catch (Exception e) {
            System.err.println("Error loading game resources:");
            e.printStackTrace();
            // Initialize with blank images to prevent NullPointerException
            initializeBlankImages();
        }
    }

    private Image loadImageResource(String path) {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) {
            System.err.println("Resource not found: " + path);
            return createBlankImage();
        }
        return new Image(is);
    }

    private Image createBlankImage() {
        // Creates a 1x1 transparent pixel
        return new Image(new String(new byte[0]));
    }

    private void initializeBlankImages() {
        Image blank = createBlankImage();
        this.pacmanRightImage = blank;
        this.pacmanUpImage = blank;
        this.pacmanDownImage = blank;
        this.pacmanLeftImage = blank;
        this.ghost1Image = blank;
        this.ghost2Image = blank;
        this.blueGhostImage = blank;
        this.wallImage = blank;
        this.bigDotImage = blank;
        this.smallDotImage = blank;
    }
    private void initializeGrid() {
        if (this.rowCount > 0 && this.columnCount > 0) {
            this.cellViews = new ImageView[this.rowCount][this.columnCount];
            for (int row = 0; row < this.rowCount; row++) {
                for (int column = 0; column < this.columnCount; column++) {
                    ImageView imageView = new ImageView();
                    imageView.setX((double)column * CELL_WIDTH);
                    imageView.setY((double)row * CELL_WIDTH);
                    imageView.setFitWidth(CELL_WIDTH);
                    imageView.setFitHeight(CELL_WIDTH);
                    this.cellViews[row][column] = imageView;
                    this.getChildren().add(imageView);
                }
            }
        }
    }

    public void update(PacManModel model) {
        assert model.getRowCount() == this.rowCount && model.getColumnCount() == this.columnCount;
        
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                CellValue value = model.getCellValue(row, column);
                if (value == CellValue.WALL) {
                    this.cellViews[row][column].setImage(this.wallImage);
                }
                else if (value == CellValue.BIGDOT) {
                    this.cellViews[row][column].setImage(this.bigDotImage);
                }
                else if (value == CellValue.SMALLDOT) {
                    this.cellViews[row][column].setImage(this.smallDotImage);
                }
                else {
                    this.cellViews[row][column].setImage(null);
                }

                if (row == model.getPacmanLocation().getX() && column == model.getPacmanLocation().getY()) {
                    switch (PacManModel.getLastDirection()) {
                        case RIGHT, NONE -> this.cellViews[row][column].setImage(this.pacmanRightImage);
                        case LEFT -> this.cellViews[row][column].setImage(this.pacmanLeftImage);
                        case UP -> this.cellViews[row][column].setImage(this.pacmanUpImage);
                        case DOWN -> this.cellViews[row][column].setImage(this.pacmanDownImage);
                    }
                }

                handleGhostDisplay(model, row, column);
            }
        }
    }

    private void handleGhostDisplay(PacManModel model, int row, int column) {
        if (PacManModel.isGhostEatingMode()) {
            if (Controller.getGhostEatingModeCounter() == 6 || 
                Controller.getGhostEatingModeCounter() == 4 || 
                Controller.getGhostEatingModeCounter() == 2) {
                setGhostImage(model, row, column, this.ghost1Image, this.ghost2Image);
            } else {
                setGhostImage(model, row, column, this.blueGhostImage, this.blueGhostImage);
            }
        } else {
            setGhostImage(model, row, column, this.ghost1Image, this.ghost2Image);
        }
    }

    private void setGhostImage(PacManModel model, int row, int column, Image ghost1Img, Image ghost2Img) {
        if (row == model.getGhost1Location().getX() && column == model.getGhost1Location().getY()) {
            this.cellViews[row][column].setImage(ghost1Img);
        }
        if (row == model.getGhost2Location().getX() && column == model.getGhost2Location().getY()) {
            this.cellViews[row][column].setImage(ghost2Img);
        }
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
        this.initializeGrid();
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        this.initializeGrid();
    }
}