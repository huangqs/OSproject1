#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  FILE fout;
  char hello[4];
  memset(hello, 4, "s");
  printf("create file returns %d", creat("write_test.txt");
  printf("write \"hi, there\" for 4 bytes returns %d", write(fout, hello, 4));
  printf("write \"hi, there\" for 6 bytes returns %d", write(fout, hello, 6));
  printf("write to an invalid file descriptor returns %d", write(-1, hello, 4));
  printf("read from file for 16 bytes returns %d", read(fout, hello, 4));
  printf("read from file for 18 bytes returns %d", read(fout, hello, 6));
  printf("read from an invalid file descriptor returns %d", read(-1, hello, 4));
  printf("close returns %d", close(fout))
  return 0;
}