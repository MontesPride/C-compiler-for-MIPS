#include "minic-stdlib.h"

int var_int;
char var_char;

int main() {

    int a;
    a = 3 * 6;
    print_i(a);
    print_i(1);
    print_c('\n');
    print_i(2147483647);
    print_c('\n');
    print_c('a');
    print_c('\n');
    print_s((char*)"asdf");

    return 0;
}