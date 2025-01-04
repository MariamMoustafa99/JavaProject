package JavaProject;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GUIPaintApp extends JPanel {

    private JButton redButton, greenButton, blueButton, yellowButton, blackButton, cyanButton;
    private JButton rectangleButton, ovalButton, lineButton, freehandButton, eraserButton, clearButton, undoButton, saveButton;
    private JCheckBox dottedCheckbox, filledCheckbox;
    private Color currentColor;
    private Point startPoint, endPoint, lastPoint;
    private boolean isDotted, isFilled;
    private SelectedDrawingOption selectedDrawingOption;
    private ArrayList<PaintAction> actionsList;

    public GUIPaintApp() {
        setLayout(new BorderLayout());

        currentColor = Color.BLACK;
        isDotted = false;
        isFilled = false;
        selectedDrawingOption = SelectedDrawingOption.FREEHAND;
        actionsList = new ArrayList<>();

        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw all the actions
                for (PaintAction action : actionsList) {
                    drawShape((Graphics2D) g, action);
                }
            }
        };

        drawingPanel.setBackground(Color.WHITE);
        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                lastPoint = startPoint; // Initialize lastPoint for freehand

                if (selectedDrawingOption == SelectedDrawingOption.FREEHAND) {
                    // Start a new freehand action
                    actionsList.add(new PaintAction(startPoint, startPoint, currentColor, selectedDrawingOption, isDotted, isFilled));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                endPoint = e.getPoint();
                if (selectedDrawingOption == SelectedDrawingOption.FREEHAND) {
                    // End of freehand drawing
                    lastPoint = null; // Clear reference
                } else if (selectedDrawingOption == SelectedDrawingOption.ERASER) {
                    // For eraser, we need to remove the part of the drawing
                    actionsList.add(new PaintAction(startPoint, endPoint, Color.WHITE, SelectedDrawingOption.RECTANGLE, false, false));
                } else {
                    // For other shapes, create a new action
                    PaintAction action = new PaintAction(startPoint, endPoint, currentColor, selectedDrawingOption, isDotted, isFilled);
                    actionsList.add(action);
                }
                drawingPanel.repaint();
            }
        });

        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                if (selectedDrawingOption == SelectedDrawingOption.FREEHAND) {
                    PaintAction action = actionsList.get(actionsList.size() - 1);
                    action.endPoint = endPoint; // Update endpoint
                    action.lineSegments.add(new LineSegment(lastPoint, endPoint)); // Add the new line segment
                    lastPoint = endPoint; // Update lastPoint
                    drawingPanel.repaint();
                } else if (selectedDrawingOption == SelectedDrawingOption.ERASER) {
                    // Draw a rectangle to erase
                    actionsList.add(new PaintAction(startPoint, endPoint, Color.WHITE, SelectedDrawingOption.RECTANGLE, false, false));
                    drawingPanel.repaint();
                } else {
                    // Draw shape in real-time for other options
                    drawingPanel.repaint(); // Refresh the drawing panel
                    Graphics2D g2d = (Graphics2D) drawingPanel.getGraphics();
                    g2d.setColor(currentColor);
                    drawShape(g2d, new PaintAction(startPoint, endPoint, currentColor, selectedDrawingOption, isDotted, isFilled));
                    g2d.dispose(); // Dispose of graphics context
                }
            }
        });

        // Create the toolbar for buttons
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.LIGHT_GRAY);

        // Add color buttons
        redButton = createColorButton(Color.RED);
        greenButton = createColorButton(Color.GREEN);
        blueButton = createColorButton(Color.BLUE);
        yellowButton = createColorButton(Color.YELLOW);
        blackButton = createColorButton(Color.BLACK);
        cyanButton = createColorButton(Color.CYAN);

        toolBar.add(redButton);
        toolBar.add(greenButton);
        toolBar.add(blueButton);
        toolBar.add(yellowButton);
        toolBar.add(blackButton);
        toolBar.add(cyanButton);
        toolBar.addSeparator();

        // Add shape buttons
        rectangleButton = createButton("Rectangle");
        ovalButton = createButton("Oval");
        lineButton = createButton("Line");
        freehandButton = createButton("Free Hand");
        eraserButton = createButton("Eraser");
        clearButton = createButton("Clear All");
        undoButton = createButton("Undo");
        saveButton = createButton("Save");

        toolBar.add(rectangleButton);
        toolBar.add(ovalButton);
        toolBar.add(lineButton);
        toolBar.add(freehandButton);
        toolBar.add(eraserButton);
        toolBar.add(clearButton);
        toolBar.add(undoButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();

        // Add checkboxes
        dottedCheckbox = createCheckbox("Dotted");
        filledCheckbox = createCheckbox("Filled");

        toolBar.add(dottedCheckbox);
        toolBar.add(filledCheckbox);

        // Add components to the panel
        add(toolBar, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
    }

    private JButton createColorButton(final Color color) {
        JButton button = new JButton();
        button.setBackground(color);
        button.setPreferredSize(new Dimension(30, 30));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentColor = color;
            }
        });
        return button;
    }

    private JButton createButton(final String label) {
        JButton button = new JButton(label);
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (label) {
                    case "Rectangle":
                        selectedDrawingOption = SelectedDrawingOption.RECTANGLE;
                        break;
                    case "Oval":
                        selectedDrawingOption = SelectedDrawingOption.OVAL;
                        break;
                    case "Line":
                        selectedDrawingOption = SelectedDrawingOption.LINE;
                        break;
                    case "Free Hand":
                        selectedDrawingOption = SelectedDrawingOption.FREEHAND;
                        break;
                    case "Eraser":
                        selectedDrawingOption = SelectedDrawingOption.ERASER;
                        break;
                    case "Clear All":
                        clearCanvas();
                        break;
                    case "Undo":
                        undoLastAction();
                        break;
                    case "Save":
                        saveImage("Drawing", "jpg");
                        break;
                }
            }
        });
        return button;
    }

    private void clearCanvas() {
        actionsList.clear();
        repaint();
    }

    private JCheckBox createCheckbox(final String label) {
        JCheckBox checkBox = new JCheckBox(label);
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (label.equals("Dotted"))
                        isDotted = true;
                    else if (label.equals("Filled"))
                        isFilled = true;
                } else {
                    if (label.equals("Dotted"))
                        isDotted = false;
                    else if (label.equals("Filled"))
                        isFilled = false;
                }
            }
        });
        return checkBox;
    }

    private void drawShape(Graphics2D g2d, PaintAction action) {
        int x = Math.min(action.startPoint.x, action.endPoint.x);
        int y = Math.min(action.startPoint.y, action.endPoint.y);
        int width = Math.abs(action.startPoint.x - action.endPoint.x);
        int height = Math.abs(action.startPoint.y - action.endPoint.y);
        g2d.setColor(action.color);

        if (action.drawingOption == SelectedDrawingOption.RECTANGLE) {
            if (action.isFilled) {
                g2d.fillRect(x, y, width, height);
            } else {
                if (action.isDotted) {
                    g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[]{9}, 0));
                }
                g2d.drawRect(x, y, width, height);
            }
        } else if (action.drawingOption == SelectedDrawingOption.OVAL) {
            if (action.isFilled) {
                g2d.fillOval(x, y, width, height);
            } else {
                if (action.isDotted) {
                    g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
                }
                g2d.drawOval(x, y, width, height);
            }
        } else if (action.drawingOption == SelectedDrawingOption.LINE) {
            if (action.isDotted) {
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            }
            g2d.drawLine(action.startPoint.x, action.startPoint.y, action.endPoint.x, action.endPoint.y);
        } else if (action.drawingOption == SelectedDrawingOption.FREEHAND) {
            g2d.setStroke(new BasicStroke(2)); // Thicker stroke for freehand
            for (LineSegment segment : action.lineSegments) {
                g2d.drawLine(segment.start.x, segment.start.y, segment.end.x, segment.end.y);
            }
        }
    }

    private void undoLastAction() {
        if (!actionsList.isEmpty()) {
            actionsList.remove(actionsList.size() - 1);
            repaint();
        }
    }

    private void saveImage(String filename, String format) {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        paint(g2d); // Paint the entire frame onto the BufferedImage
        g2d.dispose();

        try {
            File outputfile = new File(filename + "." + format);
            ImageIO.write(image, format, outputfile);
            JOptionPane.showMessageDialog(this, "Image saved successfully: " + outputfile.getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving image: " + e.getMessage());
        }
    }

    class PaintAction {
        Point startPoint;
        Point endPoint;
        Color color;
        SelectedDrawingOption drawingOption;
        boolean isDotted;
        boolean isFilled;
        ArrayList<LineSegment> lineSegments; // Store segments for freehand drawing

        public PaintAction(Point startPoint, Point endPoint, Color color, SelectedDrawingOption drawingOption, boolean isDotted, boolean isFilled) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
            this.color = color;
            this.drawingOption = drawingOption;
            this.isDotted = isDotted;
            this.isFilled = isFilled;
            this.lineSegments = new ArrayList<>(); // Initialize for freehand
        }
    }

    class LineSegment {
        Point start;
        Point end;

        public LineSegment(Point start, Point end) {
            this.start = start;
            this.end = end;
        }
    }

    enum SelectedDrawingOption {
        RECTANGLE, OVAL, LINE, FREEHAND, ERASER
    }

}
