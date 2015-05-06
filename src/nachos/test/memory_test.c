
int main(int argc, char* argv[])
{
	
	int a[1000];
	int status;
	int temp;
	printf("test\n");
	temp = exec("memory_test.coff",0,0);
	join(temp, &status);
	printf("status: %d\n", status);
	exit(0); 
}