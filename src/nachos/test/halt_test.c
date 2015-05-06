

int main(int argc, char *argv[])
{
	char *pArgv[1];
	char pc[3]={4,5,6};
	pArgv[0]= pc;
	int pid = exec("halt_test2.coff",1,pArgv);
	int status;
	int x=join(pid, &status);
	
	 halt();
	//printf("%d \n", t);
	
	
	printf("%d %d \n", x, status);
	exit(0);
}