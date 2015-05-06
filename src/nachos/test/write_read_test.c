#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  FILE fout;
  char hello[12];
  printf("create file returns %d\n", fout = creat("write_test.txt"));
  printf("write \"hello\" for 5 bytes returns %d\n", write(fout, "hello", 5));
  printf("write to an invalid file descriptor returns %d\n", write(-1, hello, 4));
  printf("read from file for 5 bytes returns %d\n", read(fout, hello, 5));
  printf("close a file returns %d\n", close(fout));
  printf("open a file returns %d\n", fout = open("write_test.txt"));
  printf("read from file for 5 bytes returns %d\n", read(fout, hello, 5));
  printf("now the file has been read up...(press any key to continue)\n");
  getchar();
  printf("read from file for 5 bytes returns %d\n", read(fout, hello, 5));
  printf("read from an invalid file descriptor returns %d\n", read(-1, hello, 4));
  printf("close returns %d", close(fout));
  printf("unlink returns %d", unlink("write_test.txt"));
  return 0;
}