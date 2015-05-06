/* halt.c
 *	Simple program to test whether running a user program works.
 *	
 *	Just do a "syscall" that shuts down the OS.
 *
 * 	NOTE: for some reason, user programs with global data structures 
 *	sometimes haven't worked in the Nachos environment.  So be careful
 *	out there!  One option is to allocate data structures as 
 * 	automatics within a procedure, but if you do this, you have to
 *	be careful to allocate a big enough stack to hold the automatics!
 */

#include "syscall.h"

int main()
{
	char *pArgv[1];
	char pc[3]={4,5,6};
	pArgv[0]= pc;
	int pid = exec("halt_test2.coff",1,pArgv);
	int status;
	int x=join(pid, &status);
	printf("???");
    int p = halt();
	
	printf("%d %d \n", x, status);
    /* not reached */
}
