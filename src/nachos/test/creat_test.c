#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  FILE fout;
  int i;
  for(i=0; i<14; i++){
    printf("create returns %d\n", fout = creat("create_test.txt"));
  }
  printf("file descriptors should be used up now... (Press any key to continue)\n");
  getch();
  printf("create returns %d\n", fout = creat("create_test.txt"));
  printf("unlink returns %d\n", unlink("create_test.txt"));
  return 0;
}