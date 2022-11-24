// Benjamin Yi - 1152795

package WhiteBoardClient;

import remote.IClientCallback;
import remote.IRemoteWhiteBoard;
import remote.IShape;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Client GUI. Also holds client logic.
 */
public class ClientGUI extends JFrame {
    private JPanel contentPane;
    private IRemoteWhiteBoard remoteWhiteBoard;
    private IClientCallback clientCallbackServant;
    private ArrayList<IShape> shapeList = new ArrayList<>();
    private JPanel WhiteBoard;
    private JTextField textField;
    private JRadioButton lineRadioButton;
    private JRadioButton circleRadioButton;
    private JRadioButton ovalRadioButton;
    private JRadioButton rectangleRadioButton;
    private JRadioButton textRadioButton;
    private JButton colourButton;
    private JPanel chatPanel;
    private JTextPane chatBox;
    private JTextField messageBox;
    private JTextPane peerListPane;
    private int x1, y1, x2, y2;
    private IShape.ShapeType currentShape;
    private boolean mouseDown = false;
    private String fileName = null;

    /**
     * Generates GUI. Sets up intialised variables.
     * @param remoteWhiteBoard server interface object
     * @throws ClassNotFoundException
     * @throws UnsupportedLookAndFeelException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public ClientGUI(IRemoteWhiteBoard remoteWhiteBoard) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        // Set-up
        setTitle("WhiteBoard");
        setContentPane(contentPane);
        this.remoteWhiteBoard = remoteWhiteBoard;
        this.currentShape = IShape.ShapeType.LINE;
        this.textField.setOpaque(false);
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // On window close, unregister client and close application
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    if (clientCallbackServant.getId() != null) {
                        onCancel();
                    } else {
                        System.exit(0);
                    }
                } catch (RemoteException ex) {
                    System.err.println("Lost connection to server.");
                    System.exit(0);
                }
            }
        });

        // Paint temporary shape while dragging mouse
        WhiteBoard.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                x2 = e.getX();
                y2 = e.getY();
                WhiteBoard.repaint();
            }
        });

        WhiteBoard.addMouseListener(new MouseAdapter() {

            // Paint temporary shape while dragging mouse
            // Or finish writing text
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                x1 = e.getX();
                y1 = e.getY();
                try {
                    confirmText();
                } catch (RemoteException ex) {
                    System.err.println("Lost connection to server.");
                    System.exit(0);
                }
                mouseDown = true;
            }

            // Finalise and save shape when releasing mouse click
            // Or start writing text
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                x2 = e.getX();
                y2 = e.getY();
                if (currentShape == IShape.ShapeType.TEXT) {
                    textField.setVisible(true);
                    textField.setEditable(true);
                    textField.setEnabled(true);
                    textField.setLocation(x2, y2);
                    textField.setForeground(colourButton.getBackground());
                    textField.requestFocusInWindow();
                } else {
                    try {
                        remoteWhiteBoard.drawShape(x1, y1, x2, y2, colourButton.getBackground(), currentShape);
                    } catch (RemoteException ex) {
                        System.err.println("Lost connection to server.");
                        System.exit(0);
                    }
                }
                mouseDown = false;
            }
        });

        // Shape type listeners
        lineRadioButton.addActionListener(e -> currentShape = IShape.ShapeType.LINE);
        circleRadioButton.addActionListener(e -> currentShape = IShape.ShapeType.CIRCLE);
        ovalRadioButton.addActionListener(e -> currentShape = IShape.ShapeType.OVAL);
        rectangleRadioButton.addActionListener(e -> currentShape = IShape.ShapeType.RECTANGLE);
        textRadioButton.addActionListener(e -> currentShape = IShape.ShapeType.TEXT);

        // Finalise text if focus is lost
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                try {
                    confirmText();
                } catch (RemoteException ex) {
                    System.err.println("Lost connection to server.");
                    System.exit(0);
                }
            }
        });

        // Colour picker
        colourButton.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(ClientGUI.this, "Choose colour", colourButton.getBackground());
            if (chosen != null) {
                colourButton.setBackground(chosen);
            }
        });

        // Write to chat
        // Special case if admin writes "/kick"
        messageBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (messageBox.getText().length() > 0) {
                    try {
                        if (clientCallbackServant.getId() == 0) {
                            String[] parsedMessage = messageBox.getText().split(" ");
                            if (parsedMessage[0].equalsIgnoreCase("/kick")) {
                                remoteWhiteBoard.kickPeer(messageBox.getText().substring(6));
                            } else {
                                remoteWhiteBoard.sendMessage(clientCallbackServant.getFullUsername()+ ": " + messageBox.getText(), clientCallbackServant);
                            }
                        } else {
                            remoteWhiteBoard.sendMessage(clientCallbackServant.getFullUsername()+ ": " + messageBox.getText(), clientCallbackServant);
                        }
                    } catch (RemoteException ex) {
                        System.err.println("Lost connection to server.");
                        System.exit(0);
                    }
                    messageBox.setText("");
                }

            }
        });
    }

    /**
     * Initialise admin toolbar if needed.
     */
    private void initialiseAdminGUI() {
        // Menu bar creation
        JMenuBar menuBar = new JMenuBar();
        JMenu fileButton = new JMenu("File");
        menuBar.add(fileButton);
        JMenuItem newButton = new JMenuItem("New");
        JMenuItem openButton = new JMenuItem("Open");
        JMenuItem saveButton = new JMenuItem("Save");
        JMenuItem saveAsButton = new JMenuItem("Save As");
        JMenuItem closeButton = new JMenuItem("Close");
        fileButton.add(newButton);
        fileButton.add(openButton);
        fileButton.add(saveButton);
        fileButton.add(saveAsButton);
        fileButton.add(closeButton);
        this.setJMenuBar(menuBar);

        // New button clears whiteboard
        newButton.addActionListener(e -> {
            try {
                remoteWhiteBoard.clearAll();
            } catch (RemoteException ex) {
                System.err.println("Lost connection to server.");
                System.exit(0);
            }
        });

        // Save button saves whiteboard to current file
        saveButton.addActionListener(e -> {
            if (fileName != null) {
                try {
                    // Opening without append will overwrite - intended
                    File file = new File(fileName);
                    FileOutputStream fileOut = new FileOutputStream(file);
                    ObjectOutputStream stream = new ObjectOutputStream(fileOut);
                    stream.writeObject(shapeList);
                } catch (IOException ex) {
                    System.err.println("File IO error");
                    System.exit(0);
                }
            }
        });

        // SaveAs button saves whiteboard to new file
        saveAsButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
            fileChooser.setDialogTitle("Save drawing as ...");

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    } else {
                        // only save as *.wbs files
                        return f.getName().toLowerCase().endsWith(".wbs");
                    }
                }

                @Override
                public String getDescription() {
                    return "WhiteBoardSave (*.wbs)";
                }
            });

            int selection = fileChooser.showSaveDialog(ClientGUI.this);
            if (selection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String fname = file.getAbsolutePath();

                // append .wbs if user did not specify
                if (!fname.endsWith(".wbs")) {
                    file = new File(fname + ".wbs");
                }
                fileName = fname;

                try {
                    FileOutputStream fileOut = new FileOutputStream(file);
                    ObjectOutputStream stream = new ObjectOutputStream(fileOut);
                    stream.writeObject(shapeList);
                } catch (IOException ex) {
                    System.err.println("File IO error");
                    System.exit(0);
                }
            }
        });

        // Open button imports whiteboard from file
        openButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
            fileChooser.setDialogTitle("Open drawing ...");

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    } else {
                        // Only open .wbs files
                        return f.getName().toLowerCase().endsWith(".wbs");
                    }
                }

                @Override
                public String getDescription() {
                    return "WhiteBoardSave (*.wbs)";
                }
            });
            int selection = fileChooser.showOpenDialog(ClientGUI.this);
            if (selection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                fileName = file.getAbsolutePath();
                try {
                    FileInputStream fileIn = new FileInputStream(file);
                    ObjectInputStream stream = new ObjectInputStream(fileIn);
                    shapeList = (ArrayList<IShape>) stream.readObject();
                    remoteWhiteBoard.replaceAll(shapeList);
                } catch (IOException | ClassNotFoundException ex) {
                    System.err.println("File input error");
                    System.exit(0);
                }

            }
        });

        // Close button is the same as exiting
        closeButton.addActionListener(e -> {
            try {
                onCancel();
            } catch (RemoteException ex) {
                System.err.println("Lost connection to server.");
                System.exit(0);
            }
        });
    }

    /**
     * Link server callback client object. Connect to server and
     * get whiteboard information
     * @param s callback client object
     * @throws RemoteException
     */
    public void setServant(IClientCallback s) throws RemoteException {
        this.clientCallbackServant = s;
        remoteWhiteBoard.register(clientCallbackServant);
        System.out.println("Connected as: " + clientCallbackServant.getUsername() + "#" + clientCallbackServant.getId());
        if (clientCallbackServant.getId() == 0) {
            initialiseAdminGUI();
        }
        remoteWhiteBoard.getShapeList(clientCallbackServant);
        WhiteBoard.repaint();
        remoteWhiteBoard.getMessageList(clientCallbackServant);
        remoteWhiteBoard.getPeerList(clientCallbackServant);
    }

    /**
     * Admin only. Notifies admin of new user joining and asks for confirmation.
     * @param c callback client object of new user
     * @return
     * @throws RemoteException
     */
    public boolean notifyNewPeer(IClientCallback c) throws RemoteException {
        int option = JOptionPane.showConfirmDialog(ClientGUI.this, c.getUsername() + " would like to connect. Allow?", "New peer", JOptionPane.YES_NO_OPTION);
        return option == JOptionPane.YES_OPTION;
    }

    /**
     * Non-admin only. Notifies admin has decided not to allow user in.
     * Shuts down client.
     * @throws RemoteException
     */
    public void notifyFailure() throws RemoteException {
        System.out.println("You were not allowed into the whiteboard");
        System.exit(0);
    }

    /**
     * Non-admin only. Notifies admin has kicked user.
     * Shuts down client.
     * @throws RemoteException
     */
    public void notifyKick() throws RemoteException {
        System.out.println("You were kicked from the whiteboard.");
        onCancel();
    }

    /**
     * Notifies server has shut down.
     * Shuts down client.
     * @throws RemoteException
     */
    public void notifyKill() throws RemoteException {
        System.out.println("The whiteboard server has been shut down.");
        System.exit(0);
    }

    /**
     * Clean-up before shutdown of client.
     * Unregisters client from server.
     * @throws RemoteException
     */
    private void onCancel() throws RemoteException {
        try {
            remoteWhiteBoard.unregister(clientCallbackServant);
        } catch (Exception ignored) {
            dispose();
            System.exit(0);
        }
        dispose();
        System.exit(0);
    }

    /**
     * Finalises text and sends to server.
     * Hides text field.
     * @throws RemoteException
     */
    private void confirmText() throws RemoteException {
        if (!textField.getText().equals("")) {
            // JTextField and g.drawText use different co-ord systems
            remoteWhiteBoard.drawText(x2, y2+15, colourButton.getBackground(), textField.getText());
            textField.setVisible(false);
            textField.setEditable(false);
            textField.setEnabled(false);
            textField.setText("");
            WhiteBoard.repaint();
        }
    }

    /**
     * Draw shape when user is dragging
     * @param g graphics object
     */
    private void drawTemporaryShape(Graphics g) {
        drawShape(g, x1, y1, x2, y2, colourButton.getBackground(), currentShape);
    }

    /**
     * Translates internal mouse co-ordinates to g.draw method.
     * Use only for shapes, not text.
     * @param g graphics object
     * @param x1 left coord
     * @param y1 upper coord
     * @param x2 right coord
     * @param y2 bottom coord
     * @param colour shape colour
     * @param shape shape type
     */
    private void drawShape(Graphics g, int x1, int y1, int x2, int y2, Color colour, IShape.ShapeType shape) {
        int xl = Math.min(x1, x2);
        int xr = Math.max(x1, x2);
        int yt = Math.min(y1, y2);
        int yb = Math.max(y1, y2);
        int width = xr - xl;
        int height = yb - yt;
        int radius = (int) Math.hypot(x2 - x1, y2 - y1);
        g.setColor(colour);

        switch(shape) {
            case OVAL:
                g.drawOval(xl, yt, width, height);
                break;
            case CIRCLE:
                // First click is centre, drag to circumference
                g.drawOval(x1-radius, y1-radius, radius*2, radius*2);
                break;
            case LINE:
                g.drawLine(x1, y1, x2, y2);
                break;
            case RECTANGLE:
                g.drawRect(xl, yt, width, height);
                break;
        }
    }

    /**
     * Saves text to whiteboard string.
     * @param g graphics object
     * @param x1 x coord
     * @param y1 y coord
     * @param colour text colour
     * @param text string
     */
    private void drawText(Graphics g, int x1, int y1, Color colour, String text) {
        g.setColor(colour);
        g.drawString(text, x1, y1);
    }

    /**
     * Updates whiteboard to new shapeList
     * @param shapeList server's shapelist
     */
    public void updateWhiteBoard(ArrayList<IShape> shapeList) {
        this.shapeList = shapeList;
        WhiteBoard.repaint();
    }

    /**
     * Updates chatbox to new history
     * @param messageList server's message history
     */
    public void updateMessageBoard(ArrayList<String> messageList) {
        chatBox.setText(String.join("\n", messageList));
        // Scroll down if needed
        chatBox.setCaretPosition(chatBox.getDocument().getLength());
    }

    /**
     * Updates peer list
     * @param peerList server's peerlist
     */
    public void updatePeerList(ArrayList<String> peerList) {
        peerListPane.setText("Peer list: \n" + String.join("\n", peerList));
    }

    /**
     * Draw on component by overriding paintComponent().
     */
    private void createUIComponents() {
        WhiteBoard = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(colourButton.getBackground());
                for (IShape s : shapeList) {
                    try {
                        if (s.getShape() == IShape.ShapeType.TEXT) {
                            drawText(g, s.getX(), s.getY(), s.getColour(), s.getText());
                        } else {
                            drawShape(g, s.getX(), s.getY(), s.getX() + s.getWidth(), s.getY() + s.getHeight(), s.getColour(), s.getShape());
                        }
                    } catch (RemoteException e) {
                        System.err.println("Connection to server lost.");
                        System.exit(0);
                    }
                }
                if (currentShape != IShape.ShapeType.TEXT && mouseDown) {
                    drawTemporaryShape(g);
                }
            }
        };

        // https://stackoverflow.com/a/2281980
        textField = new JTextField() {
            @Override
            public void setBorder(Border border) {
            }
        };
    }


}
