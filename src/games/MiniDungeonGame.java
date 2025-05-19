package games;

import globalFunc.Sound_Func;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.event.EventHandler;

public class MiniDungeonGame extends Application {
    // Game constants
    private static final int BASE_WIDTH = 15;
    private static final int BASE_HEIGHT = 15;
    private static final double TILE_SIZE = 40.0;
    private static final double GAME_SPEED = 0.3; // seconds per update

    // Game state
    private Dungeon dungeon;
    private Player player;
    private GameView gameView;
    private boolean isGameRunning = false;
    private Timeline gameLoop;
    private int currentLevel = 1;
    private int maxLevel = 5;
    private Stage primaryStage;
    private Scene gameScene;
    private boolean inCombat = false;
    private Enemy combatEnemy = null;
    private CombatSystem combatSystem;
    private ItemInventory inventory;
    private List<String> gameMessages = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        inventory = new ItemInventory();

        startNewLevel(currentLevel);

        // Set the stage
        primaryStage.setTitle("Mini Dungeon Game - Level " + currentLevel);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Play background music
        Sound_Func.playBackgroundSong();
    }

    private void startNewLevel(int level) {
        // Create dungeon with increasing size and difficulty
        int width = BASE_WIDTH + (level - 1) * 2;
        int height = BASE_HEIGHT + (level - 1) * 2;
        int treasures = 5 + level * 2;
        int enemies = 3 + level * 2;
        int traps = level * 2;
        int potions = level;
        int weapons = Math.max(1, level / 2);

        dungeon = new Dungeon(width, height, treasures, enemies, traps, potions, weapons, level);
        player = new Player(1, 1, dungeon, inventory);

        if (inventory.hasItems()) {
            // Transfer items from previous level
            player.setStats(player.getMaxHealth(), player.getAttack(), player.getDefense());
        } else {
            // First level setup
            inventory.addItem(new Item("Wooden Sword", ItemType.WEAPON, 3));
            inventory.addItem(new Item("Basic Armor", ItemType.ARMOR, 2));
        }

        combatSystem = new CombatSystem(player);
        gameView = new GameView(dungeon, player, inventory);

        // Update game scene
        BorderPane root = new BorderPane();
        root.setCenter(gameView);

        // Create sidebar for inventory
        VBox sidebar = createSidebar();
        root.setRight(sidebar);

        gameScene = new Scene(root, width * TILE_SIZE + 250, height * TILE_SIZE);
        primaryStage.setScene(gameScene);

        startGame(gameScene);

        // Add a welcome message
        addGameMessage("Welcome to level " + level + "! Find all treasures to advance.");
        if (level > 1) {
            addGameMessage("Enemies are stronger on this level!");
        }
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setStyle("-fx-background-color: #333333; -fx-padding: 10;");
        sidebar.setPrefWidth(250);

        // Add inventory title
        Text inventoryTitle = new Text("Inventory");
        inventoryTitle.setFont(Font.font(18));
        inventoryTitle.setFill(Color.WHITE);

        // Add inventory items display
        Text inventoryText = new Text();
        inventoryText.setFill(Color.LIGHTGRAY);
        inventory.setDisplayText(inventoryText);
        inventory.updateDisplay();

        // Add controls info
        Text controlsTitle = new Text("\nControls");
        controlsTitle.setFont(Font.font(18));
        controlsTitle.setFill(Color.WHITE);

        Text controlsText = new Text(
                "WASD / Arrow Keys - Move\n" +
                        "E - Attack enemy\n" +
                        "1-9 - Use item\n" +
                        "Space - Use potion\n" +
                        "I - Toggle inventory\n" +
                        "Esc - Pause game"
        );
        controlsText.setFill(Color.LIGHTGRAY);

        // Add message log
        Text messagesTitle = new Text("\nMessages");
        messagesTitle.setFont(Font.font(18));
        messagesTitle.setFill(Color.WHITE);

        Text messagesText = new Text();
        messagesText.setFill(Color.LIGHTGRAY);

        // Add all components to sidebar
        sidebar.getChildren().addAll(
                inventoryTitle,
                inventoryText,
                controlsTitle,
                controlsText,
                messagesTitle,
                messagesText
        );

        // Set messages display
        sidebar.widthProperty().addListener((obs, oldVal, newVal) -> {
            messagesText.setWrappingWidth(newVal.doubleValue() - 20);
        });

        // Update messages when they change
        Timeline messageUpdater = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> {
                    StringBuilder sb = new StringBuilder();
                    int count = Math.min(gameMessages.size(), 5); // Show last 5 messages
                    for (int i = gameMessages.size() - count; i < gameMessages.size(); i++) {
                        sb.append(gameMessages.get(i)).append("\n");
                    }
                    messagesText.setText(sb.toString());
                })
        );
        messageUpdater.setCycleCount(Timeline.INDEFINITE);
        messageUpdater.play();

        return sidebar;
    }

    private void addGameMessage(String message) {
        gameMessages.add(message);
        if (gameMessages.size() > 20) { // Limit the number of stored messages
            gameMessages.remove(0);
        }
    }

    private void startGame(Scene scene) {
        isGameRunning = true;
        inCombat = false;

        // Setup game loop
        gameLoop = new Timeline(new KeyFrame(Duration.seconds(GAME_SPEED), e -> updateGame()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();

        scene.setOnKeyPressed(this::handleKeyPress);
    }

    private void updateGame() {
        if (!isGameRunning) return;

        if (!inCombat) {
            dungeon.update();

            // Check for enemy collision after movement
            checkEnemyCollision();

            // Check for special tile interactions
            checkSpecialTiles();

            gameView.update();

            // Check game state
            if (player.getHealth() <= 0) {
                endGame(false);
            }

            if (dungeon.getTreasureCount() == 0) {
                completeLevel();
            }
        }
    }

    private void checkEnemyCollision() {
        Enemy enemy = dungeon.getEnemyAt(player.getX(), player.getY());
        if (enemy != null) {
            // Initiate combat
            startCombat(enemy);
        }
    }

    private void checkSpecialTiles() {
        char tile = dungeon.getTile(player.getX(), player.getY());

        switch (tile) {
            case 'T': // Treasure
                Sound_Func.playEatingSound();
                player.addScore(10 * currentLevel);
                dungeon.setTile(player.getX(), player.getY(), '.');
                dungeon.collectTreasure();
                addGameMessage("You found a treasure! " + dungeon.getTreasureCount() + " left.");
                break;

            case 'P': // Potion
                Sound_Func.playEatingSound();
                int healthBoost = 20 + currentLevel * 5;
                player.heal(healthBoost);
                dungeon.setTile(player.getX(), player.getY(), '.');
                addGameMessage("You found a health potion! +" + healthBoost + " HP");
                break;

            case 'X': // Trap
                Sound_Func.playDefeatSound();
                int trapDamage = 5 + currentLevel * 3;
                player.takeDamage(trapDamage);
                dungeon.setTile(player.getX(), player.getY(), '.');
                addGameMessage("You triggered a trap! -" + trapDamage + " HP");
                break;

            case 'W': // Weapon
                Item weapon = dungeon.getWeaponAt(player.getX(), player.getY());
                if (weapon != null) {
                    Sound_Func.playEatingSound();
                    inventory.addItem(weapon);
                    player.equipBestItems();
                    dungeon.setTile(player.getX(), player.getY(), '.');
                    dungeon.removeItemAt(player.getX(), player.getY());
                    addGameMessage("You found: " + weapon.getName() + "!");
                }
                break;

            case 'A': // Armor
                Item armor = dungeon.getArmorAt(player.getX(), player.getY());
                if (armor != null) {
                    Sound_Func.playEatingSound();
                    inventory.addItem(armor);
                    player.equipBestItems();
                    dungeon.setTile(player.getX(), player.getY(), '.');
                    dungeon.removeItemAt(player.getX(), player.getY());
                    addGameMessage("You found: " + armor.getName() + "!");
                }
                break;

            case 'S': // Stairs to next level (appears when all treasures are collected)
                if (dungeon.getTreasureCount() == 0) {
                    completeLevel();
                }
                break;
        }
    }

    private void startCombat(Enemy enemy) {
        inCombat = true;
        combatEnemy = enemy;
        gameView.showCombatScreen(enemy);
        addGameMessage("Combat started with " + enemy.getName() + "!");
    }

    private void processCombatAction(CombatAction action) {
        if (combatEnemy == null) return;

        CombatResult result = combatSystem.processCombatRound(combatEnemy, action);

        // Process player's action first
        switch (action) {
            case ATTACK:
                addGameMessage("You attack for " + result.playerDamage + " damage!");
                break;
            case DEFEND:
                addGameMessage("You defend and gain +" + result.playerDefenseBonus + " defense!");
                break;
            case USE_ITEM:
                addGameMessage("You used a potion and healed for " + result.playerHealing + " HP!");
                break;
            case FLEE:
                if (result.fled) {
                    addGameMessage("You managed to escape!");
                    endCombat(true);
                    return;
                } else {
                    addGameMessage("Failed to escape!");
                }
                break;
        }

        // Process enemy's action
        if (result.enemyAction == CombatAction.ATTACK) {
            addGameMessage(combatEnemy.getName() + " attacks for " + result.enemyDamage + " damage!");
        } else if (result.enemyAction == CombatAction.DEFEND) {
            addGameMessage(combatEnemy.getName() + " defends!");
        }

        // Update combat display
        gameView.updateCombatDisplay(player, combatEnemy);

        // Check for end of combat
        if (combatEnemy.getHealth() <= 0) {
            int expGained = combatEnemy.getLevel() * 10;
            int goldGained = combatEnemy.getLevel() * 5;
            player.addExperience(expGained);
            player.addGold(goldGained);
            addGameMessage("You defeated " + combatEnemy.getName() + "!");
            addGameMessage("Gained " + expGained + " EXP and " + goldGained + " gold!");

            // Check for level up
            if (player.checkLevelUp()) {
                Sound_Func.playVictorySound();
                addGameMessage("Level up! You are now level " + player.getLevel() + "!");
                addGameMessage("HP +20, Attack +2, Defense +1");
            }

            // Remove enemy from dungeon
            dungeon.removeEnemy(combatEnemy);
            endCombat(true);

        } else if (player.getHealth() <= 0) {
            endGame(false);
        }
    }

    private void endCombat(boolean victory) {
        inCombat = false;
        combatEnemy = null;
        gameView.hideCombatScreen();

        if (victory) {
            // Play victory sound
            Sound_Func.playEatingSound(); // Use as victory sound
        }
    }

    private void completeLevel() {
        if (currentLevel < maxLevel) {
            // Show level completion screen
            gameView.showLevelComplete(currentLevel);
            Sound_Func.playVictorySound();

            // Stop the game loop
            gameLoop.stop();
            isGameRunning = false;

            // Set up handler for next level
            gameScene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.SPACE) {
                    currentLevel++;
                    startNewLevel(currentLevel);
                }
            });

        } else {
            // Player has completed all levels
            endGame(true);
        }
    }

    private void endGame(boolean victory) {
        isGameRunning = false;
        gameLoop.stop();

        if (victory) {
            Sound_Func.playVictorySound();
            gameView.showGameComplete(player.getScore(), currentLevel);
        } else {
            Sound_Func.playDefeatSound();
            gameView.showGameOver(player.getScore(), currentLevel);
        }
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();

        // Global key handling
        if (code == KeyCode.ESCAPE) {
            pauseGame();
            return;
        }

        if (!isGameRunning) {
            if (code == KeyCode.SPACE) {
                resetGame();
            }
            return;
        }

        if (inCombat) {
            handleCombatKeyPress(code);
            return;
        }

        // Normal movement and actions
        switch (code) {
            case W, UP -> player.moveUp();
            case S, DOWN -> player.moveDown();
            case A, LEFT -> player.moveLeft();
            case D, RIGHT -> player.moveRight();
            case SPACE -> player.useHealthPotion();
            case I -> inventory.toggleView();
            case DIGIT1, DIGIT2, DIGIT3, DIGIT4, DIGIT5, DIGIT6, DIGIT7, DIGIT8, DIGIT9 -> {
                int index = code.toString().charAt(5) - '1'; // Convert DIGIT1-9 to 0-8 index
                if (index >= 0 && index < inventory.getSize()) {
                    inventory.useItem(index);
                }
            }
        }

        gameView.update();
    }

    private void handleCombatKeyPress(KeyCode code) {
        switch (code) {
            case A, LEFT -> processCombatAction(CombatAction.ATTACK);
            case D, RIGHT -> processCombatAction(CombatAction.DEFEND);
            case S, DOWN -> processCombatAction(CombatAction.USE_ITEM);
            case F, UP -> processCombatAction(CombatAction.FLEE);
        }
    }

    private void pauseGame() {
        if (isGameRunning) {
            isGameRunning = false;
            gameLoop.pause();
            gameView.showPauseScreen();

            // Create a named event handler we can reference later for removal
            EventHandler<KeyEvent> resumeHandler = new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE && !isGameRunning && !inCombat) {
                        isGameRunning = true;
                        gameLoop.play();
                        gameView.hidePauseScreen();
                        // Remove this handler once used
                        gameScene.removeEventFilter(KeyEvent.KEY_PRESSED, this);
                    }
                }
            };

            // Add the event filter to the scene
            gameScene.addEventFilter(KeyEvent.KEY_PRESSED, resumeHandler);
        }
    }

    private void resetGame() {
        currentLevel = 1;
        inventory = new ItemInventory();
        startNewLevel(currentLevel);
        isGameRunning = true;
        gameLoop.play();
    }

    // -------- ENUM CLASSES --------

    enum CombatAction {
        ATTACK, DEFEND, USE_ITEM, FLEE
    }

    enum ItemType {
        WEAPON, ARMOR, POTION, MISC
    }

    // -------- INNER CLASSES --------

    private class CombatSystem {
        private Player player;
        private Random random = new Random();

        public CombatSystem(Player player) {
            this.player = player;
        }

        public CombatResult processCombatRound(Enemy enemy, CombatAction playerAction) {
            CombatResult result = new CombatResult();

            // Process player action
            switch (playerAction) {
                case ATTACK:
                    result.playerDamage = calculateDamage(player.getAttack(), enemy.getDefense());
                    enemy.takeDamage(result.playerDamage);
                    break;

                case DEFEND:
                    result.playerDefenseBonus = player.getDefense() / 2;
                    player.addTemporaryDefense(result.playerDefenseBonus);
                    break;

                case USE_ITEM:
                    if (player.useHealthPotion()) {
                        result.playerHealing = 20 + player.getLevel() * 5;
                    }
                    break;

                case FLEE:
                    // 40% chance to flee, reduced by enemy level
                    int fleeChance = 40 - enemy.getLevel() * 5;
                    fleeChance = Math.max(10, fleeChance); // Minimum 10% chance
                    result.fled = random.nextInt(100) < fleeChance;
                    break;
            }

            // Process enemy action (simple AI)
            if (enemy.getHealth() < enemy.getMaxHealth() / 3 && random.nextInt(100) < 40) {
                // Low health - defend
                result.enemyAction = CombatAction.DEFEND;
                enemy.addTemporaryDefense(enemy.getDefense() / 2);
            } else {
                // Attack
                result.enemyAction = CombatAction.ATTACK;
                result.enemyDamage = calculateDamage(enemy.getAttack(), player.getDefense());
                player.takeDamage(result.enemyDamage);
            }

            return result;
        }

        private int calculateDamage(int attack, int defense) {
            int baseDamage = attack - defense / 2;
            int variance = Math.max(1, baseDamage / 4);
            int finalDamage = baseDamage + random.nextInt(variance * 2 + 1) - variance;
            return Math.max(1, finalDamage); // Minimum 1 damage
        }
    }

    private class CombatResult {
        public int playerDamage = 0;
        public int playerDefenseBonus = 0;
        public int playerHealing = 0;
        public int enemyDamage = 0;
        public CombatAction enemyAction = CombatAction.ATTACK;
        public boolean fled = false;
    }

    private class Item {
        private String name;
        private ItemType type;
        private int value;

        public Item(String name, ItemType type, int value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        public String getName() { return name; }
        public ItemType getType() { return type; }
        public int getValue() { return value; }

        @Override
        public String toString() {
            String stat = type == ItemType.WEAPON ? "ATK" :
                    type == ItemType.ARMOR ? "DEF" :
                            type == ItemType.POTION ? "HP" : "";
            return name + (stat.isEmpty() ? "" : " (" + stat + "+" + value + ")");
        }
    }

    private class ItemInventory {
        private List<Item> items = new ArrayList<>();
        private Text displayText;
        private boolean isViewExpanded = false;

        public void addItem(Item item) {
            items.add(item);
            updateDisplay();
        }

        public int getSize() {
            return items.size();
        }

        public boolean hasItems() {
            return !items.isEmpty();
        }

        public List<Item> getWeapons() {
            return items.stream().filter(i -> i.getType() == ItemType.WEAPON).toList();
        }

        public List<Item> getArmor() {
            return items.stream().filter(i -> i.getType() == ItemType.ARMOR).toList();
        }

        public List<Item> getPotions() {
            return items.stream().filter(i -> i.getType() == ItemType.POTION).toList();
        }

        public void useItem(int index) {
            if (index < 0 || index >= items.size()) return;

            Item item = items.get(index);
            if (item.getType() == ItemType.POTION) {
                player.heal(item.getValue());
                items.remove(index);
                addGameMessage("Used " + item.getName() + " and healed " + item.getValue() + " HP");
            } else if (item.getType() == ItemType.WEAPON || item.getType() == ItemType.ARMOR) {
                player.equipItem(item);
                addGameMessage("Equipped " + item.getName());
            }

            updateDisplay();
        }

        public void setDisplayText(Text text) {
            this.displayText = text;
        }

        public void toggleView() {
            isViewExpanded = !isViewExpanded;
            updateDisplay();
        }

        public void updateDisplay() {
            if (displayText == null) return;

            StringBuilder sb = new StringBuilder();
            if (!isViewExpanded) {
                // Compact view
                sb.append("Items: ").append(items.size()).append("\n");
                sb.append("Press I to see all items\n");

                // Show equipped items
                Item bestWeapon = player.getEquippedWeapon();
                Item bestArmor = player.getEquippedArmor();

                sb.append("\nEquipped:\n");
                sb.append("- ").append(bestWeapon != null ? bestWeapon.toString() : "No weapon").append("\n");
                sb.append("- ").append(bestArmor != null ? bestArmor.toString() : "No armor").append("\n");

                // Show potions count
                int potionCount = (int) items.stream().filter(i -> i.getType() == ItemType.POTION).count();
                sb.append("\nPotions: ").append(potionCount);

            } else {
                // Expanded view
                sb.append("Inventory (press I to collapse):\n");

                if (items.isEmpty()) {
                    sb.append("No items");
                } else {
                    for (int i = 0; i < items.size(); i++) {
                        sb.append(i + 1).append(". ").append(items.get(i)).append("\n");
                    }
                    sb.append("\nPress 1-9 to use/equip an item");
                }
            }

            displayText.setText(sb.toString());
        }
    }

    private class Dungeon {
        private int width, height;
        private char[][] grid;
        private List<Enemy> enemies;
        private List<Item> items;
        private int treasureCount;
        private Random random = new Random();
        private int dungeonLevel;

        public Dungeon(int width, int height, int treasures, int enemyCount,
                       int trapCount, int potionCount, int weaponCount, int level) {
            this.width = width;
            this.height = height;
            this.dungeonLevel = level;
            grid = new char[width][height];
            enemies = new ArrayList<>();
            items = new ArrayList<>();
            treasureCount = treasures;

            generateDungeon(trapCount, potionCount, weaponCount);
        }

        private void generateDungeon(int trapCount, int potionCount, int weaponCount) {
            // Generate walls and floors
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    // Create outer walls and some random walls
                    if (i == 0 || j == 0 || i == width - 1 || j == height - 1 ||
                            (random.nextInt(100) < 15 && !(i == 1 && j == 1))) {
                        grid[i][j] = '#';
                    } else {
                        grid[i][j] = '.';
                    }
                }
            }

            // Ensure the player starting position is clear
            grid[1][1] = '.';

            // Create rooms and corridors
            createRooms();

            // Place treasures
            for (int i = 0; i < treasureCount; i++) {
                placeRandomTile('T');
            }

            // Place enemies
            String[] enemyNames = {"Goblin", "Skeleton", "Orc", "Zombie", "Troll", "Vampire", "Demon"};
            for (int i = 0; i < dungeonLevel + 2; i++) {
                int x, y;
                do {
                    x = random.nextInt(width - 2) + 1;
                    y = random.nextInt(height - 2) + 1;
                } while (grid[x][y] != '.' || (x == 1 && y == 1));

                String enemyName = enemyNames[random.nextInt(enemyNames.length)];
                int enemyLevel = Math.max(1, dungeonLevel - 1 + random.nextInt(3));
                Enemy enemy = new Enemy(x, y, enemyName, enemyLevel);
                enemies.add(enemy);
                grid[x][y] = 'E';
            }

            // Place traps
            for (int i = 0; i < trapCount; i++) {
                placeRandomTile('X');
            }

            // Place potions
            for (int i = 0; i < potionCount; i++) {
                placeRandomTile('P');
            }

            // Place weapons
            String[] weaponNames = {
                    "Iron Sword", "Steel Axe", "Magic Staff", "Enchanted Blade",
                    "Mystic Wand", "Dragon Slayer", "Demon Blade"
            };

            for (int i = 0; i < weaponCount; i++) {
                int x, y;
                do {
                    x = random.nextInt(width - 2) + 1;
                    y = random.nextInt(height - 2) + 1;
                } while (grid[x][y] != '.');

                String weaponName = weaponNames[random.nextInt(weaponNames.length)];
                int weaponValue = 3 + dungeonLevel + random.nextInt(3);
                items.add(new Item(weaponName, ItemType.WEAPON, weaponValue));
                grid[x][y] = 'W';
            }

            // Place armor
            String[] armorNames = {
                    "Leather Armor", "Chain Mail", "Steel Plate", "Enchanted Robe",
                    "Dragon Scale", "Mystic Shield", "Holy Guardian"
            };

            for (int i = 0; i < Math.max(1, weaponCount - 1); i++) {
                int x, y;
                do {
                    x = random.nextInt(width - 2) + 1;
                    y = random.nextInt(height - 2) + 1;
                } while (grid[x][y] != '.');

                String armorName = armorNames[random.nextInt(armorNames.length)];
                int armorValue = 2 + dungeonLevel + random.nextInt(2);
                items.add(new Item(armorName, ItemType.ARMOR, armorValue));
                grid[x][y] = 'A';
            }
        }

        private void createRooms() {
            int numRooms = 3 + random.nextInt(3) + dungeonLevel;

            for (int i = 0; i < numRooms; i++) {
                int roomWidth = 3 + random.nextInt(4);
                int roomHeight = 3 + random.nextInt(4);
                int roomX = random.nextInt(width - roomWidth - 2) + 1;
                int roomY = random.nextInt(height - roomHeight - 2) + 1;

                // Create room
                for (int x = roomX; x < roomX + roomWidth; x++) {
                    for (int y = roomY; y < roomY + roomHeight; y++) {
                        if (x >= 0 && x < width && y >= 0 && y < height) {
                            grid[x][y] = '.';
                        }
                    }
                }

                // Connect rooms with corridors
                if (i > 0) {
                    // Find center of current room and previous room
                    int currentCenterX = roomX + roomWidth / 2;
                    int currentCenterY = roomY + roomHeight / 2;

                    // Create corridor to a random other room center
                    int targetRoomIndex = random.nextInt(i);
                    int targetRoomX = random.nextInt(width - 4) + 2;
                    int targetRoomY = random.nextInt(height - 4) + 2;

                    // Create horizontal corridor
                    int startX = Math.min(currentCenterX, targetRoomX);
                    int endX = Math.max(currentCenterX, targetRoomX);
                    for (int x = startX; x <= endX; x++) {
                        if (x >= 0 && x < width && currentCenterY >= 0 && currentCenterY < height) {
                            grid[x][currentCenterY] = '.';
                        }
                    }

                    // Create vertical corridor
                    int startY = Math.min(currentCenterY, targetRoomY);
                    int endY = Math.max(currentCenterY, targetRoomY);
                    for (int y = startY; y <= endY; y++) {
                        if (targetRoomX >= 0 && targetRoomX < width && y >= 0 && y < height) {
                            grid[targetRoomX][y] = '.';
                        }
                    }
                }
            }
        }

        private void placeRandomTile(char tile) {
            int x, y;
            do {
                x = random.nextInt(width - 2) + 1;
                y = random.nextInt(height - 2) + 1;
            } while (grid[x][y] != '.' || (x == 1 && y == 1));

            grid[x][y] = tile;
        }

        public void update() {
            // Update only visible enemies to improve performance
            for (Enemy enemy : enemies) {
                grid[enemy.getX()][enemy.getY()] = '.';

                // Only move if within player view distance
                if (isWithinViewDistance(enemy.getX(), enemy.getY(), player.getX(), player.getY(), 8)) {
                    // Move toward player sometimes
                    if (random.nextInt(100) < 30) {
                        moveEnemyTowardPlayer(enemy);
                    } else {
                        // Random movement
                        int dir = random.nextInt(4);
                        switch (dir) {
                            case 0 -> enemy.tryMove(0, -1, this);
                            case 1 -> enemy.tryMove(0, 1, this);
                            case 2 -> enemy.tryMove(-1, 0, this);
                            case 3 -> enemy.tryMove(1, 0, this);
                        }
                    }
                }

                grid[enemy.getX()][enemy.getY()] = 'E';
            }

            // If all treasures collected, place stairs
            if (treasureCount == 0) {
                boolean stairsPlaced = false;
                for (int i = 0; i < width && !stairsPlaced; i++) {
                    for (int j = 0; j < height && !stairsPlaced; j++) {
                        if (grid[i][j] == '.' && !(i == player.getX() && j == player.getY())) {
                            grid[i][j] = 'S';
                            stairsPlaced = true;
                        }
                    }
                }
            }
        }

        private boolean isWithinViewDistance(int x1, int y1, int x2, int y2, int distance) {
            int dx = Math.abs(x1 - x2);
            int dy = Math.abs(y1 - y2);
            return dx <= distance && dy <= distance;
        }

        private void moveEnemyTowardPlayer(Enemy enemy) {
            int dx = player.getX() - enemy.getX();
            int dy = player.getY() - enemy.getY();

            // Move horizontally or vertically toward player
            if (Math.abs(dx) > Math.abs(dy)) {
                // Move horizontally
                enemy.tryMove(Integer.compare(dx, 0), 0, this);
            } else {
                // Move vertically
                enemy.tryMove(0, Integer.compare(dy, 0), this);
            }
        }

        public char getTile(int x, int y) {
            if (x < 0 || y < 0 || x >= width || y >= height) return '#';
            return grid[x][y];
        }

        public void setTile(int x, int y, char tile) {
            if (x >= 0 && y >= 0 && x < width && y < height) {
                grid[x][y] = tile;
            }
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getTreasureCount() { return treasureCount; }
        public void collectTreasure() { treasureCount--; }

        public Enemy getEnemyAt(int x, int y) {
            for (Enemy enemy : enemies) {
                if (enemy.getX() == x && enemy.getY() == y) {
                    return enemy;
                }
            }
            return null;
        }

        public void removeEnemy(Enemy enemy) {
            enemies.remove(enemy);
            grid[enemy.getX()][enemy.getY()] = '.';
        }

        public Item getWeaponAt(int x, int y) {
            for (Item item : items) {
                if (item.getType() == ItemType.WEAPON) {
                    // Check if this is the item at the location
                    int itemIndex = items.indexOf(item);
                    if (itemIndex != -1 && items.get(itemIndex) == item) {
                        return item;
                    }
                }
            }
            return null;
        }

        public Item getArmorAt(int x, int y) {
            for (Item item : items) {
                if (item.getType() == ItemType.ARMOR) {
                    // Check if this is the item at the location
                    int itemIndex = items.indexOf(item);
                    if (itemIndex != -1 && items.get(itemIndex) == item) {
                        return item;
                    }
                }
            }
            return null;
        }

        public void removeItemAt(int x, int y) {
            // Remove items at this location
            items.removeIf(item -> {
                boolean isAtLocation = false;
                // Cannot directly check location, so we check indirectly
                // by seeing if there's an item of the same type at this position
                char tileType = grid[x][y];
                if ((tileType == 'W' && item.getType() == ItemType.WEAPON) ||
                        (tileType == 'A' && item.getType() == ItemType.ARMOR)) {
                    isAtLocation = true;
                }
                return isAtLocation;
            });
        }
    }

    private abstract class Entity {
        protected int x, y;
        protected int health, maxHealth;
        protected int attack, defense;
        protected int temporaryDefense;

        public Entity(int x, int y, int health, int attack, int defense) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.maxHealth = health;
            this.attack = attack;
            this.defense = defense;
            this.temporaryDefense = 0;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getHealth() { return health; }
        public int getMaxHealth() { return maxHealth; }
        public int getAttack() { return attack; }
        public int getDefense() { return defense + temporaryDefense; }

        public void takeDamage(int damage) {
            health -= damage;
            if (health < 0) health = 0;
        }

        public void heal(int amount) {
            health += amount;
            if (health > maxHealth) health = maxHealth;
        }

        public void addTemporaryDefense(int amount) {
            temporaryDefense += amount;
        }

        protected void resetTemporaryStats() {
            temporaryDefense = 0;
        }

        public abstract boolean tryMove(int dx, int dy, Dungeon dungeon);
    }

    private class Enemy extends Entity {
        private String name;
        private int level;

        public Enemy(int x, int y, String name, int level) {
            super(x, y, 20 + level * 10, 5 + level * 2, 2 + level);
            this.name = name;
            this.level = level;
        }

        public Enemy(int x, int y) {
            this(x, y, "Goblin", 1);
        }

        public String getName() { return name; }
        public int getLevel() { return level; }

        @Override
        public boolean tryMove(int dx, int dy, Dungeon dungeon) {
            int newX = x + dx;
            int newY = y + dy;

            // Check if the new position is valid
            if (dungeon.getTile(newX, newY) == '.' &&
                    !(newX == player.getX() && newY == player.getY())) {
                x = newX;
                y = newY;
                return true;
            }
            return false;
        }
    }

    private class Player extends Entity {
        private Dungeon dungeon;
        private int score = 0;
        private int experience = 0;
        private int level = 1;
        private int gold = 0;
        private Item equippedWeapon;
        private Item equippedArmor;
        private ItemInventory inventory;

        public Player(int startX, int startY, Dungeon dungeon, ItemInventory inventory) {
            super(startX, startY, 100, 5, 2);
            this.dungeon = dungeon;
            this.inventory = inventory;
        }

        public int getScore() { return score; }
        public int getExperience() { return experience; }
        public int getLevel() { return level; }
        public int getGold() { return gold; }
        public Item getEquippedWeapon() { return equippedWeapon; }
        public Item getEquippedArmor() { return equippedArmor; }

        public void setStats(int maxHealth, int attack, int defense) {
            this.maxHealth = maxHealth;
            this.health = maxHealth;
            this.attack = attack;
            this.defense = defense;
        }

        public void addScore(int points) { score += points; }
        public void addExperience(int exp) { experience += exp; }
        public void addGold(int amount) { gold += amount; }

        public boolean checkLevelUp() {
            int expNeeded = level * 100;
            if (experience >= expNeeded) {
                level++;
                maxHealth += 20;
                health = maxHealth;
                attack += 2;
                defense += 1;
                return true;
            }
            return false;
        }

        public void equipItem(Item item) {
            if (item.getType() == ItemType.WEAPON) {
                equippedWeapon = item;
                attack = 5 + level + item.getValue();
            } else if (item.getType() == ItemType.ARMOR) {
                equippedArmor = item;
                defense = 2 + level/2 + item.getValue();
            }
        }

        public boolean useHealthPotion() {
            List<Item> potions = inventory.getPotions();
            if (!potions.isEmpty()) {
                Item potion = potions.get(0);
                heal(potion.getValue());
                inventory.useItem(inventory.items.indexOf(potion));
                return true;
            }
            return false;
        }

        public void equipBestItems() {
            // Find best weapon
            Item bestWeapon = null;
            int bestWeaponValue = 0;

            for (Item item : inventory.getWeapons()) {
                if (item.getValue() > bestWeaponValue) {
                    bestWeaponValue = item.getValue();
                    bestWeapon = item;
                }
            }

            // Find best armor
            Item bestArmor = null;
            int bestArmorValue = 0;

            for (Item item : inventory.getArmor()) {
                if (item.getValue() > bestArmorValue) {
                    bestArmorValue = item.getValue();
                    bestArmor = item;
                }
            }

            // Equip best items
            if (bestWeapon != null) {
                equipItem(bestWeapon);
            }

            if (bestArmor != null) {
                equipItem(bestArmor);
            }
        }

        public void moveUp() {
            if (dungeon.getTile(x, y - 1) != '#') {
                y--;
                interactWithTile();
            }
        }

        public void moveDown() {
            if (dungeon.getTile(x, y + 1) != '#') {
                y++;
                interactWithTile();
            }
        }

        public void moveLeft() {
            if (dungeon.getTile(x - 1, y) != '#') {
                x--;
                interactWithTile();
            }
        }

        public void moveRight() {
            if (dungeon.getTile(x + 1, y) != '#') {
                x++;
                interactWithTile();
            }
        }

        private void interactWithTile() {
            // Handled in main game loop
        }

        @Override
        public boolean tryMove(int dx, int dy, Dungeon dungeon) {
            // Not used for player
            return false;
        }
    }

    private class GameView extends StackPane {
        private Dungeon dungeon;
        private Player player;
        private Canvas canvas;
        private Text statusText;
        private StackPane combatScreen;
        private Canvas combatCanvas;
        private ItemInventory inventory;
        private StackPane pauseScreen;

        public GameView(Dungeon dungeon, Player player, ItemInventory inventory) {
            this.dungeon = dungeon;
            this.player = player;
            this.inventory = inventory;

            canvas = new Canvas(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE);
            statusText = new Text();
            statusText.setFont(Font.font(16));
            statusText.setFill(Color.WHITE);
            statusText.setTranslateY(-dungeon.getHeight() * TILE_SIZE / 2 + 15);

            // Create combat screen (initially hidden)
            combatScreen = createCombatScreen();
            combatScreen.setVisible(false);

            // Create pause screen (initially hidden)
            pauseScreen = createPauseScreen();
            pauseScreen.setVisible(false);

            getChildren().addAll(canvas, statusText, combatScreen, pauseScreen);

            // Add drop shadow to the canvas for a better look
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(5.0);
            dropShadow.setOffsetX(3.0);
            dropShadow.setOffsetY(3.0);
            dropShadow.setColor(Color.color(0, 0, 0, 0.5));
            canvas.setEffect(dropShadow);

            update();
        }

        private StackPane createCombatScreen() {
            StackPane screen = new StackPane();
            screen.setPrefSize(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE);
            screen.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

            combatCanvas = new Canvas(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE);

            screen.getChildren().add(combatCanvas);

            return screen;
        }

        private StackPane createPauseScreen() {
            StackPane screen = new StackPane();
            screen.setPrefSize(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE);
            screen.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

            Text pauseText = new Text("GAME PAUSED\n\nPress ESC to resume");
            pauseText.setFont(Font.font(30));
            pauseText.setFill(Color.WHITE);
            pauseText.setTextAlignment(TextAlignment.CENTER);

            screen.getChildren().add(pauseText);

            return screen;
        }

        public void showCombatScreen(Enemy enemy) {
            combatScreen.setVisible(true);
            updateCombatDisplay(player, enemy);
        }

        public void hideCombatScreen() {
            combatScreen.setVisible(false);
        }

        public void showPauseScreen() {
            pauseScreen.setVisible(true);
        }

        public void hidePauseScreen() {
            pauseScreen.setVisible(false);
        }

        public void updateCombatDisplay(Player player, Enemy enemy) {
            GraphicsContext gc = combatCanvas.getGraphicsContext2D();

            // Clear the canvas
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, combatCanvas.getWidth(), combatCanvas.getHeight());

            // Draw title
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(24));
            gc.fillText("COMBAT", combatCanvas.getWidth() / 2 - 50, 50);

            // Draw player
            gc.setFill(Color.BLUE);
            gc.fillRect(100, 100, 80, 120);

            // Draw enemy
            gc.setFill(Color.RED);
            gc.fillRect(combatCanvas.getWidth() - 180, 100, 80, 120);

            // Draw health bars
            drawHealthBar(gc, 50, 240, player.getHealth(), player.getMaxHealth(), "Player");
            drawHealthBar(gc, combatCanvas.getWidth() - 250, 240, enemy.getHealth(), enemy.getMaxHealth(), enemy.getName());

            // Draw stats
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(14));
            gc.fillText("ATK: " + player.getAttack() + "  DEF: " + player.getDefense(), 50, 280);
            gc.fillText("ATK: " + enemy.getAttack() + "  DEF: " + enemy.getDefense(), combatCanvas.getWidth() - 250, 280);

            // Draw actions
            gc.setFont(Font.font(18));
            gc.fillText("Actions:", combatCanvas.getWidth() / 2 - 40, 350);
            gc.setFont(Font.font(16));
            gc.fillText("A - Attack", combatCanvas.getWidth() / 2 - 120, 380);
            gc.fillText("D - Defend", combatCanvas.getWidth() / 2 - 120, 410);
            gc.fillText("S - Use Potion", combatCanvas.getWidth() / 2 - 120, 440);
            gc.fillText("F - Try to Flee", combatCanvas.getWidth() / 2 - 120, 470);
        }

        private void drawHealthBar(GraphicsContext gc, double x, double y, int health, int maxHealth, String label) {
            double width = 200;
            double height = 20;

            // Draw label
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(14));
            gc.fillText(label + ": " + health + "/" + maxHealth, x, y - 5);

            // Draw background
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(x, y, width, height);

            // Draw health
            double healthWidth = (double) health / maxHealth * width;
            gc.setFill(health > maxHealth * 0.3 ? Color.GREEN : Color.RED);
            gc.fillRect(x, y, healthWidth, height);

            // Draw border
            gc.setStroke(Color.WHITE);
            gc.strokeRect(x, y, width, height);
        }

        public void resetView(Dungeon newDungeon, Player newPlayer) {
            this.dungeon = newDungeon;
            this.player = newPlayer;

            // Resize canvas for new dungeon
            canvas.setWidth(dungeon.getWidth() * TILE_SIZE);
            canvas.setHeight(dungeon.getHeight() * TILE_SIZE);

            if (combatScreen != null) {
                combatScreen.setPrefSize(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE);
                combatCanvas.setWidth(dungeon.getWidth() * TILE_SIZE);
                combatCanvas.setHeight(dungeon.getHeight() * TILE_SIZE);
            }

            if (pauseScreen != null) {
                pauseScreen.setPrefSize(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE);
            }

            statusText.setText("");
            statusText.setTranslateY(-dungeon.getHeight() * TILE_SIZE / 2 + 15);

            update();
        }

        public void update() {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // Calculate visible area (centered on player)
            int viewRadius = 8;
            int minX = Math.max(0, player.getX() - viewRadius);
            int minY = Math.max(0, player.getY() - viewRadius);
            int maxX = Math.min(dungeon.getWidth() - 1, player.getX() + viewRadius);
            int maxY = Math.min(dungeon.getHeight() - 1, player.getY() + viewRadius);

            // Draw dungeon tiles
            for (int x = 0; x < dungeon.getWidth(); x++) {
                for (int y = 0; y < dungeon.getHeight(); y++) {
                    // Check if tile is within visible range
                    boolean isVisible = isWithinDistance(x, y, player.getX(), player.getY(), viewRadius);

                    if (isVisible) {
                        char tile = dungeon.getTile(x, y);
                        switch (tile) {
                            case '#' -> {
                                gc.setFill(Color.DARKGRAY);
                                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            }
                            case '.' -> {
                                gc.setFill(Color.BLACK);
                                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                                // Draw floor texture
                                gc.setFill(Color.rgb(20, 20, 30));
                                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            }
                            case 'T' -> {
                                // Treasure chest
                                gc.setFill(Color.rgb(20, 20, 30));
                                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                                gc.setFill(Color.GOLD);
                                gc.fillRect(x * TILE_SIZE + 5, y * TILE_SIZE + 8, TILE_SIZE - 10, TILE_SIZE - 15);

                                gc.setFill(Color.rgb(139, 69, 19)); // Brown
                                gc.fillRect(x * TILE_SIZE + 15, y * TILE_SIZE + 15, 10, 5);
                            }
                            case 'E' -> {
                                // Enemy
                                gc.setFill(Color.rgb(20, 20, 30));
                                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                                gc.setFill(Color.RED);
                                gc.fillOval(x * TILE_SIZE + 5, y * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);
                            }
                            case 'P' -> {
                                // Potion
                                gc.setFill(Color.rgb(20, 20, 30));
                                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                                gc.setFill(Color.MEDIUMVIOLETRED);
                                gc.fillOval(x * TILE_SIZE + 10, y * TILE_SIZE + 5, TILE_SIZE - 20, TILE_SIZE - 15);
                                gc.setFill(Color.LIGHTPINK);
                                gc.fillOval(x * TILE_SIZE + 17, y * TILE_SIZE + 10, 6, 6);
                            }
                            case 'X' -> {
                                // Trap
                                gc.setFill(Color.rgb(20, 20, 30));
                                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                                gc.setStroke(Color.YELLOW);
                                gc.strokeLine(x * TILE_SIZE + 5, y * TILE_SIZE + 5,
                                        x * TILE_SIZE + TILE_SIZE - 5, y * TILE_SIZE + TILE_SIZE - 5);
                                gc.strokeLine(x * TILE_SIZE + TILE_SIZE - 5, y * TILE_SIZE + 5,
                                        x * TILE_SIZE + 5, y * TILE_SIZE + TILE_SIZE - 5);
                            }
                            case 'W' -> {
                                // Weapon
                                gc.setFill(Color.rgb(20, 20, 30));
                                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                                gc.setFill(Color.SILVER);
                                gc.fillRect(x * TILE_SIZE + 18, y * TILE_SIZE + 5, 4, 30);
                                gc.fillRect(x * TILE_SIZE + 12, y * TILE_SIZE + 10, 16, 4);
                            }
                            case 'A' -> {
                                // Armor
                                gc.setFill(Color.rgb(20, 20, 30));
                                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                                gc.setFill(Color.LIGHTSTEELBLUE);
                                gc.fillRoundRect(x * TILE_SIZE + 10, y * TILE_SIZE + 5, TILE_SIZE - 20, TILE_SIZE - 10, 5, 5);
                            }
                            case 'S' -> {
                                // Stairs to next level
                                gc.setFill(Color.rgb(20, 20, 30));
                                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                                gc.setFill(Color.WHITE);
                                double[] xPoints = {x * TILE_SIZE + 10, x * TILE_SIZE + TILE_SIZE - 10, x * TILE_SIZE + TILE_SIZE/2};
                                double[] yPoints = {y * TILE_SIZE + TILE_SIZE - 10, y * TILE_SIZE + TILE_SIZE - 10, y * TILE_SIZE + 10};
                                gc.fillPolygon(xPoints, yPoints, 3);
                            }
                        }
                    } else {
                        // Draw fog of war for unexplored areas
                        gc.setFill(Color.BLACK);
                        gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                }
            }

            // Draw player
            gc.setFill(Color.BLUE);
            gc.fillOval(player.getX() * TILE_SIZE + 5, player.getY() * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);

            // Draw mini-map
            drawMiniMap(gc);

            // Update status text
            statusText.setText(String.format("Level %d | Health: %d/%d | Attack: %d | Defense: %d | Score: %d | XP: %d | Gold: %d | Treasures: %d",
                    currentLevel, player.getHealth(), player.getMaxHealth(),
                    player.getAttack(), player.getDefense(),
                    player.getScore(), player.getExperience(),
                    player.getGold(), dungeon.getTreasureCount()));
        }

        private boolean isWithinDistance(int x1, int y1, int x2, int y2, int distance) {
            return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) <= distance;
        }

        private void drawMiniMap(GraphicsContext gc) {
            double mapSize = 100;
            double tileSize = mapSize / Math.max(dungeon.getWidth(), dungeon.getHeight());
            double mapX = canvas.getWidth() - mapSize - 10;
            double mapY = 10;

            // Draw background
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(mapX - 5, mapY - 5, mapSize + 10, mapSize + 10);

            // Draw tiles
            for (int x = 0; x < dungeon.getWidth(); x++) {
                for (int y = 0; y < dungeon.getHeight(); y++) {
                    // Only draw if within visible range of player or previously seen
                    boolean isVisible = isWithinDistance(x, y, player.getX(), player.getY(), 8);

                    if (isVisible) {
                        char tile = dungeon.getTile(x, y);
                        switch (tile) {
                            case '#' -> gc.setFill(Color.DARKGRAY);
                            case '.' -> gc.setFill(Color.DARKSLATEGRAY);
                            case 'T' -> gc.setFill(Color.GOLD);
                            case 'E' -> gc.setFill(Color.RED);
                            case 'P' -> gc.setFill(Color.MAGENTA);
                            case 'X' -> gc.setFill(Color.YELLOW);
                            case 'W', 'A' -> gc.setFill(Color.SILVER);
                            case 'S' -> gc.setFill(Color.WHITE);
                        }

                        gc.fillRect(mapX + x * tileSize, mapY + y * tileSize, tileSize, tileSize);
                        gc.fillRect(mapX + x * tileSize, mapY + y * tileSize, tileSize, tileSize);
                    }
                }
            }

            // Draw player on minimap
            gc.setFill(Color.BLUE);
            gc.fillOval(mapX + player.getX() * tileSize - 1, mapY + player.getY() * tileSize - 1, tileSize + 2, tileSize + 2);

            // Draw border
            gc.setStroke(Color.WHITE);
            gc.strokeRect(mapX - 5, mapY - 5, mapSize + 10, mapSize + 10);
        }

        public void showGameOver(int finalScore, int level) {
            StackPane gameOverScreen = new StackPane();
            gameOverScreen.setPrefSize(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE);
            gameOverScreen.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

            VBox content = new VBox(15);
            content.setAlignment(javafx.geometry.Pos.CENTER);

            Text gameOverText = new Text("GAME OVER");
            gameOverText.setFont(Font.font(36));
            gameOverText.setFill(Color.RED);

            Text scoreText = new Text(
                    "Level reached: " + level + "\n" +
                            "Final Score: " + finalScore + "\n" +
                            "Enemies defeated: " + ((level - 1) * 5 + (5 - dungeon.enemies.size())) + "\n\n" +
                            "Press SPACE to try again"
            );
            scoreText.setFont(Font.font(18));
            scoreText.setFill(Color.WHITE);
            scoreText.setTextAlignment(TextAlignment.CENTER);

            content.getChildren().addAll(gameOverText, scoreText);
            gameOverScreen.getChildren().add(content);

            getChildren().add(gameOverScreen);
        }

        public void showGameComplete(int finalScore, int level) {
            StackPane victoryScreen = new StackPane();
            victoryScreen.setPrefSize(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE);
            victoryScreen.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

            VBox content = new VBox(15);
            content.setAlignment(javafx.geometry.Pos.CENTER);

            Text victoryText = new Text("CONGRATULATIONS!\nYou have completed the dungeon!");
            victoryText.setFont(Font.font(32));
            victoryText.setFill(Color.GOLD);
            victoryText.setTextAlignment(TextAlignment.CENTER);

            Text scoreText = new Text(
                    "All " + level + " levels completed!\n" +
                            "Final Score: " + finalScore + "\n" +
                            "Player Level: " + player.getLevel() + "\n" +
                            "Gold Collected: " + player.getGold() + "\n\n" +
                            "Press SPACE to play again"
            );
            scoreText.setFont(Font.font(18));
            scoreText.setFill(Color.WHITE);
            scoreText.setTextAlignment(TextAlignment.CENTER);

            content.getChildren().addAll(victoryText, scoreText);
            victoryScreen.getChildren().add(content);

            getChildren().add(victoryScreen);
        }

        public void showLevelComplete(int level) {
            StackPane levelCompleteScreen = new StackPane();
            levelCompleteScreen.setPrefSize(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE);
            levelCompleteScreen.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

            VBox content = new VBox(15);
            content.setAlignment(javafx.geometry.Pos.CENTER);

            Text levelCompleteText = new Text("LEVEL " + level + " COMPLETE!");
            levelCompleteText.setFont(Font.font(32));
            levelCompleteText.setFill(Color.LIGHTGREEN);

            Text statsText = new Text(
                    "Score: " + player.getScore() + "\n" +
                            "Treasures found: " + (5 + level * 2) + "\n" +
                            "Enemies defeated: " + (5 - dungeon.enemies.size()) + "\n\n" +
                            "Press SPACE to continue to level " + (level + 1)
            );
            statsText.setFont(Font.font(18));
            statsText.setFill(Color.WHITE);
            statsText.setTextAlignment(TextAlignment.CENTER);

            content.getChildren().addAll(levelCompleteText, statsText);
            levelCompleteScreen.getChildren().add(content);

            getChildren().add(levelCompleteScreen);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}