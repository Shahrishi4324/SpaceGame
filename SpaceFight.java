package Assignments.SpaceGame;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class SpaceFight {
    // Create the 3 JFrames 
    JFrame mainFrame, endFrame, introFrame;
    // Globalize endPanel so I can access it later in the code to add the score label
    JPanel endPanel = new JPanel(); 
    // Load all the images I need
    BufferedImage background, shooter, enemy, bulletImg, enemyBomb;
    // Create all the objects I need
    Timer timer; 
    DrawingPanel drawPanel;
    Spaceship ship; 
    // To see if the bullet is fired or not
    boolean fire = false;
    Bullet bullet; 

    // Create the ArrayLists for the enemies and bombs
    int numEnemies;
    ArrayList <Enemy> enemies;
    ArrayList <EnemyBombs> bombs;

    // To get the coordinates of the enemy or bomb that was hit for the sprite sheet
    int coordinateX = 0;
    int coordinateY = 0;  

    int score = 0;

    //Number of respawns so far
    int respawn = 0;

    //The Sprite sheet has 7 rows of sprites, each consisting of 14 frames.
	//Each sprite is 64x64 pixels.
	static final int spriteW = 64;
	static final int spriteH = 64;
	static final int spriteMAXROW = 8;
	static final int spriteMAXFRAME = 14;
    BufferedImage imgExplode = null;

    public static void main(String [] args){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                new SpaceFight();
            }
        });
    }

    /** Constructor
     * The Main Loop of the Game, where all the objects are created and the game is run
    */
    public SpaceFight(){
        // Mr Harwood showed me how to use 3 JFrames in one program to switch between them
        introFrame = setUpIntro();
        mainFrame = setUpMain();
        endFrame = setUpEnd();
        
        // Load all the images
        background = loadImage("Assignments/SpaceGame/Home Background.png");
        shooter = loadImage("Assignments/SpaceGame/Shooter.png");
        enemy = loadImage("Assignments/SpaceGame/enemy.png");
        bulletImg = loadImage("Assignments/SpaceGame/Bullet.png");
        enemyBomb = loadImage("Assignments/SpaceGame/EnemyBomb.png");
        imgExplode = loadImage("Assignments/SpaceGame/explosions.png");

        // Create the enemies
        enemies = new ArrayList<Enemy>();
        numEnemies = 5;
        for (int i = 0; i < numEnemies; i++) {
            // Randomize the x coordinate of the enemies
            int x = (int)(Math.random() * 800);
            Enemy enemy = new Enemy(x, 40, 50, 50);
            enemies.add(enemy);
        }

        // Create the bombs
        bombs = new ArrayList<EnemyBombs>();

        // Create the timer
        timer = new Timer(10, new ActionListener(){
            public void actionPerformed(ActionEvent e){
                ship.move();
                for (Enemy enemy : enemies) {
                    enemy.move();
                }
                if (fire) {
                    bullet.move();
                    bullet.checkCollision();
                }
                for (EnemyBombs bomb : bombs) {
                    bomb.move();
                    bomb.checkCollision();
                }
                for (Enemy enemy : enemies) {
                    if (enemy.shouldDropBomb()) {
                        enemy.dropBomb();
                    }
                }
                // If there are no more enemies, respawn them and go to the next round
                if (enemies.size() == 0) {
                    timer.stop();
                    respawnAliens();
                    nextRound(); 
                    timer.start();
                }
                drawPanel.repaint();
            }
        });
    }

    /**
     * Sets up the intro JFrame
     * @return the intro JFrame
     */
    JFrame setUpIntro(){
        JFrame window = new JFrame(); 
        window.setTitle("Space Battle");
        window.setSize(750,500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setResizable(false);

        // Load the background image
        BufferedImage introBackground = loadImage("Assignments/SpaceGame/IntroBackground.jpg");

        // Create an anonymous inner class to draw the background image
        JPanel drawPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(introBackground, 0, 0, getWidth(), getHeight(), this);
            }
        };
        drawPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 100, 85));

        JLabel introLabel = new JLabel("Welcome to Space Battle! ");
        introLabel.setForeground(Color.WHITE);
        introLabel.setFont(new Font("Arial", Font.BOLD, 50));
        introLabel.setHorizontalAlignment(JLabel.CENTER);
        drawPanel.add(introLabel);

        // Create the start button
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                window.setVisible(false);
                mainFrame.setVisible(true);
                timer.start();
            }
        });
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setFont(new Font("Arial", Font.BOLD, 30));
        //Press ALT + ENTER to start the game 
        startButton.setMnemonic(KeyEvent.VK_ENTER);
        drawPanel.add(startButton);

        // Create the instructions label
        JLabel instructionsLabel = new JLabel("Use the left and right arrow keys to move and space to shoot. ");
        instructionsLabel.setForeground(Color.WHITE);
        instructionsLabel.setFont(new Font("Arial", Font.BOLD, 20));
        instructionsLabel.setHorizontalAlignment(JLabel.CENTER);
        drawPanel.add(instructionsLabel);

        // Add the drawPanel to the window
        window.setLayout(new BorderLayout());
        window.add(drawPanel, BorderLayout.CENTER);
        window.setVisible(true);
        return window;
    }

    /**
     * Sets up the main JFrame
     * @return the main JFrame
     */
    JFrame setUpMain(){
        JFrame window = new JFrame(); 
        window.setTitle("Space Battle");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        // Create the spaceship
        ship = new Spaceship(400, 500, 50, 50);

        // Create the drawing panel
        drawPanel = new DrawingPanel();
        drawPanel.addKeyListener(new KeyHandler());
        // Make the drawing panel focusable so it can listen for key events
        drawPanel.setFocusable(true);
        window.add(drawPanel);
        window.pack();
        window.setLocationRelativeTo(null);
        return window;
    }

    /**
     * Sets up the end JFrame
     * @return the end JFrame
     */
    JFrame setUpEnd(){
        // Create the end JFrame
        JFrame window = new JFrame(); 
        window.setTitle("Space Battle");
        window.setSize(750,500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setResizable(false);

        // Create the end panel
        endPanel.setBackground(Color.BLACK);
        endPanel.setLayout(new BorderLayout());

        // Create an anonymous inner class to draw the background image
        BufferedImage endBackground = loadImage("Assignments/SpaceGame/EndBackground.jpg");
        JPanel drawPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(endBackground, 0, 0, getWidth(), getHeight(), this);
            }
        };

        // Create the end label
        JLabel endLabel = new JLabel("Game Over! ", JLabel.CENTER);
        endLabel.setForeground(Color.WHITE);
        endLabel.setFont(new Font("Arial", Font.BOLD, 50));

        // Add the end label and draw panel to the end panel
        endPanel.add(endLabel, BorderLayout.PAGE_START);
        endPanel.add(drawPanel, BorderLayout.CENTER);
        window.add(endPanel);
        return window;
    }
    
    /**
     * Respawns the aliens when all aliens are killed
     */
    public void respawnAliens() {
        enemies.clear();  
        numEnemies++;   
        for (int i = 0; i < numEnemies; i++) {
            int x = (int)(Math.random() * 800);
            Enemy enemy = new Enemy(x, 40, 50, 50);
            enemies.add(enemy);
        }
    }

    /**
     * Creates a warning to the user that the next round is starting, makes aliens faster, and moves the aliens down the screen for a challenge
     */
    public void nextRound() {
        respawn++;
        JOptionPane.showMessageDialog(null, "Next Round!", "CONGRATULATIONS", JOptionPane.INFORMATION_MESSAGE);
        for (Enemy enemy : enemies) {
            enemy.vx += 2;
            enemy.y += respawn*25;
        }
    }

    // Create the classes for the objects
    private class Spaceship extends Rectangle{
        int vx;
        /**
         * Constructor for the Spaceship class
         * @param x the x coordinate of the spaceship
         * @param y the y coordinate of the spaceship
         * @param width the width of the spaceship
         * @param height the height of the spaceship
         */
        public Spaceship(int x, int y, int width, int height) {
            super(x, y, width, height);
            vx = 0;
        }

        /**
         * Draws the spaceship
         * @param g2 the Graphics2D object
         */
        public void draw(Graphics2D g2) {
            g2.drawImage(shooter, this.x, this.y, this.width, this.height, null);
        }

        /**
         * Moves the spaceship left and right and keeps it in the screen
         */
        public void move() {
            this.x += vx;
            if (x <= 0) {
                x = 0;
            }
            if (x >= 750) {
                x = 750;
            }
        }
    }

    // Create the Bullet class for the spaceship to fire
    private class Bullet extends Rectangle{
        int vy;
        /**
         * Constructor for the Bullet class
         * @param x the x coordinate of the bullet
         * @param y the y coordinate of the bullet
         * @param width the width of the bullet
         * @param height the height of the bullet
         */
        public Bullet(int x, int y, int width, int height) {
            super(x, y, width, height);
            vy = -10;
        }

        /**
         * Draws the bullet
         * @param g2 the Graphics2D object
         */
        public void draw(Graphics2D g2) {
            g2.drawImage(bulletImg, this.x + 15, this.y, this.width, this.height, null);
        }

        /**
         * Moves the bullet up the screen
         */
        public void move() {
            this.y += vy;
        } 

        /**
         * Checks if the bullet hits an enemy
         */
        public void checkCollision() {
            if (this.y < 0) {
                fire = false;
            }
            // If the bullet hits an enemy, remove the enemy and play the explosion animation
            for (Enemy enemy : enemies) {
                if (this.intersects(enemy)) {               
                    fire = false;
                    enemies.remove(enemy);
                    // Get the coordinates of the enemy that was hit to put the animation in that specfic place
                    coordinateX = enemy.x;
                    coordinateY = enemy.y;
                    // Play the explosion animation
                    drawPanel.runExplosion();
                    score++;
                    break;
                }
            }
            for (EnemyBombs bomb : bombs) {
                if (this.intersects(bomb)) {
                    fire = false;
                    bombs.remove(bomb);
                    // Get the coordinates of the enemy that was hit to put the animation in that specfic place
                    coordinateX = bomb.x;
                    coordinateY = bomb.y;
                    // Play the explosion animation
                    drawPanel.runExplosion();
                    break;
                }
            }
        }
    }

    private class Enemy extends Rectangle{
        int vx;

        /**
         * Constructor for the Enemy class
         * @param x the x coordinate of the enemy
         * @param y the y coordinate of the enemy
         * @param width the width of the enemy
         * @param height the height of the enemy
         */
        public Enemy(int x, int y, int width, int height) {
            super(x, y, width, height);
            vx = (int) (Math.random() * 10) + 4;
            int rand = (int) (Math.random() * 2);
            if (rand == 0) {
                vx *= -1;
            }
        }

        /**
         * Draws the enemy
         * @param g2 the Graphics2D object
         */
        public void draw(Graphics2D g2) {
            g2.drawImage(enemy, this.x, this.y, this.width, this.height, null);
        }

        /**
         * Moves the enemy left and right and keeps it in the screen
         */
        public void move() {
            this.x += vx;
            if (x <= 30) {
                vx = 5;
            } 
            if (x >= 700) {
                vx = -5;
            }
        }  

        /**
         * Checks if the enemy should drop a bomb based on a random chance
         * @return true if the enemy should drop a bomb, false otherwise
         */
        private boolean shouldDropBomb(){
            int rand = (int) (Math.random() * 100);
            return rand == 0;
        }

        /**
         * Drops a bomb from the enemy
         */
        private void dropBomb() {
            EnemyBombs bomb = new EnemyBombs(this.x, this.y, 25, 25);
            bombs.add(bomb);
            // Remove the bomb from the ArrayList if it goes off the screen and keep the ArrayList size at 10
            if (bombs.size() > 10) {
                bombs.remove(0);
            }
        }        
    }

    // Create the EnemyBombs class for the enemy to drop
    private class EnemyBombs extends Rectangle{
        int vy;
        /**
         * Constructor for the EnemyBombs class
         * @param x the x coordinate of the bomb
         * @param y the y coordinate of the bomb
         * @param width the width of the bomb
         * @param height the height of the bomb
         */
        public EnemyBombs(int x, int y, int width, int height) {
            super(x, y, width, height);
            vy = 10;
        }

        /**
         * Draws the bomb
         * @param g2 the Graphics2D object
         */
        public void draw(Graphics2D g2) {
            g2.drawImage(enemyBomb, this.x, this.y, this.width, this.height, null);
        }
        
        /**
         * Moves the bomb down the screen
         */
        public void move() {
            this.y += vy;
        }

        /**
         * Checks if the bomb hits the spaceship
         */
        public void checkCollision() {
            if (this.intersects(ship)) {
                timer.stop();
                // Switch to the end JFrame
                mainFrame.setVisible(false);

                // Add the score label to the end panel to display the final score
                JLabel scoreLabel = new JLabel("Score: " + score, JLabel.CENTER);
                scoreLabel.setForeground(Color.WHITE);
                scoreLabel.setFont(new Font("Arial", Font.BOLD, 30));
                endPanel.add(scoreLabel, BorderLayout.PAGE_END);

                // Show the end JFrame
                endFrame.setVisible(true);
            }
        }
    }

    // Create the KeyHandler class to listen for key events
    private class KeyHandler extends KeyAdapter{
        /**
         * Listens for key presses and moves the spaceship left and right
         * @param e the KeyEvent object
         */
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
                ship.vx = -5;
            } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
                ship.vx = 5;
            } 
            if (key == KeyEvent.VK_SPACE && !fire) {
                bullet = new Bullet(ship.x, ship.y, 25, 25);
                fire = true; 
            }
        }
        /**
         * Listens for key releases and stops the spaceship from moving
         * @param e the KeyEvent object 
         */
        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
                ship.vx = 0;
            } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
                ship.vx = 0;
            } 
        }
    }
    // Create the DrawingPanel class to draw all the objects
    private class DrawingPanel extends JPanel{
        // Create the variables for the explosion animation
        private boolean explosionRunning = false; 
        private int exploNum = 0; 
        private int frame = 0; 

        /**
         * Constructor for the DrawingPanel class
         */
        public DrawingPanel(){
            setPreferredSize(new Dimension(800,600));
        }

        /**
         * Draws all the objects
         * @param g the Graphics object
         */
        @Override
        public void paintComponent(Graphics g){
            //draw the background
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //draw the background image
            g2.drawImage(background, 0, 0, null);

            //draw the enemies
            for (Enemy enemy : enemies) {
                enemy.draw(g2);
            }
            //draw the bullets if space is pressed and a bullet is fired
            if (fire) {
                bullet.draw(g2);
            }
            //draw the explosion animation
            if (explosionRunning){
                g2.drawImage(imgExplode, coordinateX, coordinateY, coordinateX + 50, coordinateY + 50, 
                frame * spriteW, exploNum * spriteH, (frame + 1) * spriteW, (exploNum + 1) * spriteH, null);
            }
            //draw the bombs
            for (EnemyBombs bomb : bombs) {
                bomb.draw(g2);
            }
            //draw the score
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            g2.drawString("Score: " + score, 10, 30);

            ship.draw(g2);
        }

        /**
         * Runs the explosion animation
         */
        void runExplosion(){
            //create an ActionListener to act as a callback method for the Timer
            class MyTimerListener implements ActionListener{
                /**
                 * Runs the explosion animation
                 * @param e the ActionEvent object
                 */
                @Override
                public void actionPerformed(ActionEvent e){
                    repaint();
					frame++;
					//stop the timer from inside the ActionListener
					if (frame == spriteMAXFRAME) { 
						((Timer)e.getSource()).stop();
						explosionRunning = false;			
						frame = 0;
						exploNum++;
						if (exploNum == spriteMAXROW) exploNum = 0;
						repaint();
					}
                }
            }

            //create a Timer to animate the explosion
            Timer ExpTimer = new Timer(80, new MyTimerListener());
            explosionRunning = true; 
            ExpTimer.start(); 
        }
    }

    /**
     * Loads an image
     * @param name the name of the image
     * @return the image
     */
    static BufferedImage loadImage(String name) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(name));
        } catch (Exception e) {
            System.out.println(e.toString());
            JOptionPane.showMessageDialog(null, "An image failed to load: " + name, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return img;
    }
}
