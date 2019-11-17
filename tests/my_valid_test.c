#include "minic-stdlib.h"

int var_int;
char var_char;

int increment(int x) {
    return x + 1;
}

int sum_of_5(int a, int b, int c, int d, int e) {
    return a + b + c + d + e;
}

int main() {

    int a;
    int b;
    a = 3 * 6;
    print_i(a);
    print_c('\n');
    a = increment(a);
    print_i(a);
    print_c('\n');
    b = sum_of_5(1, 2, 3, 4, 5);
    print_i(b);
    print_c('\n');
    print_i(1);
    print_c('\n');
    print_i(2147483647);
    print_c('\n');
    print_c('a');
    print_c('\n');
    print_s((char*)"asdf\n");

    if (1 < 2 && 2 < 3 && 3 < 4) {
        print_s((char*)"true");
    }

    return 0;
}