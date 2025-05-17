# C Compiler - Lexical Analyzer and Parser

This project implements a C compiler front-end with lexical analysis and parsing capabilities. It processes C code to generate parse trees, providing a visual representation of the code's syntactic structure.

## Features

- **Lexical Analysis**: Tokenizes C code into meaningful lexical units (tokens)
- **Syntax Parsing**: Builds a parse tree according to C language grammar rules
- **Visual Representation**: Displays the parse tree using the abego TreeLayout library
- **Interactive UI**: Allows users to input C code and visualize its structure

## Project Structure

- `src/LexicalAnalyzer.java`: Implements the lexical analyzer that converts input text into tokens
- `src/ParseTree.java`: Implements the parser that builds a parse tree from tokens
- `src/Main.java`: Contains the user interface and application entry point
- `src/Lexical.c`: Sample C code for testing
- `lib/`: Contains the abego TreeLayout library for tree visualization

## Requirements

- Java Development Kit (JDK) 8 or higher
- abego TreeLayout library (included in the `lib/` directory)

## How to Use

1. Clone this repository
2. Open the project in your preferred Java IDE
3. Run `Main.java` to start the application
4. Enter C code in the input text area
5. Click "Analyze" to perform lexical analysis
6. Click "Create Parse Tree" to generate and visualize the parse tree

## Implementation Details

### Lexical Analyzer

The lexical analyzer identifies various token types including:

- Keywords (if, while, for, etc.)
- Identifiers
- Operators (arithmetic, comparison, etc.)
- Literals (numbers, strings, characters)
- Punctuation symbols

### Parser

The parser implements a recursive descent parsing algorithm that:

- Processes declarations (variables, functions, structs, enums)
- Handles expressions and statements
- Builds a hierarchical tree structure representing the syntactic structure
- Visualizes the parse tree using the abego TreeLayout library

## Screenshots

The repository includes a sample parse tree visualization in `parse_tree_1.jpg`.

## Documentation

Detailed documentation is available in `Final_Documentation_Compilers.pdf`.

## License

This project is available for educational purposes.
