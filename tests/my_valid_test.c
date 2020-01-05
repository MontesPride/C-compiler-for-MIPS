#include "minic-stdlib.h"

struct vector{
    int x;
    int y;
    int z;
};

int var_int;
char var_char;
struct vector v1;
struct vector v2;
int *pnt;
int arr[10];

int m;

void inc_m() { m = m + 1;}

int increment(int x) {
    return x + 1;
}

int sum_of_5(int a, int b, int c, int d, int e) {
    return a + b + c + d + e;
}

int main() {

    int a;
    int b;
    int c;
    int d;
    struct vector v3;

    int bo1;
    int bo2;
    int bo3;
    int boa[10];
    int bo4;
    int bo5;
    int bo6;

    a = 3 * 6;
    print_i(a);
    print_c('\n');
    a = increment(a);
    print_i(a);
    print_c('\n');
    b = sum_of_5(1, 2, 3, 4, 5);
    print_i(b);
    print_c('\n');
    c = 12 / 3 / 2;
    print_i(c);
    print_c('\n');
    d = 18 % 5;
    print_i(d);
    print_c('\n');
    print_i(1);
    print_c('\n');
    print_i(2147483647);
    print_c('\n');
    print_c('a');
    print_c('\n');
    print_s((char*)"asdf\n");
    arr[9] = 123456321;
    print_i(arr[9]);
    m = 10;
    print_i(m);
    inc_m();
    print_i(m);
    var_char = 'a';
    print_c(var_char);
    v1.x = 1337;
    print_i(v1.x);
    v3.x = 420;
    print_i(v3.x);
    v3.z = 69;
    print_i(v3.z);

    print_c('\n');
    print_c('\n');
    print_c('\n');

    bo1 = 1;
    bo2 = 2;
    bo3 = 3;
    bo4 = 4;
    bo5 = 5;
    bo6 = 6;

    boa[10] = 10;
    boa[-1] = -1;

    print_i(bo1);
    print_c('\n');
    print_i(bo2);
    print_c('\n');
    print_i(bo3);
    print_c('\n');
    print_i(bo4);
    print_c('\n');
    print_i(bo5);
    print_c('\n');
    print_i(bo6);
    print_c('\n');


    if (1 == 1) {
        print_s((char*)"true dat");
    }

      if (a < b && 1 < 2) {
        print_s((char*)"Kurde dziala\n");
      }
      if (a < b || 1 < 2) {
          print_s((char*)"Kurde dziala\n");
        }
        if (a < b || 1 < 2 && 3 > 2) {
          print_s((char*)"Kurde dziala\n");
        }
        if (a < b || 1 < 2 || 3 > 2) {
          print_s((char*)"Kurde dziala\n");
        }

    if (1 < 2 && 2 < 3 && 3 < 4) {
        print_s((char*)"true");
    }

    return 0;
}