package logic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;

public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chat Application");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    JCheckBox checkbox = new JCheckBox("Broadcast");
    JFrame listFrame = new JFrame("Client List");
    JList onlineList = new JList();
    DefaultListModel listModel;
    private static HashSet<String> selectedNames = new HashSet<String>();
    Boolean check = true;
    String clientName = "";

    public ChatClient() {

        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "South");
        frame.getContentPane().add(new JScrollPane(onlineList), "Center");
        frame.getContentPane().add(checkbox, BorderLayout.WEST);
        frame.pack();

        checkbox.setSelected(true);

        textField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (check && onlineList.isSelectionEmpty()) {
                    out.println("CHECK" + textField.getText());
                    textField.setText("");
                } else if (check == false) {
                    ListModel model = onlineList.getModel();

                    for (int index : onlineList.getSelectedIndices()) {
                        selectedNames.add(model.getElementAt(index).toString());
                    }

                    if(onlineList.getSelectedIndex()>=1)
                        out.println("SENDER"+frame.getTitle());

                    int count=0;
                    for (String n : selectedNames) {
                        out.println("LIST"+count+n);
                        count++;
                    }

                    out.println("MSG" + textField.getText());
                    textField.setText("");
                    onlineList.clearSelection();
                    selectedNames.clear();
                }

            }
        });

        checkbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (checkbox.isSelected()) {
                    check = true;
                } else {
                    check = false;
                }
            }
        });

    }

    private String getServerAddress() {
        return JOptionPane.showInputDialog(frame, "Enter IP Address of the Server:", "Welcome to the Chatter",
                JOptionPane.QUESTION_MESSAGE);
    }

    private String getName() {
        return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {

        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        listModel = new DefaultListModel();

        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                clientName = getName();
                frame.setTitle(clientName);
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("NAME")) {
                listModel.addElement(line.substring(4));
                onlineList.setModel(listModel);
            } else if (line.startsWith("NEWNAME")) {
                listModel.addElement(line.substring(7));
                onlineList.setModel(listModel);
            } else if (line.startsWith("REMOVE")) {
                listModel.removeElementAt(listModel.indexOf(line.substring(6)));
                onlineList.setModel(listModel);
            } else if (line.startsWith("MESSAGEISO8583MASTERCARD")) {
                String original = line.substring(25);
                String[] fields = original.split("-");
                messageArea.append(String.join("\n", fields) + "\n");
            } else if (line.startsWith("MESSAGEISO8583VISA")) {
                String original = line.substring(19);
                String[] fields = original.split("-");
                messageArea.append(String.join("\n", fields) + "\n");
            }else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}