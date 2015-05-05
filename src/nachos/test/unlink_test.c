#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  FILE fout, fout2;
  printf("creat returns %d\n",fout = creat("unlink_test.txt"));
  printf("open returns %d\n",fout2 = open("unlink_test.txt"));
  printf("unlink returns %d\n",unlink("unlink_test.txt"));
  printf("unlink returns %d // I'm unsure about the behaviour...\n",unlink("unlink_test.txt"));
  printf("open returns %d\n",open("unlink_test.txt"));
  printf("creat returns %d\n",creat("unlink_test.txt"));
  printf("close returns %d\n",close(fout));
  printf("The file should exist now... (Press any key to continue)\n");
  getch();
  printf("close returns %d\n",close(fout2));
  printf("But not now. (Press any key to continue)\n");
  getch();
  printf("open returns %d\n",open("unlink_test.txt"));
  printf("creat returns %d\n",fout = creat("unlink_test.txt"));
  printf("close returns %d\n",close(fout));
  printf("The file should exist now... (Press any key to continue)\n");
  getch();
  printf("unlink returns %d\n",unlink("unlink_test.txt"));
  printf("But not now. (Press any key to continue)\n");
  getch();
  printf("unlink returns %d\n",unlink("unlink_test.txt"));
  return 0;
}
