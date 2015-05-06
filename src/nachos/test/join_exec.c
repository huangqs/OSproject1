#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"
int main()
{
	int status;
	int pid2 =exec("join_exec2.coff", 0, 0); 	//normally
	int x=join(pid2, &status);
	printf("%d %d \n", x, status);
	x= join(exec("join_exec3.coff", 0, 0), &status);	// divided by 0; unhandled exception
	printf("%d %d \n", x, status);
	x= join(99, &status);					//not refer to a child process
	printf("%d %d \n", x, status);
	x= join(-99, &status);					//not refer to a child process
	printf("%d %d \n", x, status);
	x= join(0, &status);					//not refer to a child process
	printf("%d %d \n", x, status);
	x= join(pid2, &status);				//join only once.
	printf("%d %d \n", x, status);
	exit(0);
}