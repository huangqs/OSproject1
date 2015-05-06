#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  FILE fout, fout2;
  printf("when file does not exist, open returns %d\n",fout = open("open_test.txt"));
  printf("when file does not exist, create returns %d\n",fout = creat("open_test.txt"));
  printf("when file exists, open returns %d\n",fout2 = open("open_test.txt"));
  printf("close a opened file returns %d\n",close(fout));
  printf("close a closed file returns %d\n",close(fout));
  printf("unlink a file returns %d\n", unlink("open_test.txt"));
  return 0;
}