/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Mrld;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.stream.Collectors;

public class App extends JFrame {
    Server server;
    public App() {
        this.setTitle("Mrld");
        this.setSize(700, 700);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        var panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        var location = new JFormattedTextField();
        location.setEditable(false);
        location.setFont(new Font("Arial", Font.PLAIN, 30));
        location.setText("Press start to start the server");
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(location, c);

        var start = new JButton();
        start.setText("Start");
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(start, c);

        start.addActionListener((actionEvent) -> {
            if (start.getText().equals("Start")) {
                try {
                    server = new Server(8080, "/home/");
                    server.start();
                    location.setText(Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                            .flatMap(a -> Collections.list(a.getInetAddresses()).stream())
                            .filter(InetAddress::isSiteLocalAddress)
                            .filter(a -> !a.isLoopbackAddress())
                            .map(a -> "http:/"+a+":8080/")
                            .collect(Collectors.toList()).toString());

                    start.setText("Stop");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                server.stop();
                location.setText("Press start to start the server");
                start.setText("Start");
            }
        });

        this.add(panel);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
