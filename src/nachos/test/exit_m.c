#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(int argc, char *argv[])
{
	char *pArgv[1];
	char pc[3]={4,5,6};
	pArgv[0]= pc;
	int pid = exec("exit_s.coff",1,pArgv);
	int status;
	int x=join(pid, &status);
	printf("%d %d \n", x, status);
	exit(0);
}