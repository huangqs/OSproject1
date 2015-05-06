#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  FILE fout;
  printf("when file does not exist, open returns %d\n",fout = open("unlink_test.txt"));
  printf("when file does not exist, create returns %d\n",fout = create("unlink_test.txt");));
  printf("close a existing file returns %d\n",close(fout));
  printf("when file exists, open returns %d\n",fout = open("unlink_test.txt"));
  return 0;
}