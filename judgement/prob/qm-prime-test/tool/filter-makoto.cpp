#include <cstdio>
#include <cstring>

int main() {
  static char line[1000];
  static char expected_line[1000];
  if(!fgets(line, 1000, stdin)) {
    fprintf(stderr, "Illformed Makoto Output\n");
    return 1;
  }
  int a; sscanf(line, "%d", &a);
  sprintf(expected_line, "%d\n", a);
  if(strcmp(line,expected_line)) {
    fprintf(stderr, "Illformed Makoto Output\n");
    return 1;
  }
  if(fgets(line, 1000, stdin)) {
    fprintf(stderr, "Illformed Makoto Output\n");
    return 1;
  }
  if(a<0 || a>=50000) {
    fprintf(stderr, "Out of range\n");
    return 1;
  }
  printf("%d\n", a);
  return 0;
}

