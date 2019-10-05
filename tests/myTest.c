#include "minic-stdlib.h"
#include "my-header-file.h"

struct vector {
    char* name;
    int x;
    int y;
};

char a11; char a12; char a13[1];
char a21; char a22; char a23;
char a31; char a32; char a33;
char empty_char; // Empty cell character

int b11; int b12; int b13;
int b21; int b22[9000]; int b23;
int b31; int b32; int b33;
int empty_int; // Empty cell character

void c11; void c12; void c13;
void c21; void c22; void c23;
void c31[1337]; void c32; void c33;
void empty_void; // Empty cell character

int increment(int x) {
    return x + 1;
}

int decrement(int x) {
    return x - 1;
}

struct vector add(int x1, int x2, int y1, int y2) {
    struct vector v;
    v.name = "Vector";
    v.x = x1 + x2;
    v.y = y1 + y2;
    return v;
}

int main() {

    struct vector v1;
    struct vector v2;
    struct vector v;
    int iterator;
    v1.name = "vector1";
    v1.x = 1;
    v1.y = -1;

    v2.name = "vector2";
    v2.x = 0;
    v2.y = -9999;

    v = add(v1.x, v2.x, v1.y, v2.y);

    iterator = 10;
    while (iterator >= 0) {
        print("Iterator: " + iterator);
        if (iterator % 2 == 0) {
            print(", Iterator is even\n");
        }
        iterator = iterator - 1;
    }

    return 0;
}