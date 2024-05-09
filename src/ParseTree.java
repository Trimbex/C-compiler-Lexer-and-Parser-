
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.demo.TextInBox;
import org.abego.treelayout.demo.TextInBoxNodeExtentProvider;
import org.abego.treelayout.demo.swing.TextInBoxTreePane;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/*
"""
1. program → declaration_list
2. declaration_list → declaration declaration_list | ε
3. declaration → var_declaration | fun_declaration
4. var_declaration → [KEYWORD] TYPE_SPECIFIER { BITWISE_OP } ID [ASSIGNMENT_OP expression] SEMICOLON
5. fun_declaration → [KEYWORD] TYPE_SPECIFIER ID LEFT_PAREN param_list RIGHT_PAREN [compound_stmt | SEMICOLON]
6. param_list → param { COMMA param }
7. param → TYPE_SPECIFIER ID
8. compound_stmt → LEFT_BRACE statement_list RIGHT_BRACE
9. statement_list → statement statement_list | ε
10. statement → if_statement | while_statement | do_while_statement | for_statement | return_statement | switch_statement | compound_stmt | declaration | assignment_statement | break_statement | continue_statement | switch_cases
11. if_statement → "if" "(" expression ")" statement ["else" statement]
12. expression → conditional_expression
13. conditional_expression → logical_or_expression ["?" expression ":" expression]
14. logical_or_expression → logical_and_expression { "||" logical_and_expression }
15. logical_and_expression → equality_expression { "&&" equality_expression }
16. equality_expression → relational_expression { ("==" | "!=") relational_expression }
17. relational_expression → additive_expression { ("<" | "<=" | ">" | ">=") additive_expression }
18. additive_expression → multiplicative_expression { ("+" | "-") multiplicative_expression }
19. multiplicative_expression → unary_expression { ("*" | "/" | "%") unary_expression }
20. unary_expression → primary_expression | ("+" | "-") unary_expression | unary_operator unary_expression
21. primary_expression → NUMBER | ID | "(" expression ")" | function_call
22. function_call → ID "(" [ args_opt ] ")"
23. args_opt → args_list | ε
24. args_list → expression { "," expression }
25. while_statement → "while" "(" expression ")" statement
26. do_while_statement → "do" statement "while" "(" expression ")" ";"
27. for_statement → "for" "(" [ for_init_declaration ] ";" [ expression ] ";" [ for_update_exp ] ")" statement
28. for_init_declaration → TYPE_SPECIFIER [ TYPE_SPECIFIER ] ID [ "=" expression ] ";"
29. for_update_exp → ID [ UNARY_OP ]
30. return_statement → "return" [ expression ] ";"
31. switch_statement → "switch" "(" expression ")" "{" switch_cases "}"
32. switch_cases → switch_case { switch_case } [ default_case ]
33. switch_case → "case" expression ":" statement_list
34. default_case → "default" ":" statement_list
35. break_statement → "break" ";"
36. continue_statement → "continue" ";"
37. assignment_statement → ID [ASSIGNMENT_OP | COMPOUND_OP] expression SEMICOLON

"""
 */

public class ParseTree {


    private List<LexicalAnalyzer.Token> tokens;
    private int currentTokenIndex;




    boolean inStatement = false;
    boolean inParenthesis = false;
    boolean noarrayassign = false;
    boolean arrayretract = false;

    private TextInBox root = new TextInBox("Program",80,20);




    // Create a tree and add the root node
    DefaultTreeForTreeLayout<TextInBox> tree = new DefaultTreeForTreeLayout<>(root);




    public ParseTree(List<LexicalAnalyzer.Token> tokens)
    {
        this.tokens = tokens;
        this.currentTokenIndex = 0;

    }

    private boolean match(LexicalAnalyzer.TokenType expectedType) {
        return currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).type == expectedType;
    }

    private static void showInDialog(JComponent panel) {
        // Create a JScrollPane and add the panel to it
        JScrollPane scrollPane = new JScrollPane(panel);

        // Set empty border around the scroll pane
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a dialog and set its content pane to the scroll pane
        JDialog dialog = new JDialog();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Pack and set dialog properties
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void addChild(TextInBox parent,String childname)
    {
        tree.addChild(root,new TextInBox(childname,80,20));
    }

    public void parse()
    {
        while (currentTokenIndex < tokens.size()) {
            declaration_list();
        }

        tree.addChild(root,new TextInBox("EOF",80,20));


        // Setup the tree layout configuration
        double gapBetweenLevels = 50;
        double gapBetweenNodes = 10;
        DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<>(gapBetweenLevels, gapBetweenNodes);

        // Create the NodeExtentProvider for TextInBox nodes
        TextInBoxNodeExtentProvider nodeExtentProvider = new TextInBoxNodeExtentProvider();

        // Create the layout
        TreeLayout<TextInBox> treeLayout = new TreeLayout<>(tree, nodeExtentProvider, configuration);

        // Create a panel that draws the nodes and edges
        TextInBoxTreePane panel = new TextInBoxTreePane(treeLayout);

        // Show the panel in a dialog
        showInDialog(panel);

        System.out.println("Parsing complete.");

    }



    private void advance() {
        currentTokenIndex++;
    }

    private void retract() {
        currentTokenIndex--;
    }

    private String getTokenData()
    {
        return tokens.get(currentTokenIndex).data;
    }




    private void declaration_list()
    {
        // Case 1: declaration declaration_list
        if (match(LexicalAnalyzer.TokenType.KEYWORD) || match(LexicalAnalyzer.TokenType.ID) || match(LexicalAnalyzer.TokenType.TYPE_SPECIFIER)) {

            declaration(root);
            declaration_list(); // Recursive call for declaration_list
        }

        // Case 2: Just declaration
        // In this case, there's nothing more to parse, so we don't need to do anything
    }

    private void declaration(TextInBox decnode) {
        // Check for optional keywords before the type specifier

        TextInBox decl = new TextInBox("decleration",80,20);
        tree.addChild(decnode,decl);

        while (match(LexicalAnalyzer.TokenType.KEYWORD) &&  !tokens.get(currentTokenIndex).data.equals("enum") && !tokens.get(currentTokenIndex).data.equals("struct") )
        {
            TextInBox keywordnode = new TextInBox("Keyword",80,20);
            tree.addChild(decl,keywordnode);
            tree.addChild(keywordnode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the keyword token
        }

        TextInBox keywordnode = new TextInBox("Keyword",80,20);

        // Check if it's an enum declaration
        if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("enum"))
        {
            tree.addChild(decl,keywordnode);
            TextInBox enumkeyword = new TextInBox(getTokenData(),80,20);
            tree.addChild(keywordnode,enumkeyword); // ENUM KEYWORD

            advance(); // Consume the "enum" keyword token
            enum_declaration(decl);

        }
        else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("struct"))
        {
            tree.addChild(decl,keywordnode);
            TextInBox structkeyword = new TextInBox(getTokenData(),80,20);
            tree.addChild(keywordnode,structkeyword);


            advance(); // Consume the "struct" keyword token
            struct_declaration(decl);
        }

        else if (match(LexicalAnalyzer.TokenType.TYPE_SPECIFIER))
        {
            TextInBox typespecifier = new TextInBox("Type Specifier",80,20);
            tree.addChild(decl,typespecifier);
            tree.addChild(typespecifier,new TextInBox(getTokenData(),80,20));

            advance(); // Consume the type specifier token

            // Placeholder for pointer
            while (match(LexicalAnalyzer.TokenType.ARITHMETIC_OP) && tokens.get(currentTokenIndex).data.equals("*"))
            {
                tree.addChild(decl,new TextInBox(getTokenData(),80,20));
                advance();
            }

            // Check for identifier
            if (match(LexicalAnalyzer.TokenType.ID))
            {
                TextInBox idtoken3 = new TextInBox("Identifier",80,20);
                tree.addChild(decl,idtoken3);
                if(!match(LexicalAnalyzer.TokenType.SEMICOLON) && !match(LexicalAnalyzer.TokenType.COMMA))
                tree.addChild(idtoken3,new TextInBox(getTokenData(),80,20));

                advance(); // Consume the identifier token







                if(match(LexicalAnalyzer.TokenType.LEFT_BRACKET))
                {

                    TextInBox arraydec = new TextInBox("Array declaration",80,20);
                    tree.addChild(decl,arraydec);
                    tree.addChild(arraydec,new TextInBox(getTokenData(),80,20));

                    array_declaration(arraydec);
                }

                // Check if it's a function declaration
                else if (match(LexicalAnalyzer.TokenType.LEFT_PAREN))
                {



                    fun_declaration(decl);
                }
                else
                {
                    retract();
                    var_declaration(decl);



                }

                if(match(LexicalAnalyzer.TokenType.COMMA))
                {

                }



            } else {
                // Error handling: Expected identifier after type specifier
                System.err.println("Syntax error: Expecting identifier after type specifier");
            }
        } else {
            // Error handling: Expected "enum" or type specifier
            System.err.println("Syntax error: Expecting 'enum' or type specifier for declaration");
        }
    }


    private void struct_declaration(TextInBox structnode)
    {
        // Check if a struct identifier is present
        TextInBox structdec = new TextInBox("Structure Declaration",80,20);
        tree.addChild(structnode,structdec);


        if (match(LexicalAnalyzer.TokenType.ID))
        {
            TextInBox idtoken = new TextInBox(getTokenData(),80,20);
            tree.addChild(structdec,idtoken);
            advance(); // Consume the struct identifier token
        } else {
            // If no identifier is present, generate one internally
            System.out.println("No struct identifier specified, generating one internally...");
        }

        // Consume the left brace token
        if (match(LexicalAnalyzer.TokenType.LEFT_BRACE))
        {
            tree.addChild(structdec,new TextInBox(getTokenData(),80,20));
            advance();
        } else {
            // Error handling: Expected left brace after "struct" keyword
            System.err.println("Syntax error: Expected '{' after 'struct' keyword in struct declaration");
            return;
        }

        // Parse struct members recursively
        struct_member(structdec);

        // Check for right brace to end the struct declaration
        if (match(LexicalAnalyzer.TokenType.RIGHT_BRACE)) {
            tree.addChild(structdec,new TextInBox(getTokenData(),80,20));
            advance();
        } else {
            // Error handling: Expected right brace to end struct declaration
            System.err.println("Syntax error: Expected '}' to end struct declaration");
            return;
        }

        // Check for semicolon to terminate the struct declaration
        if (!match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            // Error handling: Expected semicolon to terminate struct declaration
            System.err.println("Syntax error: Expected ';' to terminate struct declaration");
        } else {
            tree.addChild(structdec,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the semicolon token
        }
    }

    private void struct_member(TextInBox structnode) {
        // Parse struct member declarations
        while (match(LexicalAnalyzer.TokenType.TYPE_SPECIFIER))
        {
            TextInBox typespecifier = new TextInBox("Type Specifier",80,20);
            tree.addChild(structnode,typespecifier);
            tree.addChild(typespecifier,new TextInBox(getTokenData(),80,20));
            // Parse the type specifier of the struct member
            advance(); // Consume the type specifier token

            // Placeholder for pointer
            while (match(LexicalAnalyzer.TokenType.ARITHMETIC_OP) && tokens.get(currentTokenIndex).data.equals("*"))
            {
                tree.addChild(structnode,new TextInBox(getTokenData(),80,20));
                advance();

            }

            // Check for identifier
            if (match(LexicalAnalyzer.TokenType.ID))
            {
                TextInBox idtoken2 = new TextInBox("Type Specifier",80,20);
                tree.addChild(structnode,idtoken2);
                tree.addChild(idtoken2,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the identifier token

                // Check for array declaration within struct member
                if (match(LexicalAnalyzer.TokenType.LEFT_BRACKET)) {




                    array_declaration(structnode); // Parse array declaration within struct member
                    retract();


                }

                // Check for semicolon or comma
                if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
                    tree.addChild(structnode,new TextInBox(getTokenData(),80,20));
                    advance(); // Consume the semicolon token
                } else if (match(LexicalAnalyzer.TokenType.COMMA)) {
                    tree.addChild(structnode,new TextInBox(getTokenData(),80,20));
                    advance(); // Consume the comma token
                } else {
                    // Error handling: Expected semicolon or comma after struct member declaration
                    System.err.println("Syntax error: Expected ';' or ',' after struct member declaration");
                    return;
                }
            } else {
                // Error handling: Expected identifier for struct member
                System.err.println("Syntax error: Expected identifier for struct member");
                return;
            }
        }
    }


    private void array_declaration(TextInBox parentnode) {
        // Check for optional keywords 'const' and 'static'

/*
        while (match(LexicalAnalyzer.TokenType.KEYWORD) && (tokens.get(currentTokenIndex).data.equals("const") || tokens.get(currentTokenIndex).data.equals("static"))) {
            advance(); // Consume the keyword tokens
        }

        // Parse the array identifier
        if (match(LexicalAnalyzer.TokenType.ID)) {
            advance(); // Consume the array identifier token
        } else {
            // Error handling: Expected identifier for array
            System.err.println("Syntax error: Expected identifier for array");
            return;
        } */

        TextInBox arraydec = new TextInBox("Array declaration",80,20);
        tree.addChild(parentnode,arraydec);
        tree.addChild(arraydec,new TextInBox(getTokenData(),80,20));

        // Parse array dimensions
        parse_array_dimensions(arraydec);

        // Check for assignment or semicolon


        if (match(LexicalAnalyzer.TokenType.ASSIGNMENT_OP)) {


            // Parse array initialization if assignment operator is present
            parse_array_initialization(arraydec);
        } else if (!match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            // Error handling: Expected semicolon or assignment operator
            System.err.println("Syntax error: Expected ';' or assignment operator after array declaration");
        }

        // Consume the semicolon token if present
        if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            tree.addChild(arraydec,new TextInBox(getTokenData(),80,20));
            advance();
        }


    }

    private void parse_array_dimensions(TextInBox parentnode) {
        // Parse array dimensions (square brackets)
        while (match(LexicalAnalyzer.TokenType.LEFT_BRACKET))
        {
            tree.addChild(parentnode,new TextInBox(getTokenData(),80,20));

            advance(); // Consume the left square bracket

            // Check for array dimension size (optional)
            if (match(LexicalAnalyzer.TokenType.NUMBER)) {

                TextInBox number = new TextInBox("Number",80,20);
                tree.addChild(parentnode,number);
                tree.addChild(number,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the number token
            }

            // Check for right square bracket
            if (match(LexicalAnalyzer.TokenType.RIGHT_BRACKET))
            {
                tree.addChild(parentnode,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the right square bracket
                if (match(LexicalAnalyzer.TokenType.LEFT_BRACKET))
                {
                    parse_array_dimensions(parentnode);
                }
            } else {
                // Error handling: Expected right square bracket
                System.err.println("Syntax error: Expected ']' in array dimension declaration");
                return;
            }
        }
    }

    private void parse_array_initialization(TextInBox parentNode)
    {
        tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the assignment operator token

        // Check for left brace to start array initialization
        if(match(LexicalAnalyzer.TokenType.STRING_LITERAL) || match(LexicalAnalyzer.TokenType.CHARACTER_LITERAL))
        {
            if(match(LexicalAnalyzer.TokenType.STRING_LITERAL))
            {
                TextInBox string = new TextInBox("String Literal",80,20);
                tree.addChild(parentNode,string);
                tree.addChild(string,new TextInBox(getTokenData(),80,20));
            } else
            {
                TextInBox string = new TextInBox("Character",80,20);
                tree.addChild(parentNode,string);
                tree.addChild(string,new TextInBox(getTokenData(),80,20));
            }
            advance();
        }
        else if (match(LexicalAnalyzer.TokenType.LEFT_BRACE))
        {
            tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the left brace token

            // Parse array initialization values
            parse_array_initialization_values(parentNode);

            // Check for right brace to end array initialization
            if (match(LexicalAnalyzer.TokenType.RIGHT_BRACE)) {
                tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the right brace token
            } else {
                // Error handling: Expected right brace to end array initialization
                System.err.println("Syntax error: Expected '}' to end array initialization");
            }
        } else {
            // Error handling: Expected left brace to start array initialization
            System.err.println("Syntax error: Expected '{' to start array initialization");
        }
    }

    private void parse_array_initialization_values(TextInBox parentNode)
    {
        // Parse expression for array initialization value



        expression(parentNode);

        //Handle urnary op error





        // Check for comma to continue parsing initialization values
        if (match(LexicalAnalyzer.TokenType.COMMA))
        {
            tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the comma token

            // Parse next expression for initialization value
            parse_array_initialization_values(parentNode);

        }
    }











    private void enum_declaration(TextInBox enumnode) {
        // Check if an enum identifier is present

        TextInBox enumdec = new TextInBox("Enum Declaration",80,20);
        tree.addChild(enumnode,enumdec);



        if (match(LexicalAnalyzer.TokenType.ID))
        {
            TextInBox idtoken = new TextInBox("ID",80,20);
            tree.addChild(enumdec,idtoken);
            tree.addChild(idtoken,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the enum identifier token

        } else {
            // If no identifier is present, generate one internally
            System.out.println("No enum identifier specified, generating one internally...");
        }

        // Consume the left brace token
        if (match(LexicalAnalyzer.TokenType.LEFT_BRACE)) {
            tree.addChild(enumdec,new TextInBox(getTokenData(),80,20));
            advance();
        } else {
            // Error handling: Expected left brace after "enum" keyword
            System.err.println("Syntax error: Expected '{' after 'enum' keyword in enum declaration");
            return;
        }

        // Parse enumeration constants recursively
        enum_item(enumdec);

        // Check for right brace to end the enumeration declaration
        if (match(LexicalAnalyzer.TokenType.RIGHT_BRACE)) {
            tree.addChild(enumdec,new TextInBox(getTokenData(),80,20));
            advance();
        } else {
            // Error handling: Expected right brace to end enum declaration
            System.err.println("Syntax error: Expected '}' to end enum declaration");
            return;
        }

        // Check for semicolon to terminate the enum declaration
        if (!match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            // Error handling: Expected semicolon to terminate enum declaration
            System.err.println("Syntax error: Expected ';' to terminate enum declaration");
        } else {
            advance(); // Consume the semicolon token
        }
    }

    private void enum_item(TextInBox enumitemnode) {
        // Check for enum item identifier
        if (!match(LexicalAnalyzer.TokenType.ID)) {
            // Error handling: Expected identifier for enum item
           // System.err.println("Syntax error: Expected identifier for enum item");
            return;
        }
        TextInBox idtoken = new TextInBox("ID",80,20);
         tree.addChild(enumitemnode,idtoken);
         tree.addChild(idtoken,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the identifier token

        // Check for optional assignment
        if (match(LexicalAnalyzer.TokenType.ASSIGNMENT_OP)) {
            tree.addChild(enumitemnode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the assignment operator token

            // Parse the value assigned to the enumeration constant (For simplicity, assuming only numeric values or identifiers)
            if (match(LexicalAnalyzer.TokenType.NUMBER) || match(LexicalAnalyzer.TokenType.ID))
            {
                TextInBox idtoken2 = new TextInBox("ID",80,20);
                if(match(LexicalAnalyzer.TokenType.NUMBER))
                    tree.addChild(enumitemnode,new TextInBox(getTokenData(),80,20));
                else
                {
                    tree.addChild(enumitemnode,idtoken2);
                    tree.addChild(idtoken2,new TextInBox(getTokenData(),80,20));
                }

                advance(); // Consume the number or identifier token
            } else {
                // Error handling: Expected numeric value or identifier after assignment operator
                System.err.println("Syntax error: Expected numeric value or identifier after assignment operator in enum declaration");
                return;
            }

        }

        // Check for comma to continue defining enumeration constants recursively
        if (match(LexicalAnalyzer.TokenType.COMMA)) {
            tree.addChild(enumitemnode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the comma token
            enum_item(enumitemnode); // Recursive call to parse the next enum item
        }
    }



    private void var_declaration(TextInBox parentNode) {
        // Consume the keyword token
        TextInBox vardec = new TextInBox("Variable declaration",80,20);
        tree.addChild(parentNode,vardec);


        if (match(LexicalAnalyzer.TokenType.ID)) {
            TextInBox idtoken3 = new TextInBox("Identifier", 80, 20);
            tree.addChild(vardec, idtoken3);
            tree.addChild(idtoken3, new TextInBox(getTokenData(), 80, 20));


            advance();


            // Check for optional initializer
            if (match(LexicalAnalyzer.TokenType.ASSIGNMENT_OP)) {
                tree.addChild(vardec, new TextInBox(getTokenData(), 80, 20));

                advance();

                if (match(LexicalAnalyzer.TokenType.BITWISE_OP)) {
                    tree.addChild(vardec, new TextInBox(getTokenData(), 80, 20));
                    advance();
                } // e.g. int x = &address;

                // Parse the expression for initialization
                expression(vardec);


                // Check for semicolon
                if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
                    tree.addChild(vardec, new TextInBox(getTokenData(), 80, 20));
                    advance();
                } else {
                    System.err.println("Syntax error: missing semicolon ");
                    System.exit(0);
                }
            } else if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
                tree.addChild(vardec, new TextInBox(getTokenData(), 80, 20));
                advance();
            } else if(match(LexicalAnalyzer.TokenType.COMMA))
            {
                tree.addChild(parentNode, new TextInBox(getTokenData(), 80, 20));

                advance();

                if(match(LexicalAnalyzer.TokenType.KEYWORD) && !getTokenData().equals("static"))
                {
                    TextInBox keyword = new TextInBox("Identifier",80,20);
                    tree.addChild(vardec,keyword);
                    tree.addChild(keyword,new TextInBox(getTokenData(),80,20));
                    advance();
                }
                else if (match(LexicalAnalyzer.TokenType.ID))
                {
                    var_declaration(parentNode);
                }
                else

                {
                    System.err.println("Syntax error: Expected a declaration");
                    System.exit(0);
                }


            }
            else
            {
                System.err.println("Syntax error: Missing semicolon ");
                System.exit(0);
            }
        }
    }

    private void fun_declaration(TextInBox parentnode) {
        // Assuming we've already matched the type specifier and ID before calling this method
        TextInBox fundec = new TextInBox("Function Declaration",80,20);
        tree.addChild(parentnode,fundec);

        // Consume the left parenthesis token
        if (match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {
            tree.addChild(fundec,new TextInBox(getTokenData(),80,20));
            advance();
        } else {
            // Error handling: Expected left parenthesis
            System.err.println("Syntax error: Expecting left parenthesis in function declaration");
            return;
        }

        // Now let's parse the parameter list
        TextInBox paramlist = new TextInBox("Parameter List",80,20);
        tree.addChild(fundec,paramlist);
        tree.addChild(paramlist,new TextInBox(getTokenData(),80,20));
        param_list(paramlist);

        // Check for right parenthesis after parameter list
        if (match(LexicalAnalyzer.TokenType.RIGHT_PAREN))
        {
            tree.addChild(fundec,new TextInBox(getTokenData(),80,20));
            advance();
        } else {
            // Error handling: Expected right parenthesis
            System.err.println("Syntax error: Expecting right parenthesis in function declaration");
            return;
        }

        // Check if there's a semicolon indicating a function declaration without a body
        if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            tree.addChild(fundec,new TextInBox(getTokenData(),80,20));
            inStatement = false;
            advance(); // Consume the semicolon token
            System.out.println("Function declaration without body parsed successfully.");
            return; // Exit the method since no body is expected
        }

        // Now let's parse the compound statement
        if(!inStatement) {

            TextInBox compstmt = new TextInBox("Compound Statement", 80, 20);
            tree.addChild(fundec, compstmt);
            tree.addChild(compstmt, new TextInBox(getTokenData(), 80, 20));
            compound_stmt(compstmt);
        }
        else
        {
            System.err.println("Syntax Error: expected ; after function call");
            System.exit(0);
        }
    }


    private void param_list(TextInBox parentNode) {
        // Check if there are parameters in the list
        TextInBox param = new TextInBox("Parameter List",80,20);
        if (match(LexicalAnalyzer.TokenType.TYPE_SPECIFIER)) {
            // Parse the first parameter

            tree.addChild(parentNode,param);
            tree.addChild(param,new TextInBox(getTokenData(),80,20));
            param(param);

            // Check for more parameters separated by commas
            while (match(LexicalAnalyzer.TokenType.COMMA))
            {
                tree.addChild(param,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the comma token
                param(param);   // Parse the next parameter
            }
        }
        // No parameters in the list, do nothing
    }

    private void param(TextInBox parentNode) {
        // Check for the type specifier of the parameter
        if (match(LexicalAnalyzer.TokenType.TYPE_SPECIFIER))
        {
            TextInBox typespec = new TextInBox("Type Specifier",80,20);
            tree.addChild(parentNode,typespec);
            tree.addChild(typespec,new TextInBox(getTokenData(),80,20));

            advance(); // Consume the type specifier token

            // Check for the identifier of the parameter
            if (match(LexicalAnalyzer.TokenType.ID)) {


                advance(); // Consume the identifier token
                if(match(LexicalAnalyzer.TokenType.LEFT_BRACKET))
                {

                    inParenthesis = true;

                    retract();
                    array_call(parentNode);

                    inParenthesis = false;


                    System.out.println("Current: " + tokens.get(currentTokenIndex).data);
                }
            } else {
                // Error handling: Expected an identifier for the parameter
                System.err.println("Syntax error: Expecting an identifier for the parameter");
            }
        } else {
            // Error handling: Expected a type specifier for the parameter
            System.err.println("Syntax error: Expecting a type specifier for the parameter");
        }
    }

    private void compound_stmt(TextInBox parentNode)
    {

        TextInBox compstmt = new TextInBox("Compound Stmt",80,20);
        tree.addChild(parentNode,compstmt);

        if (match(LexicalAnalyzer.TokenType.LEFT_BRACE)) {
            tree.addChild(compstmt,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the left brace token

            // Parse the statement list within the compound statement


            statement_list(compstmt);

            if (match(LexicalAnalyzer.TokenType.RIGHT_BRACE)) {
                tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the right brace token
            } else {
                // Error handling: Expected a right brace to close the compound statement
                System.err.println("Syntax error: Expecting '}' to close the compound statement");
                System.exit(0);
            }
        } else {
            // Error handling: Expected a left brace to start the compound statement
            System.err.println("Syntax error: Expecting '{' to start the compound statement");
            System.exit(0);
        }
    }


    private void statement_list(TextInBox parentNode) 
    {
        TextInBox stmtlist = new TextInBox("Statement List",80,20);
        tree.addChild(parentNode,stmtlist);
        // Parse the first statement in the list
        statement(stmtlist);

        // Continue parsing subsequent statements as long as there are more tokens and the next token is not a right brace
        while (currentTokenIndex < tokens.size() && !match(LexicalAnalyzer.TokenType.RIGHT_BRACE)) {
            // Parse the next statement
            statement(stmtlist);
        }
    }



    private void statement(TextInBox stmt) {

        TextInBox statement = new TextInBox("Statement",80,20);
        tree.addChild(stmt,statement);


        // Check the type of statement based on the next token
        if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("if")) {
            // Parse if statement
            if_statement(statement);
        } else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("while")) {
            // Parse while statement
            while_statement(statement);
        } else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("for")) {
            // Parse for statement
            for_statement(statement);
        } else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("return")) {
            // Parse return statement
            return_statement(statement);
        } else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("do")) {
            // Parse do-while statement
            do_while_statement(statement);
        } else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("switch")) {
            // Parse switch statement

            switch_statement(statement);
        }
        else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("enum")) {
            // Parse enum statement
            TextInBox keyword = new TextInBox("Keyword",80,20);
            tree.addChild(keyword,new TextInBox(getTokenData(),80,20));
            advance();
            enum_declaration(statement);
        }

        else if (match(LexicalAnalyzer.TokenType.LEFT_BRACE)) {
            // Parse compound statement
            compound_stmt(statement);
        } else if (match(LexicalAnalyzer.TokenType.TYPE_SPECIFIER)) {
            inStatement = true;
            // Parse declaration statement
            declaration(statement);
        } else if (match(LexicalAnalyzer.TokenType.ID)) {
            // Parse assignment statement

            if (tokens.get(currentTokenIndex + 1).type == LexicalAnalyzer.TokenType.LEFT_BRACKET) {
                // It's an array call
                array_call(statement);
            }
            else if (tokens.get(currentTokenIndex + 1).type == LexicalAnalyzer.TokenType.LEFT_PAREN)
            {
                function_call(statement);
            } else if(tokens.get(currentTokenIndex + 1).type == LexicalAnalyzer.TokenType.UNARY_OP)
            {
                expression(statement);
                advance();
            }
            else
            {
                // It's an assignment statement
                assignment_statement(statement);
            }
        }
        if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("break")) {
            // Parse break statement
            break_statement(statement);
        } else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("continue")) {
            // Parse continue statement
            continue_statement(statement);
        }
        else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("case"))
        {  //must check for switch
            for (int i = currentTokenIndex - 1; i >= 0; i--) {
                if (tokens.get(i).type == LexicalAnalyzer.TokenType.KEYWORD && tokens.get(i).data.equals("switch")) {
                    switch_cases(statement);
                }
            }
        } else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("default"))
        {  //must check for switch
            for (int i = currentTokenIndex - 1; i >= 0; i--) {
                if (tokens.get(i).type == LexicalAnalyzer.TokenType.KEYWORD && tokens.get(i).data.equals("switch")) {
                    switch_cases(statement);
                }
            }
        }
        else {
            // Parse expression statement
            // expression_statement();
        }
    }

    private void array_call(TextInBox parentNode) {
        // Parse the identifier
        if (match(LexicalAnalyzer.TokenType.ID))
        {
            TextInBox idtoken = new TextInBox("Identifier",80,20);
            tree.addChild(parentNode,idtoken);
            tree.addChild(idtoken,new TextInBox(getTokenData(),80,20));

            advance(); // Consume the identifier token
        } else {
            // Error handling: Expected an identifier for array call
            System.err.println("Syntax error: Expecting an identifier for array call");
            return;
        }

        // Parse array dimensions
        parse_array_dimensions_in_call(parentNode);


     //   System.out.println("Current array: " + tokens.get(currentTokenIndex).data);

        if(match(LexicalAnalyzer.TokenType.ASSIGNMENT_OP))
        {
            tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
            advance();

            if(match(LexicalAnalyzer.TokenType.CHARACTER_LITERAL) || match(LexicalAnalyzer.TokenType.STRING_LITERAL))
            {
                if(match(LexicalAnalyzer.TokenType.CHARACTER_LITERAL))
                {
                    TextInBox charc = new TextInBox("Character",80,20);
                    tree.addChild(parentNode,charc);
                    tree.addChild(charc,new TextInBox(getTokenData(),80,20));
                }
                else
                {
                    TextInBox string = new TextInBox("String Literal",80,20);
                    tree.addChild(parentNode,string);
                    tree.addChild(string,new TextInBox(getTokenData(),80,20));
                }

                advance();

            }
            else
            {

                //ARRAY RETRACT HERE



                //I WAS HERE
                expression(parentNode);





                if(arrayretract)
                {

                    retract();
                    arrayretract = false;
                }



            }

            if(match(LexicalAnalyzer.TokenType.SEMICOLON))
            {
                tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
                advance();
                return;
            }
            else
            {
                System.out.println("Current " + tokens.get(currentTokenIndex).data);
                System.err.println("Syntax error: Expecting a semicolon for an array call");
                System.exit(0);
            }
        }
        // Check for semicolon to terminate the array call statement
        else if (!match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            // Error handling: Expected semicolon to terminate array call
            if(!inParenthesis)
                System.err.println("Syntax error: Expected ';' to terminate array call");
        } else {
            tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the semicolon token
        }
    }

    private void parse_array_dimensions_in_call(TextInBox parentNode) {
        // Parse array dimensions (square brackets)
        if (match(LexicalAnalyzer.TokenType.LEFT_BRACKET))
        {
            tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the left square bracket

            // Parse the expression inside square brackets
            expression(parentNode); // Assuming you have a method to parse expressions

            // Check for right square bracket
            if (match(LexicalAnalyzer.TokenType.RIGHT_BRACKET))
            {
                tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the right square bracket

                if (match(LexicalAnalyzer.TokenType.LEFT_BRACKET))
                {

                    parse_array_dimensions_in_call(parentNode);
                }
            } else {
                // Error handling: Expected right square bracket
                System.err.println("Syntax error: Expected ']' in array dimension for array call");
                return;
            }
        }
    }



    private void break_statement(TextInBox parentNode) {
        // Consume the "break" keyword
        TextInBox keyword = new TextInBox("Keyword",80,20);
        tree.addChild(parentNode,keyword);
        tree.addChild(keyword,new TextInBox(getTokenData(),80,20));
        advance();

        // Check for semicolon
        if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the semicolon token
        } else {
            // Error handling: Missing semicolon after break statement
            System.err.println("Syntax error: Missing semicolon after 'break' statement");
        }
    }

    private void continue_statement(TextInBox parentNode) {
        // Consume the "continue" keyword
        TextInBox keyword = new TextInBox("Keyword",80,20);
        tree.addChild(parentNode,keyword);
        tree.addChild(keyword,new TextInBox(getTokenData(),80,20));
        advance();

        // Check for semicolon
        if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the semicolon token
        } else {
            // Error handling: Missing semicolon after continue statement
            System.err.println("Syntax error: Missing semicolon after 'continue' statement");
        }
    }

    private void switch_statement(TextInBox parentNode) {
        // Consume the "switch" keyword
        TextInBox switchstmt = new TextInBox("Switch Statement",80,20);
        TextInBox keyword = new TextInBox("Keyword",80,20);
        tree.addChild(parentNode,switchstmt);
        tree.addChild(keyword,new TextInBox(getTokenData(),80,20));
        advance();

        // Check for the opening parenthesis
        if (!match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {

            // Syntax error: Expected opening parenthesis
            System.err.println("Syntax error: Expected '(' after 'switch'");
            return;
        }
        tree.addChild(switchstmt,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the '(' token

        // Parse the expression inside the parenthesis
        expression(switchstmt);

        // Check for the closing parenthesis
        if (!match(LexicalAnalyzer.TokenType.RIGHT_PAREN)) {
            // Syntax error: Expected closing parenthesis
            System.err.println("Syntax error: Expected ')' after expression in 'switch' statement");
            return;
        }
        tree.addChild(switchstmt,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the ')' token

        // Check if there's only an expression and a semicolon
        if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            tree.addChild(switchstmt,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the semicolon token
            System.out.println("Switch statement with only expression and semicolon parsed successfully.");
            return;
        }

        // Check for the opening brace
        if (!match(LexicalAnalyzer.TokenType.LEFT_BRACE)) {

            // Syntax error: Expected opening brace
            System.err.println("Syntax error: Expected '{' after expression in 'switch' statement");
            return;
        }
        tree.addChild(switchstmt,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the '{' token

        // Parse the switch cases
        switch_cases(switchstmt);

        // Check for the closing brace
        if (!match(LexicalAnalyzer.TokenType.RIGHT_BRACE)) {

            // Syntax error: Expected closing brace
            System.err.println("Syntax error: Expected '}' after switch cases");
            return;
        }
        tree.addChild(switchstmt,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the '}' token

        // Check for optional semicolon after the switch statement
        if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            tree.addChild(switchstmt,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the semicolon token
            System.out.println("Switch statement parsed successfully.");
            return;
        }
    }



    private void switch_cases(TextInBox parentNode) {
        // Parse each case in the switch statement

        while (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("case")) {


            switch_case(parentNode);


        }

        // Check if there's a default case
        if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("default"))
        {


            default_case(parentNode);
        }
    }

    private void switch_case(TextInBox parentNode) {
        // Consume the "case" keyword
        TextInBox keyword = new TextInBox("Keyword",80,20);
        tree.addChild(parentNode,keyword);
        tree.addChild(keyword,new TextInBox(getTokenData(),80,20));

        advance();

        // Parse the case value
        expression(parentNode);

        // Check for the colon
        if (!match(LexicalAnalyzer.TokenType.COLON))
        {
            // Syntax error: Expected colon after case value
            System.err.println("Syntax error: Expected ':' after case value");
            return;
        }
        tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));

        advance(); // Consume the ':' token

        // Parse the statements within the case block
        statement_list(parentNode);


    }

    private void default_case(TextInBox parentNode) {
        // Consume the "default" keyword
        TextInBox keyword = new TextInBox("Keyword",80,20);
        tree.addChild(parentNode,keyword);
        tree.addChild(keyword,new TextInBox(getTokenData(),80,20));
        advance();

        // Check for the colon
        if (!match(LexicalAnalyzer.TokenType.COLON))
        {

            // Syntax error: Expected colon after "default"
            System.err.println("Syntax error: Expected ':' after 'default'");
            return;
        }
        tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the ':' token

        // Parse the statements within the default block
        statement_list(parentNode);
    }



    private void assignment_statement(TextInBox parentNode) {
        // Assuming the current token is an identifier (variable name)
        String variableName = tokens.get(currentTokenIndex).data;
        TextInBox assignstmt = new TextInBox("Assignment Statement",100,20);
        TextInBox idtoken = new TextInBox("Identifier",100,20);
        tree.addChild(parentNode,assignstmt);
        tree.addChild(assignstmt,idtoken);
        tree.addChild(idtoken,new TextInBox(getTokenData(),100,20));
        advance(); // Consume the variable name token

        // Check for the assignment operator
        if (match(LexicalAnalyzer.TokenType.ASSIGNMENT_OP) || match(LexicalAnalyzer.TokenType.COMPOUND_OP))
        {
            tree.addChild(assignstmt,new TextInBox(getTokenData(),80,20));





            advance(); // Consume the assignment operator



            // Parse the expression on the right-hand side of the assignment
            expression(assignstmt);





           // System.out.println(inParenthesis);


            if(inParenthesis)
            {
                retract();
                inParenthesis = false;

            }



            // Check for semicolon
            if (match(LexicalAnalyzer.TokenType.SEMICOLON))
            {
                tree.addChild(assignstmt,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the semicolon token
            } else {
              //  System.out.println("Currentttttt " + tokens.get(currentTokenIndex).data);
                System.err.println("Syntax error: Missing semicolon after assignment statement");
                System.exit(0);
            }
        } else {
            System.err.println("Syntax error: Expected assignment operator after variable name");
            System.exit(0);
        }
    }



    private void if_statement(TextInBox parentNode)
    {

        TextInBox ifstmt = new TextInBox("if Statement",100,20);
        TextInBox idtoken = new TextInBox("Keyword",100,20);
        tree.addChild(parentNode,ifstmt);
        tree.addChild(ifstmt,idtoken);
        tree.addChild(idtoken,new TextInBox(getTokenData(),100,20));


        // Consume the "if" keyword
        advance();

        // Check for the opening parenthesis
        if (!match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {
            // Syntax error: Expected opening parenthesis
            System.err.println("Syntax error: Expected '(' after 'if'");
            return;
        }
        tree.addChild(ifstmt,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the '(' token

        // Parse the expression inside the parenthesis
        expression(ifstmt);

        // Check for the closing parenthesis
        if (!match(LexicalAnalyzer.TokenType.RIGHT_PAREN)) {
            // Syntax error: Expected closing parenthesis
            System.err.println("Syntax error: Expected ')' after expression in 'if' statement");
            return;
        }
        tree.addChild(ifstmt,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the ')' token

        if(match(LexicalAnalyzer.TokenType.SEMICOLON))
        {
            tree.addChild(ifstmt,new TextInBox(getTokenData(),80,20));
            advance();
            return;
        }













        // Parse the statement or compound statement after the if condition
        statement(ifstmt);









        if(match(LexicalAnalyzer.TokenType.SEMICOLON))
        {
            tree.addChild(ifstmt,new TextInBox(getTokenData(),80,20));
            advance();
            return;
        }
        else if(match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("else") && tokens.get(currentTokenIndex++).data.equals("if"))
        {
            TextInBox keyword = new TextInBox("Keyword",80,20);
            tree.addChild(parentNode,keyword);
            tree.addChild(keyword,new TextInBox(getTokenData(),80,20));


            advance();
            if_statement(parentNode);

        }
        else if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("else"))
        {
            TextInBox keyword = new TextInBox("Keyword",80,20);
            tree.addChild(parentNode,keyword);
            tree.addChild(keyword,new TextInBox(getTokenData(),80,20));
            advance();
            statement(parentNode);
        }




        /*

        // Check if there's an "else if" part
        while (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("else") &&
                currentTokenIndex + 1 < tokens.size() && tokens.get(currentTokenIndex + 1).type == LexicalAnalyzer.TokenType.KEYWORD &&
                tokens.get(currentTokenIndex + 1).data.equals("if")) {
            advance(); // Consume the "else" keyword
            advance(); // Consume the "if" keyword
            // Recursively parse the "else if" part
            statement();
        }
        // Check if there's an "else" part
        if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("else")) {
            advance(); // Consume the "else" keyword
            // Parse the statement or compound statement after the else part
            statement();
        } */
    }


    private void expression(TextInBox parentNode)
    {
        TextInBox expressionnode = new TextInBox("Expression",80,20);
        tree.addChild(parentNode,expressionnode);
        conditional_expression(expressionnode); // Parse the conditional expression
    }

    private void conditional_expression(TextInBox parentNode)
    {
        TextInBox expressionnode = new TextInBox("Cond Exp",80,20);

        logical_or_expression(parentNode); // Parse the logical OR expression

        // Check if the current token is the ternary operator '?'
        if (match(LexicalAnalyzer.TokenType.TERNARY_OP)) {
            tree.addChild(parentNode,expressionnode);
            tree.addChild(expressionnode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the '?' token

            // Parse the expression for the true condition
            expression(expressionnode);

            // Check for the colon ':' token
            if (match(LexicalAnalyzer.TokenType.COLON)) {
                tree.addChild(expressionnode,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the ':' token

                // Parse the expression for the false condition
                expression(expressionnode);
            } else {
                // Error handling: Expected ':' after the true condition
                System.err.println("Syntax error: Expected ':' after the true condition in ternary operator");
            }
        }
    }

    private void logical_or_expression(TextInBox parentNode)
    {
        TextInBox expressionnode = new TextInBox("Logical OR Exp",80,20);

        logical_and_expression(parentNode);

        while (match(LexicalAnalyzer.TokenType.COMPARISON_OP) && tokens.get(currentTokenIndex).data.equals("||"))
        {
            tree.addChild(expressionnode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the '||' token

            // Check if there is a valid token after the logical OR operator
            if (match(LexicalAnalyzer.TokenType.ID) || match(LexicalAnalyzer.TokenType.NUMBER) || match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {
                // Valid token found, continue parsing the expression
                logical_and_expression(expressionnode);
            } else {
                // Error handling: Expected identifier, number, or left parenthesis after logical OR operator
                System.err.println("Syntax error: Expected identifier, number, or expression after logical OR operator");
                return;
            }
        }
    }

    private void logical_and_expression(TextInBox parentNode)
    {
        TextInBox expressionnode = new TextInBox("Logical AND Exp",80,20);


    //    logical_and_expression(parentNode);
        equality_expression(parentNode);

        while (match(LexicalAnalyzer.TokenType.COMPARISON_OP) && tokens.get(currentTokenIndex).data.equals("&&"))
        {
            tree.addChild(parentNode,expressionnode);
            tree.addChild(expressionnode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the '&&' token

            // Check if there is a valid token after the logical AND operator
            if (match(LexicalAnalyzer.TokenType.ID) || match(LexicalAnalyzer.TokenType.NUMBER) || match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {
                // Valid token found, continue parsing the expression
                equality_expression(expressionnode);
            } else {
                // Error handling: Expected identifier, number, or left parenthesis after logical AND operator
                System.err.println("Syntax error: Expected identifier, number, or expression after logical AND operator");
                return;
            }
        }
    }

    private void equality_expression(TextInBox parentNode)
    {
        TextInBox expressionnode = new TextInBox("Equality Exp",80,20);

        relational_expression(parentNode);

        while (match(LexicalAnalyzer.TokenType.COMPARISON_OP) &&
                (tokens.get(currentTokenIndex).data.equals("==") || tokens.get(currentTokenIndex).data.equals("!=")))
        {
            tree.addChild(parentNode,expressionnode);
            tree.addChild(expressionnode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the equality operator

            // Check if there is a valid token after the comparison operator
            if (match(LexicalAnalyzer.TokenType.ID) || match(LexicalAnalyzer.TokenType.NUMBER) || match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {
                // Valid token found, continue parsing the expression
                relational_expression(expressionnode);
            } else {
                // Error handling: Expected identifier, number, or left parenthesis after comparison operator
                System.err.println("Syntax error: Expected identifier, number, or expression after comparison operator");
                return;
            }
        }
    }


    private void relational_expression(TextInBox parentNode)
    {
        TextInBox expressionnode = new TextInBox("Relational Exp",80,20);
        additive_expression(parentNode);

        while (match(LexicalAnalyzer.TokenType.COMPARISON_OP) &&
                (tokens.get(currentTokenIndex).data.equals("<") || tokens.get(currentTokenIndex).data.equals("<=") ||
                        tokens.get(currentTokenIndex).data.equals(">") || tokens.get(currentTokenIndex).data.equals(">=")))
        {
            tree.addChild(parentNode,expressionnode);
            tree.addChild(expressionnode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the relational operator

            // Check if there is a valid token after the relational operator
            if (match(LexicalAnalyzer.TokenType.ID) || match(LexicalAnalyzer.TokenType.NUMBER) || match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {
                // Valid token found, continue parsing the expression
                additive_expression(expressionnode);
            } else {
                // Error handling: Expected identifier, number, or left parenthesis after relational operator
                System.err.println("Syntax error: Expected identifier, number, or expression after relational operator");
                return;
            }
        }
    }

    private void additive_expression(TextInBox parentNode)
    {
        TextInBox expressionnode = new TextInBox("Additive Exp",80,20);
        multiplicative_expression(parentNode);

        while (match(LexicalAnalyzer.TokenType.ARITHMETIC_OP) &&
                (tokens.get(currentTokenIndex).data.equals("+") || tokens.get(currentTokenIndex).data.equals("-")))
        {
            tree.addChild(parentNode,expressionnode);
            tree.addChild(expressionnode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the additive operator

            // Check if there is a valid token after the additive operator
            if (match(LexicalAnalyzer.TokenType.ID) || match(LexicalAnalyzer.TokenType.NUMBER) || match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {
                // Valid token found, continue parsing the expression
                multiplicative_expression(expressionnode);
            } else {
                // Error handling: Expected identifier, number, or left parenthesis after additive operator
                System.err.println("Syntax error: Expected identifier, number, or expression after additive operator");
                return;
            }
        }
    }

    private void multiplicative_expression(TextInBox parentNode)
    {
        TextInBox expressionnode = new TextInBox("Multiplicative Exp",100,20);
        unary_expression(parentNode);

        while (match(LexicalAnalyzer.TokenType.ARITHMETIC_OP) &&
                (tokens.get(currentTokenIndex).data.equals("*") || tokens.get(currentTokenIndex).data.equals("/") ||
                        tokens.get(currentTokenIndex).data.equals("%")))
        {
            tree.addChild(parentNode,expressionnode);
            tree.addChild(expressionnode,new TextInBox(getTokenData(),100,20));
            advance(); // Consume the multiplicative operator

            // Check if there is a valid token after the multiplicative operator
            if (match(LexicalAnalyzer.TokenType.ID) || match(LexicalAnalyzer.TokenType.NUMBER) || match(LexicalAnalyzer.TokenType.LEFT_PAREN))
            {
                // Valid token found, continue parsing the expression
                unary_expression(expressionnode);
            } else {
                // Error handling: Expected identifier, number, or left parenthesis after multiplicative operator
                System.err.println("Syntax error: Expected identifier, number, or expression after multiplicative operator");
                return;
            }
        }
    }

    private void unary_expression(TextInBox parentNode)
    {
        TextInBox expressionnode = new TextInBox("Unary Exp",100,20);

        if (match(LexicalAnalyzer.TokenType.ID) || match(LexicalAnalyzer.TokenType.NUMBER) || match(LexicalAnalyzer.TokenType.LEFT_PAREN))
        {

            primary_expression(parentNode); // Parse primary expression

        }
        else if (match(LexicalAnalyzer.TokenType.ARITHMETIC_OP) &&
                (tokens.get(currentTokenIndex).data.equals("+") || tokens.get(currentTokenIndex).data.equals("-")))
        {

            tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the unary operator

            // Check if there is a valid token after the unary operator
            if (match(LexicalAnalyzer.TokenType.ID) || match(LexicalAnalyzer.TokenType.NUMBER) || match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {
                // Valid token found, continue parsing the expression

                unary_expression(parentNode);
            } else {
                // Error handling: Expected identifier, number, or left parenthesis after unary operator
                System.err.println("Syntax error: Expected identifier, number, or expression after unary operator");
                return;
            }
        }
        else if (match(LexicalAnalyzer.TokenType.UNARY_OP))
        {

            tree.addChild(parentNode,expressionnode);

            int prevIndex = currentTokenIndex - 1;
            int nextIndex = currentTokenIndex + 1;
            boolean isPrevIdentifier = prevIndex >= 0 && tokens.get(prevIndex).type == LexicalAnalyzer.TokenType.ID;
            boolean isNextIdentifier = nextIndex < tokens.size() && tokens.get(nextIndex).type == LexicalAnalyzer.TokenType.ID;

           // System.out.println("TESTTT");

            // Handle unary operators like '++' and '--'
            String unaryOp = tokens.get(currentTokenIndex).data;
            tree.addChild(expressionnode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the unary operator

            // Check if there is a valid token after the unary operator
            if(!isPrevIdentifier && !isNextIdentifier)
            {
                System.err.println("Missing identifier");
                System.exit(0);
                return;

            }
            else if (match(LexicalAnalyzer.TokenType.ID))
            {
                TextInBox idtoken = new TextInBox("Identifer",80,20);
                tree.addChild(expressionnode,idtoken);

                tree.addChild(idtoken,new TextInBox(getTokenData(),80,20));
                // Consume the identifier token
                advance();

                // Parse the post-increment/decrement expression
              //  System.out.println("Parsed post-" + unaryOp + " expression");
            }
            else if(match(LexicalAnalyzer.TokenType.ARITHMETIC_OP))
            {



                unary_expression(parentNode);
            }
            else if (match(LexicalAnalyzer.TokenType.SEMICOLON))
            {
            tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));




                return; // NO ITS NEEDED

            }
            else {

                // Error handling: Expected identifier after unary operator

                // System.err.println("Syntax error: Expected identifier after unary operator '" + unaryOp + "'");
                return;
            }
        }
    }




    private void primary_expression(TextInBox parentNode)
    {
        TextInBox expressionnode = new TextInBox("Primary Exp",100,20);
        tree.addChild(parentNode,expressionnode);
        if (match(LexicalAnalyzer.TokenType.NUMBER))
        {
           tree.addChild(expressionnode,new TextInBox(getTokenData(),100,20));
            advance(); // Consume the number token
            arrayretract = false;
        } else if (match(LexicalAnalyzer.TokenType.LEFT_PAREN))
        {
            tree.addChild(expressionnode,new TextInBox(getTokenData(),100,20));
            advance(); // Consume the left parenthesis token
            expression(expressionnode); // Parse the enclosed expression
            arrayretract = false;
            if (match(LexicalAnalyzer.TokenType.RIGHT_PAREN)) {
                tree.addChild(expressionnode,new TextInBox(getTokenData(),100,20));
                advance(); // Consume the right parenthesis token
                arrayretract = false;
            } else {
                // Error handling: Expected right parenthesis
                System.err.println("Syntax error: Missing right parenthesis");
                arrayretract = false;
                System.exit(0);
            }
        }
        else if (match(LexicalAnalyzer.TokenType.ID))
        {

            // Check if it's a function call or a regular identifier
            int currentIndex = currentTokenIndex; // Store current index
            advance(); // Consume the identifier token
            if (match(LexicalAnalyzer.TokenType.LEFT_PAREN))
            {
                // It's a function call
                currentTokenIndex = currentIndex; // Reset index
                arrayretract = false;
                retract(); //CHECKLATERRRR
                function_call(expressionnode); // Parse function call
            }
            else if(match(LexicalAnalyzer.TokenType.LEFT_BRACKET))

            {

                inParenthesis = true; arrayretract = true;
                retract();
                array_call(expressionnode);


            }



            else {
                TextInBox idtoken2 = new TextInBox("Identifier",80,20);
                tree.addChild(expressionnode,idtoken2);
                retract();
                tree.addChild(idtoken2,new TextInBox(getTokenData(),80,20));
                advance();
                // It's a regular identifier
                currentTokenIndex = currentIndex; // Reset index
                // Handle regular identifier logic here
                advance(); // Consume the identifier token

                // Check if the next token is a unary operator
                if (match(LexicalAnalyzer.TokenType.UNARY_OP)) {
                    arrayretract = false;
                    // Handle unary expression
                    unary_expression(expressionnode);
                }
            }
        }
    }




    private void function_call(TextInBox parentNode)
    {
        TextInBox funcall = new TextInBox("Function Call",80,20);
        tree.addChild(parentNode,funcall);

        if (match(LexicalAnalyzer.TokenType.ID))
        {
            tree.addChild(funcall,new TextInBox(getTokenData(),80,20));

            advance(); // Consume the identifier token
        } else {
            // Error handling: Expected identifier for function name
            System.err.println("Syntax error: Expected identifier for function name in function call");
            return;
        }

        // Check for left parenthesis
        if (match(LexicalAnalyzer.TokenType.LEFT_PAREN))
        {
            tree.addChild(funcall,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the left parenthesis token

        } else {
            // Error handling: Expected left parenthesis
            System.err.println("Syntax error: Expected '(' after function name in function call");
            return;
        }

        // Parse the optional arguments

        args_opt(funcall);

        // Check for right parenthesis
        if (match(LexicalAnalyzer.TokenType.RIGHT_PAREN))
        {
            tree.addChild(funcall,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the right parenthesis token

            while (match(LexicalAnalyzer.TokenType.DOT)) {
                tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the dot operator
                // Repeat function call parsing for chaining
                function_call(parentNode);
                retract();
            }

            if(match(LexicalAnalyzer.TokenType.SEMICOLON))
            {
                tree.addChild(funcall,new TextInBox(getTokenData(),80,20));
                advance();
            } else
            {

                System.err.println("Syntax error: Expected ';' after function call");
            }


        } else {
            // Error handling: Expected right parenthesis
            System.err.println("Syntax error: Expected ')' after function arguments in function call");
        }
    }

    private void args_opt(TextInBox parentNode)
    {


        if (match(LexicalAnalyzer.TokenType.ID) || match(LexicalAnalyzer.TokenType.NUMBER) || match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {
            System.out.println("Entering args_opt()");

            args_list(parentNode); // Parse the arguments list
        } else {
            System.out.println("No arguments found in args_opt()");


        }
    }

    private void args_list(TextInBox parentNode) {
        System.out.println("Entering args_list()");




        expression(parentNode); // Parse the first argument

        // Check for more arguments separated by commas
        if (match(LexicalAnalyzer.TokenType.COMMA)) {
            tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the comma token
            args_list(parentNode); // Parse the next argument
        }
    }


    private void while_statement(TextInBox parentNode)
    {
        // Consume the "while" keyword
        TextInBox whilestmt = new TextInBox("While Statement",80,20);
        tree.addChild(parentNode,whilestmt);
        tree.addChild(whilestmt,new TextInBox(getTokenData(),80,20));
        advance();

        // Check for the opening parenthesis
        if (!match(LexicalAnalyzer.TokenType.LEFT_PAREN))
        {
            // Syntax error: Expected opening parenthesis
            System.err.println("Syntax error: Expected '(' after 'while'");
            return;
        }
        tree.addChild(whilestmt,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the '(' token

        // Parse the expression inside the parenthesis
        expression(whilestmt);

        // Check for the closing parenthesis
        if (!match(LexicalAnalyzer.TokenType.RIGHT_PAREN)) {
            // Syntax error: Expected closing parenthesis
            System.err.println("Syntax error: Expected ')' after expression in 'while' statement");
            return;
        }
        tree.addChild(whilestmt,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the ')' token

        // Parse the statement after the while loop condition
        statement(whilestmt);

        // Check for the semicolon at the end of the while loop
        if (!match(LexicalAnalyzer.TokenType.SEMICOLON))
        {
            // Syntax error: Expected semicolon at the end of while loop
            System.err.println("Syntax error: Expected ';' at the end of 'while' statement");
            return;
        }
        tree.addChild(whilestmt,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the ';' token
    }

    private void do_while_statement(TextInBox parentNode)
    {
        TextInBox dowhilestmt = new TextInBox("While Statement",80,20);
        tree.addChild(parentNode,dowhilestmt);
        tree.addChild(dowhilestmt,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the "do" keyword

        // Parse the statement block
        statement(dowhilestmt);

        // Check for the "while" keyword
        if (match(LexicalAnalyzer.TokenType.KEYWORD) && tokens.get(currentTokenIndex).data.equals("while"))
        {
            TextInBox keyword = new TextInBox("Keyword",80,20);
            tree.addChild(dowhilestmt,keyword);
            tree.addChild(keyword,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the "while" keyword

            // Check for the opening parenthesis
            if (!match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {

                // Syntax error: Expected opening parenthesis
                System.err.println("Syntax error: Expected '(' after 'while'");
                return;
            }
            tree.addChild(dowhilestmt,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the '(' token

            // Parse the expression inside the parenthesis
            expression(dowhilestmt);

            // Check for the closing parenthesis
            if (!match(LexicalAnalyzer.TokenType.RIGHT_PAREN)) {
                // Syntax error: Expected closing parenthesis
                System.err.println("Syntax error: Expected ')' after expression in 'while' statement");
                return;
            }
            tree.addChild(dowhilestmt,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the ')' token

            // Check for the semicolon at the end of the do-while statement
            if (!match(LexicalAnalyzer.TokenType.SEMICOLON)) {
                // Syntax error: Expected semicolon at the end of do-while statement
                System.err.println("Syntax error: Expected ';' at the end of 'do-while' statement");
                return;
            }
            tree.addChild(dowhilestmt,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the ';' token
        } else {
            // Syntax error: Expected "while" keyword after "do" statement block
            System.err.println("Syntax error: Expected 'while' keyword after 'do' statement block");
        }
    }


    private void for_statement(TextInBox parentNode)
    {
        TextInBox forstmt = new TextInBox("For Statement",80,20);
        tree.addChild(parentNode,forstmt);
        tree.addChild(forstmt,new TextInBox(getTokenData(),80,20));
        advance(); // for

        if (match(LexicalAnalyzer.TokenType.LEFT_PAREN)) {
            tree.addChild(forstmt,new TextInBox(getTokenData(),80,20));
            advance();

            // Check for semicolon or initialization expression
            if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
                tree.addChild(forstmt,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the semicolon token
            } else {
                for_init_declaration(forstmt);


                // Check for semicolon after initialization expression
                if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
                    tree.addChild(forstmt,new TextInBox(getTokenData(),80,20));
                    advance(); // Consume the semicolon token
                } else {
                    // Error handling: Expected semicolon after initialization expression
                    System.err.println("Syntax error: Missing semicolon after initialization expression in for loop");
                    return;
                }
            }

            // Check for semicolon or condition expression
            if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
                tree.addChild(forstmt,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the semicolon token
            } else {
                expression(forstmt);


                // Check for semicolon after condition expression
                if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
                    tree.addChild(forstmt,new TextInBox(getTokenData(),80,20));
                    advance(); // Consume the semicolon token

                } else {
                    // Error handling: Expected semicolon after condition expression
                    System.err.println("Syntax error: Missing semicolon after condition expression in for loop");
                    return;
                }
            }

            // Check for right parenthesis
            if (match(LexicalAnalyzer.TokenType.RIGHT_PAREN)) {
                tree.addChild(forstmt,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the right parenthesis token
            } else {
                // Error handling: Expected right parenthesis
                for_update_exp(forstmt);
                if (match(LexicalAnalyzer.TokenType.RIGHT_PAREN)) {
                    tree.addChild(forstmt,new TextInBox(getTokenData(),80,20));
                    advance();
                } else {
                    System.err.println("Syntax error: Missing right parenthesis in for loop");
                    return;
                }
            }

            // Parse the body of the for loop
            statement(forstmt);
        } else {
            System.err.println("Syntax error: Expected '(' after 'for' statement");
        }
    }






    private void for_init_declaration(TextInBox parentNode) {
        // Check for the type specifier
        if (match(LexicalAnalyzer.TokenType.TYPE_SPECIFIER))
        {
            TextInBox typespecifier = new TextInBox("TypeSpecifier",80,20);
            tree.addChild(parentNode,typespecifier);
            tree.addChild(typespecifier,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the type specifier token

            // Optionally, check for additional type specifier like 'const'
            if (match(LexicalAnalyzer.TokenType.TYPE_SPECIFIER))
            {
                tree.addChild(parentNode,typespecifier);
                tree.addChild(typespecifier,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the additional type specifier token
            }

            // Check for the identifier
            if (match(LexicalAnalyzer.TokenType.ID))
            {
                TextInBox idtoken = new TextInBox("Identifier",80,20);
                tree.addChild(parentNode,idtoken);
                tree.addChild(idtoken,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the identifier token

                // Check if there's an initializer
                if (match(LexicalAnalyzer.TokenType.ASSIGNMENT_OP))
                {
                    tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
                    advance(); // Consume the assignment operator token

                    // Parse the initializer expression
                    expression(parentNode); // Placeholder for parsing the initializer expression
                } else if (!match(LexicalAnalyzer.TokenType.SEMICOLON)) {
                    // Error handling: Expected semicolon or assignment operator after identifier
                    System.err.println("Syntax error: Expecting semicolon or assignment operator after identifier in for loop declaration");
                }
            } else {
                // Error handling: Expected identifier after type specifier
                System.err.println("Syntax error: Expecting identifier after type specifier");
            }
        }
    }


    private void for_update_exp(TextInBox parentNode) {
        if (match(LexicalAnalyzer.TokenType.ID))
        {
            TextInBox idtoken = new TextInBox("Identifier",80,20);
            tree.addChild(parentNode,idtoken);
            tree.addChild(idtoken,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the identifier token

            // Check if there's a unary operator after the identifier
            if (match(LexicalAnalyzer.TokenType.UNARY_OP))
            {
                tree.addChild(parentNode,new TextInBox(getTokenData(),80,20));
                advance(); // Consume the unary operator token
            }
        }
    }


    private void return_statement(TextInBox parentNode)
    {
        TextInBox returnstmt = new TextInBox("Return Stmt",80,20);
        tree.addChild(parentNode,returnstmt);
        TextInBox keyword = new TextInBox("Keyword",80,20);
        tree.addChild(returnstmt,keyword);
        tree.addChild(keyword,new TextInBox(getTokenData(),80,20));
        advance(); // Consume the "return" keyword

        // Check if there's an expression following the "return" keyword
        if (!match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            // Parse the expression
            expression(parentNode);
        }

        // Check for the semicolon at the end of the return statement
        if (match(LexicalAnalyzer.TokenType.SEMICOLON)) {
            tree.addChild(returnstmt,new TextInBox(getTokenData(),80,20));
            advance(); // Consume the semicolon token
        } else {
            // Error handling: Expected semicolon at the end of return statement
            System.err.println("Syntax error: Expected ';' at the end of 'return' statement");
        }
    }



}
