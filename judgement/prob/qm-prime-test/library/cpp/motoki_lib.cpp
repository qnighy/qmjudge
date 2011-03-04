#include <cstdio>
#include <motoki_lib.h>

int main() {
  int num;
  scanf("%d", &num);
  int result = sayResult(num);
  if(result == -1)
    printf("Prime\n");
  else
    printf("%d\n", result);
  return 0;
}

