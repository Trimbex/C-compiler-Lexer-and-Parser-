#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>


bool bl = (1?1:0) || (1?0:1) ;
int x = 40;
int x = ++x;

int a[2] = {1,3};

enum akm 
{
ko,
lm
};


struct k
{
 char name[50];
    int age;
    float height;
};



double k,l,m;

int u = 4;


char x ;

int main()
{

switch (expression)
 {
case 1:
{
break;
}
case 2:
{
break;
}
}

 char y = 'a';

do
{
x = 56;
y = y + 1;
} while ( y < 100);


if(x || 3)
{
  x += 3;

} 

else

{
 x = y;


}


for(int i = 4; i < 2; i++)
{
for ( int j = 2 ; j < 3 ; j--)
{
 a[i] = j;
}
}

}





// Token types
typedef enum {
    TOK_INT,
    TOK_FLOAT,
    TOK_PLUS,
    TOK_MINUS,
    TOK_MULTIPLY,
    TOK_DIVIDE,
    TOK_LPAREN,
    TOK_RPAREN,
    TOK_EOF
};

