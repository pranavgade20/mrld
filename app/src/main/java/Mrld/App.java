/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Mrld;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Proto;
import com.github.alexdlaird.ngrok.protocol.Tunnel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;


public class App extends JFrame {
    Server server;
    public JPanel logoAndQRPanel;
    public App() throws IOException, FontFormatException, URISyntaxException {

        AtomicReference<String> rootPath = new AtomicReference<>(System.getProperty("user.home"));
        Color primaryColor = new Color(185, 253, 244);
        Color textBackgroundColor = new Color(223,253,251);
        this.setTitle("Mrld");
        setLocation(100, 100);
        this.setSize(700, 700);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        ImageIcon logo = new ImageIcon(getClass().getClassLoader().getResource("logo.png"));
        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("icon.png"));

        Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File(getClass().getClassLoader().getResource("font.ttf").toURI())).deriveFont(16f);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(customFont);

        this.setIconImage(icon.getImage());

        logoAndQRPanel = new JPanel();
        logoAndQRPanel.setOpaque(false);
        logoAndQRPanel.add(new JLabel(logo));

        JLabel rootLabel = new JLabel("Your root directory is:");
        rootLabel.setFont(customFont);

        JTextField rootText = new JFormattedTextField();
        rootText.setFont(customFont);
        rootText.setBackground(textBackgroundColor);
        rootText.setBorder(new LineBorder(textBackgroundColor,0));
        rootText.setPreferredSize(new Dimension(192, 24));
        rootText.setText(rootPath.get());
        rootText.setEditable(false);

        ImageIcon folder = new ImageIcon(getClass().getClassLoader().getResource("folder.png"));
        JButton chooseRootButton = new JButton();//"Change root directory");
        chooseRootButton.setIcon(folder);
        chooseRootButton.setToolTipText("Click here to change the root directory");
        chooseRootButton.setFont(customFont);
        chooseRootButton.setFocusPainted(false);
        chooseRootButton.setBackground(new Color(158,237,233));
        chooseRootButton.setPreferredSize(new Dimension(24, 24));
        chooseRootButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int response = fileChooser.showOpenDialog(this);
            if(response == JFileChooser.APPROVE_OPTION) {
                rootPath.set(fileChooser.getSelectedFile().getAbsolutePath());
                rootText.setText(rootPath.get());
            }
        });

        JPanel rootButtons = new JPanel();
        rootButtons.setBackground(primaryColor);
        rootButtons.add(rootLabel);
        rootButtons.add(rootText);
        rootButtons.add(chooseRootButton);
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setOpaque(false);
        rootPanel.add(rootButtons);

        var prompt = new JFormattedTextField();
        prompt.setEditable(false);
        prompt.setBackground(primaryColor);
        prompt.setBorder(new LineBorder(primaryColor,2));
        prompt.setFont(customFont);
        prompt.setText("Press start to start the server");

        JButton startButton = new JButton("Start");
        startButton.setFont(customFont);
        startButton.setFocusPainted(false);
        startButton.setBackground(new Color(158,237,233));

        JPanel serverPanel = new JPanel();
        serverPanel.setOpaque(false);
        serverPanel.add(prompt);
        serverPanel.add(startButton);

        JPanel serverLinksPanel= new JPanel();
        serverLinksPanel.setLayout(new BoxLayout(serverLinksPanel, BoxLayout.Y_AXIS));
        serverLinksPanel.setOpaque(false);

        startButton.addActionListener((actionEvent) -> {
            if (startButton.getText().equals("Start")) {
                try {
                    int port = new Random().nextInt(50000) + 10000;
                    server = new Server(port, rootPath);
                    server.start();
                    String[] ips = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                            .flatMap(a -> Collections.list(a.getInetAddresses()).stream())
                            .filter(InetAddress::isSiteLocalAddress)
                            .filter(a -> !a.isLoopbackAddress())
                            .map(a -> "http:/"+a+":" + port +"/")
                            .toArray(String[]::new);
                    logoAndQRPanel.removeAll();
                    for(String ip: ips) {
                        serverLinksPanel.add(new ServerLinkPanel(ip, customFont, textBackgroundColor));
                        System.out.println(ip);
                    }
                    serverLinksPanel.revalidate();
                    serverLinksPanel.repaint();

                    new Thread(() -> {
                        ServerLinkPanel ngrokPanel = new ServerLinkPanel("starting tunnel...", customFont, textBackgroundColor, false);
                        serverLinksPanel.add(ngrokPanel);
                        serverLinksPanel.revalidate();
                        serverLinksPanel.repaint();
                        Tunnel httpTunnel = new NgrokClient.Builder().build().connect(
                                new CreateTunnel.Builder().withProto(Proto.HTTP).withAddr(port).build()
                        );

                        ngrokPanel.setIpText(httpTunnel.getPublicUrl());
                    }).start();
                    startButton.setText("Stop");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    server.stop();
                    logoAndQRPanel.removeAll();
                    logoAndQRPanel.add(new JLabel(logo));
                    serverLinksPanel.removeAll();
                    App.this.revalidate();
                    App.this.repaint();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                prompt.setText("Press start to start the server");
                startButton.setText("Start");
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(primaryColor);
        panel.add(logoAndQRPanel);
        panel.add(rootPanel);
        JPanel serverControlPanel = new JPanel();
        serverControlPanel.setOpaque(false);
        serverControlPanel.setLayout(new BoxLayout(serverControlPanel, BoxLayout.Y_AXIS));
        serverControlPanel.add(serverPanel);
        serverControlPanel.add(serverLinksPanel);        
        panel.add(serverControlPanel);

        JScrollPane scrollPane = new JScrollPane(this.getContentPane());
        this.setContentPane(scrollPane);
        scrollPane.setViewportView(panel);
        this.getContentPane().setBackground(primaryColor);
        this.setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new App();
            } catch (IOException | FontFormatException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    class ServerLinkPanel extends JPanel {
        JTextField ipText;
        JLabel qrLabel;
        ServerLinkPanel(String ip, Font customFont, Color textBackGroundColor, boolean setQr) {
            ipText = new JFormattedTextField(ip);
            ipText.setFont(customFont);
            ipText.setBackground(textBackGroundColor);
            ipText.setPreferredSize(new Dimension(200,24));
            ipText.setBorder(new LineBorder(textBackGroundColor));
            ipText.setEditable(false);
            qrLabel = new JLabel();
            try {
                if (setQr) {
                    BufferedImage qrCode = QRCodeGenerator.createImage(ip, 200, 200);
                    ImageIcon qrImage = new ImageIcon(qrCode);
                    qrLabel.setIcon(qrImage);
                    App.this.logoAndQRPanel.add(qrLabel);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            ImageIcon copy = new ImageIcon(getClass().getClassLoader().getResource("copy.png"));
            JButton copyButton = new JButton();
            copyButton.setBackground(new Color(158,237,233));
            copyButton.setPreferredSize(new Dimension(24, 24));
            copyButton.setFocusPainted(false);
            copyButton.setIcon(copy);
            copyButton.setToolTipText("Click here to copy the URL");
            copyButton.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ipText.getText()), null));
            this.add(ipText);
            this.add(copyButton);
            this.setOpaque(false);
        }
        ServerLinkPanel(String ip, Font customFont, Color textBackGroundColor) {
            this(ip, customFont, textBackGroundColor, true);
        }

        public void setIpText (String ip) {
            ipText.setText(ip);
            try {
                BufferedImage qrCode = QRCodeGenerator.createImage(ip, 200, 200);
                ImageIcon qrImage = new ImageIcon(qrCode);
                qrLabel.setIcon(qrImage);
                App.this.logoAndQRPanel.add(qrLabel);
                App.this.logoAndQRPanel.revalidate();
                App.this.logoAndQRPanel.repaint();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

}
