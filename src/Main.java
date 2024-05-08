import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class Main extends JFrame {

    ArrayList<LexicalAnalyzer.Token> tokens;
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JButton analyzeButton;
    private JButton createParseTree;
    private File file;

    public Main() {
        setTitle("Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        inputTextArea = new JTextArea(10, 40);
        outputTextArea = new JTextArea(10, 40);
        outputTextArea.setEditable(false); // Add this line to disable editing
        analyzeButton = new JButton("Analyze");
        createParseTree = new JButton("Create Parse Tree");

        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);

        analyzeButton.addActionListener(e -> {
            try {
                analyzeInput();
            } catch (IOException ex) {
                ex.printStackTrace(); // Or handle the exception in a more appropriate way
            }
        });

        createParseTree.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createParseTree();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(inputScrollPane);
        panel.add(outputScrollPane);
        panel.add(analyzeButton);
        panel.add(createParseTree);

        getContentPane().add(panel);
        pack();
        setVisible(true);

        redirectConsoleOutput();

        // Initialize file
        file = new File("src/Lexical.c");

        // Load content of file into inputTextArea
        loadFileContent();

        // Listen for changes in inputTextArea
        inputTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saveChanges();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveChanges();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                saveChanges();
            }
        });
    }

    private void redirectConsoleOutput() {
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputTextArea.append(String.valueOf((char) b));
            }
        };

        PrintStream printStream = new PrintStream(outputStream, true);
        System.setOut(printStream);
    }

 //   private static final String FILE_PATH = "Compilers-Project/src/Lexical.c";

    private void analyzeInput() throws IOException {
        if (!outputTextArea.getText().isEmpty()) {
            outputTextArea.setText("");
        }

        // Call the lexer method to tokenize the input text from inputTextArea
        tokens = LexicalAnalyzer.lexer("src/Lexical.c");


    }

    private void createParseTree() {
        // Placeholder method for creating parse tree
        (new ParseTree(tokens)).parse();
    }

    private void loadFileContent() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            inputTextArea.setText(fileContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveChanges() {
        // Save content of inputTextArea to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.write(inputTextArea.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}
