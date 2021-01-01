/***************************/
/* Based on a template by Oren Ish-Shalom */
/***************************/

/*************/
/* USER CODE */
/*************/
import java_cup.runtime.*;



/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/

/*****************************************************/
/* Lexer is the name of the class JFlex will create. */
/* The code will be written to the file Lexer.java.  */
/*****************************************************/
%class Lexer

/********************************************************************/
/* The current line number can be accessed with the variable yyline */
/* and the current column number with the variable yycolumn.        */
/********************************************************************/
%line
%column

/******************************************************************/
/* CUP compatibility mode interfaces with a CUP generated parser. */
/******************************************************************/
%cup

/****************/
/* DECLARATIONS */
/****************/
/*****************************************************************************/
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied verbatim (letter to letter) into the Lexer class code.     */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */
/*****************************************************************************/
%{
	/*********************************************************************************/
	/* Create a new java_cup.runtime.Symbol with information about the current token */
	/*********************************************************************************/
	private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
	private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}

	/*******************************************/
	/* Enable line number extraction from main */
	/*******************************************/
	public int getLine()    { return yyline + 1; }
	public int getCharPos() { return yycolumn;   }
%}

/***********************/
/* MACRO DECALARATIONS */
/***********************/
LINETERM	    = \r|\n|\r\n
WHITESPACE		= [\t ] | {LINETERM}
INTEGER			= 0 | [1-9][0-9]*
ID				= [a-zA-Z]+[a-zA-Z0-9]*
IN_COMMENT		= \/\/[a-zA-Z0-9\(\)\[\]\{\}\?!+\-\*\/\.; \t\f]*
MUL_COMMENT	    = \/\*([a-zA-Z0-9\(\)\[\]\{\}\?!+\-\*\/\.;]|{WhiteSpace})*\*\/
COMMENT 		= {IN_COMMENT}|{MUL_COMMENT}

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/

/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/

<YYINITIAL> {
<<EOF>>				    { return symbol(sym.EOF); }
"public"                { return symbol(sym.PUBLIC); }
"static"                { return symbol(sym.STATIC); }
"class"                 { return symbol(sym.CLASS); }
"extends"               { return symbol(sym.EXTENDS); }
"+"				        { return symbol(sym.PLUS); }
"-"				        { return symbol(sym.MINUS); }
"*"				        { return symbol(sym.MULT); }
"/"				        { return symbol(sym.DIV); }
"="				        { return symbol(sym.ASS); }
"!"  		            { return symbol(sym.NOT); }
"while"                 { return symbol(sym.WHILE); }
"if"                    { return symbol(sym.IF); }
"else"                  { return symbol(sym.ELSE); }
"true"  		        { return symbol(sym.TRUE); }
"false"  		        { return symbol(sym.FALSE); }
"<"			            { return symbol(sym.LT); }
"&&"  		            { return symbol(sym.AND); }
","			            { return symbol(sym.COMMA); }
"("			            { return symbol(sym.LPAREN); }
")"			            { return symbol(sym.RPAREN); }
"{"			            { return symbol(sym.LBR); }
"}"			            { return symbol(sym.RBR); }
"["			            { return symbol(sym.LSQBR); }
"]"			            { return symbol(sym.RSQBR); }
";"			            { return symbol(sym.SEMICOLON); }
"this"  		        { return symbol(sym.THIS); }
"return"                { return symbol(sym.RET); }
"String[]"              { return symbol(sym.STRINGARGS); }
"System.out.println"    { return symbol(sym.SYSTEM); }
{INTEGER}               { return symbol(sym.INTEGER, Integer.parseInt(yytext())); }
{ID}		            { return symbol(sym.ID, new String(yytext())); }
{WHITESPACE}            { /* do nothing */ }
{COMMENTS}              { /* do nothing */ }
}
