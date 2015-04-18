#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i;
  FILE fout;
  fout = creat("hello.txt");
  fprintf(fout, "Hello world! %d args:\n", argc);
  for(i=0;i<argc;i++) fprintf(fout, "%s\n", argv[i]);
  close(fout);
  halt(); // exit does not work now :(
}
