#include <cstdio>
#include <makoto_lib.h>

int main() {
  int num;
  scanf("%d", &num);
  int result = primeTest(num);
  printf("%d\n", result);
  return 0;
}

